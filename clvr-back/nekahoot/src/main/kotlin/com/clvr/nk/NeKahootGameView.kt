package com.clvr.nk

import com.clvr.platform.api.ClvrGameView

class NeKahootGameView(private val game: GameState): ClvrGameView<NeKahootRequest, NeKahootResponseWithPayload<*>> {
    private val lastQuestionView: Pair<HostQuestionView, ClientQuestionView>?
        get() {
            if (game.isGameFinished()) {
                return null
            }
            val timestamp = System.currentTimeMillis()
            val hostQuestionView = HostQuestionView.fromGameState(game, timestamp)
            val clientQuestionView = ClientQuestionView.fromGameState(game, timestamp)
            return hostQuestionView to clientQuestionView
        }

    private val resultsEvent: ResultsEvent?
        get() {
            if (!game.isGameFinished()) {
                return null
            }
            val results = game.getResults()
            return ResultsEvent(results)
        }

    override val hostView: List<NeKahootResponseWithPayload<*>>
        get() {
            val lastQuestionViewEvent = lastQuestionView?.let {
                NeKahootResponseWithPayload(HostQuestionResponse(it.first))
            }
            val resultsEvent = resultsEvent?.let { NeKahootResponseWithPayload(it) }

            return listOfNotNull(lastQuestionViewEvent, resultsEvent)
        }

    override val clientView: List<NeKahootResponseWithPayload<*>>
        get() {
            val lastQuestionViewEvent = lastQuestionView?.let {
                NeKahootResponseWithPayload(ClientQuestionResponse(it.second))
            }
            val resultsEvent = resultsEvent?.let { NeKahootResponseWithPayload(it) }

            return listOfNotNull(lastQuestionViewEvent, resultsEvent)
        }
}