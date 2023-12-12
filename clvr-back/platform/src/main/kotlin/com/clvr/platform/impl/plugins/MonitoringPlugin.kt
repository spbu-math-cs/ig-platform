package com.clvr.platform.impl.plugins

import com.clvr.platform.logger
import io.ktor.server.application.*

internal val MonitoringPlugin = createApplicationPlugin(name = "MonitoringPlugin") {
    application.environment.monitor.subscribe(ApplicationStarted) {
        application.logger.info { "Server is up" }
    }
    application.environment.monitor.subscribe(ApplicationStopped) {
        application.logger.info { "Server is shutting down" }
        templateDatabase.close()
    }
}