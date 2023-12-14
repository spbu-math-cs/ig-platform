package com.clvr.platform.impl.db.user

import com.clvr.platform.api.db.*
import com.clvr.platform.impl.db.DBQueryExecutor
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*

// TODO: separate user manager & user database
// TODO: use generated token with timestamps instead uuid or use some general OAUTH tool
// TODO: improve security
internal class PostgresUserDatabase(private val db: DBQueryExecutor): UserDatabase {
    companion object {
        // TODO: store password in separate table
        // @implNote: name `user` is already taken in postgresql, so name table `users`
        private val createTableQuery: String = """
            CREATE TABLE IF NOT EXISTS users (
                uuid UUID PRIMARY KEY,
                name TEXT NOT NULL, 
                password TEXT NOT NULL,
                
                UNIQUE (name)
            );
        """.trimIndent()

        private const val addUserQuery: String = "INSERT INTO users (uuid, name, password) VALUES (?, ?, ?);"

        private const val getUserByNameQuery: String = "SELECT uuid, name, password FROM users WHERE name = ?;"

        private const val getUserByUuid: String = "SELECT uuid, name FROM users WHERE uuid = ?";
    }

    init {
        createUserTable()
    }

    override fun addUser(userName: String, password: String): User {
        try {
            val uuid = UUID.randomUUID()

            db.update(addUserQuery) {
                setObject(1, uuid);
                setString(2, userName)
                setString(3, password)
            }

            return User(uuid, userName)
        } catch (e: SQLIntegrityConstraintViolationException) {
            throw DuplicateUserException()
        }
    }

    override fun getUser(userName: String, password: String): User {
        val userData = db.queryObject(
            getUserByNameQuery,
            {
                setString(1, userName)
            }
        ) {
            val uuid = getObject(1, UUID::class.java)
            val name = getString(2)
            val password = getString(3)
            UserData(uuid, name, password)
        } ?: throw NoSuchUserException()

        if (password == userData.password) {
            return userData.toUser()
        } else {
            throw ValidationException()
        }
    }

    override fun getUser(userUuid: UUID): User {
        return db.queryObject(
            getUserByUuid,
            {
                setObject(1, userUuid)
            }
        ) {
            val uuid = getObject(1, UUID::class.java)
            val name = getString(2)
            User(uuid, name)
        } ?: throw ValidationException()
    }

    private fun createUserTable() {
        db.update(createTableQuery) { }
    }
}

private data class UserData(val uuid: UUID, val name: String, val password: String) {
    fun toUser(): User {
        return User(uuid, name)
    }
}