package com.clvr.nk

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class MockCommunicator : NeKahootSessionParticipantsCommunicator {
    val hostChannel = Channel<NeKahootResponse<*>>(Channel.UNLIMITED)
    val clientsToChannels: MutableMap<String, Channel<NeKahootResponse<*>>> = mutableMapOf()

    private fun getClientChannel(clientEndpoint: String) =
        clientsToChannels.getOrPut(clientEndpoint) { Channel(Channel.UNLIMITED) }

    override fun sendToHost(event: NeKahootResponse<*>) = runBlocking {
        hostChannel.send(event)
    }

    override fun sendToClients(event: NeKahootResponse<*>) = runBlocking {
        clientsToChannels.values.forEach { it.send(event) }
    }

    override fun sendToClient(clientEndpoint: String, event: NeKahootResponse<*>) = runBlocking {
        getClientChannel(clientEndpoint).send(event)
    }
}