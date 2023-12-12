package com.clvr.platform.api

import kotlinx.serialization.Serializable

/**
 * Interface for `registering` a new game,
 * i.e. adding it to session storage held by platform
 */
interface ClvrSessionRegistry<Req: RequestEvent, Resp: ResponseEvent> {
    fun startNewGame(
        controller: ClvrGameController<Req, Resp>,
        view: ClvrGameView<Req, Resp>
    ): SessionId
}

@Serializable
data class SessionId(val id: String)

interface ClvrGameView<Req: RequestEvent, Resp: ResponseEvent> {
    /**
     * A list of events that will be sent to host when it connects
     */
    val hostView: List<Resp>

    /**
     * A list of events that will be sent to client when it connects
     */
    val clientView: List<Resp>

    // TODO: do something smart than this
    fun encodeEventToJson(event: Resp): String
    fun decodeJsonToEvent(jsonString: String): Req
}
