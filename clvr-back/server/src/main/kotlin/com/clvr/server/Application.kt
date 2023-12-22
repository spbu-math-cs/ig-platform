package com.clvr.server

import com.clvr.platform.api.ActivityInstaller
import com.clvr.platform.configurePlatform
import com.clvr.platform.installActivity
import com.clvr.ttt.TicTacToeInstaller
import com.clvr.utils.Config
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import java.io.File

private val mainLogger = KotlinLogging.logger { }

private val defaultTicTacToeTemplateFiles = listOf("dumbTemplateCollection.json", "samples.json")
    .map { fileName -> File(TicTacToeInstaller::class.java.classLoader.getResource(fileName)!!.toURI()) }

private val activityInstallers: List<ActivityInstaller<*, *>> = listOf(
    TicTacToeInstaller(defaultTicTacToeTemplateFiles)
)

// At the moment, https does not work because the certificate is self-signed.
// It is being researched if it is possible to ignore this fact on client side.
fun main() {
    mainLogger.info { "Launching the server" }

    // generate ssl certificate
//    val keyStoreFile = File(Config.getString(Config.SSL_PATH_TO_KEY_STORE_ENV))
//    val keyStore = buildKeyStore {
//        certificate(Config.getString(Config.SSL_ALIAS_NAME_ENV)) {
//            password = Config.getString(Config.SSL_PASSWORD_ENV)
//            domains = listOf("127.0.0.1", "0.0.0.0", "localhost")
//        }
//    }
//    keyStore.saveToFile(keyStoreFile, Config.getString(Config.SSL_KEY_STORE_PASSWORD_ENV))

    // setup server environment
    val serverEnvironment = applicationEngineEnvironment {
        connector {
            port = 8080
        }
//        sslConnector(
//            keyStore = keyStore,
//            keyAlias = Config.getString(Config.SSL_ALIAS_NAME_ENV),
//            keyStorePassword = { Config.getString(Config.SSL_KEY_STORE_PASSWORD_ENV).toCharArray() },
//            privateKeyPassword = { Config.getString(Config.SSL_PASSWORD_ENV).toCharArray() }
//        ) {
//            port = 443                   // default https port
//            keyStorePath = keyStoreFile
//        }
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