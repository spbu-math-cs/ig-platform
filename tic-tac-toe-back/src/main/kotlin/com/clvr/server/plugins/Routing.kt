package com.clvr.server.plugins

import com.clvr.server.TicTacToeSessionStorage
import com.clvr.server.common.*
import com.clvr.server.utils.SessionId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import java.util.*

@Serializable
data class SessionResponse(val session: SessionId)

@Serializable
data class QuizRequest(
    @SerialName("quiz_id")
    val quiz: String,

    @SerialName("game_configuration")
    val config: Config
)

@Serializable
data class QuizListResponse (
    @SerialName("quiz-list")
    val quizList: List<QuizHeader>
)

@Serializable
data class QuizCreateRequest(
    val name: String,
    val comment: String,
    val board: List<QuizCellInfo>
)

@Serializable
data class QuizIdResponse(
    @SerialName("quiz-id")
    val quizId: QuizId
)

data class UserSession(val id: String)

private val routingLogger = KotlinLogging.logger { }

fun Application.configureRouting() {
    routing {
        options("/api/game-session") {
            call.respond(HttpStatusCode.OK)
        }

        get("/login") {
            val cookie = UserSession(id = "cookies oh my cookies " + UUID.randomUUID().toString())
            call.sessions.set(cookie)
            call.respondText { "Test cookies (login)" }

            routingLogger.info { "Login clicked, cookie: $cookie" }
        }

        post("/logout") {
            val cookie = call.sessions.get(".test")
            call.sessions.clear(".test")
            call.respondText { "Test cookies (logout)" }

            routingLogger.info { "Logout clicked, cookie: $cookie" }
        }

        post("/api/game-session") {
            logCookie(call, "/api/game-session")
            val quizRequest = try {
                call.receive<QuizRequest>()
            } catch (_: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val cookiesSession = call.sessions.get<UserSession>()
            println("Cookie in routing $cookiesSession")

            val quiz = quizDatabase.getQuizById(QuizId(quizRequest.quiz)) ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            val newSession = SessionId(UUID.randomUUID().toString().take(6))
            TicTacToeSessionStorage.startNewGame(newSession, quiz, quizRequest.config)
            call.respond(HttpStatusCode.OK, SessionResponse(newSession))
        }

        get("quiz-list") {
            logCookie(call, "quiz-list")
            call.respond(HttpStatusCode.OK, QuizListResponse(quizDatabase.listQuizzes()))

            val cookiesSession = call.sessions.get(".test")
            println("Cookie in routing $cookiesSession")
        }

        get("quiz-list/{quiz-id}") {
            logCookie(call, "quiz-list/${call.parameters["quiz-id"]}")
            val quizId = QuizId(
                call.parameters["quiz-id"] ?: throw IllegalArgumentException("failed to get quiz id")
            )

            val quiz = quizDatabase.getQuizById(quizId) ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(HttpStatusCode.OK, QuizCompleteInfo(
                quiz.id.id,
                quiz.templateTitle ?: "",
                quiz.templateComment ?:"",
                quiz.questions.flatMapIndexed { row, data ->
                    data.mapIndexed { column, question -> QuizCellInfo(
                        row,
                        column,
                        question.topic,
                        question.statement,
                        question.hints,
                        question.answer
                    )}
                }
            ))
        }

        post("api/quiz") {
            val quizRequest = try {
                call.receive<QuizCreateRequest>()
            } catch (_: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val quiz = quizCreateRequestToQuiz(quizRequest)
            quizDatabase.addQuiz(quiz)
            call.respond(HttpStatusCode.OK, QuizIdResponse(quiz.id))
        }

        delete("api/quiz/{quiz-id}") {
            val quizId = QuizId(
                    call.parameters["quiz-id"] ?: throw IllegalArgumentException("failed to get quiz id")
            )

            val quiz = quizDatabase.getQuizById(quizId) ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            quizDatabase.removeQuizById(quiz.id)
            call.respond(HttpStatusCode.OK)
        }
}}

private fun logCookie(call: ApplicationCall, method: String) {
    val cookie = call.sessions.get(".test")

    if (cookie == null)
        routingLogger.info { "Method $method: cookie is not set" }
    else
        routingLogger.info { "Method $method, cookie: $cookie" }
}

// TODO: check if row and column are valid
private fun quizCreateRequestToQuiz(quizRequest: QuizCreateRequest): Quiz {
    val gridSize = 3 // TODO: make it configurable
    val questions = Array(gridSize) {
        Array(gridSize) {
            QuizQuestion("", "", "", emptyList())
        }
    }
    quizRequest.board.forEach { cell ->
        questions[cell.row][cell.column] = QuizQuestion(
            topic = cell.topic,
            statement = cell.question,
            answer = cell.answer,
            hints = cell.hints
        )
    }
    return Quiz(
        id = QuizId(UUID.randomUUID().toString()),
        questions = questions,
        gridSide = gridSize,
        templateTitle = quizRequest.name,
        templateComment = quizRequest.comment,
        templateAuthor = "Nobody",
    )
}