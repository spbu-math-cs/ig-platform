package com.clvr.platform.api.model

import java.util.*

data class UserInfo(val uuid: UUID, val name: String)

data class UserCookie(val userInfo: UserInfo)

data class UserInfoWithCookie(val userInfo: UserInfo, val userCookie: UserCookie)