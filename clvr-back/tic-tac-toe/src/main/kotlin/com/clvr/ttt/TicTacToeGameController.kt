package com.clvr.ttt

import com.clvr.platform.api.SessionParticipantsCommunicator
import com.clvr.platform.api.ClvrGameController

typealias TicTacToeSessionParticipantsCommunicator =
    SessionParticipantsCommunicator<TicTacToeResponse<*>>

class TicTacToeGameController(private val game: GameState) :
    ClvrGameController<TicTacToeRequest<*>, TicTacToeResponse<*>> {
    private fun sendQuestionResponses(manager: TicTacToeSessionParticipantsCommunicator, row: Int, column: Int) {
        val hostQuestionView = HostQuestionView.fromGameState(game, row, column)
        val clientQuestionView = ClientQuestionView.fromGameState(game, row, column)

        val hostResponse = TicTacToeResponse(
            HostQuestionResponse(hostQuestionView, BoardView.fromGameState(game))
        )
        val clientResponse = TicTacToeResponse(
            ClientQuestionResponse(clientQuestionView, BoardView.fromGameState(game))
        )
        manager.sendToHost(hostResponse)
        manager.sendToClients(clientResponse)
    }

    override fun handle(communicator: SessionParticipantsCommunicator<TicTacToeResponse<*>>, event: TicTacToeRequest<*>) = try {
        when (val payload = event.payload) {
            is QuestionRequest -> {
                val (row, column) = payload
                sendQuestionResponses(communicator, row, column)
            }
            is NextHintRequest -> {
                val (row, column) = payload
                game.openNextHint(row, column)
                sendQuestionResponses(communicator, row, column)
            }
            is ShowAnswerRequest -> {
                val (row, column) = payload
                val question = game.getQuestionStatement(row, column)
                val answer = game.getQuestionAnswer(row, column)
                val questionWithAnswer = QuestionWithAnswer(row, column, question, answer)
                val response = TicTacToeResponse(
                    ShowAnswerResponse(questionWithAnswer, BoardView.fromGameState(game))
                )
                communicator.sendToHost(response)
                communicator.sendToClients(response)
            }
            is SetFieldRequest -> {
                val (row, column, mark) = payload
                val gameResult = game.updateCellContent(row, column, mark)
                val response = TicTacToeResponse(
                    SetFieldResponse(gameResult, BoardView.fromGameState(game))
                )
                communicator.sendToHost(response)
                communicator.sendToClients(response)
            }
        }
    } catch (e: IllegalGameActionException) {
        communicator.sendToHost(
            TicTacToeResponse(
                GameError(e.message ?: "Unknown error occurred!")
            )
        )
    }

    override fun handleFromClient(
        communicator: SessionParticipantsCommunicator<TicTacToeResponse<*>>,
        clientEndpoint: String,
        event: TicTacToeRequest<*>
    ) = communicator.sendToClient(clientEndpoint,
        TicTacToeResponse(
            GameError("You are not allowed to send events to the server!")
        )
    )

    override fun handleGameStart(communicator: SessionParticipantsCommunicator<TicTacToeResponse<*>>) {
        val gameResult = game.currentResult()
        val response = TicTacToeResponse(
            SetFieldResponse(gameResult, BoardView.fromGameState(game))
        )
        communicator.sendToHost(response)
        communicator.sendToClients(response)
    }
}