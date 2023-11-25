package com.clvr.server.utils

import com.clvr.server.model.CellContent
import com.clvr.server.model.GameResult
import com.clvr.server.model.GameState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

@Serializable
data class SessionId(val id: String)

interface EventPayloadInterface {
    val type: String
}

sealed interface TicTacToeRequestPayload: EventPayloadInterface

sealed interface TicTacToeResponsePayload: EventPayloadInterface

sealed interface Event<out T: EventPayloadInterface> {
    val payload: T
}

@Serializable
data class RequestEvent<out T: EventPayloadInterface> private constructor (
    val session: SessionId,
    val type: String,
    override val payload: T
): Event<T> {
    constructor(session: SessionId, payload: T): this(session, payload.type, payload)
}

@Serializable
data class ResponseEvent<out T: EventPayloadInterface> private constructor (
    val state: String,
    override val payload: T
): Event<T> {
    constructor(payload: T): this(payload.type, payload)
}

// TODO: remove this method and implement normal one
@Suppress("UNCHECKED_CAST")
fun encodeEventToJson(event: ResponseEvent<TicTacToeResponsePayload>): String {
    return when (event.payload) {
        is HostQuestionResponse -> Json.encodeToString(event as ResponseEvent<HostQuestionResponse>)
        is ClientQuestionResponse -> Json.encodeToString(event as ResponseEvent<ClientQuestionResponse>)
        is SetFieldResponse -> Json.encodeToString(event as ResponseEvent<SetFieldResponse>)
        is ShowAnswerResponse -> Json.encodeToString(event as ResponseEvent<ShowAnswerResponse>)
        is GameError -> Json.encodeToString(event as ResponseEvent<GameError>)
    }
}

fun decodeJsonToEvent(jsonString: String): RequestEvent<TicTacToeRequestPayload> {
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
data class GameStateView(
    @SerialName("cells")
    val cellStateViews: List<CellStateView>
) {
    companion object {
        fun fromGameState(gameState: GameState): GameStateView {
            return GameStateView(
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
)

@Serializable
data class ClientQuestionView(
    val row: Int,
    val column: Int,
    val question: String,
    @SerialName("current_hints")
    val currentHints: List<String>
)

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
    val gameStateView: GameStateView
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
    val gameStateView: GameStateView
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
    val gameStateView: GameStateView
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
    val gameStateView: GameStateView
) : TicTacToeResponsePayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "OPENED_QUESTION_WITH_ANSWER"
    }
}

@Serializable
data class GameError(
    val message: String,

    @SerialName("board")
    val gameStateView: GameStateView
) : TicTacToeResponsePayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "ERROR"
    }
}