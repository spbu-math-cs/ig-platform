package com.clvr.server

import com.clvr.server.common.Quiz
import com.clvr.server.model.GameState
import com.clvr.server.utils.*
import mu.KLogger
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger: KLogger = KotlinLogging.logger { }

object SessionStorage {
    private val games: MutableMap<SessionId, GameState> = ConcurrentHashMap()
    private val sessionManagers: ConcurrentHashMap<SessionId, SessionManager> = ConcurrentHashMap()

    fun getSessionManager(session: SessionId): SessionManager =
        sessionManagers.computeIfAbsent(session) { createSessionManager(session) }

    fun startNewGame(session: SessionId, quiz: Quiz) {
        games[session] = GameState(quiz)
    }

    private fun createSessionManager(session: SessionId) = SessionManager(session) { event ->
        // TODO: move this lambda somewhere out of storage
        event as RequestEvent

        val game = games[event.session] ?: run {
            logger.error { "No game with id ${event.session.id} found!" }
            return@SessionManager
        }

        when (event.type) {
            PayloadType.OPEN_QUESTION -> {
                val (row, column) = event.payload as QuestionRequest
                val statement = game.getQuestionStatement(row, column)
                val answer = game.getQuestionAnswer(row, column)
                val response = ResponseEvent(
                    QuestionResponse(
                        QuestionView(row, column, statement, answer),
                        GameStateView.fromGameState(game)
                    )
                )
                sendToHost(response)
                sendToClients(response)
            }
            PayloadType.SET_FIELD -> {
                val (row, column, mark) = event.payload as SetFieldRequest
                val gameResult = game.updateCellContent(row, column, mark)
                logger.info { "Game result: $gameResult" }
                val response = ResponseEvent(SetFieldResponse(GameStateView.fromGameState(game)))
                sendToHost(response)
                sendToClients(response)
            }
            else -> error("Unexpected event type for host: ${event.type}")
        }
    }
}