package com.clvr.ttt

import com.clvr.platform.api.RequestEvent
import com.clvr.platform.api.SessionId
import com.clvr.ttt.common.Config
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.math.min
import kotlin.streams.asStream

class TicTacToeGameStateControllerTest {
    private val quiz = basicTestTemplate
    private val config = Config()

    companion object {
        @JvmStatic
        fun provideCells(): Stream<Arguments> {
            return sequenceOf(
                Arguments.of(0, 0),
                Arguments.of(0, 1),
                Arguments.of(1, 0),
                Arguments.of(1, 1)
            ).asStream()
        }
    }

    private fun makeRequest(
        game: GameState,
        requestPayload: TicTacToeRequestPayload
    ): Pair<List<TicTacToeResponsePayload>, List<TicTacToeResponsePayload>> {
        val controller = TicTacToeGameController(game)
        val communicator = MockCommunicator()
        val hostChannel = communicator.hostChannel
        val clientChannel = communicator.clientChannel
        controller.handle(communicator, RequestEvent(SessionId("0"), requestPayload))

        val hostEvents = generateSequence {
            hostChannel.tryReceive().getOrNull()
        }.map {
            it.payload
        }

        val clientEvents = generateSequence {
            clientChannel.tryReceive().getOrNull()
        }.map {
            it.payload
        }

        return hostEvents.toList() to clientEvents.toList()
    }

    @ParameterizedTest
    @MethodSource("provideCells")
    fun `question request`(row: Int, column: Int) = runBlocking {
        val game = GameState(quiz, config)
        val initialContent = game.getGridContent()
        val (hostEventPayloads, clientEventPayloads) = makeRequest(game, QuestionRequest(row, column))

        val hostEventPayload = assertDoesNotThrow(
            ThrowingSupplier { hostEventPayloads.single() }
        )

        val clientEventPayload = assertDoesNotThrow(
            ThrowingSupplier { clientEventPayloads.single() }
        )

        assertEquals(HostQuestionResponse.type, hostEventPayload.type)
        hostEventPayload as HostQuestionResponse
        assertEquals(BoardView.fromGameState(game), hostEventPayload.boardView)
        val expectedHostQuestionView = HostQuestionView(
            row,
            column,
            quiz.questions[row][column].statement,
            quiz.questions[row][column].hints,
            0,
            quiz.questions[row][column].answer
        )
        assertEquals(expectedHostQuestionView, hostEventPayload.questionView)

        assertEquals(ClientQuestionResponse.type, clientEventPayload.type)
        clientEventPayload as ClientQuestionResponse
        assertEquals(BoardView.fromGameState(game), clientEventPayload.boardView)
        val expectedClientQuestionView = ClientQuestionView(
            row,
            column,
            quiz.questions[row][column].statement,
            emptyList()
        )
        assertEquals(expectedClientQuestionView, clientEventPayload.questionView)
        assertEquals(emptyList<String>(), game.getOpenedHints(row, column))
        assertEquals(initialContent, game.getGridContent())
    }

    @ParameterizedTest
    @MethodSource("provideCells")
    fun `question request after opened hint`(row: Int, column: Int) = runBlocking {
        val game = GameState(quiz, config)
        game.openNextHint(row, column)

        val (hostEventPayloads, clientEventPayloads) = makeRequest(game, QuestionRequest(row, column))

        val hostEventPayload = assertDoesNotThrow(
            ThrowingSupplier { hostEventPayloads.single() }
        )
        val clientEventPayload = assertDoesNotThrow(
            ThrowingSupplier { clientEventPayloads.single() }
        )

        assertEquals(HostQuestionResponse.type, hostEventPayload.type)
        hostEventPayload as HostQuestionResponse
        assertEquals(BoardView.fromGameState(game), hostEventPayload.boardView)
        val expectedHostQuestionView = HostQuestionView(
            row,
            column,
            quiz.questions[row][column].statement,
            quiz.questions[row][column].hints,
            min(1, quiz.questions[row][column].hints.size),
            quiz.questions[row][column].answer
        )
        assertEquals(expectedHostQuestionView, hostEventPayload.questionView)

        assertEquals(ClientQuestionResponse.type, clientEventPayload.type)
        clientEventPayload as ClientQuestionResponse
        assertEquals(BoardView.fromGameState(game), clientEventPayload.boardView)
        val expectedClientQuestionView = ClientQuestionView(
            row,
            column,
            quiz.questions[row][column].statement,
            quiz.questions[row][column].hints.take(1)
        )
        assertEquals(expectedClientQuestionView, clientEventPayload.questionView)
        assertEquals(quiz.questions[row][column].hints.take(1), game.getOpenedHints(row, column))
    }

