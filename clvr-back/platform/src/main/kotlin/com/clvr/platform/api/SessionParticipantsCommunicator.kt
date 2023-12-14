package com.clvr.platform.api

interface SessionParticipantsCommunicator<Req: EventPayloadInterface, Resp: EventPayloadInterface> {
    fun sendToHost(event: ResponseEvent<Resp>)
    fun sendToClients(event: ResponseEvent<Resp>)
}