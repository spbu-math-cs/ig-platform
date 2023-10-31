package com.clvr.server.common

import kotlinx.serialization.Serializable

@Serializable
data class QuizId(val id: String)

@Serializable
data class QuizQuestion(
    val topic: String,
    val statement: String,
    val answer: String,
    val hints: List<String>
)

@Serializable
data class Quiz(
    val id: QuizId,
    val questions: Array<Array<QuizQuestion>>,
    val gridSide: Int,
    val templateTitle: String?,
    val templateAuthor: String?
)
