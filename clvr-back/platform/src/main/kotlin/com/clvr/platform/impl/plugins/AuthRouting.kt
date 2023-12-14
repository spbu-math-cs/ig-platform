package com.clvr.platform.impl.plugins

import com.clvr.platform.api.db.DuplicateUserException
import com.clvr.platform.api.db.NoSuchUserException
import com.clvr.platform.api.db.ValidationException
import com.clvr.platform.logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

// TODO: add appropriate logging
fun Application.configureAuthRouting() {
    routing {
        val logger = application.logger

        post("/login") {
            val userLoginData = try {
                call.receive<UserLoginData>()
            } catch (e: ContentTransformationException) {
                logger.error { e }
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val user = try {
                 userDatabase.getUser(userLoginData.name, userLoginData.password)
            } catch (e: NoSuchUserException) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            } catch (e: ValidationException) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val cookie = UserSession(user.uuid)
            call.sessions.set(cookie)
            call.respond(HttpStatusCode.OK)
        }

        post("/logout") {
            call.sessions.clear(cookieName)
            call.respond(HttpStatusCode.OK)
        }

        post("/register") {
            val userLoginData = try {
                call.receive<UserLoginData>()
            } catch (_: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val user = try {
                userDatabase.addUser(userLoginData.name, userLoginData.password)
            } catch (e: DuplicateUserException) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }
            val cookie = UserSession(user.uuid)
            call.sessions.set(cookie)
            call.respond(HttpStatusCode.OK)
        }

        // example how to work with cookies. In WS everything works absolutely the same
        get("/auth_example") {
            val cookie = try {
                call.sessions.get(cookieName) as UserSession
            } catch (e: Exception) {
                logger.error { "Failed to get cookie $e" }
                return@get
            }

            try {
                val user = userDatabase.getUser(cookie.uuid)
                logger.info { "{user} user: $user" }
            } catch (e: ValidationException) {
                logger.error { "{example} incorrect cookie" }
            }
        }
    }
}

internal const val cookieName: String = ".ig-platform-cookie"

internal data class UserSession(val uuid: UUID)

@Serializable
data class UserLoginData(
    @SerialName("login")
    val name: String,
    val password: String
)