package com.clvr.platform.impl

import com.clvr.platform.api.ClvrGameController
import com.clvr.platform.api.ClvrGameView
import com.clvr.platform.api.ClvrSessionRegistry
import com.clvr.platform.api.EventPayloadInterface
import com.clvr.platform.api.SessionId
import com.clvr.platform.util.SessionManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal interface ClvrSessionStorage<Req: EventPayloadInterface, Resp: EventPayloadInterface> {
    fun getSessionManager(session: SessionId): SessionManager<Req, Resp>
    fun getGameView(session: SessionId): ClvrGameView<Req, Resp>
}

class InMemorySessionStorage<Req: EventPayloadInterface, Resp: EventPayloadInterface>
    : ClvrSessionRegistry<Req, Resp>, ClvrSessionStorage<Req, Resp>
{
    private inner class GameRecord(
        val sessionManager: SessionManager<Req, Resp>,
        val gameView: ClvrGameView<Req, Resp>
    )

    private val games: MutableMap<SessionId, GameRecord> = ConcurrentHashMap()

    override fun getSessionManager(session: SessionId): SessionManager<Req, Resp> =
        games[session]!!.sessionManager

    override fun startNewGame(controller: ClvrGameController<Req, Resp>, view: ClvrGameView<Req, Resp>): SessionId {
        val newSession = SessionId(UUID.randomUUID().toString().take(6))
        val sessionManager: SessionManager<Req, Resp> = SessionManager(controller)
        games[newSession] = GameRecord(sessionManager, view)
        return newSession
    }

    override fun getGameView(session: SessionId): ClvrGameView<Req, Resp> {
        return games[session]!!.gameView
    }
}