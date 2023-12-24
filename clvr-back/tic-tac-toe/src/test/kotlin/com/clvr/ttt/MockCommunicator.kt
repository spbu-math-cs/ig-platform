package com.clvr.ttt

import com.clvr.platform.api.model.UserInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class MockCommunicator : TicTacToeSessionParticipantsCommunicator {
    val hostChannel = Channel<TicTacToeResponse<*>>(Channel.UNLIMITED)
    val clientChannel = Channel<TicTacToeResponse<*>>(Channel.UNLIMITED)

    override fun sendToHost(event: TicTacToeResponse<*>) = runBlocking {
        hostChannel.send(event)
    }

    override fun sendToClients(event: TicTacToeResponse<*>) = runBlocking {
        clientChannel.send(event)
    }

    override fun sendToClient(clientEndpoint: String, event: TicTacToeResponse<*>) = runBlocking {
        clientChannel.send(event)
    }

    override fun getClientInfo(clientEndpoint: String): UserInfo? {
        return null
    }
}