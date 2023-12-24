package com.clvr.platform.api.exceptions

// 🐺🐺🐺🐺🐺🐺🐺🐺AUTH🐺🐺🐺🐺🐺🐺🐺🐺
sealed class AuthException(message: String): IllegalArgumentException(message)
class NoSuchUserException: AuthException("No such user exception")
class DuplicateUserException: AuthException("User with such name already exists")
class ValidationException: AuthException("Failed to validate user")