package com.clvr.nk

import com.clvr.platform.api.SessionId
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EventTest {
    private val quiz = basicTestTemplate
    private val jsonPrettyFormatter = Json { prettyPrint = true }

    @Test
    fun `question request`() {
        val event = QuestionRequest(SessionId("142"))
        val expectedJsonString =
"""{
    "session": {
        "id": "142"
    },
    "type": "NEXT_QUESTION"
}"""
        val jsonString = jsonPrettyFormatter.encodeToString(event)
        Assertions.assertEquals(expectedJsonString, jsonString)

        val decodedEvent = decodeJsonToNKEvent(jsonString)
        Assertions.assertEquals(event, decodedEvent)
    }

    @Test
    fun `answer request`() {
        val event: NeKahootRequestWithPayload<AnswerRequest> = NeKahootRequestWithPayload(SessionId("533"), AnswerRequest("yumsh"))
        val expectedJsonString =
"""{
    "session": {
        "id": "533"
    },
    "type": "GIVE_ANSWER",
    "payload": {
        "answer": "yumsh"
    }
}"""
        val jsonString = jsonPrettyFormatter.encodeToString(event)
        Assertions.assertEquals(expectedJsonString, jsonString)

        val decodedEvent = decodeJsonToNKEvent(jsonString)
        Assertions.assertEquals(event, decodedEvent)
    }

    @Test
    fun `host question response`() {
        val template = quiz.questions[0]
        val hostQuestionView = HostQuestionView(
            question = template.question,
            answer = template.answer,
            answerDescription = template.answerDescription,
            answerOptions = template.answerOptions,
            time = template.time,
            answered = 4,
        )
        val event: NeKahootResponseWithPayload<HostQuestionResponse> = NeKahootResponseWithPayload(HostQuestionResponse(hostQuestionView))
        val expectedJsonString =
"""{
    "state": "OPENED_QUESTION",
    "payload": {
        "question": {
            "question": "q1",
            "answer": "opt2",
            "answer_description": null,
            "answer_options": [
                "opt1",
                "opt2",
                "opt3"
            ],
            "time": 1000,
            "answered": 4
        }
    }
}"""
        val jsonString = jsonPrettyFormatter.encodeToString(event)
        Assertions.assertEquals(expectedJsonString, jsonString)

        val decodedEvent = jsonPrettyFormatter.decodeFromString<NeKahootResponseWithPayload<HostQuestionResponse>>(jsonString)
        Assertions.assertEquals(event, decodedEvent)
    }

    @Test
    fun `client question response`() {
        val template = quiz.questions[0]
        val clientQuestionView = ClientQuestionView(
            question = template.question,
            answerOptions = template.answerOptions,
            time = template.time,
            givenAnswer = "opt2",
        )
        val event: NeKahootResponseWithPayload<ClientQuestionResponse> = NeKahootResponseWithPayload(ClientQuestionResponse(clientQuestionView))
        val expectedJsonString =
"""{
    "state": "OPENED_QUESTION",
    "payload": {
        "question": {
            "question": "q1",
            "answer_options": [
                "opt1",
                "opt2",
                "opt3"
            ],
            "time": 1000,
            "given_answer": "opt2"
        }
    }
}"""
        val jsonString = jsonPrettyFormatter.encodeToString(event)
        Assertions.assertEquals(expectedJsonString, jsonString)

        val decodedEvent = jsonPrettyFormatter.decodeFromString<NeKahootResponseWithPayload<ClientQuestionResponse>>(jsonString)
        Assertions.assertEquals(event, decodedEvent)
    }

    @Test
    fun `show answer response`() {
        val template = quiz.questions[1]
        val questionWithAnswerView = QuestionWithAnswerView(
            question = template.question,
            answer = template.answer,
            answerDescription = template.answerDescription,
            answerOptions = template.answerOptions,
            time = template.time
        )
        val event: NeKahootResponseWithPayload<ShowAnswerEvent> = NeKahootResponseWithPayload(ShowAnswerEvent(questionWithAnswerView))
        val expectedJsonString =
"""{
    "state": "SHOW_QUESTION_ANSWER",
    "payload": {
        "question": {
            "question": "q2",
            "answer": "opt1",
            "answer_description": "baza",
            "answer_options": [
                "opt1",
                "opt2"
            ],
            "time": 2000
        }
    }
}"""
        val jsonString = jsonPrettyFormatter.encodeToString(event)
        Assertions.assertEquals(expectedJsonString, jsonString)

        val decodedEvent = jsonPrettyFormatter.decodeFromString<NeKahootResponseWithPayload<ShowAnswerEvent>>(jsonString)
        Assertions.assertEquals(event, decodedEvent)
    }


    @Test
    fun `results response`() {
        val results = listOf(
            PlayerResult("player-1", 100, 1),
            PlayerResult("player-2", 30, 0),
        )
        val event: NeKahootResponseWithPayload<ResultsEvent> = NeKahootResponseWithPayload(ResultsEvent(results))
        val expectedJsonString =
            """{
    "state": "RESULT",
    "payload": {
        "results": [
            {
                "player_name": "player-1",
                "score": 100,
                "correct_questions": 1
            },
            {
                "player_name": "player-2",
                "score": 30,
                "correct_questions": 0
            }
        ]
    }
}"""
        val jsonString = jsonPrettyFormatter.encodeToString(event)
        Assertions.assertEquals(expectedJsonString, jsonString)

        val decodedEvent = jsonPrettyFormatter.decodeFromString<NeKahootResponseWithPayload<ResultsEvent>>(jsonString)
        Assertions.assertEquals(event, decodedEvent)
    }

    @Test
    fun `error response`() {
        val event: NeKahootResponseWithPayload<GameError> = NeKahootResponseWithPayload(GameError("error message"))
        val expectedJsonString =
"""{
    "state": "ERROR",
    "payload": {
        "message": "error message"
    }
}"""
        val jsonString = jsonPrettyFormatter.encodeToString(event)
        Assertions.assertEquals(expectedJsonString, jsonString)

        val decodedEvent = jsonPrettyFormatter.decodeFromString<NeKahootResponseWithPayload<GameError>>(jsonString)
        Assertions.assertEquals(event, decodedEvent)
    }
}