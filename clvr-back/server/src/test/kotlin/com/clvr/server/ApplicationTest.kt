package com.clvr.server

import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.SessionId
import com.clvr.platform.configurePlatform
import com.clvr.platform.api.lobby.StartGameEvent
import com.clvr.platform.installActivity
import com.clvr.ttt.*
import com.clvr.ttt.common.Config
import com.clvr.ttt.common.OpenMultipleQuestions
import com.clvr.ttt.common.TemplateCompleteInfo
import com.clvr.ttt.common.ReplaceMarks
import io.ktor.client.*
import io.ktor.client.call.*
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

// TODO: add test template description to test/resources & read it from file; do not use hard-coded strings for tests
class ApplicationTest {
    @Test
    fun `test events simple`() = testApplication {
        val config = Config(
            replaceMarks = ReplaceMarks.ENABLED,
            openMultipleQuestions = OpenMultipleQuestions.ENABLED
        )

        setupServer()
        val hostClient = getClient()
        val playerClient = getClient()
        val sessionId = SessionId(createGameSession(hostClient, config))
        val hostSession = createHostWebSocketSessionAndStartGame(hostClient, sessionId)

        // Check initial event for host
        val initialEvent = hostSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.EMPTY, initialEvent.payload.win)
        assertEquals(9, initialEvent.payload.boardView.cellStateViews.size)
        (0..8).forEach { assertEquals(CellContent.NOT_OPENED, initialEvent.payload.boardView.cellStateViews[it].mark) }

