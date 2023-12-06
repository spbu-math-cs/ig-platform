package com.clvr.server.view

import com.clvr.server.model.GameResult
import com.clvr.server.model.GameState
import com.clvr.server.utils.BoardView
import com.clvr.server.utils.ClientQuestionView
import com.clvr.server.utils.HostQuestionView

class GameStateView(private val game: GameState) {
    val lastQuestionView: Pair<HostQuestionView, ClientQuestionView>?
        get() {
            val (row, column) = game.currentQuestionPosition ?: return null
            val hostQuestionView = HostQuestionView.fromGameState(game, row, column)
            val clientQuestionView = ClientQuestionView.fromGameState(game, row, column)
            return hostQuestionView to clientQuestionView
        }

    val gameResult: GameResult
        get() = game.currentResult()

    val boardView: BoardView
        get() = BoardView.fromGameState(game)
}