package com.clvr.ttt

import com.clvr.platform.api.ClvrSessionRegistry
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.db.addTemplate
import com.clvr.platform.api.db.getTemplatesById

import com.clvr.platform.api.SessionId
import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.ttt.common.Config
import com.clvr.ttt.common.QuizCellInfo
import com.clvr.ttt.common.QuizCompleteInfo
import com.clvr.ttt.common.QuizQuestion
import com.clvr.ttt.common.TicTacToeTemplate
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
    val quizList: List<TemplateHeader>
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
    val templateId: TemplateId
)

fun Route.routingSetup(
    templateDatabase: TemplateDatabase,
    sessionRegistry: ClvrSessionRegistry<TicTacToeRequestPayload, TicTacToeResponsePayload>
) {
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

        val template = templateDatabase.getTemplatesById<TicTacToeTemplate>(
            TicTacToeInstaller.templateId(quizRequest.quiz)
        ) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@post
        }

        val game = GameState(template, quizRequest.config)
        val controller = TicTacToeGameController(game)
        val view = TicTacToeGameView(game)
        val newSession = sessionRegistry.startNewGame(controller, view)
        call.respond(HttpStatusCode.OK, SessionResponse(newSession))
    }

    get("quiz-list") {
        call.respond(
            HttpStatusCode.OK,
            QuizListResponse(
                templateDatabase.listTemplates(TicTacToeInstaller.ACTIVITY_ID)
            )
        )
    }

    get("quiz-list/{quiz-id}") {
        val quizId = TicTacToeInstaller.templateId(
            call.parameters["quiz-id"] ?: throw IllegalArgumentException("failed to get quiz id")
        )

        val template = templateDatabase.getTemplatesById<TicTacToeTemplate>(quizId) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        call.respond(HttpStatusCode.OK, QuizCompleteInfo(
            template.id.id,
            template.templateTitle ?: "",
            template.templateComment ?:"",
            template.questions.flatMapIndexed { row, data ->
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
        templateDatabase.addTemplate<TicTacToeTemplate>(quiz)
        call.respond(HttpStatusCode.OK, QuizIdResponse(quiz.id))
    }

    delete("api/quiz/{quiz-id}") {
        val quizId = TicTacToeInstaller.templateId(
            call.parameters["quiz-id"] ?: throw IllegalArgumentException("failed to get quiz id")
        )

        val template = templateDatabase.getTemplatesById<TicTacToeTemplate>(quizId) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@delete
        }

        templateDatabase.removeTemplateById(template.id)
        call.respond(HttpStatusCode.OK)
    }
}

// TODO: check if row and column are valid
private fun quizCreateRequestToQuiz(quizRequest: QuizCreateRequest): TicTacToeTemplate {
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
    return TicTacToeTemplate(
        id = TicTacToeInstaller.templateId(UUID.randomUUID().toString()),
        questions = questions,
        gridSide = gridSize,
        templateTitle = quizRequest.name,
        templateComment = quizRequest.comment,
        templateAuthor = "Nobody",
    )
}