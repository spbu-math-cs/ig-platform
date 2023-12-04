package com.clvr.server.db.impl

import com.clvr.server.common.Quiz
import com.clvr.server.common.QuizHeader
import com.clvr.server.common.QuizId
import com.clvr.server.db.DBQueryExecutor
import com.clvr.server.db.QuizDatabase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO: replace TEXT type with JSON. Current problem: result from Postgres fails to be converted by kotlinx serialization.
class PostgresQuizDatabase(private val db: DBQueryExecutor) : QuizDatabase {
    companion object {
        private val createTableQuery: String = """
            CREATE TABLE IF NOT EXISTS quiz (
                uuid TEXT PRIMARY KEY,
                quiz TEXT NOT NULL
            );
        """.trimIndent()

        private const val addQuizQuery: String = "INSERT INTO quiz(uuid, quiz) VALUES (?, ?) ON CONFLICT DO NOTHING;"

        private const val deleteQuizQuery: String = "DELETE FROM quiz WHERE uuid = ?;"

        private const val getQuizByUUIDQuery: String = "SELECT quiz FROM quiz WHERE uuid = ?;"

        private const val listQuizzesQuery: String = "SELECT quiz FROM quiz;"
    }

    init {
        createQuizTable()
    }

    override fun addQuiz(quiz: Quiz) {
        db.update(addQuizQuery) {
            setString(1, quiz.id.id)
            setString(2, Json.encodeToString(quiz))
        }
    }

    override fun removeQuizById(quizId: QuizId) {
        db.update(deleteQuizQuery) {
            setString(1, quizId.id)
        }
    }

    override fun getQuizById(quizId: QuizId): Quiz? {
        return db.queryObject(getQuizByUUIDQuery, { setString(1, quizId.id) }) {
            Json.decodeFromString<Quiz>(getString(1))
        }
    }

    override fun listQuizzes(): List<QuizHeader> {
        return db.query(listQuizzesQuery, { }) {
            Json.decodeFromString<Quiz>(getString(1))
        }.map { quiz -> QuizHeader(quiz.templateTitle ?: "", quiz.id.id,  quiz.templateComment ?: "") }
    }

    override fun close() {
        db.close()
    }

    private fun createQuizTable() {
        db.update(createTableQuery) { }
    }
}