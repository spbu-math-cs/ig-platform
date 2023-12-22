package com.clvr.utils;

object Config {
    const val SSL_PASSWORD_ENV = "SSL_PASSWORD"
    const val SSL_KEY_STORE_PASSWORD_ENV = "SSL_FILE_NAME"
    const val SSL_PATH_TO_KEY_STORE_ENV = "SSL_PATH_TO_KEY_STORE"
    const val SSL_ALIAS_NAME_ENV = "SSL_ALIAS_NAME"

    private val defaultConfig = mapOf(
        Pair(SSL_PASSWORD_ENV, "test_password"),
        Pair(SSL_ALIAS_NAME_ENV, "simple_alias"),
        Pair(SSL_KEY_STORE_PASSWORD_ENV, "test_password"),
        Pair(SSL_PATH_TO_KEY_STORE_ENV, "build/keystore.jks")
    )

    fun getString(envVariable: String): String {
        return getOrDefault(envVariable)
    }

    fun getInt(envVariable: String): Int {
        return getOrDefault(envVariable).toInt()
    }

    private fun getOrDefault(envVariable: String): String {
        return System.getenv(envVariable) ?: defaultConfig[envVariable]!!
    }
}
