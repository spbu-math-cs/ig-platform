package com.clvr.platform.impl.plugins

import com.clvr.platform.api.db.DBType
import com.clvr.platform.impl.db.DBQueryExecutor
import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.platform.impl.db.ListTemplateDatabase
import com.clvr.platform.impl.db.PostgresTemplateDatabase
import io.ktor.server.application.*
import java.sql.*


internal var templateDatabase: TemplateDatabase = ListTemplateDatabase()
    private set

internal fun Application.configureTemplateDatabase(dbType: DBType = DBType.EMBEDDED) {
    templateDatabase = when (dbType) {
        DBType.JVM -> ListTemplateDatabase()
        DBType.EMBEDDED -> {
            PostgresTemplateDatabase(DBQueryExecutor(connectToPostgres(embedded = true)))
        }
        DBType.REMOTE -> {
            PostgresTemplateDatabase(DBQueryExecutor(connectToPostgres(embedded = false)))
        }
    }
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
