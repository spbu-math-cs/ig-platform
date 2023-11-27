package com.clvr.server.plugins

import com.clvr.server.*
import com.clvr.server.common.*
import com.clvr.server.utils.SessionId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

            val quiz = quizDatabase.singleOrNull { it.id.id == quizRequest.quiz } ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            val newSession = SessionId(UUID.randomUUID().toString().take(6))
            TicTacToeSessionStorage.startNewGame(newSession, quiz, quizRequest.config)
            call.respond(HttpStatusCode.OK, SessionResponse(newSession))
        }

        get("quiz-list") {
            call.respond(HttpStatusCode.OK, QuizListResponse(
                quizDatabase.map { quiz ->
                    QuizHeader(quiz.templateTitle ?: "", quiz.id.id, "")
                }.toList()
            ))
        }

        get("quiz-list/{quiz-id}") {
            val quizId = QuizId(
                call.parameters["quiz-id"] ?: throw IllegalArgumentException("failed to get quiz id")
            )

            val quiz = getQuizById(quizId) ?: run {
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
            val quiz = try {
                call.receive<QuizCreateRequest>()
            } catch (_: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            // TODO: check if row and column are valid
            val quizId = addQuiz(quiz)

            call.respond(HttpStatusCode.OK, QuizIdResponse(quizId))
        }

        delete("api/quiz/{quiz-id}") {
            val quizId = QuizId(
                    call.parameters["quiz-id"] ?: throw IllegalArgumentException("failed to get quiz id")
            )

            val quiz = getQuizById(quizId) ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            removeQuizById(quiz.id)
            call.respond(HttpStatusCode.OK)    }
}}