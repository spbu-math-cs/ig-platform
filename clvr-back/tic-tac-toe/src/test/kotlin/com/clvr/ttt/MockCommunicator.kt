package com.clvr.ttt

import com.clvr.platform.api.ResponseEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class MockCommunicator : TicTacToeSessionParticipantsCommunicator {
    val hostChannel = Channel<ResponseEvent<TicTacToeResponsePayload>>(Channel.UNLIMITED)
    val clientChannel = Channel<ResponseEvent<TicTacToeResponsePayload>>(Channel.UNLIMITED)

    override fun sendToHost(event: ResponseEvent<TicTacToeResponsePayload>) = runBlocking {
        hostChannel.send(event)
    }

    override fun sendToClients(event: ResponseEvent<TicTacToeResponsePayload>) = runBlocking {
        clientChannel.send(event)
    }
}