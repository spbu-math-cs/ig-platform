package com.clvr.nk.common

import com.clvr.platform.api.Template
import com.clvr.platform.api.TemplateHeader
import com.clvr.platform.api.TemplateId
import kotlinx.serialization.Serializable

@Serializable
data class TemplateQuestion(
    val question: String,
    val answer: String,
    val answer_description: String?,
    val answer_options: List<String>,
    val time: Int,
)

@Serializable
data class NeKahootTemplate(
    override val id: TemplateId,
    val questions: List<TemplateQuestion>,
    val templateTitle: String?,
    val templateComment: String?,
    val templateAuthor: String?,
) : Template {
    override val header: TemplateHeader
        get() = TemplateHeader(templateTitle ?: "", id.id, "")
}

@Serializable
data class TemplateCompleteInfo(
    val id: String,
    val name: String,
    val comment: String,
    val questions: List<TemplateQuestion>,
)