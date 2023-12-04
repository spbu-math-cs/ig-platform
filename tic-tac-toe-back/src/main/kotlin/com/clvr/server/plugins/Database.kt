package com.clvr.server.plugins

import com.clvr.server.common.Quiz
import com.clvr.server.db.DBQueryExecutor
import com.clvr.server.db.QuizDatabase
import com.clvr.server.db.impl.ListQuizDatabase
import com.clvr.server.db.impl.PostgresQuizDatabase
import io.ktor.server.application.*
import java.sql.*
import kotlinx.serialization.json.Json
import java.io.File

private val defaultQuizFiles = listOf("dumbQuizCollection.json", "samples.json")
    .map { fileName -> File(Application::class.java.classLoader.getResource(fileName)!!.toURI()) }

var quizDatabase: QuizDatabase = ListQuizDatabase()
    private set

enum class DBType {
    JVM,
    EMBEDDED,
    REMOTE
}

fun Application.configureQuizDatabase(files: List<File> = defaultQuizFiles, dbType: DBType = DBType.EMBEDDED) {
    quizDatabase = when (dbType) {
        DBType.JVM -> ListQuizDatabase()
        DBType.EMBEDDED -> {
            PostgresQuizDatabase(DBQueryExecutor(connectToPostgres(embedded = true)))
        }
        DBType.REMOTE -> {
            PostgresQuizDatabase(DBQueryExecutor(connectToPostgres(embedded = false)))
        }
    }

    files
        .map { file -> Json.decodeFromString<List<Quiz>>(file.readText()) }
        .flatten()
        .forEach { quiz -> quizDatabase.addQuiz(quiz) }
}

private fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    return if (embedded) {
        DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;", "root", "")
    } else {
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()
        DriverManager.getConnection(url, user, password)
    }
}
