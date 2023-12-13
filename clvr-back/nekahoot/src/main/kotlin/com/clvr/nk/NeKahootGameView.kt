package com.clvr.nk

import com.clvr.platform.api.ClvrGameView

class NeKahootGameView(private val game: GameState): ClvrGameView<NeKahootRequest<*>, NeKahootResponse<*>> {
    override val hostView: List<NeKahootResponse<*>>
        get() = listOf()

    override val clientView: List<NeKahootResponse<*>>
        get() = listOf()

    override fun decodeJsonToEvent(jsonString: String): NeKahootRequest<*> {
        return decodeJsonToNKEvent(jsonString)
    }

    override fun encodeEventToJson(event: NeKahootResponse<*>): String {
        return encodeNKEventToJson(event)
    }
}