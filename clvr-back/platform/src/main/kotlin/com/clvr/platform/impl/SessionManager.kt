package com.clvr.platform.impl

import com.clvr.platform.api.ClvrGameController
import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionParticipantsCommunicator
import com.clvr.platform.api.lobby.EnterLobbyEvent
import com.clvr.platform.api.lobby.LobbyRequestEvent
import com.clvr.platform.api.lobby.LobbyResponseEvent
import com.clvr.platform.api.lobby.Player
import com.clvr.platform.api.lobby.PlayersInfo
import com.clvr.platform.api.lobby.StartGameEvent
import com.clvr.platform.api.model.UserInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

internal class SessionManager<Req: RequestEvent, Resp: ResponseEvent>(
    private val clvrGameController: ClvrGameController<Req, Resp>
): AutoCloseable, SessionParticipantsCommunicator<ResponseEvent> {
    val hostChannel: Channel<ResponseEvent> = Channel(Channel.UNLIMITED)
    private val clientChannels: MutableMap<String, Channel<ResponseEvent>> = mutableMapOf()
    private val clientInfo: MutableMap<String, UserInfo> = mutableMapOf()
    private val lobbyGameController = LobbyGameController()

    var gameStarted: Boolean = false
        private set

    private fun checkGameStarted() {
        if (!gameStarted) {
            logger.warn { "Unexpected game-specific event when game is not started" }
        }
    }

    private fun checkGameNotStarted() {
        if (gameStarted) {
            logger.warn { "Unexpected lobby event when game is started" }
        }
    }

    fun handleHostEvent(event: Req) {
        checkGameStarted()
        clvrGameController.handle(this, event)
    }

    fun handleClientEvent(clientEndpoint: String, event: Req) {
        checkGameStarted()
        clvrGameController.handleFromClient(this, clientEndpoint, event)
    }

    fun handleHostLobbyEvent(event: LobbyRequestEvent) {
        checkGameNotStarted()
        lobbyGameController.handle(this, event)
    }

    fun handleClientLobbyEvent(clientEndpoint: String, event: LobbyRequestEvent) {
        checkGameNotStarted()
        lobbyGameController.handleFromClient(this, clientEndpoint, event)
    }

    override fun sendToClient(clientEndpoint: String, event: ResponseEvent) {
        synchronized(clientChannels) {
            runBlocking {
                clientChannels[clientEndpoint]?.send(event)
            }
        }
    }

    override fun sendToClients(event: ResponseEvent) {
        synchronized(clientChannels) {
            runBlocking {
                clientChannels.values.forEach { clientChannel ->
                    clientChannel.send(event)
                }
            }
        }
    }

    override fun sendToHost(event: ResponseEvent) {
        runBlocking {
            hostChannel.send(event)
        }
    }

    fun registerClient(clientEndpoint: String, userInfo: UserInfo?): ReceiveChannel<ResponseEvent> {
        synchronized(clientChannels) {
            val userInfoOrGenerated = userInfo ?: UserInfo(
                uuid = UUID.randomUUID(),  // TODO: assigning random uuid is a little bit bullshit tbh
                name = "Unknown user #${clientEndpoint.hashCode() % 10000}"
            )
            clientInfo.putIfAbsent(clientEndpoint, userInfoOrGenerated)
            return clientChannels.computeIfAbsent(clientEndpoint) { Channel(Channel.UNLIMITED) }
        }
    }

    fun unregisterClient(clientEndpoint: String) {
        synchronized(clientChannels) {
            clientChannels[clientEndpoint]?.close()
            clientChannels.remove(clientEndpoint)
            clientInfo.remove(clientEndpoint)
        }
    }

    override fun close() {
        synchronized(clientChannels) {
            clientChannels.values.forEach { channel -> channel.close() }
            clientChannels.clear()
        }

        hostChannel.close()
    }


    internal inner class LobbyGameController: ClvrGameController<LobbyRequestEvent, LobbyResponseEvent> {
        override val activityName: String
                 get() = error("Should never be called")
        private val players: PlayersInfo
            get() = PlayersInfo(
                clientChannels.keys.toList().map { Player( getClientInfo(it)?.name ?: "unknown user" ) }
            )

        override fun handleGameStart(communicator: SessionParticipantsCommunicator<LobbyResponseEvent>) {
            error("Unexpected event for lobby game controller: game start")
        }

        override fun handle(
            communicator: SessionParticipantsCommunicator<LobbyResponseEvent>,
            event: LobbyRequestEvent
        ) {
            when (event) {
                is EnterLobbyEvent -> communicator.sendToHost(LobbyResponseEvent(players))
                is StartGameEvent -> {
                    gameStarted = true
                    clvrGameController.handleGameStart(this@SessionManager)
                }
            }
        }

        override fun handleFromClient(
            communicator: SessionParticipantsCommunicator<LobbyResponseEvent>,
            clientEndpoint: String,
            event: LobbyRequestEvent
        ) {
            when (event) {
                is EnterLobbyEvent -> {
                    communicator.sendToClients(LobbyResponseEvent(players))
                    communicator.sendToHost(LobbyResponseEvent(players))
                }
                is StartGameEvent -> error("Client doesn't have permission to start game")
            }
        }
    }

    override fun getClientInfo(clientEndpoint: String): UserInfo? {
        if (!clientInfo.containsKey(clientEndpoint)) {
            logger.error { "Got unexpected endpoint without UserInfo -- $clientEndpoint" }
     }
     return clientInfo[clientEndpoint]
    }
}