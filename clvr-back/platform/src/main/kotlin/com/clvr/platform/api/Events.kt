package com.clvr.platform.api

sealed interface Event

interface RequestEvent : Event {
    val session: SessionId

    val type: String
}

interface ResponseEvent {
    val state: String
}