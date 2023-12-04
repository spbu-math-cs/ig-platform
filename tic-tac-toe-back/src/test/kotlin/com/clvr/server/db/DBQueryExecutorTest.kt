package com.clvr.server.db

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.util.UUID

class DBQueryExecutorTest {
    private lateinit var dbConnection: Connection
    private lateinit var db: DBQueryExecutor

    companion object {
        private val CREATE_SIMPLE_TABLE_QUERY: String =
            """CREATE TABLE IF NOT EXISTS simple_table(
                    a INTEGER,
                    b TEXT, 
                    c UUID, 
                    
                    UNIQUE (a)
               )
            """.trimIndent()

        private val INSERT_INTO_SIMPLE_TABLE_QUERY: String =
            "INSERT INTO simple_table(a, b, c) VALUES(?, ?, ?) ON CONFLICT DO NOTHING;"

        private val SELECT_ALL_ROWS_FROM_SIMPLE_TABLE_QUERY: String =
            "SELECT * FROM simple_table ORDER BY a;"

        private val SELECT_ROW_FROM_SIMPLE_TABLE: String =
            "SELECT * FROM simple_table WHERE a = ?;"
    }

    @BeforeEach
    fun setup() {
        dbConnection = DBConnector.connectToEmbeddedDB()
        db = DBQueryExecutor(dbConnection)
    }

    @AfterEach
    fun tearDown() {
        dbConnection.close()
    }

    @Test
    fun `test query without table`() {
        assertEquals(25, db.queryObject("SELECT 5 * 5", {}) { getInt(1) })
        assertEquals(30, db.queryObject("SELECT 5 * ?", { setInt(1, 6) }) { getInt(1) })
        assertEquals(listOf(1, 2, 3, 4), db.query("SELECT * FROM generate_series(1,4)", {}) { getInt(1) })
    }

    @Test
    fun `test insert & simple select`() {
        val testData = listOf(
            TableRow(1, "kek", UUID.randomUUID()),
            TableRow(2, "nekek", UUID.randomUUID())
        )

        db.update(CREATE_SIMPLE_TABLE_QUERY) { }

        testData.forEach { tableRow ->
            assertEquals(1, db.update(INSERT_INTO_SIMPLE_TABLE_QUERY) {
                setInt(1, tableRow.a)
                setString(2, tableRow.b)
                setObject(3, tableRow.c)
            })
        }

        assertEquals(testData, db.query(SELECT_ALL_ROWS_FROM_SIMPLE_TABLE_QUERY, { }) {
            val a = getInt(1)
            val b = getString(2)
            val c = getObject(3, UUID::class.java)
            TableRow(a, b, c)
        })

        assertEquals(0, db.update(INSERT_INTO_SIMPLE_TABLE_QUERY) {
            setInt(1, testData[0].a)
            setString(2, testData[0].b)
            setObject(3, testData[0].c)
        })
    }

    @Test
    fun `test nonexistent element`() {
        db.update(CREATE_SIMPLE_TABLE_QUERY) { }
        assertNull(db.queryObject(SELECT_ROW_FROM_SIMPLE_TABLE, { setInt(1, 1) }) { getInt(1) })
    }

    private data class TableRow(val a: Int, val b: String, val c: UUID)
}