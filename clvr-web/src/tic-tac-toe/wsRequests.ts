import {Mark} from "./types"

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

interface RequestShowAnswer {
    type: "SHOW_ANSWER"
    row: number
    column: number
}

interface RequestShowNextHint {
    type: "SHOW_NEXT_HINT"
    row: number
    column: number
    currentHintsNum: number
}

/**
 * A request to the game server.
 */
export type Request = {
        type: "START_GAME"
    }
    | RequestOpenQuestion
    | RequestSetField
    | RequestShowAnswer
    | RequestShowNextHint
