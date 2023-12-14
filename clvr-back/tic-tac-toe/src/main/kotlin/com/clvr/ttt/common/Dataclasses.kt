package com.clvr.ttt.common

import com.clvr.platform.api.Template
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import kotlinx.serialization.Serializable

@Serializable
data class TemplateQuestion(
    val topic: String,
    val statement: String,
    val answer: String,
    val hints: List<String>
)

@Serializable
data class TicTacToeTemplate(
    override val id: TemplateId,
    val questions: Array<Array<TemplateQuestion>>,
    val gridSide: Int,
    val templateTitle: String?,
    val templateComment: String?,
    val templateAuthor: String?
) : Template {
    override val header: TemplateHeader
        get() = TemplateHeader(templateTitle ?: "", id.id, "")
}


@Serializable
data class TemplateCellInfo(
        val row: Int,
        val column: Int,
        val topic: String,
        val question: String,
        val hints: List<String>,
        val answer: String
)

@Serializable
data class TemplateCompleteInfo(
        val id: String,
        val name: String,
        val comment: String,
        val board: List<TemplateCellInfo>
)