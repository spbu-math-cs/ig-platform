package com.clvr.nk

import com.clvr.nk.common.NeKahootTemplate
import com.clvr.nk.common.TemplateCompleteInfo
import com.clvr.nk.common.TemplateQuestion
import com.clvr.platform.api.ClvrSessionRegistry
import com.clvr.platform.api.SessionId
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.platform.api.db.addTemplate
import com.clvr.platform.api.db.getTemplatesById
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SessionIdResponse(
    val session: SessionId,
)

@Serializable
data class TemplateIdResponse(
    @SerialName("template-id")
    val templateId: TemplateId,
)

@Serializable
data class TemplateListResponse(
    @SerialName("template-list")
    val templateList: List<TemplateHeader>,
)

@Serializable
data class TemplateRequest(
    @SerialName("template_id")
    val template: String,
)

@Serializable
data class TemplateCreateRequest(
    val name: String,
    val comment: String,
    val questions: List<TemplateQuestion>,
)

fun Route.routingSetup(
    templateDatabase: TemplateDatabase,
    sessionRegistry: ClvrSessionRegistry<NeKahootRequest<*>, NeKahootResponse<*>>
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

        val template = templateDatabase.getTemplatesById<NeKahootTemplate>(
            NeKahootInstaller.templateId(templateRequest.template)
        ) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@post
        }

        val game = GameState(template)
        val controller = NeKahootGameController(game)
        val view = NeKahootGameView(game)
        val newSession = sessionRegistry.startNewGame(controller, view)
        call.respond(HttpStatusCode.OK, SessionIdResponse(newSession))
    }

    get("template-list") {
        call.respond(
            HttpStatusCode.OK,
            TemplateListResponse(
                templateDatabase.listTemplates(NeKahootInstaller.ACTIVITY_ID)
            )
        )
    }

    get("template/{template-id}") {
        val templateId = NeKahootInstaller.templateId(
            call.parameters["template-id"] ?: throw IllegalArgumentException("failed to get template-id")
        )

        val template = templateDatabase.getTemplatesById<NeKahootTemplate>(templateId) ?: run {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        call.respond(HttpStatusCode.OK, TemplateCompleteInfo(
            template.id.id,
            template.templateTitle ?: "",
            template.templateComment ?: "",
            template.questions,
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
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        templateDatabase.addTemplate<NeKahootTemplate>(template)
        call.respond(HttpStatusCode.OK, TemplateIdResponse(template.id))
    }
}

private fun validateTemplateRequest(templateRequest: TemplateCreateRequest) {
    templateRequest.questions.forEach { q ->
        if (q.time < 5) {
            throw IllegalArgumentException("time is less than 5 seconds")
        }
        if (q.question.isBlank()) {
            throw IllegalArgumentException("question is blank")
        }
        if (q.answer_options.size !in 2..6) {
            throw IllegalArgumentException("less than 2 or more than 6 answer options")
        }
        if (q.answer_options.any { it.isBlank() }) {
            throw IllegalArgumentException("answer option is blank")
        }
        if (q.answer !in q.answer_options) {
            throw IllegalArgumentException("correct answer is not in answer options")
        }
    }
}

private fun templateCreateRequestToTemplate(templateRequest: TemplateCreateRequest): NeKahootTemplate {
    validateTemplateRequest(templateRequest)
    return NeKahootTemplate(
        id = NeKahootInstaller.templateId(UUID.randomUUID().toString()),
        questions = templateRequest.questions,
        templateTitle = templateRequest.name,
        templateComment = templateRequest.comment,
        templateAuthor = "Nobody",
    )
}