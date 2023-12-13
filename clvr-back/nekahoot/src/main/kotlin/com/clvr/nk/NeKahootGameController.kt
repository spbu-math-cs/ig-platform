package com.clvr.nk

import com.clvr.platform.api.SessionParticipantsCommunicator
import com.clvr.platform.api.ClvrGameController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

typealias NeKahootSessionParticipantsCommunicator =
        SessionParticipantsCommunicator<NeKahootRequest<*>, NeKahootResponse<*>>

class NeKahootGameController(private val game: GameState) :
    ClvrGameController<NeKahootRequest<*>, NeKahootResponse<*>> {

    private fun sendQuestionResponses(manager: NeKahootSessionParticipantsCommunicator) {
        if (game.isGameFinished()) {
            val resultsView = ResultsView.fromGameState(game)
            val resultsEvent = NeKahootResponse(
                ResultsEvent(resultsView)
            )
            manager.sendToHost(resultsEvent)
            manager.sendToClients(resultsEvent)
            return
        }
        val hostQuestionView = HostQuestionView.fromGameState(game)
        val clientQuestionView = ClientQuestionView.fromGameState(game)

        val hostResponse = NeKahootResponse(
            HostQuestionResponse(hostQuestionView)
        )
        val clientResponse = NeKahootResponse(
            ClientQuestionResponse(clientQuestionView)
        )
        manager.sendToHost(hostResponse)
        manager.sendToClients(clientResponse)
    }

    private fun sendCorrectAnswerResponses(manager: NeKahootSessionParticipantsCommunicator) {
        val hostAnswerView = HostQuestionView.fromGameState(game)
        val clientAnswerView = ClientQuestionView.fromGameState(game)

        val hostResponse = NeKahootResponse(
            HostQuestionResponse(hostAnswerView)
        )
        val clientResponse = NeKahootResponse(
            ClientQuestionResponse(clientAnswerView)
        )
        manager.sendToHost(hostResponse)
        manager.sendToClients(clientResponse)
    }

    private fun sendAnswerResponses(manager: NeKahootSessionParticipantsCommunicator, clientName: String) {
        val hostAnswerView = HostQuestionView.fromGameState(game)
        val clientAnswerView = ClientQuestionView.fromGameState(game, clientName)

        val hostResponse = NeKahootResponse(
            HostQuestionResponse(hostAnswerView)
        )
        val clientResponse = NeKahootResponse(
            ClientQuestionResponse(clientAnswerView)
        )
        manager.sendToHost(hostResponse)
        manager.sendToClient(clientName, clientResponse)
    }

    private fun sendResultsResponses(manager: NeKahootSessionParticipantsCommunicator) {
        val resultsView = ResultsView.fromGameState(game)
        val resultsEvent = NeKahootResponse(
            ResultsEvent(resultsView)
        )
        manager.sendToHost(resultsEvent)
        manager.sendToClients(resultsEvent)
    }

    override fun handle(
        communicator: SessionParticipantsCommunicator<NeKahootRequest<*>, NeKahootResponse<*>>,
        event: NeKahootRequest<*>
    ) = try {
        when (event.payload) {
            is StartGameRequest -> {
                game.startGame()
                sendQuestionResponses(communicator)
            }
            is QuestionRequest -> {
                game.nextQuestion()
                when {
                    game.isGameFinished() -> sendResultsResponses(communicator)
                    else -> runBlocking {
                        coroutineScope {
                            game.openQuestion()
                            sendQuestionResponses(communicator)
                            delay(game.getTime() * 1000L)
                            sendCorrectAnswerResponses(communicator)
                            game.closeQuestion()
                        }
                    }
                }
            }
            is AnswerRequest -> throw HostAnswerException()
        }
    } catch (e: IllegalGameActionException) {
        communicator.sendToHost(
            NeKahootResponse(
                GameError(e.message ?: "Unknown error occurred!")
            )
        )
    }

    override fun handleFromClient(
        communicator: SessionParticipantsCommunicator<NeKahootRequest<*>, NeKahootResponse<*>>,
        clientEndpoint: String,
        event: NeKahootRequest<*>
    ) = try {
        when (val payload = event.payload) {
            is StartGameRequest -> throw ClientStartGameException()
            is QuestionRequest -> throw ClientOpenQuestionException()
            is AnswerRequest -> {
                when {
                    game.isQuestionOpened() -> {
                        game.answerQuestion(clientEndpoint, payload.answer)
                        sendAnswerResponses(communicator, clientEndpoint)
                    }
                    else -> throw LateAnswerException()
                }
            }
        }
    } catch (e: IllegalGameActionException) {
        communicator.sendToClient(
            clientEndpoint,
            NeKahootResponse(
                GameError(e.message ?: "Unknown error occurred!")
            )
        )
    }
}