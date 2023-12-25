package com.clvr.platform.impl.plugins

import com.clvr.platform.api.exceptions.DuplicateUserException
import com.clvr.platform.api.exceptions.NoSuchUserException
import com.clvr.platform.api.exceptions.ValidationException
import com.clvr.platform.api.model.UserCookie
import com.clvr.platform.api.model.UserInfo
import com.clvr.platform.impl.aufManager
import com.clvr.platform.logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val cookieName: String = ".ig-platform-cookie"

// TODO: add appropriate logging
fun Application.configureAufRouting() {
    routing {
        val logger = application.logger
        val aufManager = application.aufManager

        post("/login") {
            val userLoginData = try {
                call.receive<UserLoginData>()
            } catch (e: ContentTransformationException) {
                logger.error { e }
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userInfoWithCookie = try {
                aufManager.getUserInfoWithCookie(userLoginData.name, userLoginData.password)
            } catch (e: NoSuchUserException) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            } catch (e: ValidationException) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            call.sessions.set(userInfoWithCookie.userCookie)
            call.respond(
                HttpStatusCode.OK,
                userInfoToUserInfoResponse(userInfoWithCookie.userInfo)
            )
        }

        post("/logout") {
            call.sessions.clear(cookieName)
            call.respond(HttpStatusCode.OK)
        }

        // get info about user based on cookie
        get("/user") {
            val userCookie = try {
                call.sessions.get(cookieName) as UserCookie
            } catch (e: Exception) {
                logger.error { "Failed to get cookie $e" }
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val userInfo = try {
                aufManager.getUserInfoByCookie(userCookie)
            } catch (e: ValidationException) {
                logger.error { "{example} incorrect cookie" }
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                userInfoToUserInfoResponse(userInfo)
            )
        }

        post("/register") {
            val userLoginData = try {
                call.receive<UserLoginData>()
            } catch (_: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userInfoWithCookie = try {
                aufManager.addUser(userLoginData.name, userLoginData.password)
            } catch (e: DuplicateUserException) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.sessions.set(userInfoWithCookie.userCookie)
            call.respond(
                HttpStatusCode.OK,
                userInfoToUserInfoResponse(userInfoWithCookie.userInfo)
            )
        }
    }
}

private fun userInfoToUserInfoResponse(userInfo: UserInfo): UserInfoResponse {
    return UserInfoResponse(
        userInfo.name,
        userInfo.uuid.toString()
    )
}



@Serializable
data class UserLoginData(
    val name: String,
    val password: String
)

@Serializable
data class UserInfoResponse(
    val name: String,
    @SerialName("id")
    val uuid: String
)