package com.clvr.ttt

import com.clvr.platform.api.SessionId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventTest {
    private val jsonPrettyFormatter = Json { prettyPrint = true }

    @Test
    fun `check json format corresponds docs API`() {
        val event = DummyRequestEvent(
                SessionId("1703"),
                DummyRequestPayload("very important information"),
        )
        val expectedJsonString =
"""{
    "session": {
        "id": "1703"
    },
    "type": "MAIN_BOARD",
    "payload": {
        "field": "very important information"
    }
}"""

        assertEquals(expectedJsonString, jsonPrettyFormatter.encodeToString(event))
    }

    @Test
    fun `check some request from API-doc`() {
        val event: TicTacToeRequest<SetFieldRequest> = TicTacToeRequest(SessionId("1723"), SetFieldRequest(1, 1, CellContent.X))
        val expectedJsonString =
"""{
    "session": {
        "id": "1723"
    },
    "type": "SET_FIELD",
    "payload": {
        "row": 1,
        "column": 1,
        "mark": "X"
    }
}"""
        val jsonString = jsonPrettyFormatter.encodeToString(event)
        assertEquals(expectedJsonString, jsonString)

        val decodedEvent = decodeJsonToTTTEvent(jsonString)
        assertEquals(event, decodedEvent)
    }

    interface MyRequest {
        val session: SessionId
        val type: String
    }

    @Serializable
    private data class DummyRequestEvent private constructor(
        override val session: SessionId,
        override val type: String,
        val payload: DummyRequestPayload,
    ) : MyRequest {
        constructor(session: SessionId, payload: DummyRequestPayload): this(
            session,
            "MAIN_BOARD", // Just a random one
            payload)
    }

    @Serializable
    private data class DummyRequestPayload(
            @SerialName("field")
            val importantField: String
    )
}