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
export type EnabledDisabled = "ENABLED" | "DISABLED"

export type Error = {
    error_message: string
    id: number
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

export type TemplateInfo = {
    name: string
    id: string
    comment: string
}

export type Quiz = TemplateInfo & {
    board: Question[]
}

export type ProtoQuiz = {
    name: string
    comment: string
    board: Question[]
}

export type GameConfig = {
    replaceMarks: EnabledDisabled
    openMultipleQuestions: EnabledDisabled
}

/**
 * Describes the current game state.
 */
export type GameState = {
    state: "_LOADING"
} | {
    state: "MAIN_BOARD"
    board: Board
} | {
    state: "OPENED_QUESTION_HOST"
    board: Board
    question: HostQuestion
} | {
    state: "OPENED_QUESTION_CLIENT"
    board: Board
    question: ClientQuestion
} | {
    state: "OPENED_QUESTION_WITH_ANSWER"
    question: QuestionWithAnswer,
    board: Board
}
