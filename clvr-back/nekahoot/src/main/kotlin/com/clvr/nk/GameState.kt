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

    private var currentPlayersAnswers: MutableMap<String, Pair<Long, String>> = mutableMapOf()
    private var currentQuestionNumber: Int = 0
    private var gameStarted: Boolean = false
    private var isQuestionOpened: Boolean = false
    private var questionOpenTimestamp: Long = 0

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

    fun getTemplateTitle(): String? =
        template.templateTitle

    fun getTemplateComment(): String? =
        template.templateComment

    fun getTemplateAuthor(): String? =
        template.templateAuthor

    private fun updatePlayerResults() {
        currentPlayersAnswers.forEach { (playerName, answer) ->
            val (timestamp, answerText) = answer
            val isCorrect = answerText == getAnswer()
            val (score, correctQuestions) = playerResults.getOrPut(playerName) { 0 to 0 }
            val newScore = score + if (isCorrect) 1000 - (timestamp - questionOpenTimestamp).toInt() / getTime() else 0
            val newCorrectQuestions = correctQuestions + if (isCorrect) 1 else 0
            playerResults[playerName] = newScore to newCorrectQuestions
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
        currentQuestionNumber++
    }

    fun openQuestion(timestamp: Long) {
        isQuestionOpened = true
        questionOpenTimestamp = timestamp
    }

    fun closeQuestion() {
        isQuestionOpened = false
        updatePlayerResults()
        currentPlayersAnswers = mutableMapOf()
    }

    fun isQuestionOpened(): Boolean =
        isQuestionOpened

    fun getNumberOfAnswers(): Int =
        currentPlayersAnswers.size

    fun getAnswerOfPlayer(playerName: String?): String =
        currentPlayersAnswers[playerName]?.second ?: ""

    fun answerQuestion(timestamp: Long, playerName: String, answer: String) {
        currentPlayersAnswers[playerName] = timestamp to answer
    }
}