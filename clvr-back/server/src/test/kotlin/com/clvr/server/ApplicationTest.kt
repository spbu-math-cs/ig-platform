package com.clvr.server

import com.clvr.platform.api.TemplateId
import com.clvr.platform.api.SessionId
import com.clvr.platform.configurePlatform
import com.clvr.platform.installActivity
import com.clvr.ttt.*
import com.clvr.ttt.common.Config
import com.clvr.ttt.common.OpenMultipleQuestions
import com.clvr.ttt.common.QuizCompleteInfo
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

// TODO: add test quiz description to test/resources & read it from file; do not use hard-coded strings for tests
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
        val hostSession = createHostWebSocketSession(hostClient, sessionId)

        // Check initial event for host
        val initialEvent = hostSession.receiveResponse<SetFieldResponse>()
        assertEquals(GameResult.EMPTY, initialEvent.payload.win)
        assertEquals(9, initialEvent.payload.boardView.cellStateViews.size)
        (0..8).forEach { assertEquals(CellContent.NOT_OPENED, initialEvent.payload.boardView.cellStateViews[it].mark) }

        // Set field event
        hostSession.sendRequest(TicTacToeRequest(sessionId, SetFieldRequest(1, 1, CellContent.X)))
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
        hostSession.sendRequest(TicTacToeRequest(sessionId, QuestionRequest(0, 1)))
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
        hostSession.sendRequest(TicTacToeRequest(sessionId, NextHintRequest(0, 1)))
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
        hostSession.sendRequest(TicTacToeRequest(sessionId, ShowAnswerRequest(0, 1)))
        val questionAnswerHostEvent = hostSession.receiveResponse<ShowAnswerResponse>()
        assertEquals(0, questionAnswerHostEvent.payload.question.row)
        assertEquals(1, questionAnswerHostEvent.payload.question.column)
        assertEquals("Whitespace", questionAnswerHostEvent.payload.question.answer)

        // Check player receives event with answer description
        val questionAnswerPlayerEvent = playerSession.receiveResponse<ShowAnswerResponse>()
        assertEquals(questionAnswerHostEvent, questionAnswerPlayerEvent)

        // Make win X win
        hostSession.sendRequest(TicTacToeRequest(sessionId, SetFieldRequest(0, 0, CellContent.X)))
        hostSession.sendRequest(TicTacToeRequest(sessionId, SetFieldRequest(2, 2, CellContent.X)))
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
        val hostSession = createHostWebSocketSession(hostClient, sessionId)
        val playerSession = createPlayerWebSocketSession(playerClient, sessionId)

        hostSession.receiveResponse<SetFieldResponse>()
        playerSession.receiveResponse<SetFieldResponse>()

        // Open question
        hostSession.sendRequest(TicTacToeRequest(sessionId, QuestionRequest(0, 0)))
        hostSession.receiveResponse<HostQuestionResponse>()
        playerSession.receiveResponse<ClientQuestionResponse>()

        // Set other cell => error
        hostSession.sendRequest(TicTacToeRequest(sessionId, SetFieldRequest(1, 1, CellContent.X)))
        assertEquals(
            "Set mark in the opened cell before opening the next one",
            hostSession.receiveResponse<GameError>().payload.message
        )
        assertTrue(playerSession.incoming.isEmpty)

        // Set correct cell
        hostSession.sendRequest(TicTacToeRequest(sessionId, SetFieldRequest(0, 0, CellContent.X)))
        hostSession.receiveResponse<SetFieldResponse>()
        playerSession.receiveResponse<SetFieldResponse>()

        // Set that cell again => error
        hostSession.sendRequest(TicTacToeRequest(sessionId, SetFieldRequest(0, 0, CellContent.O)))
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
        val hostSession = createHostWebSocketSession(hostClient, sessionId)

        hostSession.receiveResponse<SetFieldResponse>()

        // Open question
        hostSession.sendRequest(TicTacToeRequest(sessionId, QuestionRequest(0, 0)))
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
    fun `quizzes creation api test`() = testApplication {
                setupServer()
        val client = getClient()

        val quizId = createQuiz(client)
        val quiz = client
            .get("/tic-tac-toe/quiz-list/${quizId.id}")
            .body<QuizCompleteInfo>()

        assertEquals(quizId.id, quiz.id)
        assertEquals("template name", quiz.name)
        assertEquals("template comment", quiz.comment)
        assertEquals(createTemplate.board, quiz.board)

        deleteQuiz(client, quizId)
        assertEquals(
            HttpStatusCode.NotFound,
            client.get("/tic-tac-toe/quiz-list/${quizId.id}").status
        )
    }

    @Test
    fun `test main page`() = testApplication {
        setupServer()
        val client = getClient()

        val quizList = client.get("/tic-tac-toe/quiz-list").body<QuizListResponse>().quizList
        assertEquals(1, quizList.size)
        assertEquals("Random template", quizList[0].name)
        assertEquals("ABCD", quizList[0].id)
        assertEquals("", quizList[0].comment)

        assertEquals(HttpStatusCode.NotFound, client.get("/tic-tac-toe/quiz-list/KEK").status)
        assertEquals(HttpStatusCode.OK, client.get("/tic-tac-toe/quiz-list/ABCD").status)

        val quizInfo = client.get("/tic-tac-toe/quiz-list/ABCD").body<QuizCompleteInfo>()
        assertEquals("ABCD", quizInfo.id)

        val addedId = createQuiz(client).id

        assertEquals(2, client.get("/tic-tac-toe/quiz-list").body<QuizListResponse>().quizList.size)

        deleteQuiz(client, TicTacToeInstaller.templateId("ABCD"))

        val quizListAfterUpdates = client.get("/tic-tac-toe/quiz-list").body<QuizListResponse>().quizList
        assertEquals(1, quizListAfterUpdates.size)
        assertEquals(addedId, quizListAfterUpdates[0].id)

        assertEquals(HttpStatusCode.OK, client.get("/tic-tac-toe/quiz-list/$addedId").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/tic-tac-toe/quiz-list/ABCD").status)
    }

    private suspend fun createQuiz(client: HttpClient): TemplateId {
        client.post("/tic-tac-toe/api/quiz") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(createTemplate))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)

            val jsonObject: JsonObject = Json.decodeFromString(bodyAsText())
            val quiz = jsonObject["quiz-id"] as JsonObject
            return TicTacToeInstaller.templateId(
                quiz["id"]?.jsonPrimitive?.content ?: throw IllegalStateException("id cannot be null")
            )
        }
    }

    private suspend fun deleteQuiz(client: HttpClient, templateId: TemplateId) {
        client.delete("/tic-tac-toe/api/quiz/${templateId.id}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    private suspend fun createGameSession(client: HttpClient, config: Config): String {
        client.post("/tic-tac-toe/api/game-session") {
            contentType(ContentType.Application.Json)
            setBody(QuizRequest("ABCD", config))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)

            val jsonObject: JsonObject = Json.decodeFromString(bodyAsText())
            val session = jsonObject["session"] as JsonObject
            return session["id"]?.jsonPrimitive?.content ?: throw IllegalStateException("id cannot be null")
        }
    }

    private suspend fun createHostWebSocketSession(client: HttpClient, sessionId: SessionId): ClientWebSocketSession {
        return client.webSocketSession("/ws/tic-tac-toe/host/${sessionId.id}")
    }

    private suspend fun createPlayerWebSocketSession(client: HttpClient, sessionId: SessionId): ClientWebSocketSession {
        return client.webSocketSession("/ws/tic-tac-toe/client/${sessionId.id}")
    }

    private fun ApplicationTestBuilder.setupServer() {
        application {
            configurePlatform()
            installActivity(TicTacToeInstaller(listOf(testQuizFile)))
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

    private suspend fun ClientWebSocketSession.sendRequest(event: TicTacToeRequest<TicTacToeRequestPayload>) {
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
    private fun encodeTicTacToeRequestToJson(event: TicTacToeRequest<TicTacToeRequestPayload>): String {
        return when (event.payload) {
            is QuestionRequest -> Json.encodeToString(event as TicTacToeRequest<QuestionRequest>)
            is SetFieldRequest -> Json.encodeToString(event as TicTacToeRequest<SetFieldRequest>)
            is NextHintRequest -> Json.encodeToString(event as TicTacToeRequest<NextHintRequest>)
            is ShowAnswerRequest -> Json.encodeToString(event as TicTacToeRequest<ShowAnswerRequest>)
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