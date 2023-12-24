# CLVR API

# Общие моменты

Все игры будут использовать шаблоны, поэтому запросы (структура URL-ов и некоторых полей будет совпадать).

Сама игра будет проводиться при помощи WebSocket'ов.

## Events structure

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

В случае ненадобности, поле `payload` может отсутствовать.

Все ответы по WebSocket имеют следующую структуру:
```json 
{
  "state": "<client state during this event>",
  "payload": "<json with additional information>"
}
```

## Error notification
В любой момент сервер может отправить сообщение с ошибкой.
Оно может прийти как вместо ответа на какой-то запрос (если он некорректен и выполнить его невозможно), так и само по себе, без привязки к конкретному запросу.
Формат сообщения об ошибке:

```json 
{
  "state": "ERROR",
  "payload": {
    "message": "<message with details about error>"
  }
}
```

## Homepage
GET /

Возвращает все необходимые скрипты, HTML и т.д., чтобы далее frontend правильно работал.

*Пока, домен отдающий главную страницу будет отличаться от домена с API.
В будущем это будет решаться либо через proxy, либо будет единый сервер, умеющий обрабатывать все запросы.*

### Main page

Основная страница содержит 2 основных элемента: 
* Сетка игр с иконками (захордкоженные на стороне фронта). В нашем случае будет 2 игры: tic-tac-toe и упрощенная версия kahoot.
* Авторизация - переход на страницу, где можно либо зарегистрироваться, либо зайти в уже существующий аккаунт.

### Authentication

WARNING: надо проверить как все работает на практике, пока скорее proposal

Аутентификация будет происходить при помощи cookies, поэтому даже при подключении через WebSocket не должно быть проблем.

Все запросы должны проходить по зашифрованному каналу.

#### Registration 

##### Request 
POST /register

```json 
{
  "login": "<user login, it will be displayed as user name>",
  "password": "<user password>"
}
```

##### Response 

OK - если регистрация прошла успешно + cookies

#### Login 

##### Request 
POST /login

```json 
{
  "login": "<user login, it will be displayed as user name>",
  "password": "<user password>"
}
```

##### Response

OK - если вход прошел успешно + cookies

#### Полезные ссылки

