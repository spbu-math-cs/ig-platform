package com.clvr.platform.impl

import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionId
import com.clvr.platform.api.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SessionManagerTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `session manager simple test`() {
        val sessionId = SessionId("1")

        val echoSessionManager = SessionManager(
            object: ClvrGameController<DummyRequest, DummyResponse> {
                override fun handle(communicator: SessionParticipantsCommunicator<DummyResponse>, event: DummyRequest) {
                    when (event.payload) {
                        is TestHostPayload -> communicator.sendToHost(DummyResponse(event.payload))
                        is TestClientPayload -> communicator.sendToClients(DummyResponse(event.payload))
                    }
                }
                override fun handleFromClient(communicator: SessionParticipantsCommunicator<DummyResponse>, clientEndpoint: String, event: DummyRequest) {
                    communicator.sendToClient(clientEndpoint, DummyResponse(event.payload))
                }

                override fun handleGameStart(communicator: SessionParticipantsCommunicator<DummyResponse>) = Unit
            }
        )

        val firstClientChannel = echoSessionManager.registerClient("client-1")
        val hostChannel = echoSessionManager.hostChannel

        var clientEvent = DummyRequest(sessionId, TestClientPayload("client-1 data"))

        echoSessionManager.handleHostEvent(clientEvent)
        runBlocking {
            assertEquals(clientEvent.payload, (firstClientChannel.receive() as DummyResponse).payload)
            assertTrue(hostChannel.isEmpty)
        }

        val secondClientChannel = echoSessionManager.registerClient("client-2")
        clientEvent = DummyRequest(sessionId, TestClientPayload("client-2 data"))
        echoSessionManager.handleHostEvent(clientEvent)
        runBlocking {
            assertEquals(clientEvent.payload, (firstClientChannel.receive() as DummyResponse).payload)
            assertEquals(clientEvent.payload, (secondClientChannel.receive() as DummyResponse).payload)
        }

        clientEvent = DummyRequest(sessionId, TestClientPayload("only to client-2"))
        echoSessionManager.handleClientEvent("client-2", clientEvent)
        runBlocking {
            assertEquals(clientEvent.payload, (secondClientChannel.receive() as DummyResponse).payload)
            assertTrue(firstClientChannel.isEmpty)
        }

        echoSessionManager.unregisterClient("client-1")
        assertTrue(firstClientChannel.isClosedForReceive)

        val hostEvent = DummyRequest(sessionId, TestHostPayload("host data"))
        echoSessionManager.handleHostEvent(hostEvent)
        runBlocking {
            assertEquals(hostEvent.payload, (hostChannel.receive() as DummyResponse).payload)
            assertTrue(secondClientChannel.isEmpty)
        }
    }

    data class DummyRequest(
        override val session: SessionId,
        val payload: DummyPayload
    ): RequestEvent {
        override val type: String = payload.type
    }

    @Serializable
    data class DummyResponse(
        val payload: DummyPayload
    ): ResponseEvent {
        override val state: String = payload.type

        override fun encodeToJson(json: Json): String {
            return json.encodeToString(this)
        }
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