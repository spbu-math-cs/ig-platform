package com.clvr.ttt

import com.clvr.platform.api.SessionParticipantsCommunicator
import com.clvr.platform.api.ClvrGameController
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

typealias TicTacToeSessionParticipantsCommunicator =
    SessionParticipantsCommunicator<TicTacToeResponse<*>>

class TicTacToeGameController(private val game: GameState) :
    ClvrGameController<TicTacToeRequest, TicTacToeResponse<*>> {
    private val endpointToTeam: MutableMap<String, Player> = mutableMapOf()

    override val activityName: String = "tic-tac-toe"

    private fun sendQuestionResponses(manager: TicTacToeSessionParticipantsCommunicator, row: Int, column: Int) {
        val hostQuestionView = HostQuestionView.fromGameState(game, row, column)
        val clientQuestionView = ClientQuestionView.fromGameState(game, row, column)

        val hostResponse = TicTacToeResponse(
            HostQuestionResponse(hostQuestionView, BoardView.fromGameState(game))
        )
        val clientResponse = TicTacToeResponse(
            ClientQuestionResponse(clientQuestionView, BoardView.fromGameState(game))
        )
        manager.sendToHost(hostResponse)
        manager.sendToClients(clientResponse)
    }

    override fun handle(communicator: SessionParticipantsCommunicator<TicTacToeResponse<*>>, event: TicTacToeRequest) {
        try {
            if (event is PressButtonRequest) {
                communicator.sendToHost(
                    TicTacToeResponse(
                        GameError("Host shouldn't press buttons")
                    )
                )
                return
            }

            event as TicTacToeRequestWithPayload<*>

            when (val payload = event.payload) {
                is QuestionRequest -> {
                    val (row, column) = payload
                    sendQuestionResponses(communicator, row, column)
                }
                is NextHintRequest -> {
                    val (row, column) = payload
                    game.openNextHint(row, column)
                    sendQuestionResponses(communicator, row, column)
                }
                is ShowAnswerRequest -> {
                    val (row, column) = payload
                    val question = game.getQuestionStatement(row, column)
                    val answer = game.getQuestionAnswer(row, column)
                    val questionWithAnswer = QuestionWithAnswer(row, column, question, answer)
                    val response = TicTacToeResponse(
                        ShowAnswerResponse(questionWithAnswer, BoardView.fromGameState(game))
                    )
                    communicator.sendToHost(response)
                    communicator.sendToClients(response)
                }
                is SetFieldRequest -> {
                    val (row, column, mark) = payload
                    val gameResult = game.updateCellContent(row, column, mark)
                    val response = TicTacToeResponse(
                        SetFieldResponse(gameResult, BoardView.fromGameState(game))
                    )
                    communicator.sendToHost(response)
                    communicator.sendToClients(response)
                }
                is SelectTeamRequest -> {
                    communicator.sendToHost(
                        TicTacToeResponse(
                            GameError("Server shouldn't select team")
                        )
                    )
                }
            }
        } catch (e: IllegalGameActionException) {
            communicator.sendToHost(
                TicTacToeResponse(
                    GameError(e.message ?: "Unknown error occurred!")
                )
            )
        }
    }

    override fun handleFromClient(
        communicator: SessionParticipantsCommunicator<TicTacToeResponse<*>>,
        clientEndpoint: String,
        event: TicTacToeRequest
    ) {
        if (event is PressButtonRequest) {
            val (row, column) = game.currentQuestionPosition ?: run {
                communicator.sendToHost(
                    TicTacToeResponse(
                        GameError("No opened question")
                    )
                )
                return
            }
            val hostQuestionView = HostQuestionView.fromGameState(game, row, column)
            val team = endpointToTeam[clientEndpoint] ?: run {
                logger.error { "Unknown team for user $clientEndpoint, defaulting to X" }
                Player.X
            }
            val response = TicTacToeResponse(
                PressButtonResponse(
                    hostQuestionView,
                    BoardView.fromGameState(game),
                    team
                )
            )
            // Ideally, there should be API to notify other users that someone has answered
            communicator.sendToHost(response)
            return
        }

        event as TicTacToeRequestWithPayload<*>

        when (event.payload) {
            is SelectTeamRequest -> {
                endpointToTeam[clientEndpoint] = event.payload.team
                val playerName = communicator.getClientInfo(clientEndpoint)?.name
                    ?: "unknown user"
                val response = TicTacToeResponse(
                    SelectTeamResponse(playerName, event.payload.team)
                )
                communicator.sendToHost(response)
                communicator.sendToClients(response)
            }
            else -> {
                communicator.sendToClient(clientEndpoint,
                    TicTacToeResponse(
                        GameError("You are not allowed to send events to the server!")
                    )
                )
            }
        }
    }

    override fun handleGameStart(communicator: SessionParticipantsCommunicator<TicTacToeResponse<*>>) {
        val gameResult = game.currentResult()
        val response = TicTacToeResponse(
            SetFieldResponse(gameResult, BoardView.fromGameState(game))
        )
        communicator.sendToHost(response)
        communicator.sendToClients(response)
    }
}