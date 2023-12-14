package com.clvr.platform.impl.db.user

import com.clvr.platform.api.db.DuplicateUserException
import com.clvr.platform.api.db.NoSuchUserException
import com.clvr.platform.api.db.ValidationException
import com.clvr.platform.impl.db.DBConnector
import com.clvr.platform.impl.db.DBQueryExecutor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class PostgresUserDatabaseTest {
    private lateinit var dbQueryExecutor: DBQueryExecutor
    private lateinit var userDatabase: PostgresUserDatabase

    @BeforeEach
    fun setup() {
        dbQueryExecutor = DBQueryExecutor(DBConnector.connectToEmbeddedDB())
        userDatabase = PostgresUserDatabase(dbQueryExecutor)
    }

    @AfterEach
    fun tearDown() {
        dbQueryExecutor.close()
    }

    @Test
    fun getUser() {
        assertDoesNotThrow { userDatabase.addUser("admin", "admin") }
        assertEquals("admin", userDatabase.getUser("admin", "admin").name)
        assertThrowsExactly(NoSuchUserException::class.java) { userDatabase.getUser("not admin", "admin") }
        assertThrowsExactly(ValidationException::class.java) { userDatabase.getUser("admin", "definitely wrong password") }
    }

    @Test
    fun getUserByUuid() {
        val user = userDatabase.addUser("test", "test")
        assertEquals("test", userDatabase.getUser(user.uuid).name)
        assertThrowsExactly(ValidationException::class.java) { userDatabase.getUser(UUID.randomUUID()) }
    }

    @Test
    fun addUserWithTheSameName() {
        assertDoesNotThrow { userDatabase.addUser("name", "password") }
        assertThrowsExactly(DuplicateUserException::class.java) {
            userDatabase.addUser("name", "maybe not the same password")
        }
    }
}