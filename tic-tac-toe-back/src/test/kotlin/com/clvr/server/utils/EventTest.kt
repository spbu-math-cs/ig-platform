package com.clvr.server.utils

import com.clvr.server.model.CellContent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EventTest {
    private val jsonPrettyFormatter = Json { prettyPrint = true }

    @Test
    fun `check json format corresponds docs API`() {
        val event: RequestEvent<TestPayload> = RequestEvent(SessionId("1703"), TestPayload("very important information"))
        val expectedJsonString =
"""{
    "session": {
        "id": "1703"
    },
    "type": "MAIN_BOARD",
    "payload": {
        "importantField": "very important information"
    }
}"""

        assertEquals(expectedJsonString, jsonPrettyFormatter.encodeToString(event))
    }

    @Test
    fun `check some request from API-doc`() {
        val event: RequestEvent<SetFieldRequest> = RequestEvent(SessionId("1723"), SetFieldRequest(1, 1, CellContent.X))
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

        val decodedEvent: RequestEvent<SetFieldRequest> = decodeJsonToEvent(jsonString) as RequestEvent<SetFieldRequest>
        assertEquals(event, decodedEvent)
    }

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class TestPayload(
        @JsonNames("field")
        val importantField: String
    ): EventPayloadInterface {
        override val type: PayloadType = PayloadType.MAIN_BOARD // Just a random one
    }
}