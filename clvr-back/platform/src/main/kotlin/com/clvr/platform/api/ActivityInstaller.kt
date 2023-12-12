package com.clvr.platform.api

import com.clvr.platform.api.db.TemplateDatabase
import io.ktor.server.routing.*

interface ActivityInstaller<Req: RequestEvent, Resp: ResponseEvent> {
    val activityName: String

    fun install(
        route: Route,
        templateDatabase: TemplateDatabase,
        sessionRegistry: ClvrSessionRegistry<Req, Resp>
    )
}