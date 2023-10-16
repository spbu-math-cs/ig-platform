package com.clvr.server.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class SessionManager(
    private val sessionId: Long,
    private val hostEventHandler: SessionManager.(event: Event<*>) -> Unit
): AutoCloseable {
    val hostChannel: Channel<Event<*>> = Channel(Channel.UNLIMITED)
    private val clientChannels: MutableMap<String, Channel<Event<*>>> = mutableMapOf()

    fun handleHostEvent(event: Event<*>) {
        hostEventHandler(this, event)
    }

    fun sendToClients(event: Event<*>) {
        synchronized(clientChannels) {
            runBlocking {
                clientChannels.values.forEach { clientChannel ->
                    clientChannel.send(event)
                }
            }
        }
    }

    fun sendToHost(event: Event<*>) {
        runBlocking {
            hostChannel.send(event)
        }
    }

    fun registerClient(clientEndpoint: String): Channel<Event<*>> {
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