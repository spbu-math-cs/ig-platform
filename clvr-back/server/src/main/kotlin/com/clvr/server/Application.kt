package com.clvr.server

import com.clvr.nk.NeKahootInstaller
import com.clvr.platform.api.ActivityInstaller
import com.clvr.platform.configurePlatform
import com.clvr.platform.installActivity
import com.clvr.ttt.TicTacToeInstaller
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import java.io.File

private val mainLogger = KotlinLogging.logger { }

private val defaultTicTacToeTemplateFiles = listOf("dumbTemplateCollection.json", "samples.json")
    .map { fileName -> File(TicTacToeInstaller::class.java.classLoader.getResource(fileName)!!.toURI()) }

private val defaultNeKahootTemplateFiles = listOf("demoNeKahoot.json")
    .map { fileName -> File(NeKahootInstaller::class.java.classLoader.getResource(fileName)!!.toURI()) }

private val activityInstallers: List<ActivityInstaller<*, *>> = listOf(
    TicTacToeInstaller(defaultTicTacToeTemplateFiles),
    NeKahootInstaller(defaultNeKahootTemplateFiles),
)

fun main() {
    mainLogger.info { "Launching the server" }

    // setup server environment
    val serverEnvironment = applicationEngineEnvironment {
        connector {
            port = 8080
        }
        module(Application::module)
    }

    embeddedServer(Netty, environment = serverEnvironment)
        .start(wait = true)
}

fun Application.module() {
    configurePlatform()
    activityInstallers.forEach {
        installActivity(it)
    }
}