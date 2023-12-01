package com.clvr.server.plugins

import com.clvr.server.TicTacToeSessionStorage
import com.clvr.server.TicTacToeSessionManager
import com.clvr.server.logger
import com.clvr.server.model.GameResult
import com.clvr.server.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KLogger
import java.lang.Exception

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
            val sessionId = SessionId(
                call.parameters["session_id"] ?: throw IllegalArgumentException("failed to get session id")
            )
            val hostEndpoint: String = endpoint(call.request.origin)
            val sessionManager: TicTacToeSessionManager = TicTacToeSessionStorage.getSessionManager(sessionId)
            val hostChannel = sessionManager.hostChannel

            logger.info { "Host $hostEndpoint connected to game $sessionId" }

            getNewHostInitialEvents(sessionId).forEach { initialEvent ->
                outgoing.send(Frame.Text(encodeEventToJson(initialEvent)))
            }

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

                            val event: RequestEvent<TicTacToeRequestPayload> = decodeJsonToEvent(jsonEvent)
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

        webSocket("/ws/client/{session_id}") {
            val sessionId = SessionId(
                call.parameters["session_id"] ?: throw IllegalArgumentException("failed to get session id")
            )
            val clientEndpoint: String = call.request.origin.remoteAddress + ":" + call.request.origin.remotePort
            val sessionManager: TicTacToeSessionManager = TicTacToeSessionStorage.getSessionManager(sessionId)
            val clientChannel = sessionManager.registerClient(clientEndpoint)
            logger.info { "Client $clientEndpoint connected to game $sessionId" }

            getNewClientInitialEvents(sessionId).forEach { initialEvent ->
                outgoing.send(Frame.Text(encodeEventToJson(initialEvent)))
            }

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

private fun endpoint(endpoint: RequestConnectionPoint): String = endpoint.remoteAddress + ":" + endpoint.remotePort

private fun getNewHostInitialEvents(sessionId: SessionId): List<ResponseEvent<TicTacToeResponsePayload>> {
    val gameResult = TicTacToeSessionStorage.getGameResult(sessionId)
    val gameStateView = TicTacToeSessionStorage.getGameStateView(sessionId)
    val boardStateEvent = ResponseEvent(
        SetFieldResponse(gameResult, gameStateView)
    )

    val (lastQuestionView, _) = TicTacToeSessionStorage.getLastQuestionView(sessionId)
        ?: return listOf(boardStateEvent)

    val lastQuestionViewEvent = ResponseEvent(
        HostQuestionResponse(lastQuestionView, gameStateView)
    )

    return listOf(boardStateEvent, lastQuestionViewEvent)
}

private fun getNewClientInitialEvents(sessionId: SessionId): List<ResponseEvent<TicTacToeResponsePayload>> {
    val gameResult = TicTacToeSessionStorage.getGameResult(sessionId)
    val gameStateView = TicTacToeSessionStorage.getGameStateView(sessionId)
    val boardStateEvent = ResponseEvent(
        SetFieldResponse(gameResult, gameStateView)
    )

    val (_, lastQuestionView) = TicTacToeSessionStorage.getLastQuestionView(sessionId)
        ?: return listOf(boardStateEvent)

    val lastQuestionViewEvent = ResponseEvent(
        ClientQuestionResponse(lastQuestionView, gameStateView)
    )

    return listOf(boardStateEvent, lastQuestionViewEvent)
}