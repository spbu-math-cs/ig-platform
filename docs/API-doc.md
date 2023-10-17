# CLVR API

## Homepage
GET /

Возвращает все необходимые скрипты, HTML и т.д., чтобы далее frontend правильно работал.

*Пока, домен отдающий главную страницу будет отличаться от домена с API. 
В будущем это будет решаться либо через proxy, либо будет единый сервер, умеющий обрабатывать все запросы.*

## Create game session

#### Request
POST /api/game-session

```json
{
  "quiz": {
    "id": "<quiz id>"
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

CONNECT /ws/host/{session_id}

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
    "question": {
      "row": "<row num>",
      "column": "<column num>",
      "text": "<question>"
    },
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
CONNECT ws/board/{session_id}

В данном режиме нет запросов, но клиент слушает сообщения от сервера и применяет их. 
На данный момент все сообщения к хосту применимы и к клиентам, кто смотрит трансляцию. 
Отрисовка будет отличаться за счет JS.