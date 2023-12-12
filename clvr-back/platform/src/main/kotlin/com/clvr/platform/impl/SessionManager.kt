package com.clvr.platform.impl

import com.clvr.platform.api.ClvrGameController
import com.clvr.platform.api.EventPayloadInterface
import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionParticipantsCommunicator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking

internal class SessionManager<Req: EventPayloadInterface, Resp: EventPayloadInterface>(
    private val clvrGameController: ClvrGameController<Req, Resp>
): AutoCloseable, SessionParticipantsCommunicator<Req, Resp> {
    val hostChannel: Channel<ResponseEvent<Resp>> = Channel(Channel.UNLIMITED)
    private val clientChannels: MutableMap<String, Channel<ResponseEvent<Resp>>> = mutableMapOf()

    fun handleHostEvent(event: RequestEvent<Req>) {
        clvrGameController.handle(this, event)
    }

    override fun sendToClients(event: ResponseEvent<Resp>) {
        synchronized(clientChannels) {
            runBlocking {
                clientChannels.values.forEach { clientChannel ->
                    clientChannel.send(event)
                }
            }
        }
    }

    override fun sendToHost(event: ResponseEvent<Resp>) {
        runBlocking {
            hostChannel.send(event)
        }
    }

    fun registerClient(clientEndpoint: String): ReceiveChannel<ResponseEvent<Resp>> {
        synchronized(clientChannels) {
            return clientChannels.computeIfAbsent(clientEndpoint) { Channel(Channel.UNLIMITED) }
        }
    }

    fun unregisterClient(clientEndpoint: String) {
        synchronized(clientChannels) {
            clientChannels[clientEndpoint]?.close()
            clientChannels.remove(clientEndpoint)
        }
    }

    override fun close() {
        synchronized(clientChannels) {
            clientChannels.values.forEach { channel -> channel.close() }
            clientChannels.clear()
        }

        hostChannel.close()
    }
}