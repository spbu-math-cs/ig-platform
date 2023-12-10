package com.clvr.platform.api

import com.clvr.platform.util.EventHandler
import kotlinx.serialization.Serializable

interface ClvrSessionRegistry<Req: EventPayloadInterface, Resp: EventPayloadInterface> {
    fun startNewGame(
        controller: ClvrGameController<Req, Resp>,
        view: ClvrGameView<Req, Resp>
    ): SessionId
}

@Serializable
data class SessionId(val id: String)

typealias ClvrGameController<Req, Resp> = EventHandler<Req, Resp>

interface ClvrGameView<Req: EventPayloadInterface, Resp: EventPayloadInterface> {
    val hostView: List<ResponseEvent<Resp>>
    val clientView: List<ResponseEvent<Resp>>

    // TODO: do something smart than this
    fun encodeEventToJson(event: ResponseEvent<Resp>): String
    fun decodeJsonToEvent(jsonString: String): RequestEvent<Req>
}
