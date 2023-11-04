package com.clvr.server.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking

fun interface EventHandler<Req: EventPayloadInterface, Resp: EventPayloadInterface> {
    fun handle(manager: SessionManager<Req, Resp>, event: RequestEvent<Req>)
}

class SessionManager<Req: EventPayloadInterface, Resp: EventPayloadInterface>(
    private val hostEventHandler: EventHandler<Req, Resp>
): AutoCloseable {
    val hostChannel: Channel<ResponseEvent<Resp>> = Channel(Channel.UNLIMITED)
    private val clientChannels: MutableMap<String, Channel<ResponseEvent<Resp>>> = mutableMapOf()

    fun handleHostEvent(event: RequestEvent<Req>) {
        hostEventHandler.handle(this, event)
    }

    fun sendToClients(event: ResponseEvent<Resp>) {
        synchronized(clientChannels) {
            runBlocking {
                clientChannels.values.forEach { clientChannel ->
                    clientChannel.send(event)
                }
            }
        }
    }

    fun sendToHost(event: ResponseEvent<Resp>) {
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