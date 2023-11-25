package com.clvr.server.controller

import com.clvr.server.TicTacToeEventHandler
import com.clvr.server.TicTacToeSessionManager
import com.clvr.server.model.GameState
import com.clvr.server.model.IllegalGameActionException
import com.clvr.server.utils.*

class TicTacToeGameStateController(private val game: GameState) : TicTacToeEventHandler {
    private fun getQuestionViews(row: Int, column: Int): Pair<HostQuestionView, ClientQuestionView> {
        val statement = game.getQuestionStatement(row, column)
        val answer = game.getQuestionAnswer(row, column)
        val allHints = game.getAllHints(row, column)
        val openedHints = game.getOpenedHints(row, column)
        val hostQuestionView = HostQuestionView(row, column, statement, allHints, openedHints.size, answer)
        val clientQuestionView = ClientQuestionView(row, column, statement, openedHints)

        return hostQuestionView to clientQuestionView
    }

    private fun sendQuestionResponses(manager: TicTacToeSessionManager, row: Int, column: Int) {
        val (hostQuestionView, clientQuestionView) = getQuestionViews(row, column)
        val hostResponse = ResponseEvent(
            HostQuestionResponse(hostQuestionView, GameStateView.fromGameState(game))
        )
        val clientResponse = ResponseEvent(
            ClientQuestionResponse(clientQuestionView, GameStateView.fromGameState(game))
        )
        manager.sendToHost(hostResponse)
        manager.sendToClients(clientResponse)
    }

    override fun handle(manager: TicTacToeSessionManager, event: RequestEvent<TicTacToeRequestPayload>) = try {
        when (event.payload) {
            is QuestionRequest -> {
                val (row, column) = event.payload
                sendQuestionResponses(manager, row, column)
            }
            is NextHintRequest -> {
                val (row, column) = event.payload
                game.openNextHint(row, column)
                sendQuestionResponses(manager, row, column)
            }
            is ShowAnswerRequest -> {
                val (row, column) = event.payload
                val question = game.getQuestionStatement(row, column)
                val answer = game.getQuestionAnswer(row, column)
                val questionWithAnswer = QuestionWithAnswer(row, column, question, answer)
                val response = ResponseEvent(
                    ShowAnswerResponse(questionWithAnswer, GameStateView.fromGameState(game))
                )
                manager.sendToHost(response)
                manager.sendToClients(response)
            }
            is SetFieldRequest -> {
                val (row, column, mark) = event.payload
                val gameResult = game.updateCellContent(row, column, mark)
                val response = ResponseEvent(
                    SetFieldResponse(gameResult, GameStateView.fromGameState(game))
                )
                manager.sendToHost(response)
                manager.sendToClients(response)
            }
        }
    } catch (e: IllegalGameActionException) {
        manager.sendToHost(ResponseEvent(
            GameError(e.message ?: "Unknown error occurred!", GameStateView.fromGameState(game))
        ))
    }
}