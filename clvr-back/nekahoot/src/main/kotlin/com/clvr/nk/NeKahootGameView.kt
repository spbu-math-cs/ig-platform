package com.clvr.nk

import com.clvr.platform.api.ClvrGameView

class NeKahootGameView(private val game: GameState): ClvrGameView<NeKahootRequest, NeKahootResponseWithPayload<*>> {
    override val hostView: List<NeKahootResponseWithPayload<*>>
        get() = listOf()

    override val clientView: List<NeKahootResponseWithPayload<*>>
        get() = listOf()

    override fun decodeJsonToEvent(jsonString: String): NeKahootRequest {
        return decodeJsonToNKEvent(jsonString)
    }

    override fun encodeEventToJson(event: NeKahootResponseWithPayload<*>): String {
        return encodeNKEventToJson(event)
    }
}