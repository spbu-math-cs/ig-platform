package com.clvr.server

import com.clvr.server.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import mu.KLogger
import mu.KotlinLogging

private val mainLogger = KotlinLogging.logger { }

fun main() {
    mainLogger.info { "Launching the server" }
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
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
        anyHost()
    }
    configureCallLogging()
    configureSerialization()
    configureDatabases()
    configureSockets()
    configureRouting()
}
