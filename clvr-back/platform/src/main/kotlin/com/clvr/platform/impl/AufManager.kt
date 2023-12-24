package com.clvr.platform.impl

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies
import com.clvr.platform.api.db.*
import com.clvr.platform.api.exceptions.NoSuchUserException
import com.clvr.platform.api.exceptions.ValidationException
import com.clvr.platform.api.model.UserCookie
import com.clvr.platform.api.model.UserInfo
import com.clvr.platform.api.model.UserInfoWithCookie
import com.clvr.platform.impl.plugins.userDatabase
import io.ktor.server.application.*

class AufManager(
    private val userDatabase: UserDatabase
) {
    fun addUser(userName: String, password: String): UserInfoWithCookie {
        val passwordHash = genPasswordHash(userName, password)
        val userInfo = userDatabase.addUser(userName, passwordHash)
        return UserInfoWithCookie(userInfo, getUserCookieByUserInfo(userInfo))
    }

    fun validateUserByCookie(userCookie: UserCookie) {
        if (userDatabase.getUser(userCookie.userInfo.uuid) == null) throw ValidationException()
    }

    fun getUserInfoByCookie(userCookie: UserCookie): UserInfo {
        return userDatabase.getUser(userCookie.userInfo.uuid) ?: throw NoSuchUserException()
    }

    fun getUserInfoWithCookie(userName: String, password: String): UserInfoWithCookie {
        val passwordHash = userDatabase.getUserPasswordHash(userName) ?: throw NoSuchUserException()
        if (!verifyPassword(userName, password, passwordHash)) throw ValidationException()

        // getUser should never be null. The only when it is possible if the user was deleted right after password verification.
        val userInfo = userDatabase.getUser(userName) ?: throw NoSuchUserException()
        return UserInfoWithCookie(userInfo, getUserCookieByUserInfo(userInfo))
    }

    companion object {
        private const val salt = "https://youtu.be/dQw4w9WgXcQ?si=xUb7-Uy8wUHLt13c"
        private val BCryptHasher = BCrypt.with(LongPasswordStrategies.hashSha512(BCrypt.Version.VERSION_2A))
        private val BCryptVerifier = BCrypt.verifyer(BCrypt.Version.VERSION_2A, LongPasswordStrategies.hashSha512(BCrypt.Version.VERSION_2A))

        private fun genPasswordHash(userName: String, password: String): String {
            return String(BCryptHasher.hash(10, getStringToHash(userName, password).toCharArray()))
        }

        private fun verifyPassword(userName: String, password: String, passwordHash: String): Boolean {
            return BCryptVerifier.verify(getStringToHash(userName, password).toCharArray(), passwordHash.toCharArray()).verified
        }

        private fun getUserCookieByUserInfo(userInfo: UserInfo): UserCookie {
            return UserCookie(userInfo)
        }

        private fun getStringToHash(userName: String, password: String): String {
            return password + salt + userName
        }
    }
}

private lateinit var aufManagerSingleton: AufManager

fun Application.configureAuf() {
    aufManagerSingleton = AufManager(userDatabase)
}

val Application.aufManager: AufManager
        get() = aufManagerSingleton