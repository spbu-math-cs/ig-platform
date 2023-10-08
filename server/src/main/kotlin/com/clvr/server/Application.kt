package com.clvr.server

import com.clvr.server.plugins.configureDatabases
import com.clvr.server.plugins.configureRouting
import com.clvr.server.plugins.configureSerialization
import com.clvr.server.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
            .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureSockets()
    configureRouting()
}
