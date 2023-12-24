package com.clvr.ttt

import com.clvr.platform.api.ClvrGameView

class TicTacToeGameView(private val game: GameState): ClvrGameView<TicTacToeRequest, TicTacToeResponse<*>> {
    private val lastQuestionView: Pair<HostQuestionView, ClientQuestionView>?
        get() {
            val (row, column) = game.currentQuestionPosition ?: return null
            val hostQuestionView = HostQuestionView.fromGameState(game, row, column)
            val clientQuestionView = ClientQuestionView.fromGameState(game, row, column)
            return hostQuestionView to clientQuestionView
        }

    private val gameResult: GameResult
        get() = game.currentResult()

    private val boardView: BoardView
        get() = BoardView.fromGameState(game)

    override val hostView: List<TicTacToeResponse<*>>
        get() {
            val gameStateView = TicTacToeGameView(game)
            val boardStateEvent = TicTacToeResponse(
                SetFieldResponse(gameStateView.gameResult, gameStateView.boardView)
            )

            val (lastQuestionView, _) = gameStateView.lastQuestionView ?: return listOf(boardStateEvent)

            val lastQuestionViewEvent = TicTacToeResponse(
                HostQuestionResponse(lastQuestionView, gameStateView.boardView)
            )

            return listOf(boardStateEvent, lastQuestionViewEvent)
        }

    override val clientView: List<TicTacToeResponse<*>>
        get() {
            val gameStateView = TicTacToeGameView(game)

            val boardStateEvent = TicTacToeResponse(
                SetFieldResponse(gameStateView.gameResult, gameStateView.boardView)
            )

            val (_, lastQuestionView) = gameStateView.lastQuestionView ?: return listOf(boardStateEvent)

            val lastQuestionViewEvent = TicTacToeResponse(
                ClientQuestionResponse(lastQuestionView, gameStateView.boardView)
            )

            return listOf(boardStateEvent, lastQuestionViewEvent)
        }
}