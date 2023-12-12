package com.clvr.platform.api

interface RequestEvent {
    val session: SessionId

    val type: String
}

interface ResponseEvent {
    val state: String
}