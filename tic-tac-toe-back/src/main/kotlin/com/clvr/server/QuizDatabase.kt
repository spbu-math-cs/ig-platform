package com.clvr.server

import com.clvr.server.common.Quiz
import com.clvr.server.common.QuizId
import com.clvr.server.common.QuizQuestion
import com.clvr.server.plugins.QuizCreateRequest
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

typealias QuizDatabase = List<Quiz>

private val defaultQuizFiles = listOf("dumbQuizCollection.json", "samples.json")
    .map { fileName -> File(Application::class.java.classLoader.getResource(fileName)!!.toURI()) }

var quizDatabase: QuizDatabase = emptyList()
    private set

fun Application.configureQuizDatabase(files: List<File> = defaultQuizFiles) {
    quizDatabase = files.map { file -> Json.decodeFromString<QuizDatabase>(file.readText()) }.flatten()
}

fun addQuiz(quiz: QuizCreateRequest): QuizId {
    val gridSize = 3 // TODO: make it configurable
    val questions = Array(gridSize) {
        Array(gridSize) {
            QuizQuestion("", "", "", emptyList())
        }
    }
    quiz.board.forEach { cell ->
        questions[cell.row][cell.column] = QuizQuestion(
            topic = cell.topic,
            statement = cell.question,
            answer = cell.answer,
            hints = cell.hints
        )
    }
    val newQuiz = Quiz(
        id = QuizId(UUID.randomUUID().toString()),
        questions = questions,
        gridSide = gridSize,
        templateTitle = quiz.name,
        templateComment = quiz.comment,
        templateAuthor = "Nobody",
    )
    quizDatabase = quizDatabase + newQuiz
    return newQuiz.id
}

fun removeQuizById(quizId: QuizId) {
    quizDatabase = quizDatabase.filter { it.id != quizId }
}

fun getQuizById(quizId: QuizId): Quiz? = quizDatabase.singleOrNull { it.id == quizId }