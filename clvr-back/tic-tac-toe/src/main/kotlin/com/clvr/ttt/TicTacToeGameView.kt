package com.clvr.ttt

import com.clvr.platform.api.ClvrGameView
import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent

class TicTacToeGameView(private val game: GameState): ClvrGameView<TicTacToeRequestPayload, TicTacToeResponsePayload> {
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

    override val hostView: List<ResponseEvent<TicTacToeResponsePayload>>
        get() {
            val gameStateView = TicTacToeGameView(game)
            val boardStateEvent = ResponseEvent(
                SetFieldResponse(gameStateView.gameResult, gameStateView.boardView)
            )

            val (lastQuestionView, _) = gameStateView.lastQuestionView ?: return listOf(boardStateEvent)

            val lastQuestionViewEvent = ResponseEvent(
                HostQuestionResponse(lastQuestionView, gameStateView.boardView)
            )

            return listOf(boardStateEvent, lastQuestionViewEvent)
        }

    override val clientView: List<ResponseEvent<TicTacToeResponsePayload>>
        get() {
            val gameStateView = TicTacToeGameView(game)

            val boardStateEvent = ResponseEvent(
                SetFieldResponse(gameStateView.gameResult, gameStateView.boardView)
            )

            val (_, lastQuestionView) = gameStateView.lastQuestionView ?: return listOf(boardStateEvent)

            val lastQuestionViewEvent = ResponseEvent(
                ClientQuestionResponse(lastQuestionView, gameStateView.boardView)
            )

            return listOf(boardStateEvent, lastQuestionViewEvent)
        }

    override fun decodeJsonToEvent(jsonString: String): RequestEvent<TicTacToeRequestPayload> {
        return decodeJsonToTTTEvent(jsonString)
    }

    override fun encodeEventToJson(event: ResponseEvent<TicTacToeResponsePayload>): String {
        return encodeTTTEventToJson(event)
    }
}