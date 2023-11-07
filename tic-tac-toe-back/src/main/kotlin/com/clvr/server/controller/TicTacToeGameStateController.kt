package com.clvr.server.controller

import com.clvr.server.TicTacToeEventHandler
import com.clvr.server.TicTacToeSessionManager
import com.clvr.server.model.GameState
import com.clvr.server.utils.*

class TicTacToeGameStateController(private val game: GameState) : TicTacToeEventHandler {
    override fun handle(manager: TicTacToeSessionManager, event: RequestEvent<TicTacToeRequestPayload>) {
        when (event.payload) {
            is QuestionRequest -> {
                val (row, column) = event.payload
                val statement = game.getQuestionStatement(row, column)
                val answer = game.getQuestionAnswer(row, column)
                val response = ResponseEvent(
                    QuestionResponse(
                        QuestionView(row, column, statement, answer),
                        GameStateView.fromGameState(game)
                    )
                )
                manager.sendToHost(response)
                manager.sendToClients(response)
            }
            is SetFieldRequest -> {
                val (row, column, mark) = event.payload
                val gameResult = game.updateCellContent(row, column, mark)
                val response = ResponseEvent(SetFieldResponse(GameStateView.fromGameState(game)))
                manager.sendToHost(response)
                manager.sendToClients(response)
            }
        }
    }
}