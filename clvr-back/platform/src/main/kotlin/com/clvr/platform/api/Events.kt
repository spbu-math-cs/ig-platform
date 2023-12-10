package com.clvr.platform.api

import kotlinx.serialization.Serializable

interface EventPayloadInterface {
    val type: String
}

sealed interface Event<out T: EventPayloadInterface> {
    val payload: T
}

@Serializable
data class RequestEvent<out T: EventPayloadInterface> private constructor (
    val session: SessionId,
    val type: String,
    override val payload: T
): Event<T> {
    constructor(session: SessionId, payload: T): this(session, payload.type, payload)
}

@Serializable
data class ResponseEvent<out T: EventPayloadInterface> private constructor (
    val state: String,
    override val payload: T
): Event<T> {
    constructor(payload: T): this(payload.type, payload)
}