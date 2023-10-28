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
            "id": "239",
        }
    })


clients: set[web.WebSocketResponse] = set()

BOARD = {"cells": [{
    "row": 0,
    "column": 0,
    "mark": "<span style=\"font-size: 60pt;\">😈</span>",
    "questions": ["Опишите значение следующих эмодзи", "🔰😤🆎"]
}, {
    "row": 0,
    "column": 1,
    "mark": "Языки I",
    "questions": ["На каком языке написана следующая программа", "<pre>Say hello.\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n</pre>", "", "<img src=\"https://upload.wikimedia.org/wikipedia/commons/3/3c/Whitespace_in_vim2.png\"></img>"]
}, {
    "row": 0,
    "column": 2,
    "mark": "<div style=\"font-size: 5pt; line-height: 1.2;\">Какая-то рандомная дичь которая парит только меня да и то только потому что я посмотрел про нее видос на ютубе и я скорее всего про нее забуду через пару месяцев но сейчас мне кажется что это очень интересно и я хочу дать на это вопрос</div>",
    "questions": ["Это фанарт по известному американскому комиксу. Назовите хотя бы одного изображенного персонажа", "<img src=\"https://ibb.co/HgRfTYC\"></img>", "<img src=\"https://i.redd.it/9nhnc6ter3151.jpg\">", "<img src=\"https://assets.amuniversal.com/8b8b6b403ed2013c1aef005056a9545d\"></img>"]
}, {
    "row": 1,
    "column": 0,
    "mark": "\"матеша\"",
    "questions": ["Назовите наименьнший первообразный корень числа 998244353"]
}, {
    "row": 1,
    "column": 1,
    "mark": "Языки II",
    "questions": ["Заполните пропуск", "https://ibb.co/yy4JjBD"]
}, {
    "row": 1,
    "column": 2,
    "mark": "Kotlin🥰",
    "questions": ["Каков результат работы следующей программы?", "<img src=\"https://ibb.co/d7LqDcN\"/>"]
}, {
    "row": 2,
    "column": 0,
    "mark": "ООП🤮",
}, {
    "row": 2,
    "column": 1,
    "mark": "<span style=\"font-size: 60pt\">🤔</span>",
}, {
    "row": 2,
    "column": 2,
    "mark": "<img src=\"https://media.tenor.com/x8v1oNUOmg4AAAAd/rickroll-roll.gif\" width=\"100px\"/>",
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
        assert data["session"]["id"] == "239"

        match data["type"]:
            case "OPEN_QUESTION":
                await broadcast_message({
                    "state": "OPENED_QUESTION",
                    "payload": {
                        "question": {
                            "row": data["payload"]["row"],
                            "column": data["payload"]["column"],
                            "text": "<span style=\"color: blue;\">What is the answer to life, the universe and everything?</span>",
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

    await broadcast_message({
        "state": "MAIN_BOARD",
        "payload": {
            "board": BOARD,
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
        clients.remove(ws)

    return ws


app = web.Application()
app.add_routes([
    web.post("/api/game-session", create_game_session),
    web.get("/ws/host/239", functools.partial(handle_ws_connection, "host")),
    web.get("/ws/board/239", functools.partial(handle_ws_connection, "board")),
])

web.run_app(app, port=8080)
