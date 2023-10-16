package com.clvr.server.plugins

import com.clvr.server.utils.Event
import com.clvr.server.utils.SessionManager
import com.clvr.server.utils.decodeJsonToEvent
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration

fun Application.configureSockets() {
    val exampleSessionManager = SessionManager(1) { }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
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

        webSocket("/ws/host/") {
            val hostChannel = exampleSessionManager.hostChannel

            launch {
                for (event in hostChannel) {
                    outgoing.send(Frame.Text(Json.encodeToString(event)))
                }
            }

            for (frame in incoming) {
                if (frame !is Frame.Text) {
                    continue
                }

                val event: Event<*> = decodeJsonToEvent(frame.readText())
                exampleSessionManager.handleHostEvent(event)
            }
        }
    }
}
