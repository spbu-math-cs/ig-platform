package com.clvr.server.utils

import com.clvr.server.model.CellContent
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
        is QuestionResponse -> Json.encodeToString(event as ResponseEvent<QuestionResponse>)
        is SetFieldResponse -> Json.encodeToString(event as ResponseEvent<SetFieldResponse>)
    }
}

fun decodeJsonToEvent(jsonString: String): RequestEvent<TicTacToeRequestPayload> {
    val jsonObject: JsonObject = Json.decodeFromString(jsonString)

    return when (jsonObject["type"]!!.jsonPrimitive.content) {
        QuestionRequest.type -> Json.decodeFromString<RequestEvent<QuestionRequest>>(jsonString)
        SetFieldRequest.type -> Json.decodeFromString<RequestEvent<SetFieldRequest>>(jsonString)
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
data class QuestionView(
    val row: Int,
    val column: Int,
    val text: String,
    val answer: String
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
data class QuestionResponse(
    @SerialName("question")
    val questionView: QuestionView,

    @SerialName("board")
    val gameStateView: GameStateView
): TicTacToeResponsePayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "OPENED_QUESTION"
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
    @SerialName("board")
    val gameStateView: GameStateView
): TicTacToeResponsePayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "MAIN_BOARD"
    }
}