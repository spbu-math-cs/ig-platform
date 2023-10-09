package com.clvr.server

import com.clvr.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging

private val mainLogger = KotlinLogging.logger { }

fun main() {
    mainLogger.info { "Launching the server" }
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
            .start(wait = true)
}

fun Application.module() {
    configureCallLogging()
    configureSerialization()
    configureDatabases()
    configureSockets()
    configureRouting()
}
