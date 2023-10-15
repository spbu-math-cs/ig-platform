package com.clvr.server.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

class SessionManagerTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `session manager simple test`() {
        val sessionId: Long = 1

        val echoSessionManager: SessionManager = SessionManager(
            sessionId
        ) { event ->
            when (event.payload) {
                is TestHostPayload -> sendToHost(event)
                is TestClientPayload -> sendToClients(event)
                else -> throw RuntimeException("unknown payload")
            }
        }

        val firstClientChannel: Channel<Event<*>> = echoSessionManager.registerClient("client-1")
        val hostChannel: Channel<Event<*>> = echoSessionManager.hostChannel

        var clientEvent: Event<TestClientPayload> = Event(
            Session(sessionId),
            "client event",
            TestClientPayload("x")
        )

        echoSessionManager.handleHostEvent(clientEvent)
        runBlocking {
            assertEquals(clientEvent, firstClientChannel.receive())
            assertTrue(hostChannel.isEmpty)
        }

        val secondClientChannel: Channel<Event<*>> = echoSessionManager.registerClient("client-2")
        clientEvent = Event(
            Session(sessionId),
            "client event",
            TestClientPayload("y")
        )
        echoSessionManager.handleHostEvent(clientEvent)
        runBlocking {
            assertEquals(clientEvent, firstClientChannel.receive())
            assertEquals(clientEvent, secondClientChannel.receive())
        }

        echoSessionManager.unregisterClient("client-1")
        assertTrue(firstClientChannel.isClosedForSend)

        val hostEvent: Event<TestHostPayload> = Event(
            Session(sessionId),
            "host event",
            TestHostPayload("a")
        )
        echoSessionManager.handleHostEvent(hostEvent)
        runBlocking {
            assertEquals(hostEvent, hostChannel.receive())
            assertTrue(secondClientChannel.isEmpty)
        }
    }

    data class TestHostPayload(val data: String)
    data class TestClientPayload(val data: String)
}