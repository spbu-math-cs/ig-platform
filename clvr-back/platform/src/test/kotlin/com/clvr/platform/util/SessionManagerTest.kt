package com.clvr.platform.util

import com.clvr.platform.api.EventPayloadInterface
import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionId
import com.clvr.platform.impl.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SessionManagerTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `session manager simple test`() {
        val sessionId = SessionId("1")

        val echoSessionManager = SessionManager<DummyPayload, DummyPayload> { manager, event ->
            when (event.payload) {
                is TestHostPayload -> manager.sendToHost(ResponseEvent(event.payload))
                is TestClientPayload -> manager.sendToClients(ResponseEvent(event.payload))
            }
        }

        val firstClientChannel = echoSessionManager.registerClient("client-1")
        val hostChannel = echoSessionManager.hostChannel

        var clientEvent: RequestEvent<TestClientPayload> = RequestEvent(sessionId, TestClientPayload("client-1 data"))

        echoSessionManager.handleHostEvent(clientEvent)
        runBlocking {
            assertEquals(clientEvent.payload, firstClientChannel.receive().payload)
            assertTrue(hostChannel.isEmpty)
        }

        val secondClientChannel = echoSessionManager.registerClient("client-2")
        clientEvent = RequestEvent(sessionId, TestClientPayload("client-2 data"))
        echoSessionManager.handleHostEvent(clientEvent)
        runBlocking {
            assertEquals(clientEvent.payload, firstClientChannel.receive().payload)
            assertEquals(clientEvent.payload, secondClientChannel.receive().payload)
        }

        echoSessionManager.unregisterClient("client-1")
        assertTrue(firstClientChannel.isClosedForReceive)

        val hostEvent: RequestEvent<TestHostPayload> = RequestEvent(sessionId, TestHostPayload("host data"))
        echoSessionManager.handleHostEvent(hostEvent)
        runBlocking {
            assertEquals(hostEvent.payload, hostChannel.receive().payload)
            assertTrue(secondClientChannel.isEmpty)
        }
    }

    sealed interface DummyPayload: EventPayloadInterface

    data class TestHostPayload(val data: String): DummyPayload {
        override val type: String = "MAIN_BOARD"
    }
    data class TestClientPayload(val data: String): DummyPayload {
        override val type: String = "MAIN_BOARD"
    }
}