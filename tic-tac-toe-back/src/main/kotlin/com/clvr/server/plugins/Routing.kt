package com.clvr.server.plugins

import com.clvr.server.GameState
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

private val quizFile = Application::class.java.classLoader.getResource("dumbQuizCollection.json")!!
private val quizDatabase = Json.decodeFromString<QuizDatabase>(quizFile.readText())
internal val games: MutableMap<Id, GameState> = ConcurrentHashMap()

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
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

            val newId = Id(/*UUID.randomUUID().hashCode()*/239.toString())
            games[newId] = GameState(quiz)
            call.respond(HttpStatusCode.OK, GameSession(newId))
        }
    }
}
