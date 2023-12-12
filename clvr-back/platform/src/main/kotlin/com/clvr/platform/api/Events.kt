package com.clvr.platform.api

import kotlinx.serialization.Serializable

interface EventPayloadInterface {
    val type: String
}

// TODO: this doesn't allow payload = null (issue #93)
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