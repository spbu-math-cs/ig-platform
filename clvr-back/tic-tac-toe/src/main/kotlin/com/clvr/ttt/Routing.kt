package com.clvr.ttt

import com.clvr.platform.api.ClvrSessionRegistry
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.db.addTemplate
import com.clvr.platform.api.db.getTemplatesById

import com.clvr.platform.api.SessionId
import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.ttt.common.Config
import com.clvr.ttt.common.TemplateCellInfo
import com.clvr.ttt.common.TemplateCompleteInfo
import com.clvr.ttt.common.TemplateQuestion
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
data class TemplateRequest(
    @SerialName("template_id")
    val template: String,

    @SerialName("game_configuration")
    val config: Config
)

@Serializable
data class TemplateListResponse (
    @SerialName("template-list")
    val templateList: List<TemplateHeader>
)

@Serializable
data class TemplateCreateRequest(
    val name: String,
    val comment: String,
    val board: List<TemplateCellInfo>
)

@Serializable
data class TemplateIdResponse(
    @SerialName("template-id")
    val templateId: TemplateId
)

fun Route.routingSetup(
    templateDatabase: TemplateDatabase,
    sessionRegistry: ClvrSessionRegistry<TicTacToeRequestPayload, TicTacToeResponsePayload>
) {
    options("game") {
        call.respond(HttpStatusCode.OK)
    }
    post("game") {
        val templateRequest = try {
            call.receive<TemplateRequest>()
        } catch (_: ContentTransformationException) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val template = templateDatabase.getTemplatesById<TicTacToeTemplate>(
            TicTacToeInstaller.templateId(templateRequest.template)
        ) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@post
        }

        val game = GameState(template, templateRequest.config)
        val controller = TicTacToeGameController(game)
        val view = TicTacToeGameView(game)
        val newSession = sessionRegistry.startNewGame(controller, view)
        call.respond(HttpStatusCode.OK, SessionResponse(newSession))
    }

    get("template-list") {
        call.respond(
            HttpStatusCode.OK,
            TemplateListResponse(
                templateDatabase.listTemplates(TicTacToeInstaller.ACTIVITY_ID)
            )
        )
    }

    get("template/{template-id}") {
        val templateId = TicTacToeInstaller.templateId(
            call.parameters["template-id"] ?: throw IllegalArgumentException("failed to get template id")
        )

        val template = templateDatabase.getTemplatesById<TicTacToeTemplate>(templateId) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        call.respond(HttpStatusCode.OK, TemplateCompleteInfo(
            template.id.id,
            template.templateTitle ?: "",
            template.templateComment ?:"",
            template.questions.flatMapIndexed { row, data ->
                data.mapIndexed { column, question -> TemplateCellInfo(
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

    post("template") {
        val templateRequest = try {
            call.receive<TemplateCreateRequest>()
        } catch (_: ContentTransformationException) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val template = try {
            templateCreateRequestToTemplate(templateRequest)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "unknown error")
            return@post
        }
        templateDatabase.addTemplate<TicTacToeTemplate>(template)
        call.respond(HttpStatusCode.OK, TemplateIdResponse(template.id))
    }

    delete("template/{template-id}") {
        val templateId = TicTacToeInstaller.templateId(
            call.parameters["template-id"] ?: throw IllegalArgumentException("failed to get template id")
        )

        val template = templateDatabase.getTemplatesById<TicTacToeTemplate>(templateId) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@delete
        }

        templateDatabase.removeTemplateById(template.id)
        call.respond(HttpStatusCode.OK)
    }
}

private fun validateTemplateRequest(templateRequest: TemplateCreateRequest) {
    if (templateRequest.board.size != 9) {
        throw IllegalArgumentException("board size is not 9")
    }
    templateRequest.board.forEach { cell ->
        if (cell.row !in 0..2 || cell.column !in 0..2) {
            throw IllegalArgumentException("invalid row or column")
        }
    }
    templateRequest.board.forEach { cell ->
        if (cell.topic.isBlank()) {
            throw IllegalArgumentException("topic is blank")
        }
        if (cell.question.isBlank()) {
            throw IllegalArgumentException("question is blank")
        }
        if (cell.answer.isBlank()) {
            throw IllegalArgumentException("answer is blank")
        }
    }
}

private fun templateCreateRequestToTemplate(templateRequest: TemplateCreateRequest): TicTacToeTemplate {
    validateTemplateRequest(templateRequest)
    val gridSize = 3
    val questions = Array(gridSize) {
        Array(gridSize) {
            TemplateQuestion("", "", "", emptyList())
        }
    }
    templateRequest.board.forEach { cell ->
        questions[cell.row][cell.column] = TemplateQuestion(
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
        templateTitle = templateRequest.name,
        templateComment = templateRequest.comment,
        templateAuthor = "Nobody",
    )
}