package com.clvr.platform.impl

import com.clvr.platform.api.ClvrGameController
import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionParticipantsCommunicator
import com.clvr.platform.impl.lobby.EnterLobbyEvent
import com.clvr.platform.impl.lobby.LobbyRequestEvent
import com.clvr.platform.impl.lobby.LobbyResponseEvent
import com.clvr.platform.impl.lobby.Player
import com.clvr.platform.impl.lobby.PlayersInfo
import com.clvr.platform.impl.lobby.StartGameEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking

internal class SessionManager<Req: RequestEvent, Resp: ResponseEvent>(
    private val clvrGameController: ClvrGameController<Req, Resp>
): AutoCloseable, SessionParticipantsCommunicator<ResponseEvent> {
    val hostChannel: Channel<ResponseEvent> = Channel(Channel.UNLIMITED)
    private val clientChannels: MutableMap<String, Channel<ResponseEvent>> = mutableMapOf()
    private val lobbyGameController = LobbyGameController()

    var gameStarted: Boolean = false
        private set

    fun handleHostEvent(event: Req) {
        clvrGameController.handle(this, event)
    }

    fun handleClientEvent(clientEndpoint: String, event: Req) {
        clvrGameController.handleFromClient(this, clientEndpoint, event)
    }

    fun handleHostLobbyEvent(event: LobbyRequestEvent) {
        lobbyGameController.handle(this, event)
    }

    fun handleClientLobbyEvent(clientEndpoint: String, event: LobbyRequestEvent) {
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

    fun registerClient(clientEndpoint: String): ReceiveChannel<ResponseEvent> {
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


    internal inner class LobbyGameController: ClvrGameController<LobbyRequestEvent, LobbyResponseEvent> {
        private val players: PlayersInfo
            get() = PlayersInfo(
                clientChannels.keys.toList().map { Player(it) }
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
}