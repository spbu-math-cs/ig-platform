package com.clvr.nk

import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface NeKahootRequestPayload {
    val type: String
}

sealed interface NeKahootResponsePayload {
    val state: String
}

@Serializable
data class NeKahootRequest<T: NeKahootRequestPayload> private constructor(
    override val session: SessionId,
    override val type: String,
    val payload: T,
): RequestEvent {
    constructor(session: SessionId, payload: T): this(session, payload.type, payload)
}

@Serializable
data class NeKahootResponse<T: NeKahootResponsePayload> private constructor(
    override val state: String,
    val payload: T,
): ResponseEvent {
    constructor(payload: T): this(payload.state, payload)
}

// TODO: remove this method and implement normal one
@Suppress("UNCHECKED_CAST")
fun encodeNKEventToJson(event: NeKahootResponse<*>): String {
    return when (event.payload) {
        is HostQuestionResponse -> Json.encodeToString(event as NeKahootResponse<HostQuestionResponse>)
        is ClientQuestionResponse -> Json.encodeToString(event as NeKahootResponse<ClientQuestionResponse>)
        is ShowAnswerEvent -> Json.encodeToString(event as NeKahootResponse<ShowAnswerEvent>)
        is ResultsEvent -> Json.encodeToString(event as NeKahootResponse<ResultsEvent>)
        is GameError -> Json.encodeToString(event as NeKahootResponse<GameError>)
    }
}

fun decodeJsonToNKEvent(jsonString: String): NeKahootRequest<*> {
    val jsonObject: JsonObject = Json.decodeFromString(jsonString)

    return when (jsonObject["type"]!!.jsonPrimitive.content) {
        QuestionRequest.type -> Json.decodeFromString<NeKahootRequest<QuestionRequest>>(jsonString)
        AnswerRequest.type -> Json.decodeFromString<NeKahootRequest<AnswerRequest>>(jsonString)
        StartGameRequest.type -> Json.decodeFromString<NeKahootRequest<StartGameRequest>>(jsonString)
        else -> throw IllegalArgumentException("Unknown type of event")
    }
}

@Serializable
data class HostQuestionView(
    val question: String,
    val answer: String,
    val answer_description: String?,
    val answer_options: List<String>,
    val time: Int,
    val answered: Int,
) {
    companion object {
        fun fromGameState(game: GameState): HostQuestionView =
            HostQuestionView(
                game.getQuestion(),
                game.getAnswer(),
                game.getAnswerDescription(),
                game.getAnswerOptions(),
                game.getTime(),
                game.getNumberOfAnswers(),
            )
    }
}

@Serializable
data class ClientQuestionView(
    val question: String,
    val answer_options: List<String>,
    val time: Int,
    val given_answer: String,
) {
    companion object {
        fun fromGameState(game: GameState, clientName: String? = null): ClientQuestionView =
            ClientQuestionView(
                game.getQuestion(),
                game.getAnswerOptions(),
                game.getTime(),
                game.getAnswerOfPlayer(clientName),
            )
    }
}

@Serializable
data class QuestionWithAnswerView(
    val question: String,
    val answer: String,
    val answer_description: String?,
    val answer_options: List<String>,
    val time: Int,
) {
    companion object {
        fun fromGameState(game: GameState): QuestionWithAnswerView =
            QuestionWithAnswerView(
                game.getQuestion(),
                game.getAnswer(),
                game.getAnswerDescription(),
                game.getAnswerOptions(),
                game.getTime(),
            )
    }
}

@Serializable
class StartGameRequest: NeKahootRequestPayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "START_GAME"
    }
}

@Serializable
class QuestionRequest: NeKahootRequestPayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "NEXT_QUESTION"
    }
}

@Serializable
data class AnswerRequest(
    val answer: String,
): NeKahootRequestPayload {
    override val type: String = Companion.type

    companion object {
        const val type: String = "GIVE_ANSWER"
    }
}

@Serializable
data class HostQuestionResponse(
    @SerialName("question")
    val questionView: HostQuestionView,
): NeKahootResponsePayload {
    override val state: String = Companion.state

    companion object {
        const val state: String = "OPENED_QUESTION"
    }
}

@Serializable
data class ClientQuestionResponse(
    @SerialName("question")
    val questionView: ClientQuestionView,
): NeKahootResponsePayload {
    override val state: String = Companion.state

    companion object {
        const val state: String = "OPENED_QUESTION"
    }
}

@Serializable
data class ShowAnswerEvent(
    @SerialName("question")
    val questionView: QuestionWithAnswerView,
): NeKahootResponsePayload {
    override val state: String = Companion.state

    companion object {
        const val state: String = "SHOW_QUESTION_ANSWER"
    }
}

@Serializable
data class ResultsEvent(
    val results: List<PlayerResult>,
): NeKahootResponsePayload {
    override val state: String = Companion.state

    companion object {
        const val state: String = "RESULT"
    }
}

@Serializable
data class GameError(
    val message: String,
) : NeKahootResponsePayload {
    override val state: String = Companion.state

    companion object {
        const val state: String = "ERROR"
    }
}