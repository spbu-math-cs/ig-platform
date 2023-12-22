package com.clvr.platform.impl.db.user

import com.clvr.platform.api.db.*
import com.clvr.platform.api.exceptions.DuplicateUserException
import com.clvr.platform.api.model.UserInfo
import com.clvr.platform.impl.db.DBQueryExecutor
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*

internal class PostgresUserDatabase(private val db: DBQueryExecutor): UserDatabase {
    companion object {
        // TODO: store password hash in separate table
        // @implNote: name `user` is already taken in postgresql, so name table `users`
        private val createTableQuery: String = """
            CREATE TABLE IF NOT EXISTS users (
                uuid UUID PRIMARY KEY,
                name TEXT NOT NULL, 
                password_hash TEXT NOT NULL,
                
                UNIQUE (name)
            );
        """.trimIndent()

        private const val addUserQuery: String = "INSERT INTO users (uuid, name, password_hash) VALUES (?, ?, ?);"

        private const val getUserByNameQuery: String = "SELECT uuid, name FROM users WHERE name = ?;"

        private const val getUserByUuidQuery: String = "SELECT uuid, name FROM users WHERE uuid = ?;"

        private const val getUserPasswordHashQuery: String = "SELECT password_hash FROM users WHERE name = ?;"
    }

    init {
        createUserTable()
    }

    override fun addUser(userName: String, passwordHash: String): UserInfo {
        try {
            val uuid = UUID.randomUUID()

            db.update(addUserQuery) {
                setObject(1, uuid);
                setString(2, userName)
                setString(3, passwordHash)
            }

            return UserInfo(uuid, userName)
        } catch (e: SQLIntegrityConstraintViolationException) {
            throw DuplicateUserException()
        }
    }

    override fun getUser(userName: String): UserInfo? {
        return db.queryObject(
            getUserByNameQuery,
            {
                setString(1, userName)
            }
        ) {
            val uuid = getObject(1, UUID::class.java)
            val name = getString(2)
            UserInfo(uuid, name)
        }
    }

    override fun getUser(userUuid: UUID): UserInfo? {
        return db.queryObject(
            getUserByUuidQuery,
            {
                setObject(1, userUuid)
            }
        ) {
            val uuid = getObject(1, UUID::class.java)
            val name = getString(2)
            UserInfo(uuid, name)
        }
    }

    override fun getUserPasswordHash(userName: String): String? {
        return db.queryObject(
            getUserPasswordHashQuery,
            {
                setString(1, userName)
            }
        ) {
            getString(1)
        }
    }

    private fun createUserTable() {
        db.update(createTableQuery) { }
    }
}