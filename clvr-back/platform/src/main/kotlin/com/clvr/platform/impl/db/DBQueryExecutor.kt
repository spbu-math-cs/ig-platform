package com.clvr.platform.impl.db

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

internal class DBQueryExecutor(private val connection: Connection): AutoCloseable {
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
    ): T? {
        val result = query(query, prepareStatement, rowMapper)
        if (result.isEmpty())
            return null
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

    override fun close() {
        connection.close()
    }
}