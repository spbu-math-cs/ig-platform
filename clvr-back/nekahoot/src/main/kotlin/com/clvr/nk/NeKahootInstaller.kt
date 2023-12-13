package com.clvr.nk

import com.clvr.nk.common.NeKahootTemplate
import com.clvr.platform.api.ActivityInstaller
import com.clvr.platform.api.ClvrSessionRegistry
import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.platform.api.db.preloadTemplates
import io.ktor.server.routing.*
import java.io.File

class NeKahootInstaller(
    private val templateFiles: List<File>
) : ActivityInstaller<NeKahootRequest<*>, NeKahootResponse<*>> {
    override val activityName: String = ACTIVITY_ID

    override fun install(
        route: Route,
        templateDatabase: TemplateDatabase,
        sessionRegistry: ClvrSessionRegistry<NeKahootRequest<*>, NeKahootResponse<*>>
    ) {
        templateDatabase.preloadTemplates<NeKahootTemplate>(templateFiles)
        with (route) {
            routingSetup(templateDatabase, sessionRegistry)
        }
    }

    companion object {
        const val ACTIVITY_ID: String = "nekahoot"

        fun templateId(id: String): TemplateId {
            return TemplateId(ACTIVITY_ID, id)
        }
    }
}