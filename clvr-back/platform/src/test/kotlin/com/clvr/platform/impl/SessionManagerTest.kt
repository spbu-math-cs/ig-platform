package com.clvr.platform.impl

import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SessionManagerTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `session manager simple test`() {
        val sessionId = SessionId("1")

        val echoSessionManager = SessionManager<DummyRequest, DummyResponse> { manager, event ->
            when (event.payload) {
                is TestHostPayload -> manager.sendToHost(DummyResponse(event.payload))
                is TestClientPayload -> manager.sendToClients(DummyResponse(event.payload))
            }
        }

        val firstClientChannel = echoSessionManager.registerClient("client-1")
        val hostChannel = echoSessionManager.hostChannel

        var clientEvent = DummyRequest(sessionId, TestClientPayload("client-1 data"))

        echoSessionManager.handleHostEvent(clientEvent)
        runBlocking {
            assertEquals(clientEvent.payload, firstClientChannel.receive().payload)
            assertTrue(hostChannel.isEmpty)
        }

        val secondClientChannel = echoSessionManager.registerClient("client-2")
        clientEvent = DummyRequest(sessionId, TestClientPayload("client-2 data"))
        echoSessionManager.handleHostEvent(clientEvent)
        runBlocking {
            assertEquals(clientEvent.payload, firstClientChannel.receive().payload)
            assertEquals(clientEvent.payload, secondClientChannel.receive().payload)
        }

        echoSessionManager.unregisterClient("client-1")
        assertTrue(firstClientChannel.isClosedForReceive)

        val hostEvent = DummyRequest(sessionId, TestHostPayload("host data"))
        echoSessionManager.handleHostEvent(hostEvent)
        runBlocking {
            assertEquals(hostEvent.payload, hostChannel.receive().payload)
            assertTrue(secondClientChannel.isEmpty)
        }
    }

    data class DummyRequest(
        override val session: SessionId,
        val payload: DummyPayload
    ): RequestEvent {
        override val type: String = payload.type
    }

    data class DummyResponse(
        val payload: DummyPayload
    ): ResponseEvent {
        override val state: String = payload.type
    }

    sealed interface DummyPayload {
        val type: String
    }

    data class TestHostPayload(val data: String): DummyPayload {
        override val type: String = "MAIN_BOARD"
    }
    data class TestClientPayload(val data: String): DummyPayload {
        override val type: String = "MAIN_BOARD"
    }
}