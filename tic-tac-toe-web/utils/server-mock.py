# A mock of the server for testing purposes
#
# Simulates the server responses to the client, but
# ignores the modifications made by the client.

import asyncio
import functools
import json
import uuid

import aiohttp
import aiohttp_cors
from aiohttp import web


with open("../../tic-tac-toe-back/src/main/resources/dumbQuizCollection.json") as quiz_file:
    quiz_collection = json.load(quiz_file)


host_clients: set[web.WebSocketResponse] = set()
board_clients: set[web.WebSocketResponse] = set()
sessions: dict[str, ...] = {}


def get_quiz_by_id(quiz_id):
    return next(quiz for quiz in quiz_collection if quiz["id"]["id"] == quiz_id)


def board_from_quiz(quiz):
    return [
        {
            "row": row,
            "column": column,
            "topic": quiz["questions"][row][column]["topic"],
            "question": quiz["questions"][row][column]["statement"],
            "hints": quiz["questions"][row][column]["hints"],
            "answer": quiz["questions"][row][column]["answer"],
        }
        for row in range(quiz["gridSide"])
        for column in range(quiz["gridSide"])
    ]


def board_description_from_quiz(quiz, session):
    return {"cells": [
        {
            "row": row,
            "column": column,
            "topic": quiz["questions"][row][column]["topic"],
            "mark": session["marks"][row][column],
        }
        for row in range(quiz["gridSide"])
        for column in range(quiz["gridSide"])
    ]}


async def get_quiz_list(_request: web.Request) -> web.Response:
    return web.json_response({
        "quiz-list": [{
            "id": quiz["id"]["id"],
            "name": quiz["templateTitle"],
            "comment": "Literally anything, some random text, please ignore",
        } for quiz in quiz_collection]
    })


async def get_quiz(request: web.Request) -> web.Response:
    quiz_id = request.match_info["quiz_id"]
    quiz = get_quiz_by_id(quiz_id)
    return web.json_response({
        "id": quiz["id"]["id"],
        "name": quiz["templateTitle"],
        "comment": "Literally anything, some random text, please ignore",
        "board": board_from_quiz(quiz),
    })


async def create_game_session(request: web.Request) -> web.Response:
    quiz_id = request.match_info["quiz_id"]
    quiz = get_quiz_by_id(quiz_id)
    session_id = str(uuid.uuid4())
    sessions[session_id] = {
        "quiz_id": quiz_id,
        "currently_shown_hints": 0,
        "marks": [["EMPTY" for _ in range(quiz["gridSide"])] for _ in range(quiz["gridSide"])]
    }
    return web.json_response({
        "session": {
            "id": session_id,
        }
    })


async def broadcast_message(message_json):
    await asyncio.gather(
        *[client.send_json(message_json) for client in board_clients | host_clients],
        return_exceptions=False
    )


async def broadcast_message_to_board(message_json):
    await asyncio.gather(
        *[client.send_json(message_json) for client in board_clients],
        return_exceptions=False
    )


async def broadcast_message_to_host(message_json):
    await asyncio.gather(
        *[client.send_json(message_json) for client in host_clients],
        return_exceptions=False
    )


async def handle_host_message(_ws: web.WebSocketResponse, msg: aiohttp.WSMessage):
    if msg.type == aiohttp.WSMsgType.TEXT:
        data = msg.json()
        row = data["payload"]["row"]
        column = data["payload"]["column"]
        session_id = data["session"]["id"]
        session = sessions[session_id]
        quiz_id = session["quiz_id"]
        quiz = get_quiz_by_id(quiz_id)
        raw_board = board_from_quiz(quiz)
        board = board_description_from_quiz(quiz, session)

        match data["type"]:
            case "OPEN_QUESTION" | "SHOW_NEXT_HINT":
                if data["type"] == "OPEN_QUESTION":
                    session["currently_shown_hints"] = 0
                else:
                    session["currently_shown_hints"] = data["payload"]["current_hints_num"]

                await asyncio.gather(
                    broadcast_message_to_host({
                        "state": "OPENED_QUESTION_HOST",
                        "payload": {
                            "board": board,
                            "question": {
                                "row": row,
                                "column": column,
                                "question": raw_board[row * 3 + column]["question"],
                                "hints": raw_board[row * 3 + column]["hints"],
                                "answer": raw_board[row * 3 + column]["answer"],
                                "current_hints_num": session["currently_shown_hints"],
                            },
                        }
                    }),
                    broadcast_message_to_board({
                        "state": "OPENED_QUESTION_CLIENT",
                        "payload": {
                            "board": board,
                            "question": {
                                "row": row,
                                "column": column,
                                "question": raw_board[row * 3 + column]["question"],
                                "current_hints": raw_board[row * 3 + column]["hints"][:session["currently_shown_hints"]],
                            },
                        }
                    }),
                )
            case "SHOW_ANSWER":
                await broadcast_message({
                    "state": "OPENED_QUESTION_WITH_ANSWER",
                    "payload": {
                        "board": board,
                        "question": {
                            "row": row,
                            "column": column,
                            "question": raw_board[row * 3 + column]["question"],
                            # "hints": board[row * 3 + column]["hints"],
                            "answer": raw_board[row * 3 + column]["answer"],
                        },
                    },
                })
            case "SET_FIELD":
                session["marks"][row][column] = data["payload"]["mark"]
                board["cells"][row * 3 + column]["mark"] = data["payload"]["mark"]
                await broadcast_message({
                    "state": "MAIN_BOARD",
                    "payload": {
                        "board": board,
                        "win": "EMPTY",
                    },
                })
            case _:
                raise ValueError(f"Unknown message type: {data['type']}")


async def handle_board_message(_msg: aiohttp.WSMessage) -> None:
    raise ValueError("We don't accept messages from the board")


async def handle_ws_connection(kind: str, request: web.Request) -> web.WebSocketResponse:
    session_id = request.match_info["session_id"]
    session = sessions[session_id]
    quiz_id = session["quiz_id"]
    quiz = get_quiz_by_id(quiz_id)
    board = board_description_from_quiz(quiz, session)

    ws = web.WebSocketResponse()
    await ws.prepare(request)

    if kind == "host":
        host_clients.add(ws)
    else:
        board_clients.add(ws)

    await broadcast_message({
        "state": "MAIN_BOARD",
        "payload": {
            "board": board,
            "win": "EMPTY",
        },
    })

    try:
        async for msg in ws:
            msg: aiohttp.WSMessage
            match kind:
                case "host":
                    await handle_host_message(ws, msg)
                case "board":
                    await handle_board_message(msg)
    finally:
        if kind == "host":
            host_clients.remove(ws)
        else:
            board_clients.remove(ws)

    return ws


app = web.Application()

app.add_routes([
    web.get("/quiz-list", get_quiz_list),
    web.get("/quiz-list/{quiz_id}", get_quiz),
    web.post("/api/game-session/{quiz_id}", create_game_session),
    web.get("/ws/host/{session_id}", functools.partial(handle_ws_connection, "host")),
    web.get("/ws/client/{session_id}", functools.partial(handle_ws_connection, "board")),
])

cors = aiohttp_cors.setup(app, defaults={
    "*": aiohttp_cors.ResourceOptions(
        allow_credentials=True,
        expose_headers="*",
        allow_headers="*"
    )
})

for route in list(app.router.routes()):
    cors.add(route)

web.run_app(app, port=8080)
