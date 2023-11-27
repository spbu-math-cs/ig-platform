package com.clvr.server

import com.clvr.server.common.Config
import com.clvr.server.common.Quiz
import com.clvr.server.controller.TicTacToeGameStateController
import com.clvr.server.model.GameState
import com.clvr.server.utils.*
import java.util.concurrent.ConcurrentHashMap

typealias TicTacToeSessionManager = SessionManager<TicTacToeRequestPayload, TicTacToeResponsePayload>
typealias TicTacToeEventHandler = EventHandler<TicTacToeRequestPayload, TicTacToeResponsePayload>

private data class GameRecord(
    val gameState: GameState,
    val sessionManager: TicTacToeSessionManager
)

// TODO: get rid of !!
// TODO: add exception handling here
object TicTacToeSessionStorage {
    private val games: MutableMap<SessionId, GameRecord> = ConcurrentHashMap()

    fun getSessionManager(session: SessionId): TicTacToeSessionManager =
        games[session]!!.sessionManager

    fun startNewGame(session: SessionId, quiz: Quiz, config: Config) {
        val gameState = GameState(quiz, config)
        val sessionManager = SessionManager(TicTacToeGameStateController(gameState))
        games[session] = GameRecord(gameState, sessionManager)
    }

    fun getGameStateView(session: SessionId): GameStateView {
        return GameStateView.fromGameState(games[session]!!.gameState)
    }
}