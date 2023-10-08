# CLVR API

## Homepage
GET /

Возвращает все необходимые скрипты, HTML и т.д., чтобы далее frontend правильно работал.

## Create game session

#### Request
POST /v0/api/game-session

```json
{
  "template": {
    "id": "<template id>"
  }
}
```

#### Response

```json 
{
  "session": {
    "id": "<session id>"
  }
}
```

## Game session as host

CONNECT /v0/ws/host/{session_id}

Все запросы по WebSocket имеют следующую структуру:
```json 
{
  "session": {
    "id": "<session_id>"
  },
  "type": "<event type>",
  "payload": "<json with additional information>"
}
```

Все ответы по WebSocket имеют следующую структуру:
```json 
{ 
  "state": "<client state during this event>",
  "payload": "<json with additional information>"
}
```

### Open question
#### Request
```json 
{
  "session": {
    "id": "<session_id>"
  },
  "type": "OPEN_QUESTION",
  "payload": { 
    "row": "<row num>",
    "column": "<column num>"
  }
}
```

#### Response
```json 
{
  "state": "OPENED_QUESTION",
  "payload": {
    "row": "<row num>",
    "column": "<column num>",
    "question": "<question>"
  }
}
```

### Set Field
#### Request

```json 
{
  "session": {
    "id": "<session_id>"
  },
  "type": "SET_FIELD",
  "payload": { 
    "row": "<row num>",
    "column": "<column num>",
    "mark": "<X or O>"
  }
}
```

#### Response

```json 
{
  "state": "MAIN_BOARD",
  "payload": {
    "board": {
      "cells": [{
        "row": "<row num>",
        "column": "<column num>",
        "mark": "<X or O or empty>"
      }, ...
      ]
    }
  }
}
```


## Game session as board
CONNECT /v0/ws/board/{session_id}

В данном режиме нет запросов, но клиент слушает сообщения от сервера и применяет их. 
На данный момент все сообщения к хосту применимы и к клиентам, кто смотрит трансляцию. 
Отрисовка будет отличаться за счет JS.