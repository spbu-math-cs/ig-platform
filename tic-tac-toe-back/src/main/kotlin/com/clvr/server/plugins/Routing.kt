package com.clvr.server.plugins

import com.clvr.server.SessionStorage
import com.clvr.server.common.Quiz
import com.clvr.server.common.QuizId
import com.clvr.server.utils.SessionId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SessionResponse(val session: SessionId)

@Serializable
data class QuizRequest(val quiz: QuizId)

typealias QuizDatabase = List<Quiz>

private val quizFile = Application::class.java.classLoader.getResource("dumbQuizCollection.json")!!
private val quizDatabase = Json.decodeFromString<QuizDatabase>(quizFile.readText())

fun Application.configureRouting() {
    routing {
        options("/api/game-session") {
            call.respond(HttpStatusCode.OK)
        }
        post("/api/game-session") {
            val quizRequest = try {
                call.receive<QuizRequest>()
            } catch (_: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val quiz = quizDatabase.singleOrNull { it.id == quizRequest.quiz } ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            val newSession = SessionId(/*UUID.randomUUID()*/239.toString())
            SessionStorage.startNewGame(newSession, quiz)
            call.respond(HttpStatusCode.OK, SessionResponse(newSession))
        }
    }
}
