package com.clvr.server.utils

import com.clvr.server.CellContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.lang.IllegalArgumentException

@Serializable
data class Session(val id: Long)

interface EventPayloadInterface {
    val type: String
}

interface Event<T: EventPayloadInterface> {
    val payload: T
}

@Serializable
class RequestEvent<T: EventPayloadInterface>(
    val session: Session,
    val type: String,
    override val payload: T
): Event<T>

@Serializable
class ResponseEvent<T: EventPayloadInterface>(
    val state: String,
    override val payload: T
): Event<T>

// TODO: remove this method and implement normal one
@Suppress("UNCHECKED_CAST")
fun encodeEventToJson(event: Event<*>): String {
    return when (event.payload) {
        is QuestionRequest -> Json.encodeToString(event as ResponseEvent<QuestionRequest>)
        is QuestionResponse -> Json.encodeToString(event as ResponseEvent<QuestionResponse>)
        is SetFieldRequest -> Json.encodeToString(event as ResponseEvent<SetFieldRequest>)
        is SetFieldResponse -> Json.encodeToString(event as ResponseEvent<SetFieldResponse>)
        else -> throw IllegalArgumentException("unknown payload type")
    }
}

fun decodeJsonToEvent(jsonString: String): Event<*> {
    val jsonObject: JsonObject = Json.decodeFromString(jsonString)

    // TODO: reimplement with map of available classes & get rid of "
    return when (jsonObject["type"].toString()) {
        "\"OPEN_QUESTION\"" -> Json.decodeFromString<RequestEvent<QuestionRequest>>(jsonString)
        "\"OPENED_QUESTION\"" -> Json.decodeFromString<RequestEvent<QuestionResponse>>(jsonString)
        "\"SET_FIELD\"" -> Json.decodeFromString<RequestEvent<SetFieldRequest>>(jsonString)
        "\"MAIN_BOARD\"" -> Json.decodeFromString<RequestEvent<SetFieldResponse>>(jsonString)
        else -> throw IllegalArgumentException("incorrect json string")
    }
}

fun <T: EventPayloadInterface> requestEventOf(sessionId: Long, payload: T): RequestEvent<T> {
    return RequestEvent(
        session = Session(sessionId),
        payload.type,
        payload
    )
}

fun <T: EventPayloadInterface> responseEventOf(payload: T): ResponseEvent<T> {
    return ResponseEvent(
        payload.type,
        payload
    )
}

@Serializable
data class Cell(
    val row: Int,
    val column: Int,
    val mark: CellContent
)

@Serializable
data class Board(
    val cells: List<Cell>
)

@Serializable
data class Question(
    val row: Int,
    val column: Int,
    val text: String
)

@Serializable
data class QuestionRequest(
    val row: Int,
    val column: Int
): EventPayloadInterface {
    override val type: String = "OPEN_QUESTION"
}

@Serializable
data class QuestionResponse(
    val question: Question,
    val board: Board
): EventPayloadInterface {
    override val type: String = "OPENED_QUESTION"
}

@Serializable
data class SetFieldRequest(
    val row: Int,
    val column: Int,
    val mark: CellContent
): EventPayloadInterface {
    override val type: String = "SET_FIELD"
}

@Serializable
data class SetFieldResponse(
    val board: Board
): EventPayloadInterface {
    override val type: String = "MAIN_BOARD"
}