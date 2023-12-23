package com.clvr.platform.api

import com.clvr.platform.api.lobby.LobbyRequestEvent
import com.clvr.platform.api.lobby.StartGameEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LobbyEventsTest {
    private val jsonPrettyFormatter = Json { prettyPrint = true }

    @Test
    fun `start game request`() {
        val event = StartGameEvent(SessionId("142"))
        val expectedJsonString =
"""{
    "session": {
        "id": "142"
    },
    "type": "START_GAME"
}"""
        val jsonString = jsonPrettyFormatter.encodeToString(event)
        assertEquals(expectedJsonString, jsonString)

        val decodedEvent = LobbyRequestEvent.decodeFromString(jsonString)
        assertEquals(event, decodedEvent)
    }
}