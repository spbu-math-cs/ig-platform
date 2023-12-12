package com.clvr.ttt

import com.clvr.platform.api.ActivityInstaller
import com.clvr.platform.api.ClvrSessionRegistry
import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.db.TemplateDatabase
import com.clvr.platform.api.db.preloadTemplates
import com.clvr.ttt.common.TicTacToeTemplate
import io.ktor.server.routing.*
import java.io.File

class TicTacToeInstaller(
    private val templateFiles: List<File>
) : ActivityInstaller<TicTacToeRequestPayload, TicTacToeResponsePayload> {
    override val activityName: String = ACTIVITY_ID

    override fun install(
        route: Route,
        templateDatabase: TemplateDatabase,
        sessionRegistry: ClvrSessionRegistry<TicTacToeRequestPayload, TicTacToeResponsePayload>
    ) {
        templateDatabase.preloadTemplates<TicTacToeTemplate>(templateFiles)
        with (route) {
            routingSetup(templateDatabase, sessionRegistry)
        }
    }

    companion object {
        const val ACTIVITY_ID: String = "tic-tac-toe"

        fun templateId(id: String): TemplateId {
            return TemplateId(ACTIVITY_ID, id)
        }
    }
}