package com.clvr.ttt.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReplaceMarks {
    DISABLED, ENABLED
}

@Serializable
enum class OpenMultipleQuestions {
    DISABLED, ENABLED
}

@Serializable
data class Config(
    @SerialName("replace_marks")
    val replaceMarks: ReplaceMarks = ReplaceMarks.DISABLED,

    @SerialName("open_multiple_questions")
    val openMultipleQuestions: OpenMultipleQuestions = OpenMultipleQuestions.DISABLED
)