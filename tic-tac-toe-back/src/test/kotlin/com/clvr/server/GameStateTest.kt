package com.clvr.server

import com.clvr.server.common.Quiz
import com.clvr.server.common.QuizId
import com.clvr.server.common.QuizQuestion
import com.clvr.server.model.CellContent
import com.clvr.server.model.GameResult
import com.clvr.server.model.GameState
import com.clvr.server.model.Player
import kotlin.test.*

class GameStateTest {
    private val quiz = Quiz(
        QuizId("random id"),
        arrayOf(
            arrayOf(QuizQuestion("t1", "s1", "a1", listOf()), QuizQuestion("t2", "s2", "a2", listOf("h21", "h22"))),
            arrayOf(QuizQuestion("t3", "s3", "a3", listOf("hint hint hint")), QuizQuestion("kek", "what?", "kek!", listOf("kek1", "kek2", "kek3")))),
        2, 
        null, 
        "unstoppablechillmachine"
    )

    @Test
    fun testTurn() {
        val gameState = GameState(quiz)
        
        assertEquals(Player.X, gameState.turn)
        gameState.updateCellContent(0, 0, CellContent.X)
        assertEquals(Player.X, gameState.turn)
        gameState.updateCellContent(1, 1, CellContent.O)
        assertEquals(Player.O, gameState.turn)
        gameState.updateCellContent(0, 1, CellContent.EMPTY)
        assertEquals(Player.X, gameState.turn)
    }   

    @Test 
    fun testCellValidation() {
        val gameState = GameState(quiz)

        assertEquals(true, gameState.isCellValid(0, 0))
        assertEquals(true, gameState.isCellValid(1, 1))
        assertEquals(false, gameState.isCellValid(-1, 0))
        assertEquals(false, gameState.isCellValid(2, 0))
        assertEquals(false, gameState.isCellValid(0, -1))
        assertEquals(false, gameState.isCellValid(0, 2))
    }

    @Test
    fun testGetters() {
        val gameState = GameState(quiz)

        assertEquals(2, gameState.getSide())
        assertEquals("unstoppablechillmachine", gameState.getTemplateAuthor())
        assertEquals(null, gameState.getTemplateTitle())
        assertEquals("kek", gameState.getQuestionTopic(1, 1))
        assertEquals("s1", gameState.getQuestionStatement(0, 0))
        assertEquals("a2", gameState.getQuestionAnswer(0, 1))
        
    } 

    @Test 
    fun testHints() {
        val gameState = GameState(quiz)

        assertEquals(null, gameState.getNextHint(0, 0))
        assertEquals("hint hint hint", gameState.getNextHint(1, 0))
        assertEquals(null, gameState.getNextHint(1, 0))
    }

    @Test
    fun testChangeQuestion() {
        val gameState = GameState(quiz)
        
        gameState.changeQuestion(1, 1, QuizQuestion("a", "a", "a", listOf()))
        assertEquals("a", gameState.getQuestionAnswer(1, 1))
    }

    @Test
    fun testGridStateSimple() {
        for (content in listOf(CellContent.X, CellContent.O)) {
            val result = if (content == CellContent.X) GameResult.X_WIN else GameResult.O_WIN
            for (x in 0..1) {
                for (y in 0..1) {
                    for (dx in 0..1) {
                        for (dy in 0..1) {
                            if (dx == 0 && dy == 0) {
                                continue;
                            }

                            val gameState = GameState(quiz)

                            assertEquals(false, gameState.isGameEnded())
                            assertEquals(GameResult.UNKNOWN, gameState.currentResult())
                            assertEquals(listOf(
                                listOf(CellContent.NOT_OPENED, CellContent.NOT_OPENED),
                                listOf(CellContent.NOT_OPENED, CellContent.NOT_OPENED)
                            ), gameState.getGridContent())


                            gameState.updateCellContent(x, y, content)
                            assertEquals(false, gameState.isGameEnded())
                            assertEquals(GameResult.UNKNOWN, gameState.currentResult())


                            val gridContent = gameState.getGridContent()
                            for (i in 0..1) {
                                for (j in 0..1) {
                                    if (i == x && j == y) {
                                        assertEquals(content, gridContent[i][j])
                                    } else {
                                        assertEquals(CellContent.NOT_OPENED, gridContent[i][j])
                                    }
                                }
                            }

                            gameState.updateCellContent((x + dx) % 2, (y + dy) % 2, content)
                            assertEquals(true, gameState.isGameEnded())
                            assertEquals(result, gameState.currentResult())

                            val gridContent2 = gameState.getGridContent()
                            for (i in 0..1) {
                                for (j in 0..1) {
                                    if ((i == x && j == y) || (i == (x + dx) % 2 && j == (y + dy) % 2)) {
                                        assertEquals(content, gridContent2[i][j])
                                    } else {
                                        assertEquals(CellContent.NOT_OPENED, gridContent2[i][j])
                                    }
                                }
                            }
                        }
                    }
                    
                }
            }
        }
    }

    @Test 
    fun testGridStateHard() {
        val gameState = GameState(quiz)
        gameState.updateCellContent(0, 1, CellContent.EMPTY)
        gameState.updateCellContent(1, 0, CellContent.X)
        assertEquals(GameResult.X_WIN, gameState.currentResult())
    }
}