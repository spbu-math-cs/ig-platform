package com.clvr.nk

import com.clvr.platform.api.SessionId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class NeKahootGameStateControllerTest {
    private val quiz = basicTestTemplate

    private fun makeRequest(
        game: GameState,
        requestPayload: NeKahootRequestPayload
    ): Pair<List<NeKahootResponsePayload>, Map<String, List<NeKahootResponsePayload>>> {
        val controller = NeKahootGameController(game)
        val communicator = MockCommunicator()
        val hostChannel = communicator.hostChannel
        val clientChannel = communicator.clientsToChannels
        controller.handle(communicator, NeKahootRequest(SessionId("0"), requestPayload))

        val hostEvents = generateSequence {
            hostChannel.tryReceive().getOrNull()
        }.map {
            it.payload
        }

        val clientEvents = clientChannel.mapValues { (_, channel) ->
            generateSequence {
                channel.tryReceive().getOrNull()
            }.map {
                it.payload
            }.toList()
        }

        return hostEvents.toList() to clientEvents.toMap()
    }

    @Test
    fun `example test`() {
        val game = GameState(quiz)
        val startGameRequest = StartGameRequest()
        val (hostEvents, clientsToEvents) = makeRequest(game, startGameRequest)
        assertEquals(1, hostEvents.size)
        clientsToEvents.forEach { (_, events) ->
            assertEquals(1, events.size)
        }
    }
}