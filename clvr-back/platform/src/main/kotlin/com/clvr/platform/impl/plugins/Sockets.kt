package com.clvr.platform.impl.plugins

import com.clvr.platform.impl.ClvrSessionStorage
import com.clvr.platform.api.ClvrGameView
import com.clvr.platform.logger
import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionId
import com.clvr.platform.impl.SessionManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.Exception

private suspend fun <Req: RequestEvent, Resp: ResponseEvent> DefaultWebSocketServerSession.configureHostSession(
    sessionStorage: ClvrSessionStorage<Req, Resp>
) {
    val logger = application.logger

    val sessionId = SessionId(
        call.parameters["session_id"] ?: throw IllegalArgumentException("failed to get session id")
    )
    val hostEndpoint: String = endpoint(call.request.origin)

    val sessionManager: SessionManager<Req, Resp> = sessionStorage.getSessionManager(sessionId)
    val gameView: ClvrGameView<Req, Resp> = sessionStorage.getGameView(sessionId)

    val hostChannel = sessionManager.hostChannel

    logger.info { "Host $hostEndpoint connected to game $sessionId" }

    gameView.hostView.forEach { initialEvent ->
        outgoing.send(Frame.Text(gameView.encodeEventToJson(initialEvent)))
    }

    coroutineScope {
        launch {
            for (event in hostChannel) {
                try {
                    val jsonEvent: String = gameView.encodeEventToJson(event)
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

                    val event: Req = gameView.decodeJsonToEvent(jsonEvent)
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

private suspend fun <Req: RequestEvent, Resp: ResponseEvent> DefaultWebSocketServerSession.configureClientSession(
    sessionStorage: ClvrSessionStorage<Req, Resp>
) {
    val logger = application.logger

    val sessionId = SessionId(
        call.parameters["session_id"] ?: throw IllegalArgumentException("failed to get session id")
    )
    val clientEndpoint: String = call.request.origin.remoteAddress + ":" + call.request.origin.remotePort
    val sessionManager: SessionManager<Req, Resp> = sessionStorage.getSessionManager(sessionId)
    val gameView: ClvrGameView<Req, Resp> = sessionStorage.getGameView(sessionId)
    val clientChannel = sessionManager.registerClient(clientEndpoint)
    logger.info { "Client $clientEndpoint connected to game $sessionId" }

    gameView.clientView.forEach { initialEvent ->
        outgoing.send(Frame.Text(gameView.encodeEventToJson(initialEvent)))
    }

    try {
        coroutineScope {
            launch {
                for (event in clientChannel) {
                    try {
                        val jsonEvent: String = gameView.encodeEventToJson(event)
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

internal fun Application.configureSockets() {
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
    }
}

internal fun Application.addWebsocketRouting(activityName: String, storage: ClvrSessionStorage<*, *>) {

    routing {
        webSocket("/ws/${activityName}/host/{session_id}") {
            configureHostSession(storage)
        }

        webSocket("/ws/${activityName}/client/{session_id}") {
            configureClientSession(storage)
        }
    }
}


private fun endpoint(endpoint: RequestConnectionPoint): String = endpoint.remoteAddress + ":" + endpoint.remotePort