package com.clvr.server

import com.clvr.server.common.Quiz
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import java.io.File

typealias QuizDatabase = List<Quiz>

private val mainQuizFile = File(
    Application::class.java.classLoader.getResource("dumbQuizCollection.json")!!.toURI()
)

var quizDatabase: QuizDatabase = emptyList()
    private set

fun Application.configureQuizDatabase(file: File = mainQuizFile) {
    quizDatabase = Json.decodeFromString<QuizDatabase>(file.readText())
}