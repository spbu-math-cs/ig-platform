package com.clvr.server

import com.clvr.server.common.QuizId
import com.clvr.server.model.CellContent
import com.clvr.server.model.GameResult
import com.clvr.server.plugins.*
import com.clvr.server.utils.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

// TODO: add test quiz description to test/resources & read it from file; do not use hard-coded strings for tests
class ApplicationTest {
    @Test
    fun `test events simple`() = testApplication {
        setupServer()
        val hostClient = getClient()
        val playerClient = getClient()
        val sessionId = SessionId(createGameSession(hostClient))
        val hostSession = createHostWebSocketSession(hostClient, sessionId)

        // Check initial event for host
        val initialEvent = hostSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.EMPTY, initialEvent.payload.win)
        assertEquals(9, initialEvent.payload.gameStateView.cellStateViews.size)
        (0..8).forEach { assertEquals(CellContent.NOT_OPENED, initialEvent.payload.gameStateView.cellStateViews[it].mark) }

        // Set field event
        hostSession.sendRequest(RequestEvent(sessionId, SetFieldRequest(1, 1, CellContent.X)))
        val setFieldEvent = hostSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.EMPTY, setFieldEvent.payload.win)
        assertEquals(CellContent.X, setFieldEvent.payload.gameStateView.cellStateViews[4].mark)
        (0..8).forEach { if (it != 4) assertEquals(CellContent.NOT_OPENED, setFieldEvent.payload.gameStateView.cellStateViews[it].mark) }

        // Check player initial event
        val playerSession = createPlayerWebSocketSession(playerClient, sessionId)
        val playerInitialEvent = playerSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.EMPTY, playerInitialEvent.payload.win)
        assertEquals(9, playerInitialEvent.payload.gameStateView.cellStateViews.size)
        assertEquals(CellContent.X, playerInitialEvent.payload.gameStateView.cellStateViews[4].mark)
        (0..8).forEach { if (it != 4) assertEquals(CellContent.NOT_OPENED, playerInitialEvent.payload.gameStateView.cellStateViews[it].mark) }

        // Open question
        hostSession.sendRequest(RequestEvent(sessionId, QuestionRequest(0, 1)))
        var openedQuestionHostEvent = hostSession.receiveResponse<HostQuestionResponse>()
        assertEquals(0, openedQuestionHostEvent.payload.questionView.row)
        assertEquals(1, openedQuestionHostEvent.payload.questionView.column)
        assertEquals(0, openedQuestionHostEvent.payload.questionView.currentHintsNum)
        assertEquals("На каком языке написана следующая программа", openedQuestionHostEvent.payload.questionView.question)
        assertEquals("Языки I", openedQuestionHostEvent.payload.gameStateView.cellStateViews[1].topic)
        assertEquals("Whitespace", openedQuestionHostEvent.payload.questionView.answer)

        // Check player receives open question event broadcast
        var openedQuestionPlayerEvent = playerSession.receiveResponse<ClientQuestionResponse>()
        assertEquals(0, openedQuestionPlayerEvent.payload.questionView.row)
        assertEquals(1, openedQuestionPlayerEvent.payload.questionView.column)
        assertEquals(openedQuestionHostEvent.payload.questionView.question, openedQuestionPlayerEvent.payload.questionView.question)
        assertEquals(0, openedQuestionPlayerEvent.payload.questionView.currentHints.size)

        // Show next hint
        hostSession.sendRequest(RequestEvent(sessionId, NextHintRequest(0, 1)))
        openedQuestionHostEvent = hostSession.receiveResponse<HostQuestionResponse>()
        assertEquals(0, openedQuestionHostEvent.payload.questionView.row)
        assertEquals(1, openedQuestionHostEvent.payload.questionView.column)
        assertEquals(1, openedQuestionHostEvent.payload.questionView.currentHintsNum)
        assertEquals("На каком языке написана следующая программа", openedQuestionHostEvent.payload.questionView.question)

        // Check player receives event with hint broadcast
        openedQuestionPlayerEvent = playerSession.receiveResponse<ClientQuestionResponse>()
        assertEquals(0, openedQuestionPlayerEvent.payload.questionView.row)
        assertEquals(1, openedQuestionPlayerEvent.payload.questionView.column)
        assertEquals(openedQuestionHostEvent.payload.questionView.question, openedQuestionPlayerEvent.payload.questionView.question)
        assertEquals(listOf("hint: На каком языке написана следующая программа"), openedQuestionPlayerEvent.payload.questionView.currentHints)

        // Show answer
        hostSession.sendRequest(RequestEvent(sessionId, ShowAnswerRequest(0, 1)))
        val questionAnswerHostEvent = hostSession.receiveResponse<ShowAnswerResponse>()
        assertEquals(0, questionAnswerHostEvent.payload.question.row)
        assertEquals(1, questionAnswerHostEvent.payload.question.column)
        assertEquals("Whitespace", questionAnswerHostEvent.payload.question.answer)

        // Check player receives event with answer description
        val questionAnswerPlayerEvent = playerSession.receiveResponse<ShowAnswerResponse>()
        assertEquals(questionAnswerHostEvent, questionAnswerPlayerEvent)

        // Make win X win
        hostSession.sendRequest(RequestEvent(sessionId, SetFieldRequest(0, 0, CellContent.X)))
        hostSession.sendRequest(RequestEvent(sessionId, SetFieldRequest(2, 2, CellContent.X)))
        hostSession.receiveResponse<SetFieldResponse>() // skip
        val finalHostResponseEvent = hostSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.X, finalHostResponseEvent.payload.win)

        // Check client receives events with game result
        playerSession.receiveResponse<SetFieldResponse>() // skip
        val finalPlayerResponseEvent = playerSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.X, finalPlayerResponseEvent.payload.win)
    }



    private suspend fun createGameSession(client: HttpClient): String {
        client.post("/api/game-session") {
            contentType(ContentType.Application.Json)
            setBody(QuizRequest(QuizId("ABCD")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)

            val jsonObject: JsonObject = Json.decodeFromString(bodyAsText())
            val session = jsonObject["session"] as JsonObject
            return session["id"]?.jsonPrimitive?.content ?: throw IllegalStateException("id cannot be null")
        }
    }

    private suspend fun createHostWebSocketSession(client: HttpClient, sessionId: SessionId): ClientWebSocketSession {
       return client.webSocketSession("/ws/host/${sessionId.id}")
    }

    private suspend fun createPlayerWebSocketSession(client: HttpClient, sessionId: SessionId): ClientWebSocketSession {
        return client.webSocketSession("/ws/client/${sessionId.id}")
    }

    private fun ApplicationTestBuilder.setupServer() {
        application {
            configureCallLogging()
            configureSerialization()
            configureDatabases()
            configureSockets()
            configureRouting()
            configureQuizDatabase(testQuizFile)
        }
    }

    private val testQuizFile = File(
        Application::class.java.classLoader.getResource("applicationTestQuizCollection.json")!!.toURI()
    )

    private fun ApplicationTestBuilder.getClient(): HttpClient {
        return createClient {
            install(WebSockets)
            install(ContentNegotiation) {
                json()
            }
        }
    }

    private suspend fun ClientWebSocketSession.sendRequest(event: RequestEvent<TicTacToeRequestPayload>) {
        outgoing.send(Frame.Text(encodeRequestEventToJson(event)))
    }

    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <reified T: TicTacToeResponsePayload> ClientWebSocketSession.receiveResponse(): ResponseEvent<T> {
        val frame = incoming.receive()
        assertTrue(frame is Frame.Text)
        val event = decodeResponseJsonToEvent((frame as Frame.Text).readText())
        assertTrue(event.payload is T)
        return event as ResponseEvent<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun encodeRequestEventToJson(event: RequestEvent<TicTacToeRequestPayload>): String {
        return when (event.payload) {
            is QuestionRequest -> Json.encodeToString(event as RequestEvent<QuestionRequest>)
            is SetFieldRequest -> Json.encodeToString(event as RequestEvent<SetFieldRequest>)
            is NextHintRequest -> Json.encodeToString(event as RequestEvent<NextHintRequest>)
            is ShowAnswerRequest -> Json.encodeToString(event as RequestEvent<ShowAnswerRequest>)
        }
    }

    private fun decodeResponseJsonToEvent(jsonString: String): ResponseEvent<TicTacToeResponsePayload> {
        val jsonObject: JsonObject = Json.decodeFromString(jsonString)

        return when (jsonObject["state"]!!.jsonPrimitive.content) {
            HostQuestionResponse.type -> Json.decodeFromString<ResponseEvent<HostQuestionResponse>>(jsonString)
            ClientQuestionResponse.type -> Json.decodeFromString<ResponseEvent<ClientQuestionResponse>>(jsonString)
            SetFieldResponse.type -> Json.decodeFromString<ResponseEvent<SetFieldResponse>>(jsonString)
            ShowAnswerResponse.type -> Json.decodeFromString<ResponseEvent<ShowAnswerResponse>>(jsonString)
            else -> error("Response expected")
        }
    }
}
