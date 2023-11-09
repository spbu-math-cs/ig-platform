// Common types for game state

/**
 * A session is a unique identifier for a game session, which is used to connect to the game server.
 *
 * It is returned by the POST /api/game-session endpoint.
 */
export type Session = {
    id: string
}

export type Mark = "X" | "O" | "NOT_OPENED"

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

/**
 * The game state when a question is opened.
 */
interface GameStateQuestion {
    state: "OPENED_QUESTION_HOST"
    board: Board
    question: Question
}

// TODO: change this interface (and the above one) to comply with API
interface GameStateQuestionClient {
    state: "OPENED_QUESTION_CLIENT"
    board: Board
    question: Question
}

/**
 * Describes the current game state.
 */
export type GameState = GameStateLoading | GameStateMainBoard | GameStateQuestion | GameStateQuestionClient
