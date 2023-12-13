package com.clvr.platform

import com.clvr.platform.api.ActivityInstaller
import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.db.DBType
import com.clvr.platform.impl.InMemorySessionStorage
import com.clvr.platform.impl.plugins.*
import com.clvr.platform.impl.plugins.MonitoringPlugin
import com.clvr.platform.impl.plugins.addWebsocketRouting
import com.clvr.platform.impl.plugins.configureCallLogging
import com.clvr.platform.impl.plugins.configureSerialization
import com.clvr.platform.impl.plugins.configureSockets
import com.clvr.platform.impl.plugins.configureTemplateDatabase
import com.clvr.platform.impl.plugins.templateDatabase
import com.clvr.platform.impl.plugins.userDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import mu.KLogger
import mu.KotlinLogging

// TODO: this shouldn't be in api
private val mainLogger = KotlinLogging.logger { }

val Application.logger: KLogger
    get() = mainLogger

fun Application.configurePlatform(dbType: DBType = DBType.EMBEDDED) {
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
        cookie<UserSession>(cookieName) {
            cookie.path = "/"
        }
    }
    install(MonitoringPlugin)

    configureCallLogging()
    configureSerialization()
    configureTemplateDatabase(dbType)
    configureSockets()
    configureAuthRouting()
}

fun <Req: RequestEvent, Resp: ResponseEvent> Application.installActivity(
    installer: ActivityInstaller<Req, Resp>
) {
    val sessionStorage = InMemorySessionStorage<Req, Resp>()
    addWebsocketRouting(installer.activityName, sessionStorage)
    routing {
        route("/${installer.activityName}") {
            installer.install(this, templateDatabase, userDatabase, sessionStorage)
        }
    }
}