package com.clvr.nk

import com.clvr.nk.common.NeKahootTemplate
import kotlinx.serialization.Serializable

@Serializable
data class PlayerResult(
    val player_name: String,
    val score: Int,
    val correct_questions: Int,
)

class GameState(private val template: NeKahootTemplate) {
    var playerResults: MutableMap<String, Pair<Int, Int>> = mutableMapOf()

    private var currentPlayersAnswers: MutableMap<String, String> = mutableMapOf()
    private var currentQuestionNumber: Int = 0
    private var gameStarted: Boolean = false
    private var isQuestionOpened: Boolean = false

    fun isGameFinished(): Boolean =
        currentQuestionNumber == template.questions.size

    fun startGame() {
        gameStarted = true
    }

    fun getQuestion(): String =
        template.questions[currentQuestionNumber].question

    fun getAnswer(): String =
        template.questions[currentQuestionNumber].answer

    fun getAnswerDescription(): String? =
        template.questions[currentQuestionNumber].answer_description

    fun getAnswerOptions(): List<String> =
        template.questions[currentQuestionNumber].answer_options

    fun getTime(): Int =
        template.questions[currentQuestionNumber].time

    private fun updatePlayerResults() {
        currentPlayersAnswers.forEach { (playerName, answer) ->
            val (score, correctQuestions) = playerResults[playerName] ?: Pair(0, 0)
            if (answer == getAnswer()) {
                playerResults[playerName] = Pair(score + 100, correctQuestions + 1)
            } else {
                playerResults[playerName] = Pair(score, correctQuestions)
            }
        }
    }

    fun getResults(): List<PlayerResult> =
        playerResults.map {(playerName, playerData) ->
            PlayerResult(playerName, playerData.first, playerData.second)
        }.sortedByDescending { it.score }

    fun nextQuestion() {
        if (isGameFinished() || !gameStarted) {
            return
        }
        updatePlayerResults()
        currentPlayersAnswers = mutableMapOf()
        currentQuestionNumber++
    }

    fun openQuestion() {
        isQuestionOpened = true
    }

    fun closeQuestion() {
        isQuestionOpened = false
    }

    fun isQuestionOpened(): Boolean =
        isQuestionOpened

    fun getNumberOfAnswers(): Int =
        currentPlayersAnswers.size

    fun getAnswerOfPlayer(playerName: String?): String =
        currentPlayersAnswers[playerName] ?: ""

    fun answerQuestion(playerName: String, answer: String) {
        currentPlayersAnswers[playerName] = answer
    }
}