package com.clvr.server.db

import com.clvr.server.common.Quiz
import com.clvr.server.common.QuizHeader
import com.clvr.server.common.QuizId

interface QuizDatabase: AutoCloseable {
    fun addQuiz(quiz: Quiz)

    fun removeQuizById(quizId: QuizId)

    fun getQuizById(quizId: QuizId): Quiz?

    fun listQuizzes(): List<QuizHeader>

    override fun close() { }
}