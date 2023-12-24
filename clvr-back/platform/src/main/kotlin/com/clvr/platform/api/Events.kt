package com.clvr.platform.api

import kotlinx.serialization.json.Json

sealed interface Event

interface RequestEvent : Event {
    val session: SessionId

    val type: String
}

interface ResponseEvent {
    val state: String

    fun encodeToJson(json: Json): String
}