        // Set field event
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, SetFieldRequest(1, 1, CellContent.X)))
        val setFieldEvent = hostSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.EMPTY, setFieldEvent.payload.win)
        assertEquals(CellContent.X, setFieldEvent.payload.boardView.cellStateViews[4].mark)
        (0..8).forEach { if (it != 4) assertEquals(CellContent.NOT_OPENED, setFieldEvent.payload.boardView.cellStateViews[it].mark) }

        // Check player initial event
        val playerSession = createPlayerWebSocketSession(playerClient, sessionId)
        val playerInitialEvent = playerSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.EMPTY, playerInitialEvent.payload.win)
        assertEquals(9, playerInitialEvent.payload.boardView.cellStateViews.size)
        assertEquals(CellContent.X, playerInitialEvent.payload.boardView.cellStateViews[4].mark)
        (0..8).forEach { if (it != 4) assertEquals(CellContent.NOT_OPENED, playerInitialEvent.payload.boardView.cellStateViews[it].mark) }

        // Open question
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, QuestionRequest(0, 1)))
        var openedQuestionHostEvent = hostSession.receiveResponse<HostQuestionResponse>()
        assertEquals(0, openedQuestionHostEvent.payload.questionView.row)
        assertEquals(1, openedQuestionHostEvent.payload.questionView.column)
        assertEquals(0, openedQuestionHostEvent.payload.questionView.currentHintsNum)
        assertEquals("На каком языке написана следующая программа", openedQuestionHostEvent.payload.questionView.question)
        assertEquals("Языки I", openedQuestionHostEvent.payload.boardView.cellStateViews[1].topic)
        assertEquals("Whitespace", openedQuestionHostEvent.payload.questionView.answer)

        // Check player receives open question event broadcast
        var openedQuestionPlayerEvent = playerSession.receiveResponse<ClientQuestionResponse>()
        assertEquals(0, openedQuestionPlayerEvent.payload.questionView.row)
        assertEquals(1, openedQuestionPlayerEvent.payload.questionView.column)
        assertEquals(openedQuestionHostEvent.payload.questionView.question, openedQuestionPlayerEvent.payload.questionView.question)
        assertEquals(0, openedQuestionPlayerEvent.payload.questionView.currentHints.size)

        // Show next hint
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, NextHintRequest(0, 1)))
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
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, ShowAnswerRequest(0, 1)))
        val questionAnswerHostEvent = hostSession.receiveResponse<ShowAnswerResponse>()
        assertEquals(0, questionAnswerHostEvent.payload.question.row)
        assertEquals(1, questionAnswerHostEvent.payload.question.column)
        assertEquals("Whitespace", questionAnswerHostEvent.payload.question.answer)

        // Check player receives event with answer description
        val questionAnswerPlayerEvent = playerSession.receiveResponse<ShowAnswerResponse>()
        assertEquals(questionAnswerHostEvent, questionAnswerPlayerEvent)

        // Make win X win
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, SetFieldRequest(0, 0, CellContent.X)))
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, SetFieldRequest(2, 2, CellContent.X)))
        hostSession.receiveResponse<SetFieldResponse>() // skip
        val finalHostTicTacToeResponse = hostSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.X, finalHostTicTacToeResponse.payload.win)

        // Check client receives events with game result
        playerSession.receiveResponse<SetFieldResponse>() // skip
        val finalPlayerTicTacToeResponse = playerSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.X, finalPlayerTicTacToeResponse.payload.win)
    }

    @Test
    fun `test errors`() = testApplication {
        val config = Config(
            replaceMarks = ReplaceMarks.DISABLED,
            openMultipleQuestions = OpenMultipleQuestions.DISABLED
        )

        setupServer()
        val hostClient = getClient()
        val playerClient = getClient()
        val sessionId = SessionId(createGameSession(hostClient, config))
        val hostSession = createHostWebSocketSessionAndStartGame(hostClient, sessionId)
        val playerSession = createPlayerWebSocketSession(playerClient, sessionId)

        hostSession.receiveResponse<SetFieldResponse>()
        playerSession.receiveResponse<SetFieldResponse>()

        // Open question
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, QuestionRequest(0, 0)))
        hostSession.receiveResponse<HostQuestionResponse>()
        playerSession.receiveResponse<ClientQuestionResponse>()

        // Set other cell => error
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, SetFieldRequest(1, 1, CellContent.X)))
        assertEquals(
            "Set mark in the opened cell before opening the next one",
            hostSession.receiveResponse<GameError>().payload.message
        )
        assertTrue(playerSession.incoming.isEmpty)

        // Set correct cell
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, SetFieldRequest(0, 0, CellContent.X)))
        hostSession.receiveResponse<SetFieldResponse>()
        playerSession.receiveResponse<SetFieldResponse>()

        // Set that cell again => error
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, SetFieldRequest(0, 0, CellContent.O)))
        assertEquals(
            "Changing result in the cell is forbidden",
            hostSession.receiveResponse<GameError>().payload.message
        )

        assertTrue(playerSession.incoming.isEmpty)
    }

    @Test
    fun `test client mid-question connection`() = testApplication {
        val config = Config(
            replaceMarks = ReplaceMarks.DISABLED,
            openMultipleQuestions = OpenMultipleQuestions.DISABLED
        )

        setupServer()
        val hostClient = getClient()
        val sessionId = SessionId(createGameSession(hostClient, config))
        val hostSession = createHostWebSocketSessionAndStartGame(hostClient, sessionId)

        hostSession.receiveResponse<SetFieldResponse>()

        // Open question
        hostSession.sendRequest(TicTacToeRequestWithPayload(sessionId, QuestionRequest(0, 0)))
        hostSession.receiveResponse<HostQuestionResponse>()

        // New client connects
        val playerClient = getClient()
        val playerSession = createPlayerWebSocketSession(playerClient, sessionId)

        playerSession.receiveResponse<SetFieldResponse>().payload.boardView
        assertEquals(
            ClientQuestionView(0, 0, "Опишите значение следующих эмодзи", emptyList()),
            playerSession.receiveResponse<ClientQuestionResponse>().payload.questionView
        )

        assertTrue(playerSession.incoming.isEmpty)
    }

    @Test
    fun `templates creation api test`() = testApplication {
                setupServer()
        val client = getClient()

        val templateId = createTemplate(client)
        val template = client
            .get("/tic-tac-toe/template/${templateId.id}")
            .body<TemplateCompleteInfo>()

        assertEquals(templateId.id, template.id)
        assertEquals("template name", template.name)
        assertEquals("template comment", template.comment)
        assertEquals(createTemplate.board, template.board)

        deleteTemplate(client, templateId)
        assertEquals(
            HttpStatusCode.NotFound,
            client.get("/tic-tac-toe/template/${templateId.id}").status
        )
    }

    @Test
    fun `test main page`() = testApplication {
        setupServer()
        val client = getClient()

        val templateList = client.get("/tic-tac-toe/template-list").body<TemplateListResponse>().templateList
        assertEquals(1, templateList.size)
        assertEquals("Random template", templateList[0].name)
        assertEquals("ABCD", templateList[0].id)
        assertEquals("", templateList[0].comment)

        assertEquals(HttpStatusCode.NotFound, client.get("/tic-tac-toe/template/KEK").status)
        assertEquals(HttpStatusCode.OK, client.get("/tic-tac-toe/template/ABCD").status)

        val templateInfo = client.get("/tic-tac-toe/template/ABCD").body<TemplateCompleteInfo>()
        assertEquals("ABCD", templateInfo.id)

        val addedId = createTemplate(client).id

        assertEquals(2, client.get("/tic-tac-toe/template-list").body<TemplateListResponse>().templateList.size)

        deleteTemplate(client, TicTacToeInstaller.templateId("ABCD"))

        val templateListAfterUpdates = client.get("/tic-tac-toe/template-list").body<TemplateListResponse>().templateList
        assertEquals(1, templateListAfterUpdates.size)
        assertEquals(addedId, templateListAfterUpdates[0].id)

        assertEquals(HttpStatusCode.OK, client.get("/tic-tac-toe/template/$addedId").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/tic-tac-toe/template/ABCD").status)
    }

    private suspend fun createTemplate(client: HttpClient): TemplateId {
        client.post("/tic-tac-toe/template") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createTemplate))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)

            val jsonObject: JsonObject = Json.decodeFromString(bodyAsText())
            val template = jsonObject["id"] as JsonObject
            return TicTacToeInstaller.templateId(
                template["id"]?.jsonPrimitive?.content ?: throw IllegalStateException("id cannot be null")
            )
        }
    }

    private suspend fun deleteTemplate(client: HttpClient, templateId: TemplateId) {
        client.delete("/tic-tac-toe/template/${templateId.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    private suspend fun createGameSession(client: HttpClient, config: Config): String {
        client.post("/tic-tac-toe/game") {
            contentType(ContentType.Application.Json)
            setBody(TemplateRequest("ABCD", config))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)

            val jsonObject: JsonObject = Json.decodeFromString(bodyAsText())
            val session = jsonObject["session"] as JsonObject
            return session["id"]?.jsonPrimitive?.content ?: throw IllegalStateException("id cannot be null")
        }
    }

    private suspend fun createHostWebSocketSessionAndStartGame(client: HttpClient, sessionId: SessionId): ClientWebSocketSession {
        val session = client.webSocketSession("/ws/tic-tac-toe/host/${sessionId.id}")
        session.incoming.receive() // Receiving response with empty lobby
        session.outgoing.send(Frame.Text(Json.encodeToString(StartGameEvent(sessionId))))
        return session
    }

    private suspend fun createPlayerWebSocketSession(client: HttpClient, sessionId: SessionId): ClientWebSocketSession {
        return client.webSocketSession("/ws/tic-tac-toe/player/${sessionId.id}")
    }

    private fun ApplicationTestBuilder.setupServer() {
        application {
            configurePlatform()
            installActivity(TicTacToeInstaller(listOf(testTemplateFile)))
        }
    }

    private val testTemplateFile = File(
        Application::class.java.classLoader.getResource("applicationTestTemplateCollection.json")!!.toURI()
    )

    private fun ApplicationTestBuilder.getClient(): HttpClient {
        return createClient {
            install(WebSockets)
            install(ContentNegotiation) {
                json()
            }
        }
    }

    private suspend fun ClientWebSocketSession.sendRequest(event: TicTacToeRequestWithPayload<TicTacToeRequestPayload>) {
        outgoing.send(Frame.Text(encodeTicTacToeRequestToJson(event)))
    }

    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <reified T: TicTacToeResponsePayload> ClientWebSocketSession.receiveResponse(): TicTacToeResponse<T> {
        val frame = incoming.receive()
        assertTrue(frame is Frame.Text)
        val event = decodeResponseJsonToEvent((frame as Frame.Text).readText())
        assertTrue(event.payload is T)
        return event as TicTacToeResponse<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun encodeTicTacToeRequestToJson(event: TicTacToeRequestWithPayload<TicTacToeRequestPayload>): String {
        return when (event.payload) {
            is QuestionRequest -> Json.encodeToString(event as TicTacToeRequestWithPayload<QuestionRequest>)
            is SetFieldRequest -> Json.encodeToString(event as TicTacToeRequestWithPayload<SetFieldRequest>)
            is NextHintRequest -> Json.encodeToString(event as TicTacToeRequestWithPayload<NextHintRequest>)
            is ShowAnswerRequest -> Json.encodeToString(event as TicTacToeRequestWithPayload<ShowAnswerRequest>)
            is SelectTeamRequest -> Json.encodeToString(event as TicTacToeRequestWithPayload<SelectTeamRequest>)
        }
    }

    private fun decodeResponseJsonToEvent(jsonString: String): TicTacToeResponse<*> {
        val jsonObject: JsonObject = Json.decodeFromString(jsonString)

        return when (jsonObject["state"]!!.jsonPrimitive.content) {
            HostQuestionResponse.state -> Json.decodeFromString<TicTacToeResponse<HostQuestionResponse>>(jsonString)
            ClientQuestionResponse.state -> Json.decodeFromString<TicTacToeResponse<ClientQuestionResponse>>(jsonString)
            SetFieldResponse.state -> Json.decodeFromString<TicTacToeResponse<SetFieldResponse>>(jsonString)
            ShowAnswerResponse.state -> Json.decodeFromString<TicTacToeResponse<ShowAnswerResponse>>(jsonString)
            GameError.state -> Json.decodeFromString<TicTacToeResponse<GameError>>(jsonString)
            else -> error("Response expected")
        }
    }
}