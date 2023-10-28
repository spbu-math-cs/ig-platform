import {Mark} from "./state"

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
