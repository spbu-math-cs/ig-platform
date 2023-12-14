package com.clvr.platform.api

import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.platform.api.db.UserDatabase
import io.ktor.server.routing.*

interface ActivityInstaller<Req: RequestEvent, Resp: ResponseEvent> {
    val activityName: String

    fun install(
        route: Route,
        templateDatabase: TemplateDatabase,
        userDatabase: UserDatabase,
        sessionRegistry: ClvrSessionRegistry<Req, Resp>
    )
}