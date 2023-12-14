package com.clvr.nk

import com.clvr.platform.api.SessionId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds

class NeKahootGameStateControllerTest {
    private val quiz = basicTestTemplate

    private fun makeRequest(
        game: GameState,
        requestPayload: NeKahootRequestPayload,
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

    private fun checkEvents(
        hostEvents: List<NeKahootResponsePayload>,
        clientsToEvents: Map<String, List<NeKahootResponsePayload>>
    ) {
        val hostEventPayloads = hostEvents.map { payload ->
            Assertions.assertDoesNotThrow(
                ThrowingSupplier { payload }
            )
        }
        val clientsToEventPayloads = clientsToEvents.mapValues { (_, events) ->
            events.map { payload ->
                Assertions.assertDoesNotThrow(
                    ThrowingSupplier { payload }
                )
            }
        }

        Assertions.assertEquals(2, hostEventPayloads.size)
        clientsToEventPayloads.forEach { (_, eventPayloads) ->
            Assertions.assertEquals(2, eventPayloads.size)
        }

        Assertions.assertEquals("OPENED_QUESTION", hostEventPayloads[0].state)
        Assertions.assertEquals("SHOW_QUESTION_ANSWER", hostEventPayloads[1].state)
        clientsToEventPayloads.forEach { (_, events) ->
            Assertions.assertEquals("OPENED_QUESTION", events[0].state)
            Assertions.assertEquals("SHOW_QUESTION_ANSWER", events[1].state)
        }
    }

    @Test
    fun `question close logic`() = runTest(timeout = 5.seconds) {
        val game = GameState(quiz)
        val startGameRequest = StartGameRequest()
        val (hostEvents, clientsToEvents) = makeRequest(game, startGameRequest)
        checkEvents(hostEvents, clientsToEvents)

        val questionRequest = QuestionRequest()
        val (hostEvents2, clientsToEvents2) = makeRequest(game, questionRequest)
        checkEvents(hostEvents2, clientsToEvents2)

        val (hostEvents3, clientsToEvents3) = makeRequest(game, questionRequest)
        checkEvents(hostEvents3, clientsToEvents3)
    }
}