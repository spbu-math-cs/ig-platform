package com.clvr.server.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("replace_marks")
    val replaceMarks: Boolean = false,

    @SerialName("open_multiple_questions")
    val openMultipleQuestions: Boolean = false
)