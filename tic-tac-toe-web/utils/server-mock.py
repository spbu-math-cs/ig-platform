# A mock of the server for testing purposes
#
# Simulates the server responses to the client, but
# ignores the modifications made by the client.

import asyncio
import functools

import aiohttp
from aiohttp import web


async def create_game_session(request: web.Request) -> web.Response:
    data = await request.json()
    assert data["quiz"]["id"], 1

    return web.json_response({
        "quiz": {
            "id": "ABCD",
        }
    })


clients: set[web.WebSocketResponse] = set()


BOARD = {"cells": [{
        "row": 0,
        "column": 0,
        "state": "X",
    }, {
        "row": 0,
        "column": 1,
        "state": "O",
    }, {
        "row": 0,
        "column": 2,
        "state": "",
    }, {
        "row": 1,
        "column": 0,
        "state": "O",
    }, {
        "row": 1,
        "column": 1,
        "state": "X",
    }, {
        "row": 1,
        "column": 2,
        "state": "O",
    }, {
        "row": 2,
        "column": 0,
        "state": "O",
    }, {
        "row": 2,
        "column": 1,
        "state": "X",
    }, {
        "row": 2,
        "column": 2,
        "state": "",
    },
]}


async def broadcast_message(json) -> None:
    await asyncio.gather(
        *[client.send_json(json) for client in clients],
        return_exceptions=False
    )


async def handle_host_message(_ws: web.WebSocketResponse, msg: aiohttp.WSMessage):
    if msg.type == aiohttp.WSMsgType.TEXT:
        data = msg.json()
        assert data["session"]["id"] == "ABCD"

        match data["type"]:
            case "OPEN_QUESTION":
                await broadcast_message({
                    "state": "OPENED_QUESTION",
                    "payload": {
                        "question": {
                            "row": data["payload"]["row"],
                            "column": data["payload"]["column"],
                            "text": "What is the answer to life, the universe and everything?",
                        },
                        "board": BOARD,
                    },
                })
            case "SET_FIELD":
                await broadcast_message({
                    "state": "MAIN_BOARD",
                    "payload": {
                        "board": BOARD,
                    },
                })
            case _:
                raise ValueError(f"Unknown message type: {data['type']}")


async def handle_board_message(_msg: aiohttp.WSMessage) -> None:
    raise ValueError("We don't accept messages from the board")


async def handle_ws_connection(kind: str, request: web.Request) -> web.WebSocketResponse:
    ws = web.WebSocketResponse()
    await ws.prepare(request)
    clients.add(ws)

    try:
        async for msg in ws:
            msg: aiohttp.WSMessage
            match kind:
                case "host": await handle_host_message(ws, msg)
                case "board": await handle_board_message(msg)
    finally:
        clients.remove(ws)

    return ws


app = web.Application()
app.add_routes([
    web.post("/api/game-session", create_game_session),
    web.get("/host/ABCD", functools.partial(handle_ws_connection, "host")),
    web.get("/board/ABCD", functools.partial(handle_ws_connection, "board")),
])

web.run_app(app, port=8080)
