package com.clvr.platform.impl.plugins

import com.clvr.platform.api.db.DBType
import com.clvr.platform.impl.db.DBQueryExecutor
import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.platform.api.db.UserDatabase
import com.clvr.platform.impl.db.template.PostgresTemplateDatabase
import com.clvr.platform.impl.db.user.PostgresUserDatabase
import io.ktor.server.application.*
import java.sql.*

internal lateinit var templateDatabase: TemplateDatabase
    private set

internal lateinit var userDatabase: UserDatabase
    private set

internal fun Application.configureTemplateDatabase(dbType: DBType = DBType.EMBEDDED) {
    val dbQueryExecutor = when(dbType) {
        DBType.EMBEDDED -> DBQueryExecutor(connectToPostgres(embedded = true))
        DBType.REMOTE -> DBQueryExecutor(connectToPostgres(embedded = false))
    }

    templateDatabase = PostgresTemplateDatabase(dbQueryExecutor)
    userDatabase = PostgresUserDatabase(dbQueryExecutor)
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
