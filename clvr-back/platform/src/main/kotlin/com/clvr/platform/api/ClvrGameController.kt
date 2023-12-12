package com.clvr.platform.api

fun interface ClvrGameController<Req: RequestEvent, Resp: ResponseEvent> {
    fun handle(communicator: SessionParticipantsCommunicator<Req, Resp>, event: Req)
}

interface SessionParticipantsCommunicator<Req: RequestEvent, Resp: ResponseEvent>{
    fun sendToHost(event: Resp)
    fun sendToClients(event: Resp)
}