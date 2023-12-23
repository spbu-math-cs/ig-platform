package com.clvr.nk

import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.ResponseEvent
import com.clvr.platform.api.SessionId
import kotlinx.serialization.*
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
sealed interface NeKahootRequest: RequestEvent {
    override val session: SessionId
    override val type: String
}

@Serializable
data class NeKahootRequestWithPayload<T: NeKahootRequestPayload> private constructor(
    override val session: SessionId,
    override val type: String,
    val payload: T,
): NeKahootRequest {
    constructor(session: SessionId, payload: T): this(session, payload.type, payload)
}

@Serializable
data class NeKahootResponseWithPayload<T: NeKahootResponsePayload> private constructor(
    override val state: String,
    val payload: T,
): ResponseEvent {
    constructor(payload: T): this(payload.state, payload)

    @Suppress("UNCHECKED_CAST")
    override fun encodeToJson(json: Json): String {
        return when (payload) {
            is HostQuestionResponse -> json.encodeToString(this as NeKahootResponseWithPayload<HostQuestionResponse>)
            is ClientQuestionResponse -> json.encodeToString(this as NeKahootResponseWithPayload<ClientQuestionResponse>)
            is ShowAnswerEvent -> json.encodeToString(this as NeKahootResponseWithPayload<ShowAnswerEvent>)
            is ResultsEvent -> json.encodeToString(this as NeKahootResponseWithPayload<ResultsEvent>)
            is GameError -> json.encodeToString(this as NeKahootResponseWithPayload<GameError>)
            else -> error("Unexpected payload: $payload")
        }
    }
}

fun decodeJsonToNKEvent(jsonString: String): NeKahootRequest {
    val jsonObject: JsonObject = Json.decodeFromString(jsonString)

    return when (jsonObject["type"]!!.jsonPrimitive.content) {
        QuestionRequest.type -> Json.decodeFromString<QuestionRequest>(jsonString)
        AnswerRequest.type -> Json.decodeFromString<NeKahootRequestWithPayload<AnswerRequest>>(jsonString)
        else -> throw IllegalArgumentException("Unknown type of event")
    }
}

@Serializable
data class HostQuestionView(
    val question: String,
    val answer: String,
    @SerialName("answer_description")
    val answerDescription: String?,
    @SerialName("answer_options")
    val answerOptions: List<String>,
    val time: Int,
    val answered: Int,
) {
    companion object {
        fun fromGameState(game: GameState, timestamp: Long): HostQuestionView =
            HostQuestionView(
                game.getQuestion(),
                game.getAnswer(),
                game.getAnswerDescription(),
                game.getAnswerOptions(),
                game.getLeftTime(timestamp),
                game.getNumberOfAnswers(),
            )
    }
}

@Serializable
data class ClientQuestionView(
    val question: String,
    @SerialName("answer_options")
    val answerOptions: List<String>,
    val time: Int,
    @SerialName("given_answer")
    val givenAnswer: String,
) {
    companion object {
        fun fromGameState(game: GameState, timestamp: Long, clientName: String? = null): ClientQuestionView =
            ClientQuestionView(
                game.getQuestion(),
                game.getAnswerOptions(),
                game.getLeftTime(timestamp),
                game.getAnswerOfPlayer(clientName),
            )
    }
}

@Serializable
data class QuestionWithAnswerView(
    val question: String,
    val answer: String,
    @SerialName("answer_description")
    val answerDescription: String?,
    @SerialName("answer_options")
    val answerOptions: List<String>,
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
data class QuestionRequest(
    override val session: SessionId
) : NeKahootRequest {
    @EncodeDefault
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