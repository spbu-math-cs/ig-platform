package com.clvr.nk

import com.clvr.platform.api.SessionId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
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

    private fun makeRequestFromClient(
        game: GameState,
        requestPayload: NeKahootRequestPayload,
        clientEndpoint: String,
    ): Pair<List<NeKahootResponsePayload>, Map<String, List<NeKahootResponsePayload>>> {
        val controller = NeKahootGameController(game)
        val communicator = MockCommunicator()
        val hostChannel = communicator.hostChannel
        val clientChannel = communicator.clientsToChannels
        controller.handleFromClient(communicator, clientEndpoint, NeKahootRequest(SessionId("0"), requestPayload))

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

    private fun checkCloseOfQuestion(
        hostEventPayloads: List<NeKahootResponsePayload>,
        clientsToEventPayloads: Map<String, List<NeKahootResponsePayload>>
    ) {
        Assertions.assertEquals(2, hostEventPayloads.size)
        clientsToEventPayloads.forEach { (_, eventPayloads) ->
            Assertions.assertEquals(2, eventPayloads.size)
        }

        Assertions.assertEquals(HostQuestionResponse.state, hostEventPayloads[0].state)
        Assertions.assertEquals(ShowAnswerEvent.state, hostEventPayloads[1].state)
        clientsToEventPayloads.forEach { (_, events) ->
            Assertions.assertEquals(HostQuestionResponse.state, events[0].state)
            Assertions.assertEquals(ShowAnswerEvent.state, events[1].state)
        }
    }

    @Test
    fun `question close logic`() = runTest(timeout = 5.seconds) {
        val game = GameState(quiz)
        val startGameRequest = StartGameRequest()
        val (hostEvents, clientsToEvents) = makeRequest(game, startGameRequest)
        checkCloseOfQuestion(hostEvents, clientsToEvents)

        val questionRequest = QuestionRequest()
        val (hostEvents2, clientsToEvents2) = makeRequest(game, questionRequest)
        checkCloseOfQuestion(hostEvents2, clientsToEvents2)

        val (hostEvents3, clientsToEvents3) = makeRequest(game, questionRequest)
        checkCloseOfQuestion(hostEvents3, clientsToEvents3)
    }


    private fun checkAnswerResponse(
        clientEndpoint: String,
        game: GameState,
        hostEventPayloads: List<NeKahootResponsePayload>,
        clientsToEventPayloads: Map<String, List<NeKahootResponsePayload>>
    ) {
        Assertions.assertEquals(1, hostEventPayloads.size)
        Assertions.assertNotNull(clientsToEventPayloads[clientEndpoint])
        clientsToEventPayloads.forEach { (endpoint, eventPayloads) ->
            when (endpoint) {
                clientEndpoint -> Assertions.assertEquals(1, eventPayloads.size)
                else -> Assertions.assertEquals(0, eventPayloads.size)
            }
        }

        val payload = hostEventPayloads.single()
        Assertions.assertEquals(HostQuestionResponse.state, payload.state)
        payload as HostQuestionResponse
        Assertions.assertEquals(game.getNumberOfAnswers(), payload.questionView.answered)

        val clientPayload = clientsToEventPayloads[clientEndpoint]!!.single()
        Assertions.assertEquals(ClientQuestionResponse.state, clientPayload.state)
        clientPayload as ClientQuestionResponse
        Assertions.assertEquals(game.getAnswerOfPlayer(clientEndpoint), clientPayload.questionView.givenAnswer)
    }

    private fun checkHostErrorResponse(
        expectedException: IllegalGameActionException,
        hostEventPayloads: List<NeKahootResponsePayload>,
        clientsToEventPayloads: Map<String, List<NeKahootResponsePayload>>
    ) {
        Assertions.assertEquals(1, hostEventPayloads.size)
        clientsToEventPayloads.forEach { (_, eventPayloads) ->
            Assertions.assertEquals(0, eventPayloads.size)
        }
        val hostPayload = hostEventPayloads.single()
        Assertions.assertEquals(GameError.state, hostPayload.state)
        hostPayload as GameError
        Assertions.assertEquals(expectedException.message, hostPayload.message)
    }

    private fun checkClientErrorResponse(
        expectedException: IllegalGameActionException,
        clientEndpoint: String,
        hostEventPayloads: List<NeKahootResponsePayload>,
        clientsToEventPayloads: Map<String, List<NeKahootResponsePayload>>
    ) {
        Assertions.assertEquals(0, hostEventPayloads.size)
        Assertions.assertNotNull(clientsToEventPayloads[clientEndpoint])
        clientsToEventPayloads.forEach { (endpoint, eventPayloads) ->
            when (endpoint) {
                clientEndpoint -> Assertions.assertEquals(1, eventPayloads.size)
                else -> Assertions.assertEquals(0, eventPayloads.size)
            }
        }
        val clientPayload = clientsToEventPayloads[clientEndpoint]!!.single()
        Assertions.assertEquals(GameError.state, clientPayload.state)
        clientPayload as GameError
        Assertions.assertEquals(expectedException.message, clientPayload.message)
    }

    @Test
    fun `answer to question logic`() = runTest {
        val game = GameState(quiz)
        game.startGame()
        game.openQuestion(System.currentTimeMillis())
        launch {
            delay(game.getTime().toLong().seconds)
            game.closeQuestion()
        }

        val client1 = "client-1"
        val client1Request = AnswerRequest("opt2")
        val client2 = "client-2"
        val client2Request = AnswerRequest("opt1")
        val client2RequestAttempt2 = AnswerRequest("opt2")
        val client3 = "client-3"
        val client3Request = AnswerRequest("opt2")


        delay(10)
        val (hostEvents, clientsToEvents) = makeRequestFromClient(game, client1Request, client1)
        checkAnswerResponse(client1, game, hostEvents, clientsToEvents)

        delay(533)
        val (hostEvents2, clientsToEvents2) = makeRequestFromClient(game, client2Request, client2)
        checkAnswerResponse(client2, game, hostEvents2, clientsToEvents2)

        delay(1)
        val (hostEvents2Attempt2, clientsToEvents2Attempt2) = makeRequestFromClient(game, client2RequestAttempt2, client2)
        checkClientErrorResponse(AlreadyAnsweredException(), client2, hostEvents2Attempt2, clientsToEvents2Attempt2)

        delay(500)
        val (hostEvents3, clientsToEvents3) = makeRequestFromClient(game, client3Request, client3)
        checkClientErrorResponse(LateAnswerException(), client3, hostEvents3, clientsToEvents3)
    }

    @Test
    fun `illegal actions from host`() = runTest {
        val game = GameState(quiz)
        game.startGame()
        game.openQuestion(System.currentTimeMillis())
        launch {
            delay(game.getTime().toLong().seconds)
            game.closeQuestion()
        }

        val answerRequest = AnswerRequest("opt2")
        val (hostEvents, clientsToEvents) = makeRequest(game, answerRequest)
        checkHostErrorResponse(HostAnswerException(), hostEvents, clientsToEvents)
    }

    @Test
    fun `illegal actions from client`() = runTest {
        val game = GameState(quiz)
        game.startGame()
        game.openQuestion(System.currentTimeMillis())
        launch {
            delay(game.getTime().toLong().seconds)
            game.closeQuestion()
        }

        val client1 = "client-1"
        val client1Request = StartGameRequest()
        val (hostEvents, clientsToEvents) = makeRequestFromClient(game, client1Request, client1)
        checkClientErrorResponse(ClientStartGameException(), client1, hostEvents, clientsToEvents)

        val client2 = "client-2"
        val client2Request = QuestionRequest()
        val (hostEvents2, clientsToEvents2) = makeRequestFromClient(game, client2Request, client2)
        checkClientErrorResponse(ClientOpenQuestionException(), client2, hostEvents2, clientsToEvents2)
    }
}