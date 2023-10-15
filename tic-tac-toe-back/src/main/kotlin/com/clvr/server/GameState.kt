package com.clvr.server

data class Question(val topic: String, val statement: String, val answer: String, val hints: List<String>)

typealias GameTemplate = Array<Array<Question>>

//content is empty when both teams answered incorrectly, check rules of the game
enum class CellContent { NOT_OPENED, X, O, EMPTY }

data class CellState(var hintsUsed: Int, var content: CellContent)

typealias GridState = Array<Array<CellState>>

enum class GameResult { UNKNOWN, X_WIN, O_WIN }

enum class Player { X, O }

fun oppositePlayer(player: Player) = if (player == Player.X) Player.O else Player.X 

fun getPlayerByContent(content: CellContent): Player = 
    when (content) {
        CellContent.X -> Player.X
        CellContent.O -> Player.O
        else -> throw IllegalArgumentException()
    }

fun getResultByContent(content: CellContent): GameResult =
    when (content) {
        CellContent.X -> GameResult.X_WIN
        CellContent.O -> GameResult.O_WIN
        else -> throw IllegalArgumentException()
    }

class GameState(private val questions: GameTemplate) {
    val side = questions.size
    
    private val state: GridState = Array(side) {
        Array(side) {
            CellState(0, CellContent.NOT_OPENED)
        }
    }

    var turn: Player = Player.X
        private set

    private fun getKthHint(row: Int, column: Int, id: Int) = 
        questions[row][column].hints[id]

    fun isCellValid(row: Int, column: Int): Boolean = 
        row >= 0 && row < side && column >= 0 && column < side

    fun getQuestionTopic(row: Int, column: Int) =
        questions[row][column].topic

    fun getQuestionStatement(row: Int, column: Int) = 
        questions[row][column].statement

    fun getQuestionAnswer(row: Int, column: Int) = 
        questions[row][column].answer

    fun changeQuestion(row: Int, column: Int, newQuestion: Question) {
        questions[row][column] = newQuestion
    }
    
    fun getNextHint(row: Int, column: Int): String {
        state[row][column].hintsUsed++
        return getKthHint(row, column, state[row][column].hintsUsed)
    }

    fun getGridContent(): List<List<CellContent>> = 
        state.map { row -> row.map { it.content } }

    fun updateCellContent(row: Int, column: Int, newContent: CellContent): GameResult {
        state[row][column].content = newContent
        if (newContent == CellContent.EMPTY) {
            turn = oppositePlayer(turn)
        } else {
            turn = getPlayerByContent(newContent)
            state.forEach { 
                row -> row.forEach { 
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

        for (row in 0 until side) {
            for (column in 0 until side) {
                if (state[row][column].content == CellContent.EMPTY || state[row][column].content == CellContent.NOT_OPENED) {
                    continue
                }

                if (state[row][column].content == CellContent.X) {
                    cntX++
                } else {
                    cntO++
                }

                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr == 0 && dc == 0) {
                            continue
                        }

                        var ok: Boolean = true
                        for (i in 0 until side) {
                            if (!isCellValid(row + dr * i, column + dc * i) || state[row + dr * i][column + dc * i].content != state[row][column].content) {
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

        if (cntX + cntO < side * side) {
            return GameResult.UNKNOWN
        }                    

        return if (cntX > cntO) GameResult.X_WIN else GameResult.O_WIN
    }

    fun isGameEnded(): Boolean = currentResult() != GameResult.UNKNOWN
}