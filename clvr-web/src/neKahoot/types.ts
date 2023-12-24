// Common types for game state

/**
 * A session is a unique identifier for a game session, which is used to connect to the game server.
 *
 * It is returned by the POST /api/game-session endpoint.
 */
export type Session = {
    id: string
}

export type Error = {
    error_message: string
    id: number
}

/**
 * Describes the current game state.
 */
export type GameState = {
    state: "_LOADING"
} | {
    state: "PREPARING"
    players: {name: string}[]
} | {
    state: "OPENED_QUESTION"
    question: string
    info?: {
        answer: string
        answerDescription?: string
        answered: number
    }
    answerOptions: string[]
    timeLimit: Date
    givenAnswer?: string
} | {
    state: "RESULTS"
    players: {
        playerName: string
        score: number
        correctAnswers: number
    }[]
} | {
    state: "SHOW_QUESTION_ANSWER"
    question: string
    answer: string
    answerDescription?: string
    answerOptions: string[]
    timeLimit: Date
    givenAnswer?: string
}

type TemplateInfo = {
    name: string
    id: string
    comment: string
}

export type Role = "host" | "player"

export type Request = {
    kind: "GIVE_ANSWER",
    answer: string,
} | {
    kind: "NEXT_QUESTION",
} | {
    kind: "START_GAME",
}
