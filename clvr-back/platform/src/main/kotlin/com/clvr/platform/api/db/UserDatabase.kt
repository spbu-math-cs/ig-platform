package com.clvr.platform.api.db

import com.clvr.platform.api.model.UserInfo
import java.util.UUID

interface UserDatabase {
    fun addUser(userName: String, passwordHash: String): UserInfo

    fun getUser(userName: String): UserInfo?

    fun getUser(userUuid: UUID): UserInfo?

    fun getUserPasswordHash(userName: String): String?
}