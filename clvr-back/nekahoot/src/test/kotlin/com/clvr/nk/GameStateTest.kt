package com.clvr.nk

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameStateTest {
    private val quiz = basicTestTemplate

    @Test
    fun testStartFinishGame() {
        val gameState = GameState(quiz)
        val numberOfQuestions = quiz.questions.size

        Assertions.assertFalse(gameState.isGameFinished())
        gameState.startGame()

        for (i in 1..numberOfQuestions) {
            Assertions.assertFalse(gameState.isGameFinished())
            gameState.nextQuestion()
        }

        Assertions.assertTrue(gameState.isGameFinished())
    }

    @Test
    fun testGetters() {
        val gameState = GameState(quiz)
        gameState.startGame()

        Assertions.assertEquals("q1", gameState.getQuestion())
        Assertions.assertEquals("opt2", gameState.getAnswer())
        Assertions.assertEquals(null, gameState.getAnswerDescription())
        Assertions.assertEquals(listOf("opt1", "opt2", "opt3"), gameState.getAnswerOptions())
        Assertions.assertEquals(1000, gameState.getTime())
        Assertions.assertEquals(1000, gameState.getLeftTime(0L))
        Assertions.assertEquals("template title", gameState.getTemplateTitle())
        Assertions.assertEquals("template comment", gameState.getTemplateComment())
        Assertions.assertEquals("frungl", gameState.getTemplateAuthor())
    }

    @Test
    fun testOrder() {
        val gameState = GameState(quiz)
        gameState.startGame()

        Assertions.assertEquals("q1", gameState.getQuestion())
        gameState.nextQuestion()
        Assertions.assertEquals("q2", gameState.getQuestion())
        gameState.nextQuestion()
        Assertions.assertEquals("q3", gameState.getQuestion())
    }

    @Test
    fun testQuestionTimeline() {
        val gameState = GameState(quiz)
        gameState.startGame()
        Assertions.assertFalse(gameState.isQuestionOpened())

        val start = 0L
        gameState.openQuestion(start)
        Assertions.assertTrue(gameState.isQuestionOpened())
        Assertions.assertEquals(0, gameState.getNumberOfAnswers())
        Assertions.assertEquals("", gameState.getAnswerOfPlayer("player1"))
        Assertions.assertEquals("", gameState.getAnswerOfPlayer("player2"))
        Assertions.assertEquals(1000, gameState.getLeftTime(start))

        gameState.answerQuestion(start + 533, "player1", "opt1")
        Assertions.assertEquals(1, gameState.getNumberOfAnswers())
        Assertions.assertEquals("opt1", gameState.getAnswerOfPlayer("player1"))
        Assertions.assertEquals("", gameState.getAnswerOfPlayer("player2"))
        Assertions.assertEquals(467, gameState.getLeftTime(start + 533))

        gameState.answerQuestion(start + 2000, "player2", "opt2")
        Assertions.assertEquals(2, gameState.getNumberOfAnswers())
        Assertions.assertEquals("opt1", gameState.getAnswerOfPlayer("player1"))
        Assertions.assertEquals("opt2", gameState.getAnswerOfPlayer("player2"))
        Assertions.assertEquals(0, gameState.getLeftTime(start + 2000))

        gameState.closeQuestion()
        Assertions.assertFalse(gameState.isQuestionOpened())
    }

    @Test
    fun testResults() {
        val gameState = GameState(quiz)
        gameState.startGame()

        var currentTime = 1L
        gameState.openQuestion(currentTime)
        gameState.answerQuestion(currentTime + 0, "player2", "opt2")
        gameState.answerQuestion(currentTime + 500, "player1", "opt2")
        gameState.answerQuestion(currentTime + 421, "player3", "opt1")
        gameState.closeQuestion()

        val results = gameState.getResults()
        val expectedResults = listOf(
            PlayerResult("player2", 1000, 1),
            PlayerResult("player1", 500, 1),
            PlayerResult("player3", 0, 0)
        )
        Assertions.assertEquals(expectedResults, results)

        gameState.nextQuestion()
        currentTime += 53323930L
        gameState.openQuestion(currentTime)
        gameState.answerQuestion(currentTime + 0, "player4", "opt1")
        gameState.answerQuestion(currentTime + 1500, "player3", "opt1")
        gameState.answerQuestion(currentTime + 2000, "player2", "opt1")
        gameState.closeQuestion()

        val results2 = gameState.getResults()
        val expectedResults2 = listOf(
            PlayerResult("player2", 1000, 2),
            PlayerResult("player4", 1000, 1),
            PlayerResult("player1", 500, 1),
            PlayerResult("player3", 250, 1)
        )
        Assertions.assertEquals(expectedResults2, results2)
    }
}