[ktor ssl](https://ktor.io/docs/ssl.html)
[ktor cookies](https://ktor.io/docs/sessions.html#examples)

## Start game

После создания игры у хоста будет возможность подождать, пока подключатся игроки, и уже затем начать игру.

#### Request
```json 
{
  "type": "START_GAME"
}
```

#### Response 

Response у каждой игры свой.

#### New players connections

По мере того как подключаются и отключаются игроки, хосту и клиентам приходят ивенты со списком всех подключенных игроков.

```json
{
  "state": "PREPARING",
  "payload": {
    "players": [{
      "name": "<player name>"
      }, ...
    ]
  }
}
```



# Tic-tac-toe

## Main tic-tac-toe page

На основной странице меню состоящие из 2 опций: создать игру как host или войти как игрок.

### Get template list

Запрос списка всех существующих шаблонов.

#### Request

GET /tic-tac-toe/template

#### Response
```json 
{
  "templates": [{
    "name": "<template name>",
    "id": "<template id>",
    "comment": "<some additional information about template, e.g. short description>"
  }, ...]
}
```

### Get template description

Запрос полного описания конкретного шаблона.

#### Request

GET /tic-tac-toe/template/{template-id}

#### Response

```json 
{
  "id": "<template id>",
  "name": "<template name>",
  "comment": "<some additional information about template, e.g. short description>",
  "board": [{
    "row": "<row>",
    "column": "<column>",
    "topic": "<topic>",
    "question": "<question text>",
    "hints": ["<hint1>", "<hint2>", ...],
    "answer": "<answer>"
  }, ...]
}
```

### Template constructor

#### Create template

Создание нового шаблона.

#### Request

POST /tic-tac-toe/template

```json 
{
  "name": "<template name>",
  "comment": "<some additional information about template, e.g. short description>",
  "board": [{
    "row": "<row>",
    "column": "<column>",
    "topic": "<topic>",
    "question": "<question text>",
    "hints": ["<hint1>", "<hint2>", ...],
    "answer": "<answer>"
  }, ...]
}
```

Валидация:
* количество вопрос ровно 9
* для каждого вопроса присутствуют (непустые): `question`, `answer`, `topic`
* `row` и `column` проходят все комбинации (0..2, 0..2)

Первичная валидация происходит на стороне клиента (она может быть не полной), места, из-за которых не проходит валидация,
подсвечиваются/подчеркиваются/выделяются каким-либо еще другим образом.

Полная валидация происходит на стороне сервера, и в случае проваленной валидации отправляется [400](https://http.cat/status/400).

#### Response

```json 
{
  "id": "<template id>"
}
```

### Delete template

Удаление шаблона.

#### Request

DELETE /tic-tac-toe/template/{template-id}

### Create game session

Создание игровой сессии по шаблону с id `template-id`.

#### Request
POST /tic-tac-toe/game

```json 
{
  "template_id": "<template id>",
  "game_configuration": {
    "replace_marks": "<ENABLED or DISABLED>",
    "open_multiple_questions": "<ENABLED or DISABLED>"
  }
}
```

В данном json'е, все поля внутри `game_configuration` опциональны. Если не присутствует какая-то опция, то выставляется ее дефолтное значение на стороне сервера. 
Список всех возможных опций с их дефолтными значениями:
```json
{
  "replace_marks": "ENABLED",
  "open_multiple_questions": "ENABLED"
}
```

Описание опций: 
* `replace_marks` - можно ли заменять уже проставленные X и O 
* `open_multiple_questions` - можно ли открывать следующий вопрос, пока не разобран текущий (не проставлен ни X, ни O, ни EMPTY)

#### Response

```json 
{
  "session": {
    "id": "<session id>"
  }
}
```

## Game session as host

CONNECT /ws/tic-tac-toe/host/{session_id}

### Board description

В каждом ответе (кроме сообщения об ошибке), в качестве одного из поля `payload` присутствует поле `board` с полным описанием доски. 
Это описание имеет следующую структуру:
```json 
{
  "board": {
    "cells": [{
      "row": "<row num>",
      "column": "<column num>",
      "mark": "<X or O or EMPTY>",
      "topic": "<topic>"
    }, ...]
  }
}
```

### Start game

#### Response to HOST and BOARD and PLAYER
```json 
{
  "state": "MAIN_BOARD",
  "payload": {
    "win": "<X or O or EMPTY>",
    "board": "<board description>"
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
    "mark": "<X or O or EMPTY or NOT_OPENED>"
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

### Wrong Answer

Этот запрос отправляется, если одна из команд ответила на текущий вопрос неправильно. 
После этого запроса происходит переход либо в OPEN_QUESTION, если другая команда ещё не отвечала, 
либо в MAIN_BOARD, если обе команды уже успели ответить неправильно (в этом случае в клетку, соответствующую вопросу,
выставляется значение EMPTY)

#### Request
```json 
{
  "session": {
    "id": "<session_id>"
  },
  "type": "WRONG_ANSWER",
  "payload": {
    "row": "<row num>",
    "column": "<column num>"
  }
}
```

#### Response

Такой же, как в `OPEN_QUESTION` или `SET_FIELD`

### Correct Answer

Этот запрос отправляется, если команда правильно отвечает на вопрос. Запрос аналогичен SET_FIELD, 
разница лишь в том, что здесь сервер сам определяет, что нужно поставить в текущую клетку, так как он знает, 
какая команда сейчас отвечает.

#### Request
```json 
{
  "session": {
    "id": "<session_id>"
  },
  "type": "CORRECT_ANSWER",
  "payload": {
    "row": "<row num>",
    "column": "<column num>"
  }
}
```

#### Response (both to HOST and to CLIENT)

Такой же, как и в случае `SET_FIELD`

## Game session as board
CONNECT ws/tic-tac-toe/board/{session_id}

В данном режиме нет запросов, но клиент слушает сообщения от сервера и применяет их.

## Game session as client 

CONNECT /ws/tic-tac-toe/player/{session_id}/

### Choose team

#### Request
```json
{
  "session": {
    "id": "<session_id>"
  }
  "type": "TEAM_SELECTION"
  "payload": {
    "team": "<X or O>"
  }
}
```

#### Response (to host and clients)
```json
{
  "state": "TEAM_SELECTED",
  "payload": {
    "player": "<player name>",
    "team": "<X or O>"
  }
}
```

### Press Button

#### Request

```json 
{
  "session": {
    "id": "<session_id>"
  },
  "type": "PRESS_BUTTON"
}
```

#### Response to HOST
```json 
{
  "state": "TEAM_{team}_IS_ANSWERING",
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

#### Response to CLIENT
```json
{
  "state": "TEAM_{team}_IS_ANSWERING",
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

Далее при правильном ответе на вопрос host отправляет запрос CORRECT_ANSWER, а при неправильном WRONG_ANSWER

# NeKahoot

## Main NeKahoot page

Основная страница содержит меню из 2 опций: создать игру как host или войти как игрок.

### Get template list

Запрос списка всех существующих шаблонов.

#### Request

GET /nekahoot/template

#### Response
```json 
{
  "templates": [{
    "name": "<template name>",
    "id": "<template id>",
    "comment": "<some additional information about template, e.g. short description>"
  }, ...]
}
```

### Get template description

Запрос полного описания конкретного шаблона.

#### Request

GET /nekahoot/template/{template-id}

#### Response

```json 
{
  "id": "<template id>",
  "name": "<template name>",
  "comment": "<some additional information about template, e.g. short description>",
  "questions": [{
    "question": "<question text>",
    "answer": "<answer>",
    "answer_description": "<description for answer (optional)>",
    "answer_options": ["<option1>", "<option2>", ...],
    "time": "<time for question in seconds>"
  }, ...]
}
```

### Template constructor

#### Create template

Создание нового шаблона.

#### Request

POST /nekahoot/template

```json 
{
  "name": "<template name>",
  "comment": "<some additional information about template, e.g. short description>",
  "questions": [{
    "question": "<question text>",
    "answer": "<answer>",
    "answer_description": "<description for answer (optional)>",
    "answer_options": ["<option1>", "<option2>", ...],
    "time": "<time for question in seconds>"
  }, ...]
}
```

Валидация: 
* поле `answer` всегда является одним из `answer options`
* поле `time` - целое число > 5
* количество опций в `answer options` > 1

Первичная валидация происходит на стороне клиента (она может быть не полной), места, из-за которых не проходит валидация,
подсвечиваются/подчеркиваются/выделяются каким-либо еще другим образом.

Полная валидация происходит на стороне сервера, в случае проваленной валидации отправляется [400](https://http.cat/status/400).

#### Response

```json 
{
  "id": "<template id>"
}
```

### Delete template

Удаление шаблона.

#### Request

DELETE /nekahoot/template/{template-id}

### Create game session

Создание игровой сессии по шаблону с id `template-id`.

#### Request
POST /nekahoot/game

```json 
{
  "template_id": "<template id>"
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

CONNECT /ws/nekahoot/host/{session_id}

После создание игры у хоста будет доступно только два типа действия: 
* начать игру (start game)
* перейти к следующему вопросу, после того как был показан ответ на предыдущий

### Start game

#### Response to HOST
```json 
{
  "state": "OPENED_QUESTION",
  "payload": {
    "question": {
      "question": "<question text>",
      "answer": "<answer>",
      "answer_description": "<description for answer (optional)>",
      "answer_options": ["<option1>", "<option2>", ...],
      "time": "<left time for question in seconds>",
      "answered": "<number of people who already answered question>"
    }
  }
}
```

#### Response to CLIENT
```json 
{
  "state": "OPENED_QUESTION",
  "payload": {
    "question": {
      "question": "<question text>",
      "answer_options": ["<option1>", "<option2>", ...],
      "time": "<left time for question in seconds>",
      "given_answer": "<empty if player has not given answer | number of given answer OR answer itself>"
    }
  }
}
```

### Automatic event after end of the question time

Через `time` секунд клиентам и хосту придет ивент с ответом: 
```json
{
  "state": "SHOW_QUESTION_ANSWER",
  "payload": {
    "question": {
      "question": "<question text>",
      "answer": "<answer>",
      "answer_description": "<description for answer (optional)>",
      "answer_options": ["<option1>", "<option2>", ...],
      "time": "<left time for question in seconds>"
    }
  }
}
```

### Next question 

#### Request

```json 
{
  "type": "NEXT_QUESTION"
}
```

#### Response 

Если вопрос не был последним, то приходит такой же response, как и в `Start game`.

Если же вопрос был последним, то приходит список игроков упорядоченных по количеству набранных очков: 
```json
{
  "state": "RESULT",
  "payload": [
    {
      "player_name": "<player name>",
      "score": "<total score>",
      "correct_question": "<total number of correct answered questions>"
    }
  ]
}
```

## Game session as player

CONNECT /ws/nekahoot/player/{session_id}

После создания игры у клиента по факту будет возможность только выбирать ответ на вопрос. 

### Choose answer

Пока время на ответ не истекло, игрок может дать ответ на вопрос. 
При попытке дать второй ответ на вопрос или ответить по истечению времени, то будет возвращена ошибка. 

#### Request 

```json 
{ 
  "state": "GIVE_ANSWER",
  "payload": {
    "answer": "?<number of answer OR answer itself>?"
  }
}
```

#### Response to HOST
Такой же, как и в случае с `Start game`

#### Response to CLIENT
Такой же, как и в случае с `Start game`
