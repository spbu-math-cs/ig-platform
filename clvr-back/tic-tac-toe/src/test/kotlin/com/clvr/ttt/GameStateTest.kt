package com.clvr.ttt

import com.clvr.ttt.common.Config
import com.clvr.ttt.common.OpenMultipleQuestions
import com.clvr.ttt.common.TemplateQuestion
import com.clvr.ttt.common.ReplaceMarks
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class GameStateTest {
    private val quiz = basicTestTemplate
    private val config = Config(
        replaceMarks = ReplaceMarks.ENABLED,
        openMultipleQuestions = OpenMultipleQuestions.ENABLED
    )

    @Test
    fun testTurn() {
        val gameState = GameState(quiz, config)
        
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
        val gameState = GameState(quiz, config)

        assertEquals(true, gameState.isCellValid(0, 0))
        assertEquals(true, gameState.isCellValid(1, 1))
        assertEquals(false, gameState.isCellValid(-1, 0))
        assertEquals(false, gameState.isCellValid(2, 0))
        assertEquals(false, gameState.isCellValid(0, -1))
        assertEquals(false, gameState.isCellValid(0, 2))
    }

    @Test
    fun testGetters() {
        val gameState = GameState(quiz, config)

        assertEquals(2, gameState.getSide())
        assertEquals("unstoppablechillmachine", gameState.getTemplateAuthor())
        assertEquals(null, gameState.getTemplateTitle())
        assertEquals("template comment", gameState.getTemplateComment())
        assertEquals("kek", gameState.getQuestionTopic(1, 1))
        assertEquals("s1", gameState.getQuestionStatement(0, 0))
        assertEquals("a2", gameState.getQuestionAnswer(0, 1))
        
    } 

    @Test 
    fun testHints() {
        val gameState = GameState(quiz, config)

        assertEquals(false, gameState.openNextHint(0, 0))
        assertEquals(emptyList<String>(), gameState.getOpenedHints(0, 0))
        assertEquals(true, gameState.openNextHint(1, 0))
        assertEquals(listOf("hint hint hint"), gameState.getOpenedHints(1, 0))
        assertEquals(false, gameState.openNextHint(1, 0))
        assertEquals(listOf("hint hint hint"), gameState.getOpenedHints(1, 0))
    }

    @Test
    fun testChangeQuestion() {
        val gameState = GameState(quiz, config)
        
        gameState.changeQuestion(1, 1, TemplateQuestion("a", "a", "a", listOf()))
        assertEquals("a", gameState.getQuestionAnswer(1, 1))
    }

    @Test
    fun testGridStateSimple() {
        for (content in listOf(CellContent.X, CellContent.O)) {
            val result = if (content == CellContent.X) GameResult.X else GameResult.O
            for (x in 0..1) {
                for (y in 0..1) {
                    for (dx in 0..1) {
                        for (dy in 0..1) {
                            if (dx == 0 && dy == 0) {
                                continue
                            }

                            val gameState = GameState(quiz, config)

                            assertEquals(false, gameState.isGameEnded())
                            assertEquals(GameResult.EMPTY, gameState.currentResult())
                            assertEquals(listOf(
                                listOf(CellContent.NOT_OPENED, CellContent.NOT_OPENED),
                                listOf(CellContent.NOT_OPENED, CellContent.NOT_OPENED)
                            ), gameState.getGridContent())


                            gameState.updateCellContent(x, y, content)
                            assertEquals(false, gameState.isGameEnded())
                            assertEquals(GameResult.EMPTY, gameState.currentResult())


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
        val gameState = GameState(quiz, config)
        gameState.updateCellContent(0, 1, CellContent.EMPTY)
        gameState.updateCellContent(1, 0, CellContent.X)
        assertEquals(GameResult.X, gameState.currentResult())
    }

    @Test
    fun `open multiple questions`() {
        val gameState = GameState(quiz, Config())

        gameState.getQuestionStatement(0, 0)

        assertThrows(MultipleQuestionsOpeningException::class.java) {
            gameState.getQuestionStatement(1, 1)
        }
    }

    @Test
    fun `revisit played question`() {
        val gameState = GameState(quiz, Config())

        gameState.getQuestionStatement(0, 0)
        gameState.updateCellContent(0, 0, CellContent.X)
        gameState.getQuestionStatement(1, 1)

        assertThrows(OpenAlreadyMarkedQuestionException::class.java) {
            gameState.getQuestionStatement(0, 0)
        }
    }

    @Test
    fun `change result in non-empty cell`() {
        val gameState = GameState(quiz, Config())

        gameState.updateCellContent(0, 0, CellContent.X)

        assertDoesNotThrow {
            gameState.updateCellContent(1, 1, CellContent.O)
        }

        assertThrows(IllegalCellContentException::class.java) {
            gameState.updateCellContent(1, 1, CellContent.X)
        }
    }
}