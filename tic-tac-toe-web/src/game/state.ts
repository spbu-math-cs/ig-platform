// Common types for game state

/**
 * A session is a unique identifier for a game session, which is used to connect to the game server.
 *
 * It is returned by the POST /api/game-session endpoint.
 */
export type Session = {
  id: string
}

export type Mark = "X" | "O" | ""

type Cell = {
  row: number
  column: number
  mark: Mark
}

type Board = {
  cells: Cell[]
}

type Question = {
  row: number
  column: number
  text: string
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
  state: "OPENED_QUESTION"
  board: Board
  question: Question
}

/**
 * Describes the current game state.
 */
export type GameState = GameStateLoading | GameStateMainBoard | GameStateQuestion
