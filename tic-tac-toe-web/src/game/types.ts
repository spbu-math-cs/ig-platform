// Common types for game state

/**
 * A session is a unique identifier for a game session, which is used to connect to the game server.
 *
 * It is returned by the POST /api/game-session endpoint.
 */
export type Session = {
    id: string
}

export type Mark = "X" | "O" | "EMPTY" | "NOT_OPENED"

export type Error = {
    error_message: string
}

type Cell = {
    row: number
    column: number
    mark: Mark
    topic: string
}

type Board = {
    cells: Cell[]
}

type Question = {
    row: number
    column: number
    question: string
    answer: string
    topic: string
    hints: string[]
}

type HostQuestion = {
    row: number
    column: number
    question: string
    answer: string
    hints: string[]
    currentHintsNum: number
}

type ClientQuestion = {
    row: number
    column: number
    question: string
    currentHints: string[]
}

type QuestionWithAnswer = {
    row: number
    column: number
    question: string
    answer: string
}

export type QuizInfo = {
    name: string
    id: string
    comment: string
}

export type Quiz = QuizInfo & {
    board: Question[]
}

/**
 * The game state when the game is connecting, reconnecting, or otherwise not ready.
 *
 * TODO: differentiate between reconnection attempt and failed connection
 */
interface GameStateLoading {
    state: "_LOADING"
}

/**
 * The game state where the board is shown.
 */
interface GameStateMainBoard {
    state: "MAIN_BOARD"
    board: Board
}

interface OpenedQuestionHost {
    state: "OPENED_QUESTION_HOST"
    board: Board
    question: HostQuestion
}

interface OpenedQuestionClient {
    state: "OPENED_QUESTION_CLIENT"
    board: Board
    question: ClientQuestion
}

interface OpenedQuestionWithAnswer {
    state: "OPENED_QUESTION_WITH_ANSWER"
    question: QuestionWithAnswer,
    board: Board
}

/**
 * Describes the current game state.
 */
export type GameState =
    GameStateLoading
    | GameStateMainBoard
    | OpenedQuestionHost
    | OpenedQuestionClient
    | OpenedQuestionWithAnswer
