package com.clvr.platform.impl

import com.clvr.platform.api.exceptions.DuplicateUserException
import com.clvr.platform.api.exceptions.NoSuchUserException
import com.clvr.platform.api.exceptions.ValidationException
import com.clvr.platform.api.model.UserCookie
import com.clvr.platform.api.model.UserInfo
import com.clvr.platform.impl.db.DBConnector
import com.clvr.platform.impl.db.DBQueryExecutor
import com.clvr.platform.impl.db.user.PostgresUserDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class AufManagerTest {
    private lateinit var dbQueryExecutor: DBQueryExecutor
    private lateinit var aufManager: AufManager

    @BeforeEach
    fun setup() {
        dbQueryExecutor = DBQueryExecutor(DBConnector.connectToEmbeddedDB())
        aufManager = AufManager(PostgresUserDatabase(dbQueryExecutor))
    }

    @AfterEach
    fun tearDown() {
        dbQueryExecutor.close()
    }

    @Test
    fun getUserInfoByCookie() {
        val userData = aufManager.addUser("admin", "really strong password")
        assertEquals("admin", aufManager.getUserInfoByCookie(userData.userCookie).name)

        assertThrowsExactly(NoSuchUserException::class.java) {
            aufManager.getUserInfoByCookie(UserCookie(UserInfo(UUID.randomUUID(), "aufovich")))
        }
    }

    @Test
    fun getUserCookie() {
        val userRegisterData = aufManager.addUser("admin", "really strong password")
        val userLoginData = aufManager.getUserInfoWithCookie("admin", "really strong password")

        assertEquals(userRegisterData, userLoginData)

        assertThrowsExactly(ValidationException::class.java) {
            aufManager.getUserInfoWithCookie("admin", "really weak password")
        }
        assertThrowsExactly(NoSuchUserException::class.java) {
            aufManager.getUserInfoWithCookie("not admin", "really strong password")
        }
    }

    @Test
    fun validateUserByCookie() {
        val userData = aufManager.addUser("some user", "abacaba")
        assertDoesNotThrow { aufManager.validateUserByCookie(userData.userCookie) }
        assertThrowsExactly(ValidationException::class.java) {
            aufManager.validateUserByCookie(UserCookie(UserInfo(UUID.randomUUID(), "not some user")))
        }
    }

    @Test
    fun addUserWithTheSameName() {
        aufManager.addUser("name", "password")
        assertThrowsExactly(DuplicateUserException::class.java) {
            aufManager.addUser("name", "maybe not the same password")
        }
    }
}