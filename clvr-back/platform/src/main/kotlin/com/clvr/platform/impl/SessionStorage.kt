package com.clvr.platform.impl

import com.clvr.platform.api.ClvrGameController
import com.clvr.platform.api.ClvrGameView
import com.clvr.platform.api.ClvrSessionRegistry
import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionId
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Interface that allows platform get info about game session by its id.
 * Note that it is separated from [ClvrSessionRegistry] interface which is a part of API, although
 * implementations make sense only when they implement both interfaces.
 *
 * This separation both hides implementation-specific methods of [ClvrSessionStorage]
 * (like [getSessionManager]) from activity developers, and prohibits platform to create new sessions, only providing
 * access to already registered (via [ClvrSessionRegistry]) sessions
 */
internal interface ClvrSessionStorage<Req: RequestEvent, Resp: ResponseEvent> {
    fun getSessionManager(session: SessionId): SessionManager<Req, Resp>
    fun getGameView(session: SessionId): ClvrGameView<Req, Resp>
}

internal class InMemorySessionStorage<Req: RequestEvent, Resp: ResponseEvent>
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