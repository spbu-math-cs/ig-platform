package com.clvr.platform.api.db

import java.util.UUID

data class User(val uuid: UUID, val name: String)

// 🐺🐺🐺🐺🐺🐺🐺🐺AUTH🐺🐺🐺🐺🐺🐺🐺🐺
sealed class AuthException(message: String): IllegalArgumentException(message)
class NoSuchUserException: AuthException("No such user exception")
class DuplicateUserException: AuthException("User with such name already exists")
class ValidationException: AuthException("Failed to validate user")

interface UserDatabase {
    fun addUser(userName: String, password: String): User

    fun getUser(userName: String, password: String): User

    fun getUser(userUuid: UUID): User

    fun validateUserUuid(userUuid: UUID) {
        // getUser should throw ValidationException in case if there is no user with such userUuid
        getUser(userUuid)
    }
}