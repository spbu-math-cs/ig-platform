package com.clvr.platform.impl

import at.favre.lib.crypto.bcrypt.BCrypt
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
        val passwordHash = genPasswordHash(password)
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
        if (!verifyPassword(password, passwordHash)) throw ValidationException()

        // getUser should never be null. The only when it is possible if the user was deleted right after password verification.
        val userInfo = userDatabase.getUser(userName) ?: throw NoSuchUserException()
        return UserInfoWithCookie(userInfo, getUserCookieByUserInfo(userInfo))
    }

    companion object {
        private fun genPasswordHash(password: String): String {
            return String(BCrypt.withDefaults().hash(10, password.toCharArray()))
        }

        private fun verifyPassword(password: String, passwordHash: String): Boolean {
            return BCrypt.verifyer().verify(password.toCharArray(), passwordHash.toCharArray()).verified
        }

        private fun getUserCookieByUserInfo(userInfo: UserInfo): UserCookie {
            return UserCookie(userInfo)
        }
    }
}

private lateinit var aufManagerSingleton: AufManager

fun Application.configureAuf() {
    aufManagerSingleton = AufManager(userDatabase)
}

val Application.aufManager: AufManager
        get() = aufManagerSingleton