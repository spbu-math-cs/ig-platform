package com.clvr.server.db.impl

import com.clvr.server.common.Quiz
import com.clvr.server.common.QuizHeader
import com.clvr.server.common.QuizId
import com.clvr.server.db.QuizDatabase

class ListQuizDatabase: QuizDatabase {
    private val quizzes = mutableListOf<Quiz>()
    
    override fun addQuiz(quiz: Quiz) {
        quizzes += quiz
    }

    override fun removeQuizById(quizId: QuizId) {
        quizzes.removeIf { quiz -> quiz.id == quizId }
    }

    override fun getQuizById(quizId: QuizId): Quiz? {
        val res = quizzes.singleOrNull { quiz -> quiz.id == quizId }
        return res
    }

    override fun listQuizzes(): List<QuizHeader> {
        return quizzes.map { quiz ->
            QuizHeader(quiz.templateTitle ?: "", quiz.id.id, "")
        }.toList()
    }
}