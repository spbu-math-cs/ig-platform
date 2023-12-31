package com.clvr.ttt

import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionId
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface TicTacToeRequestPayload {
    val type: String
}

sealed interface TicTacToeResponsePayload {
    val state: String
}

sealed interface TicTacToeRequest : RequestEvent

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class PressButtonRequest(
    override val session: SessionId,
): TicTacToeRequest {
    @EncodeDefault
    override val type: String = Companion.type

    companion object {
        const val type: String = "PRESS_BUTTON"
    }
}

// The hack with private constructor is used to ensure that properties are serialized in correct order,
//  but type is pre-defined by payload
@Serializable
data class TicTacToeRequestWithPayload<T: TicTacToeRequestPayload> private constructor (
    override val session: SessionId,
    override val type: String,
    val payload: T,
): TicTacToeRequest {
    constructor(session: SessionId, payload: T): this(session, payload.type, payload)
}

@Serializable
data class TicTacToeResponse<T: TicTacToeResponsePayload> private constructor (
    override val state: String,
    val payload: T
): ResponseEvent {
    constructor(payload: T): this(payload.state, payload)

    @Suppress("UNCHECKED_CAST")
    override fun encodeToJson(json: Json): String {
        return when (payload) {
            is HostQuestionResponse -> json.encodeToString(this as TicTacToeResponse<HostQuestionResponse>)
            is ClientQuestionResponse -> json.encodeToString(this as TicTacToeResponse<ClientQuestionResponse>)
            is SetFieldResponse -> json.encodeToString(this as TicTacToeResponse<SetFieldResponse>)
            is ShowAnswerResponse -> json.encodeToString(this as TicTacToeResponse<ShowAnswerResponse>)
            is GameError -> json.encodeToString(this as TicTacToeResponse<GameError>)
            is SelectTeamResponse -> json.encodeToString(this as TicTacToeResponse<SelectTeamResponse>)
            is PressButtonResponse -> json.encodeToString(this as TicTacToeResponse<PressButtonResponse>)
            else -> error("Unexpected payload $payload")
        }
    }
}

fun decodeJsonToTTTEvent(jsonString: String): TicTacToeRequest {
    val jsonObject: JsonObject = Json.decodeFromString(jsonString)

    return when (jsonObject["type"]!!.jsonPrimitive.content) {
        QuestionRequest.type -> Json.decodeFromString<TicTacToeRequestWithPayload<QuestionRequest>>(jsonString)
        SetFieldRequest.type -> Json.decodeFromString<TicTacToeRequestWithPayload<SetFieldRequest>>(jsonString)
        NextHintRequest.type -> Json.decodeFromString<TicTacToeRequestWithPayload<NextHintRequest>>(jsonString)
        ShowAnswerRequest.type -> Json.decodeFromString<TicTacToeRequestWithPayload<ShowAnswerRequest>>(jsonString)
        SelectTeamRequest.type -> Json.decodeFromString<TicTacToeRequestWithPayload<SelectTeamRequest>>(jsonString)
        PressButtonRequest.type -> Json.decodeFromString<PressButtonRequest>(jsonString)
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
    override val state: String = Companion.state

    companion object {
        const val state: String = "OPENED_QUESTION_HOST"
    }
}

@Serializable
data class ClientQuestionResponse(
    @SerialName("question")
    val questionView: ClientQuestionView,

    @SerialName("board")
    val boardView: BoardView
): TicTacToeResponsePayload {
    override val state: String = Companion.state

    companion object {
        const val state: String = "OPENED_QUESTION_CLIENT"
    }
}

@Serializable
data class SetFieldRequest(
    val row: Int,
    val column: Int,
    val mark: CellContent,
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
    override val state: String = Companion.state

    companion object {
        const val state: String = "MAIN_BOARD"
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
    override val state: String = Companion.state

    companion object {
        const val state: String = "OPENED_QUESTION_WITH_ANSWER"
    }
}

@Serializable
data class SelectTeamRequest(
    val team: Player
): TicTacToeRequestPayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "TEAM_SELECTION"
    }
}

@Serializable
data class SelectTeamResponse(
    val player: String,
    val team: Player
) : TicTacToeResponsePayload {
    override val state: String = Companion.state

    companion object {
        const val state: String = "TEAM_SELECTED"
    }
}

@Serializable
data class PressButtonResponse(
    @SerialName("question")
    val questionView: HostQuestionView,

    @SerialName("board")
    val boardView: BoardView,

    @Transient
    val team: Player = Player.X
) : TicTacToeResponsePayload {
    override val state: String = "TEAM_${team}_IS_ANSWERING"
}

@Serializable
data class GameError(val message: String) : TicTacToeResponsePayload {
    override val state: String = Companion.state

    companion object {
        const val state: String = "ERROR"
    }
}