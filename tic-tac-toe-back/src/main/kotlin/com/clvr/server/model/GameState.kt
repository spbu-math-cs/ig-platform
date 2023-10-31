package com.clvr.server.model

import com.clvr.server.common.QuizQuestion
import com.clvr.server.common.Quiz
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CellContent {
    NOT_OPENED, X, O, EMPTY
}

data class CellState(var hintsUsed: Int, var content: CellContent)

typealias GridState = Array<Array<CellState>>

enum class GameResult { UNKNOWN, X_WIN, O_WIN }

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
        CellContent.X -> GameResult.X_WIN
        CellContent.O -> GameResult.O_WIN
        else          -> throw IllegalArgumentException()
    }

class GameState(private val quiz: Quiz) {
    private val state: GridState = Array(quiz.gridSide) {
        Array(quiz.gridSide) {
            CellState(0, CellContent.NOT_OPENED)
        }
    }

    var turn: Player = Player.X
        private set

    private fun getKthHint(row: Int, column: Int, id: Int) = 
        quiz.questions[row][column].hints.getOrNull(id)

    fun isCellValid(row: Int, column: Int): Boolean = 
        row >= 0 && row < quiz.gridSide && column >= 0 && column < quiz.gridSide

    fun getSide(): Int = quiz.gridSide

    fun getTemplateAuthor(): String? = quiz.templateAuthor

    fun getTemplateTitle(): String? = quiz.templateTitle

    fun getQuestionTopic(row: Int, column: Int) =
        quiz.questions[row][column].topic

    fun getQuestionStatement(row: Int, column: Int): String {
        return quiz.questions[row][column].statement
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

    fun getQuestionAnswer(row: Int, column: Int) = 
        quiz.questions[row][column].answer

    fun changeQuestion(row: Int, column: Int, newQuestion: QuizQuestion) {
        quiz.questions[row][column] = newQuestion
    }
    
    fun getNextHint(row: Int, column: Int): String? {
        return getKthHint(row, column, state[row][column].hintsUsed++)
    }

    fun getGridContent(): List<List<CellContent>> =
        state.map { row ->
            row.map {
               it.content
            }
        }

    fun updateCellContent(row: Int, column: Int, newContent: CellContent): GameResult {
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

        for (row in 0 until quiz.gridSide) {
            for (column in 0 until quiz.gridSide) {
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
                        for (i in 0 until quiz.gridSide) {
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

        if (cntX + cntO < quiz.gridSide * quiz.gridSide) {
            return GameResult.UNKNOWN
        }

        return if (cntX > cntO) GameResult.X_WIN else GameResult.O_WIN
    }

    fun isGameEnded(): Boolean = currentResult() != GameResult.UNKNOWN
}