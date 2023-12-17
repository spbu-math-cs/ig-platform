package com.clvr.platform.api

interface ClvrGameController<Req: RequestEvent, Resp: ResponseEvent> {
    fun handle(communicator: SessionParticipantsCommunicator<Req, Resp>, event: Req)

    fun handleFromClient(communicator: SessionParticipantsCommunicator<Req, Resp>, clientEndpoint: String, event: Req)
}

interface SessionParticipantsCommunicator<Req: RequestEvent, Resp: ResponseEvent>{
    fun sendToHost(event: Resp)
    fun sendToClients(event: Resp)
    fun sendToClient(clientEndpoint: String, event: Resp)
}