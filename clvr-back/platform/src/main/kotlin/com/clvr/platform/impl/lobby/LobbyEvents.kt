package com.clvr.platform.impl.lobby

import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionId
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface LobbyRequestEvent : RequestEvent {
    companion object {
        fun decodeFromString(jsonString: String): LobbyRequestEvent? {
            return try {
                Json.decodeFromString<StartGameEvent>(jsonString)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class EnterLobbyEvent(override val session: SessionId): LobbyRequestEvent {
    @EncodeDefault
    override val type: String = Companion.type

    companion object {
        const val type: String = "ENTER_LOBBY"
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class StartGameEvent(override val session: SessionId): LobbyRequestEvent {
    @EncodeDefault
    override val type: String = Companion.type

    companion object {
        const val type: String = "START_GAME"
    }
}

@Serializable
data class PlayersInfo(val players: List<Player>)

@Serializable
data class Player(val name: String)

@Serializable
data class LobbyResponseEvent private constructor (
    override val state: String,
    val payload: PlayersInfo
) : ResponseEvent {
    constructor(payload: PlayersInfo): this(Companion.state, payload)

    override fun encodeToJson(json: Json): String {
        return json.encodeToString(this)
    }

    companion object {
        const val state: String = "PREPARING"
    }
}