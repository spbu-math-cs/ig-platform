package com.clvr.nk

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class MockCommunicator : NeKahootSessionParticipantsCommunicator {
    val hostChannel = Channel<NeKahootResponseWithPayload<*>>(Channel.UNLIMITED)
    val clientsToChannels: MutableMap<String, Channel<NeKahootResponseWithPayload<*>>> = mutableMapOf()

    private fun getClientChannel(clientEndpoint: String) =
        clientsToChannels.getOrPut(clientEndpoint) { Channel(Channel.UNLIMITED) }

    override fun sendToHost(event: NeKahootResponseWithPayload<*>) = runBlocking {
        hostChannel.send(event)
    }

    override fun sendToClients(event: NeKahootResponseWithPayload<*>) = runBlocking {
        clientsToChannels.values.forEach { it.send(event) }
    }

    override fun sendToClient(clientEndpoint: String, event: NeKahootResponseWithPayload<*>) = runBlocking {
        getClientChannel(clientEndpoint).send(event)
    }
}