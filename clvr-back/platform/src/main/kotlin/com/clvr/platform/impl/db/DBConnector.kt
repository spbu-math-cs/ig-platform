package com.clvr.platform.impl.db

import java.sql.Connection
import java.sql.DriverManager

internal object DBConnector {
    fun connectToDB(url: String, user: String, password: String): Connection =
        DriverManager.getConnection(url, user, password)

    fun connectToEmbeddedDB(): Connection =
        DriverManager.getConnection("jdbc:h2:mem:testdb;MODE=PostgreSQL;", "root", "")
}