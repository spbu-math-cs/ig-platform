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

### Board description

В каждом ответе, в качестве одного из поля `payload` присутствует поле `board` с полным описанием доски. 
Это описание имеет следующую структуру: 
```json 
{
  "board": {
    "cells": [{
      "row": "<row num>",
      "column": "<column num>",
      "mark": "<X or O or empty>",
      "topic": "<topic>"
    }, ...]
  }
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

#### Response to HOST
```json 
{
  "state": "OPENED_QUESTION_HOST",
  "payload": {
    "question": {
      "row": "<row num>",
      "column": "<column num>",
      "question": "<question text>",
      "hints": ["<hint1>", "<hint2>", ...],
      "current_hints_num": "<current hints num>",
      "answer": "<answer>"
    },
    "board": "<board description>"
  }
}
```

При первом открытии `current_hints_num` равно 0.

#### Response to CLIENT
```json
{
  "state": "OPENED_QUESTION_CLIENT",
  "payload": {
    "question": {
      "row": "<row num>",
      "column": "<column num>",
      "question": "<question text>",
      "current_hints": "[<hint1>, <hint2>, ...]"
    },
    "board": "<board description>"
  }
}
```

При первом открытии `current_hints` - пустой список.

### Show next hint 
#### Request
```json 
{
  "session": {
    "id": "<session_id>"
  },
  "type": "SHOW_NEXT_HINT",
  "payload": {
    "row": "<row num>",
    "column": "<column num>",
    "current_hints_num": "<current hints num>"
  }
}
```

Если `current_hints_num` равно количеству всех подсказок, то сервер не присылает никакого ответа.

#### Response to HOST
Такой же, как и в случае с [Open question](#response-to-host)

#### Response to CLIENT
Такой же, как и в случае с [Open question](#response-to-client)

### Show answer 
#### Request
```json 
{
  "session": {
    "id": "<session_id>"
  },
  "type": "SHOW_ANSWER",
  "payload": {
    "row": "<row num>",
    "column": "<column num>"
  }
}
```

#### Response to (both to HOST and to CLIENT) 
```json 
{
  "state": "OPENED_QUESTION_WITH_ANSWER",
  "payload": {
    "question": {
      "row": "<row num>",
      "column": "<column num>",
      "question": "<question text>",
      "answer": "<answer>"
    },
    "board": "<board description>"
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
    "mark": "<X or O or EMPTY>"
  }
}
```

#### Response (both to HOST and to CLIENT)
```json 
{
  "state": "MAIN_BOARD",
  "payload": {
    "win": "<X or O or EMPTY>",
    "board": "<board description>"
  }
}
```


## Game session as board
CONNECT ws/board/{session_id}

В данном режиме нет запросов, но клиент слушает сообщения от сервера и применяет их.