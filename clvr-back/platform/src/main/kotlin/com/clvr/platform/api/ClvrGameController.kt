package com.clvr.platform.api

import com.clvr.platform.api.model.UserInfo

interface ClvrGameController<Req: RequestEvent, Resp: ResponseEvent> {
    // TODO: this is a hack to make sessionId depend on activity, it should be deleted ASAP
    val activityName: String

    fun handleGameStart(communicator: SessionParticipantsCommunicator<Resp>)

    fun handle(communicator: SessionParticipantsCommunicator<Resp>, event: Req)

    fun handleFromClient(communicator: SessionParticipantsCommunicator<Resp>, clientEndpoint: String, event: Req)
}

interface SessionParticipantsCommunicator<in Resp: ResponseEvent>{
    fun sendToHost(event: Resp)
    fun sendToClients(event: Resp)
    fun sendToClient(clientEndpoint: String, event: Resp)
    fun getClientInfo(clientEndpoint: String): UserInfo?
}