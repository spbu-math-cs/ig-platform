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
       val event: Event<TestPayload> = Event(
            Session(1703),
           "test payload",
           TestPayload("very important information")
        )

        val expectedJsonString =
"""{
    "session": {
        "id": 1703
    },
    "type": "test payload",
    "payload": {
        "importantField": "very important information"
    }
}"""

        assertEquals(expectedJsonString, jsonPrettyFormatter.encodeToString(event))
    }

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    data class TestPayload(
        @JsonNames("field")
        val importantField: String
    )
}