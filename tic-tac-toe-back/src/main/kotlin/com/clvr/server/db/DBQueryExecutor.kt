package com.clvr.server.db

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class DBQueryExecutor(private val connection: Connection) {
    fun <T> query(
        query: String,
        prepareStatement: PreparedStatement.() -> Unit,
        rowMapper: ResultSet.() -> T
    ): List<T> {
        val statement = connection.prepareStatement(query)
        prepareStatement(statement)
        statement.executeQuery().use { resultSet ->
            val result = ArrayList<T>()
            while (resultSet.next()) {
                result.add(rowMapper(resultSet))
            }
            return result
        }
    }

    fun <T> queryObject(
        query: String,
        prepareStatement: PreparedStatement.() -> Unit,
        rowMapper: ResultSet.() -> T
    ): T {
        val result = query(query, prepareStatement, rowMapper)
        if (result.isEmpty())
            throw NoSuchElementException("No suitable object was found")
        return result[0]
    }

    fun update(
        query: String,
        prepareStatement: PreparedStatement.() -> Unit
    ): Int {
        val statement = connection.prepareStatement(query)
        prepareStatement(statement)
        return statement.executeUpdate()
    }
}