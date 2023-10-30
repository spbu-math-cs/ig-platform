package com.clvr.server.plugins

import com.clvr.server.GameState
import com.clvr.server.logger
import com.clvr.server.utils.Board
import com.clvr.server.utils.Cell
import com.clvr.server.utils.Event
import com.clvr.server.utils.Question
import com.clvr.server.utils.QuestionRequest
import com.clvr.server.utils.QuestionResponse
import com.clvr.server.utils.RequestEvent
import com.clvr.server.utils.SessionManager
import com.clvr.server.utils.SetFieldRequest
import com.clvr.server.utils.SetFieldResponse
import com.clvr.server.utils.decodeJsonToEvent
import com.clvr.server.utils.encodeEventToJson
import com.clvr.server.utils.responseEventOf
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import mu.KLogger
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

fun Application.configureSockets() {
    val logger: KLogger = logger()

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        // Leave it in order to test simple connection
        webSocket("/ws") { // websocketSession
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }

        webSocket("/ws/host/{session_id}") {
            val sessionId: Long = call.parameters["session_id"]?.toLong() ?: throw IllegalArgumentException("failed to get session id")
            val hostEndpoint: String = endpoint(call.request.origin)
            val sessionManager: SessionManager = this@configureSockets.getSessionManager(sessionId)
            val hostChannel = sessionManager.hostChannel

            logger.info { "Host $hostEndpoint connected to game $sessionId" }

            launch {
                for (event in hostChannel) {
                    val jsonEvent: String = encodeEventToJson(event)
                    outgoing.send(Frame.Text(jsonEvent))
                    logger.debug { "Send event $jsonEvent to host $hostEndpoint in $sessionId game" }
                }
            }

            for (frame in incoming) {
                if (frame !is Frame.Text) {
                    continue
                }

                val jsonEvent: String = frame.readText()
                logger.debug { "Receive event $jsonEvent to host $hostEndpoint in $sessionId game" }

                val event: Event<*> = decodeJsonToEvent(jsonEvent)
                sessionManager.handleHostEvent(event)
            }
        }

        webSocket("/ws/client/{session_id}") {
            val sessionId: Long = call.parameters["session_id"]?.toLong() ?: throw IllegalArgumentException("failed to get session id")
            val clientEndpoint: String = call.request.origin.remoteAddress + ":" + call.request.origin.remotePort
            val sessionManager: SessionManager = this@configureSockets.getSessionManager(sessionId)
            val clientChannel: Channel<Event<*>> = sessionManager.registerClient(clientEndpoint)
            logger.info { "Client $clientEndpoint connected to game $sessionId" }

            try {
                for (event in clientChannel) {
                    val jsonEvent: String = encodeEventToJson(event)
                    outgoing.send(Frame.Text(jsonEvent))
                    logger.debug { "Send event $jsonEvent to client $clientEndpoint in $sessionId game" }
                }
            } finally {
                sessionManager.unregisterClient(clientEndpoint)
            }
        }
    }
}

private val sessionManagers: ConcurrentHashMap<Long, SessionManager> = ConcurrentHashMap()

private fun Application.getSessionManager(sessionId: Long): SessionManager =
    sessionManagers.computeIfAbsent(sessionId) { createSessionManager(sessionId) }

private val GameState.asBoard: Board
    get() = Board(
        getGridContent().mapIndexed { i, row ->
            row.mapIndexed { j, cellContent ->
                Cell(i, j, cellContent)
            }
        }.flatten()
    )

// TODO: create normal handler
fun Application.createSessionManager(sessionId: Long) = SessionManager(sessionId) { event ->
    val logger: KLogger = logger()

    event as RequestEvent

    val game = games[Id(event.session.id.toString())] ?: run {
        logger.error { "No game with id ${event.session.id} found!" }
        return@SessionManager
    }

    when (event.type) {
        "OPEN_QUESTION" -> {
            val (row, column) = event.payload as QuestionRequest
            val statement = game.getQuestionStatement(row, column)
            val response = responseEventOf(
                QuestionResponse(Question(row, column, statement), game.asBoard)
            )
            sendToHost(response)
            sendToClients(response)
        }
        "SET_FIELD" -> {
            val (row, column, mark) = event.payload as SetFieldRequest
            val gameResult = game.updateCellContent(row, column, mark)
            logger.info { "Game result: $gameResult" }
            val response = responseEventOf(SetFieldResponse(game.asBoard))
            sendToHost(response)
            sendToClients(response)
        }
    }
}

private fun endpoint(endpoint: RequestConnectionPoint): String = endpoint.remoteAddress + ":" + endpoint.remotePort