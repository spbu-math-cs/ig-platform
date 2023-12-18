package com.clvr.platform.api

import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.platform.impl.AufManager
import io.ktor.server.routing.*

interface ActivityInstaller<Req: RequestEvent, Resp: ResponseEvent> {
    val activityName: String

    fun install(
        route: Route,
        templateDatabase: TemplateDatabase,
        aufManager: AufManager,
        sessionRegistry: ClvrSessionRegistry<Req, Resp>
    )
}