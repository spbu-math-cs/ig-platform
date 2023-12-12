package com.clvr.platform.api

fun interface ClvrGameController<Req: EventPayloadInterface, Resp: EventPayloadInterface> {
    fun handle(communicator: SessionParticipantsCommunicator<Req, Resp>, event: RequestEvent<Req>)
}

interface SessionParticipantsCommunicator<Req: EventPayloadInterface, Resp: EventPayloadInterface> {
    fun sendToHost(event: ResponseEvent<Resp>)
    fun sendToClients(event: ResponseEvent<Resp>)
}