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

@Serializable
data class QuizHeader(val name: String, val id: String, val comment: String)

@Serializable
data class QuizCellInfo(
        val row: Int,
        val column: Int,
        val topic: String,
        val question: String,
        val hints: List<String>,
        val answer: String
)

@Serializable
data class QuizCompleteInfo(
        val id: String,
        val name: String,
        val comment: String,
        val board: List<QuizCellInfo>
)