// Common types for game state

/**
 * A session is a unique identifier for a game session, which is used to connect to the game server.
 *
 * It is returned by the POST /api/game-session endpoint.
 */
export type Session = {
  id: string
}

type Mark = "X" | "O" | ""

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

/**
 * A host to server request to open a question and show it to the players.
 */
interface RequestOpenQuestion {
  type: "OPEN_QUESTION"
  row: number
  column: number
}

/**
 * A host to server request to set a field on the board.
 */
interface RequestSetField {
  type: "SET_FIELD"
  row: number
  column: number
  mark: Mark
}

/**
 * A request to the game server.
 */
export type Request = RequestOpenQuestion | RequestSetField
