package com.clvr.server.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.lang.IllegalArgumentException

@Serializable
data class Session(val id: Long)

interface EventPayloadInterface {
    fun type(): String
}

@Serializable
data class Event<T: EventPayloadInterface>(
    val session: Session,
    val type: String,
    val payload: T
)

// TODO: remove this method and implement normal one
@Suppress("UNCHECKED_CAST")
fun encodeEventToJson(event: Event<*>): String {
    return when (event.payload) {
        is QuestionRequest -> Json.encodeToString(event as Event<QuestionRequest>)
        is QuestionResponse -> Json.encodeToString(event as Event<QuestionResponse>)
        is SetFieldRequest -> Json.encodeToString(event as Event<SetFieldRequest>)
        is SetFieldResponse -> Json.encodeToString(event as Event<SetFieldResponse>)
        else -> throw IllegalArgumentException("unknown payload type")
    }
}

fun decodeJsonToEvent(jsonString: String): Event<*> {
    val jsonObject: JsonObject = Json.decodeFromString(jsonString)

    // TODO: reimplement with map of available classes & get rid of "
    return when (jsonObject["type"].toString()) {
        "\"OPEN_QUESTION\"" -> Json.decodeFromString<Event<QuestionRequest>>(jsonString)
        "\"OPENED_QUESTION\"" -> Json.decodeFromString<Event<QuestionResponse>>(jsonString)
        "\"SET_FIELD\"" -> Json.decodeFromString<Event<SetFieldRequest>>(jsonString)
        "\"MAIN_BOARD\"" -> Json.decodeFromString<Event<SetFieldResponse>>(jsonString)
        else -> throw IllegalArgumentException("incorrect json string")
    }
}

fun <T: EventPayloadInterface> eventOf(sessionId: Long, payload: T): Event<T> {
    return Event(
        session = Session(sessionId),
        payload.type(),
        payload
    )
}

@Serializable
enum class CellMark {
    X,
    O,
    NOT_OPENED,
    EMPTY
}

@Serializable
data class Cell(
    val row: Int,
    val column: Int,
    val mark: CellMark
)

@Serializable
data class Board(
    val cells: List<Cell>
)

@Serializable
data class Question(
    val row: Int,
    val column: Int,
    val test: String
)

@Serializable
data class QuestionRequest(
    val row: Int,
    val column: Int
): EventPayloadInterface {
    override fun type(): String = "OPEN_QUESTION"
}

@Serializable
data class QuestionResponse(
    val question: Question,
    val board: Board
): EventPayloadInterface {
    override fun type(): String = "OPENED_QUESTION"
}

@Serializable
data class SetFieldRequest(
    val row: Int,
    val column: Int,
    val mark: CellMark
): EventPayloadInterface {
    override fun type(): String = "SET_FIELD"
}

@Serializable
data class SetFieldResponse(
    val board: Board
): EventPayloadInterface {
    override fun type(): String = "MAIN_BOARD"
}