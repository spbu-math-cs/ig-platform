package com.clvr.nk

import com.clvr.platform.api.SessionParticipantsCommunicator
import com.clvr.platform.api.ClvrGameController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

typealias NeKahootSessionParticipantsCommunicator =
        SessionParticipantsCommunicator<NeKahootResponseWithPayload<*>>

class NeKahootGameController(private val game: GameState) :
    ClvrGameController<NeKahootRequest, NeKahootResponseWithPayload<*>> {

    private fun sendQuestionResponses(manager: NeKahootSessionParticipantsCommunicator) {
        val timestamp = System.currentTimeMillis()
        val hostQuestionView = HostQuestionView.fromGameState(game, timestamp)
        val clientQuestionView = ClientQuestionView.fromGameState(game, timestamp)

        val hostResponse = NeKahootResponseWithPayload(
            HostQuestionResponse(hostQuestionView)
        )
        val clientResponse = NeKahootResponseWithPayload(
            ClientQuestionResponse(clientQuestionView)
        )
        manager.sendToHost(hostResponse)
        manager.sendToClients(clientResponse)
    }

    private fun sendCorrectAnswerResponses(manager: NeKahootSessionParticipantsCommunicator) {
        val questionWithAnswerView = QuestionWithAnswerView.fromGameState(game)

        val hostResponse = NeKahootResponseWithPayload(
            ShowAnswerEvent(questionWithAnswerView)
        )
        val clientResponse = NeKahootResponseWithPayload(
            ShowAnswerEvent(questionWithAnswerView)
        )
        manager.sendToHost(hostResponse)
        manager.sendToClients(clientResponse)
    }

    private fun sendAnswerResponses(manager: NeKahootSessionParticipantsCommunicator, clientName: String) {
        val timestamp = System.currentTimeMillis()
        val hostAnswerView = HostQuestionView.fromGameState(game, timestamp)
        val clientAnswerView = ClientQuestionView.fromGameState(game, timestamp, clientName)

        val hostResponse = NeKahootResponseWithPayload(
            HostQuestionResponse(hostAnswerView)
        )
        val clientResponse = NeKahootResponseWithPayload(
            ClientQuestionResponse(clientAnswerView)
        )
        manager.sendToHost(hostResponse)
        manager.sendToClient(clientName, clientResponse)
    }

    private fun sendResultsResponses(manager: NeKahootSessionParticipantsCommunicator) {
        val results = game.getResults()
        val resultsEvent = NeKahootResponseWithPayload(
            ResultsEvent(results)
        )
        manager.sendToHost(resultsEvent)
        manager.sendToClients(resultsEvent)
    }

    private fun nextStep(communicator: SessionParticipantsCommunicator<NeKahootResponseWithPayload<*>>) = when {
        game.isGameFinished() -> sendResultsResponses(communicator)
        else -> runBlocking {
            coroutineScope {
                game.openQuestion(System.currentTimeMillis())
                sendQuestionResponses(communicator)
                delay(game.getTime().milliseconds)
                sendCorrectAnswerResponses(communicator)
                game.closeQuestion()
            }
        }
    }

    override fun handle(
        communicator: SessionParticipantsCommunicator<NeKahootResponseWithPayload<*>>,
        event: NeKahootRequest
    ) = try {
        when (event) {
            is QuestionRequest -> {
                game.nextQuestion()
                nextStep(communicator)
            }
            is NeKahootRequestWithPayload<*> -> {
                when (event.payload) {
                    is AnswerRequest -> throw HostAnswerException()
                }
            }
        }
    } catch (e: IllegalGameActionException) {
        communicator.sendToHost(
            NeKahootResponseWithPayload(
                GameError(e.message ?: "Unknown error occurred!")
            )
        )
    }

    override fun handleFromClient(
        communicator: SessionParticipantsCommunicator<NeKahootResponseWithPayload<*>>,
        clientEndpoint: String,
        event: NeKahootRequest
    ) = try {
        when (event) {
            is QuestionRequest -> throw ClientOpenQuestionException()
            is NeKahootRequestWithPayload<*> -> {
                when (val payload = event.payload) {
                    is AnswerRequest -> {
                        if (game.getAnswerOfPlayer(clientEndpoint).isNotEmpty()) {
                            throw AlreadyAnsweredException()
                        }
                        when {
                            game.isQuestionOpened() -> {
                                game.answerQuestion(System.currentTimeMillis(), clientEndpoint, payload.answer)
                                sendAnswerResponses(communicator, clientEndpoint)
                            }
                            else -> throw LateAnswerException()
                        }
                    }
                }
            }
        }
    } catch (e: IllegalGameActionException) {
        communicator.sendToClient(
            clientEndpoint,
            NeKahootResponseWithPayload(
                GameError(e.message ?: "Unknown error occurred!")
            )
        )
    }

    override fun handleGameStart(communicator: SessionParticipantsCommunicator<NeKahootResponseWithPayload<*>>) {
        game.startGame()
        nextStep(communicator)
    }
}