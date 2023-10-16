package com.clvr.server.utils

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
        val event: Event<TestPayload> = eventOf(1703, TestPayload("very important information"))
        val expectedJsonString =
"""{
    "session": {
        "id": 1703
    },
    "type": "TEST_PAYLOAD",
    "payload": {
        "importantField": "very important information"
    }
}"""

        assertEquals(expectedJsonString, jsonPrettyFormatter.encodeToString(event))
    }

    @Test
    fun `check some request from API-doc`() {
        val event: Event<SetFieldRequest> = eventOf(1703, SetFieldRequest(1, 1, CellMark.X))
        val expectedJsonString =
"""{
    "session": {
        "id": 1703
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

        val decodedEvent: Event<SetFieldRequest> = decodeJsonToEvent(jsonString) as Event<SetFieldRequest>
        assertEquals(event, decodedEvent)
    }

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class TestPayload(
        @JsonNames("field")
        val importantField: String
    ): EventPayloadInterface {
        override fun type(): String = "TEST_PAYLOAD"
    }
}