    @ParameterizedTest
    @MethodSource("provideCells")
    fun `next hint request`(row: Int, column: Int) = runBlocking {
        val game = GameState(quiz, config)

        repeat(5) {
            val (hostEventPayloads, clientEventPayloads) = makeRequest(game, NextHintRequest(row, column))
            val hostEventPayload = assertDoesNotThrow(
                ThrowingSupplier { hostEventPayloads.single() }
            )
            val clientEventPayload = assertDoesNotThrow(
                ThrowingSupplier { clientEventPayloads.single() }
            )

            assertEquals(HostQuestionResponse.type, hostEventPayload.type)
            hostEventPayload as HostQuestionResponse
            assertEquals(BoardView.fromGameState(game), hostEventPayload.boardView)
            val expectedHostQuestionView = HostQuestionView(
                row,
                column,
                quiz.questions[row][column].statement,
                quiz.questions[row][column].hints,
                min(it + 1, quiz.questions[row][column].hints.size),
                quiz.questions[row][column].answer
            )
            assertEquals(expectedHostQuestionView, hostEventPayload.questionView)

            assertEquals(ClientQuestionResponse.type, clientEventPayload.type)
            clientEventPayload as ClientQuestionResponse
            assertEquals(BoardView.fromGameState(game), clientEventPayload.boardView)
            val expectedClientQuestionView = ClientQuestionView(
                row,
                column,
                quiz.questions[row][column].statement,
                quiz.questions[row][column].hints.take(it + 1)
            )
            assertEquals(expectedClientQuestionView, clientEventPayload.questionView)

            assertEquals(quiz.questions[row][column].hints.take(it + 1), game.getOpenedHints(row, column))
        }
    }

    @ParameterizedTest
    @MethodSource("provideCells")
    fun `show answer request`(row: Int, column: Int) = runBlocking {
        val game = GameState(quiz, config)
        val initialContent = game.getGridContent()

        val (hostEventPayloads, clientEventPayloads) = makeRequest(game, ShowAnswerRequest(row, column))
        assertEquals(hostEventPayloads, clientEventPayloads)

        val eventPayload = assertDoesNotThrow(
            ThrowingSupplier { hostEventPayloads.single() }
        )

        assertEquals(ShowAnswerResponse.type, eventPayload.type)
        eventPayload as ShowAnswerResponse

        assertEquals(BoardView.fromGameState(game), eventPayload.boardView)
        val expectedQuestionWithAnswer = QuestionWithAnswer(
            row,
            column,
            quiz.questions[row][column].statement,
            quiz.questions[row][column].answer
        )
        assertEquals(expectedQuestionWithAnswer, eventPayload.question)

        assertEquals(initialContent, game.getGridContent())
    }

    @Test
    fun `set first cell`() = runBlocking {
        val game = GameState(quiz, config)
        val row = 0
        val column = 0

        val (hostEventPayloads, clientEventPayloads) = makeRequest(game, SetFieldRequest(row, column, CellContent.X))
        assertEquals(hostEventPayloads, clientEventPayloads)

        val eventPayload = assertDoesNotThrow(
            ThrowingSupplier { hostEventPayloads.single() }
        )

        assertEquals(SetFieldResponse.type, eventPayload.type)
        eventPayload as SetFieldResponse

        assertEquals(BoardView.fromGameState(game), eventPayload.boardView)
        assertEquals(GameResult.EMPTY, eventPayload.win)

        assertEquals(CellContent.X, game.getGridContent()[row][column])
    }

    @Test
    fun `set cell and win`() {
        val game = GameState(quiz, config)
        game.updateCellContent(0, 0, CellContent.X)
        val row = 1
        val column = 1

        val (hostEventPayloads, clientEventPayloads) = makeRequest(game, SetFieldRequest(row, column, CellContent.X))
        assertEquals(hostEventPayloads, clientEventPayloads)

        val eventPayload = assertDoesNotThrow(
            ThrowingSupplier { hostEventPayloads.single() }
        )

        assertEquals(SetFieldResponse.type, eventPayload.type)
        eventPayload as SetFieldResponse

        assertEquals(BoardView.fromGameState(game), eventPayload.boardView)
        assertEquals(GameResult.X, eventPayload.win)

        assertEquals(CellContent.X, game.getGridContent()[row][column])
    }
}