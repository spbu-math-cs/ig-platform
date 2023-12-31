package com.clvr.ttt

import com.clvr.ttt.common.Config
import com.clvr.ttt.common.OpenMultipleQuestions
import com.clvr.ttt.common.TemplateQuestion
import com.clvr.ttt.common.ReplaceMarks
import com.clvr.ttt.common.TicTacToeTemplate
import kotlinx.serialization.Serializable

@Serializable
enum class CellContent {
    NOT_OPENED, X, O, EMPTY
}

data class CellState(var hintsUsed: Int, var content: CellContent)

typealias GridState = Array<Array<CellState>>

@Serializable
enum class GameResult { EMPTY, X, O }

@Serializable
enum class Player { X, O }

private fun oppositePlayer(player: Player) = if (player == Player.X) Player.O else Player.X

private fun getPlayerByContent(content: CellContent): Player =
    when (content) {
        CellContent.X -> Player.X
        CellContent.O -> Player.O
        else          -> throw IllegalArgumentException()
    }

private fun getResultByContent(content: CellContent): GameResult =
    when (content) {
        CellContent.X -> GameResult.X
        CellContent.O -> GameResult.O
        else          -> throw IllegalArgumentException()
    }

class GameState(private val template: TicTacToeTemplate, private val config: Config) {
    private val state: GridState = Array(template.gridSide) {
        Array(template.gridSide) {
            CellState(0, CellContent.NOT_OPENED)
        }
    }

    var turn: Player = Player.X
        private set

    var currentQuestionPosition: Pair<Int, Int>? = null
        private set

    private fun ensureCellIsOpened(row: Int, column: Int) {
        val prevPosition = currentQuestionPosition ?: Pair(row, column)

        if (state[row][column].content == CellContent.NOT_OPENED && config.openMultipleQuestions == OpenMultipleQuestions.DISABLED) {
            if (prevPosition.first != row || prevPosition.second != column) {
                throw MultipleQuestionsOpeningException()
            }
        }

        if (state[row][column].content != CellContent.NOT_OPENED) {
            throw OpenAlreadyMarkedQuestionException()
        }

        // TODO: maybe we should do
        //   state[row][column].content = CellContent.EMPTY
        //  here?
        currentQuestionPosition = Pair(row, column)
    }

    fun getOpenedHints(row: Int, column: Int): List<String> {
        ensureCellIsOpened(row, column)
        return template.questions[row][column].hints.take(state[row][column].hintsUsed)
    }

    fun getAllHints(row: Int, column: Int): List<String> {
        ensureCellIsOpened(row, column)
        return template.questions[row][column].hints
    }

    fun isCellValid(row: Int, column: Int): Boolean = 
        row >= 0 && row < template.gridSide && column >= 0 && column < template.gridSide

    fun getSide(): Int = template.gridSide

    fun getTemplateAuthor(): String? = template.templateAuthor

    fun getTemplateTitle(): String? = template.templateTitle

    fun getTemplateComment(): String? = template.templateComment

    fun getQuestionTopic(row: Int, column: Int) =
        template.questions[row][column].topic

    fun getQuestionStatement(row: Int, column: Int): String {
        ensureCellIsOpened(row, column)
        return template.questions[row][column].statement
//        if (state[row][column].hintsUsed == 0) {
//            state[row][column].hintsUsed++
//            return quiz.questions[row][column].statement
//        } else {
//            val hint = getKthHint(row, column, state[row][column].hintsUsed - 1)
//            if (hint == null) {
//                state[row][column].hintsUsed = 0
//                return quiz.questions[row][column].answer
//            }
//            state[row][column].hintsUsed++
//            return hint
//        }
    }

    fun getQuestionAnswer(row: Int, column: Int): String {
        ensureCellIsOpened(row, column)
        return template.questions[row][column].answer
    }

    fun changeQuestion(row: Int, column: Int, newQuestion: TemplateQuestion) {
        template.questions[row][column] = newQuestion
    }
    
    fun openNextHint(row: Int, column: Int): Boolean {
        ensureCellIsOpened(row, column)

        if (state[row][column].hintsUsed == template.questions[row][column].hints.size) {
            return false
        }
        ++state[row][column].hintsUsed
        return true
    }

    fun getGridContent(): List<List<CellContent>> =
        state.map { row ->
            row.map {
               it.content
            }
        }

    fun updateCellContent(row: Int, column: Int, newContent: CellContent): GameResult {
        require(newContent != CellContent.NOT_OPENED)

        if (config.replaceMarks == ReplaceMarks.DISABLED) {
            if (state[row][column].content != CellContent.NOT_OPENED) {
                throw IllegalCellContentException()
            }
        }

        ensureCellIsOpened(row, column)
        currentQuestionPosition = null

        state[row][column].content = newContent
        if (newContent == CellContent.EMPTY) {
            turn = oppositePlayer(turn)
        } else {
            turn = getPlayerByContent(newContent)
            state.forEach {
                    currentRow -> currentRow.forEach {
                if (it.content == CellContent.EMPTY) {
                    it.content = newContent
                }
            }
            }
        }

        //TODO: store rollbacks

        return currentResult()
    }

    fun currentResult(): GameResult {
        var cntX: Int = 0
        var cntO: Int = 0

        for (row in 0 until template.gridSide) {
            for (column in 0 until template.gridSide) {
                if (state[row][column].content == CellContent.EMPTY || state[row][column].content == CellContent.NOT_OPENED) {
                    continue
                }

                if (state[row][column].content == CellContent.X) {
                    cntX++
                } else {
                    cntO++
                }

                for (deltaRow in -1..1) {
                    for (deltaCol in -1..1) {
                        if (deltaRow == 0 && deltaCol == 0) {
                            continue
                        }

                        var ok: Boolean = true
                        for (i in 0 until template.gridSide) {
                            if (!isCellValid(row + deltaRow * i, column + deltaCol * i) ||
                                state[row + deltaRow * i][column + deltaCol * i].content != state[row][column].content) {

                                ok = false
                                break
                            }
                        }

                        if (ok) {
                            return getResultByContent(state[row][column].content)
                        }
                    }
                }
            }
        }

        if (cntX + cntO < template.gridSide * template.gridSide) {
            return GameResult.EMPTY
        }

        return if (cntX > cntO) GameResult.X else GameResult.O
    }

    fun isGameEnded(): Boolean = currentResult() != GameResult.EMPTY
}