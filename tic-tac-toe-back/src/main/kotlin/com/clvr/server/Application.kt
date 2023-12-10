package com.clvr.server

import com.clvr.server.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.sessions.*
import mu.KLogger
import mu.KotlinLogging

private val mainLogger = KotlinLogging.logger { }

fun main() {
    mainLogger.info { "Launching the server" }
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module)
            .start(wait = true)
}

fun Application.logger(): KLogger = mainLogger

fun Application.module() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Cookie)
        allowCredentials = true
        anyHost()
    }
    install(Sessions) {
        cookie<UserSession>(".test") {
            cookie.path = "/"
//            cookie.domain = ".test"
//            cookie.secure = true
        }
    }
    install(MonitoringPlugin)

    configureCallLogging()
    configureSerialization()
    configureQuizDatabase()
    configureSockets()
    configureRouting()
}