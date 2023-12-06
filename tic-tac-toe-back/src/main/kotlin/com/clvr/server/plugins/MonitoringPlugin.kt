package com.clvr.server.plugins

import com.clvr.server.logger
import io.ktor.server.application.*

val MonitoringPlugin = createApplicationPlugin(name = "MonitoringPlugin") {
    application.environment.monitor.subscribe(ApplicationStarted) {
        application.logger().info { "Server is up" }
    }
    application.environment.monitor.subscribe(ApplicationStopped) {
        application.logger().info { "Server is shutting down" }
        quizDatabase.close()
    }
}