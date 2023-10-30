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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KLogger
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap

fun Application.configureSockets() {
    val logger: KLogger = logger()

    install(WebSockets) {
//        pingPeriod = Duration.ofSeconds(15)
//        timeout = Duration.ofSeconds(15)
//        maxFrameSize = Long.MAX_VALUE
//        masking = false
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

            coroutineScope {
                launch {
                    for (event in hostChannel) {
                        try {
                            val jsonEvent: String = encodeEventToJson(event)
                            outgoing.send(Frame.Text(jsonEvent))
                            logger.debug { "Send event $jsonEvent to host $hostEndpoint in $sessionId game" }
                        } catch (e: Exception) {
                            logger.error { "Failed to send event to host $hostEndpoint because of $e" }
                            hostChannel.send(event)
                        }
                    }
                }

                launch {
                    for (frame in incoming) {
                        try {
                            if (frame !is Frame.Text) {
                                continue
                            }

                            val jsonEvent: String = frame.readText()
                            logger.debug { "Receive event $jsonEvent from host $hostEndpoint in $sessionId game" }

                            val event: Event<*> = decodeJsonToEvent(jsonEvent)
                            sessionManager.handleHostEvent(event)
                        } catch (e: Exception) {
                            logger.error { "Failed to process event incoming frame because of error $e" }
                        }
                    }
                }

                val closeReason = closeReason.await()
                logger.info { "Connection with host $hostEndpoint was closed because of $closeReason" }
                cancel("Connection with host was closed because of $closeReason")
            }
        }

        webSocket("/ws/board/{session_id}") {
            val sessionId: Long =
                call.parameters["session_id"]?.toLong() ?: throw IllegalArgumentException("failed to get session id")
            val clientEndpoint: String = call.request.origin.remoteAddress + ":" + call.request.origin.remotePort
            val sessionManager: SessionManager = this@configureSockets.getSessionManager(sessionId)
            val clientChannel: Channel<Event<*>> = sessionManager.registerClient(clientEndpoint)
            logger.info { "Client $clientEndpoint connected to game $sessionId" }

            try {
                coroutineScope {
                    launch {
                        for (event in clientChannel) {
                            try {
                                val jsonEvent: String = encodeEventToJson(event)
                                outgoing.send(Frame.Text(jsonEvent))
                                logger.debug { "Send event $jsonEvent to client $clientEndpoint in $sessionId game" }
                            } catch (e: Exception) {
                                logger.error { "Failed to send event to client $clientEndpoint because of $e" }
                            }
                        }
                    }

                    val closeReason = closeReason.await()
                    logger.info { "Connection with client $clientEndpoint was closed because of $closeReason" }
                    cancel("Connection with client was closed because of $closeReason")
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