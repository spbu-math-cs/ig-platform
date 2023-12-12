package com.clvr.ttt

import com.clvr.platform.api.EventPayloadInterface
import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface TicTacToeRequestPayload: EventPayloadInterface

sealed interface TicTacToeResponsePayload: EventPayloadInterface

// TODO: remove this method and implement normal one
@Suppress("UNCHECKED_CAST")
fun encodeTTTEventToJson(event: ResponseEvent<TicTacToeResponsePayload>): String {
    return when (event.payload) {
        is HostQuestionResponse -> Json.encodeToString(event as ResponseEvent<HostQuestionResponse>)
        is ClientQuestionResponse -> Json.encodeToString(event as ResponseEvent<ClientQuestionResponse>)
        is SetFieldResponse -> Json.encodeToString(event as ResponseEvent<SetFieldResponse>)
        is ShowAnswerResponse -> Json.encodeToString(event as ResponseEvent<ShowAnswerResponse>)
        is GameError -> Json.encodeToString(event as ResponseEvent<GameError>)
    }
}

fun decodeJsonToTTTEvent(jsonString: String): RequestEvent<TicTacToeRequestPayload> {
    val jsonObject: JsonObject = Json.decodeFromString(jsonString)

    return when (jsonObject["type"]!!.jsonPrimitive.content) {
        QuestionRequest.type -> Json.decodeFromString<RequestEvent<QuestionRequest>>(jsonString)
        SetFieldRequest.type -> Json.decodeFromString<RequestEvent<SetFieldRequest>>(jsonString)
        NextHintRequest.type -> Json.decodeFromString<RequestEvent<NextHintRequest>>(jsonString)
        ShowAnswerRequest.type -> Json.decodeFromString<RequestEvent<ShowAnswerRequest>>(jsonString)
        else -> error("Request expected")
    }
}


@Serializable
data class CellStateView(
    val row: Int,
    val column: Int,
    val mark: CellContent,
    val topic: String
)

@Serializable
data class BoardView(
    @SerialName("cells")
    val cellStateViews: List<CellStateView>
) {
    companion object {
        fun fromGameState(gameState: GameState): BoardView {
            return BoardView(
                gameState.getGridContent().mapIndexed { i, row ->
                    row.mapIndexed { j, cellContent ->
                        CellStateView(i, j, cellContent, gameState.getQuestionTopic(i, j))
                    }
                }.flatten()
            )
        }
    }
}

@Serializable
data class HostQuestionView(
    val row: Int,
    val column: Int,
    val question: String,
    val hints: List<String>,
    @SerialName("current_hints_num")
    val currentHintsNum: Int,
    val answer: String
) {
    companion object {
        fun fromGameState(game: GameState, row: Int, column: Int): HostQuestionView {
            val statement = game.getQuestionStatement(row, column)
            val answer = game.getQuestionAnswer(row, column)
            val allHints = game.getAllHints(row, column)
            val openedHints = game.getOpenedHints(row, column)

            return HostQuestionView(row, column, statement, allHints, openedHints.size, answer)
        }
    }
}

@Serializable
data class ClientQuestionView(
    val row: Int,
    val column: Int,
    val question: String,
    @SerialName("current_hints")
    val currentHints: List<String>
) {
    companion object {
        fun fromGameState(game: GameState, row: Int, column: Int): ClientQuestionView {
            val statement = game.getQuestionStatement(row, column)
            val openedHints = game.getOpenedHints(row, column)

            return ClientQuestionView(row, column, statement, openedHints)
        }
    }
}

@Serializable
data class QuestionWithAnswer(
    val row: Int,
    val column: Int,
    val question: String,
    val answer: String,
)

@Serializable
data class QuestionRequest(
    val row: Int,
    val column: Int
): TicTacToeRequestPayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "OPEN_QUESTION"
    }
}

@Serializable
data class HostQuestionResponse(
    @SerialName("question")
    val questionView: HostQuestionView,

    @SerialName("board")
    val boardView: BoardView
): TicTacToeResponsePayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "OPENED_QUESTION_HOST"
    }
}

@Serializable
data class ClientQuestionResponse(
    @SerialName("question")
    val questionView: ClientQuestionView,

    @SerialName("board")
    val boardView: BoardView
): TicTacToeResponsePayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "OPENED_QUESTION_CLIENT"
    }
}

@Serializable
data class SetFieldRequest(
    val row: Int,
    val column: Int,
    val mark: CellContent
): TicTacToeRequestPayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "SET_FIELD"
    }
}

@Serializable
data class SetFieldResponse(
    val win: GameResult,

    @SerialName("board")
    val boardView: BoardView
): TicTacToeResponsePayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "MAIN_BOARD"
    }
}

@Serializable
data class NextHintRequest(
    val row: Int,
    val column: Int
): TicTacToeRequestPayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "SHOW_NEXT_HINT"
    }
}

@Serializable
data class ShowAnswerRequest(
    val row: Int,
    val column: Int
): TicTacToeRequestPayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "SHOW_ANSWER"
    }
}

@Serializable
data class ShowAnswerResponse(
    val question: QuestionWithAnswer,

    @SerialName("board")
    val boardView: BoardView
) : TicTacToeResponsePayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "OPENED_QUESTION_WITH_ANSWER"
    }
}

@Serializable
data class GameError(val message: String) : TicTacToeResponsePayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "ERROR"
    }
}