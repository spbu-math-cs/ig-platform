package com.clvr.platform.impl.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*

internal fun Application.configureCallLogging() {
    install(CallLogging) {
        disableDefaultColors()
    }
}