package com.clvr.server.utils

import com.clvr.server.model.CellContent
import com.clvr.server.model.GameState
import com.clvr.server.utils.PayloadType.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

@Serializable
enum class PayloadType {
    OPEN_QUESTION,
    OPENED_QUESTION,
    SET_FIELD,
    MAIN_BOARD
}

@Serializable
data class SessionId(val id: String)

interface EventPayloadInterface {
    val type: PayloadType
}

@Serializable
sealed interface Event<T: EventPayloadInterface> {
    val payload: T
}

@Serializable
data class RequestEvent<T: EventPayloadInterface> private constructor (
    val session: SessionId,
    val type: PayloadType,
    override val payload: T
): Event<T> {
    constructor(session: SessionId, payload: T): this(session, payload.type, payload)
}

@Serializable
data class ResponseEvent<T: EventPayloadInterface> private constructor (
    val state: PayloadType,
    override val payload: T
): Event<T> {
    constructor(payload: T): this(payload.type, payload)
}

// TODO: remove this method and implement normal one
@Suppress("UNCHECKED_CAST")
fun encodeEventToJson(event: Event<*>): String {
    return when (event.payload.type) {
        OPEN_QUESTION   -> Json.encodeToString(event as RequestEvent<QuestionRequest>)
        OPENED_QUESTION -> Json.encodeToString(event as ResponseEvent<QuestionResponse>)
        SET_FIELD       -> Json.encodeToString(event as RequestEvent<SetFieldRequest>)
        MAIN_BOARD      -> Json.encodeToString(event as ResponseEvent<SetFieldResponse>)
    }
}

fun decodeJsonToEvent(jsonString: String): Event<*> {
    val jsonObject: JsonObject = Json.decodeFromString(jsonString)

    // TODO: reimplement with map of available classes & get rid of "
    return when (Json.decodeFromJsonElement<PayloadType>(jsonObject["type"]!!)) {
        OPEN_QUESTION -> Json.decodeFromString<RequestEvent<QuestionRequest>>(jsonString)
        OPENED_QUESTION -> Json.decodeFromString<RequestEvent<QuestionResponse>>(jsonString)
        SET_FIELD -> Json.decodeFromString<RequestEvent<SetFieldRequest>>(jsonString)
        MAIN_BOARD -> Json.decodeFromString<RequestEvent<SetFieldResponse>>(jsonString)
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
): EventPayloadInterface {
    override val type: PayloadType = OPEN_QUESTION
}

@Serializable
data class QuestionResponse(
    @SerialName("question")
    val questionView: QuestionView,

    @SerialName("board")
    val gameStateView: GameStateView
): EventPayloadInterface {
    override val type: PayloadType = OPENED_QUESTION
}

@Serializable
data class SetFieldRequest(
    val row: Int,
    val column: Int,
    val mark: CellContent
): EventPayloadInterface {
    override val type: PayloadType = SET_FIELD
}

@Serializable
data class SetFieldResponse(
    @SerialName("board")
    val gameStateView: GameStateView
): EventPayloadInterface {
    override val type: PayloadType = MAIN_BOARD
}