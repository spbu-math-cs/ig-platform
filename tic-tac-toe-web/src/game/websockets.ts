import {useEffect, useState} from "react"
import useWebSocket, {ReadyState} from "react-use-websocket"
import {GameState, Session} from "./types"
import {Request} from "./wsRequests"

type Role = "host" | "client"

/**
 * Connects to the game server and returns the current game state and a function to send requests.
 *
 * This function works as a React hook and should not be used multiple times. Instead, the returned
 * values should be passed down to child components as props or context.
 *
 * Reconnecting is handled automatically.
 *
 * See app/api-test/page.ts for an example.
 *
 * @param role The role being played
 * @param session The session to connect with (returned by POST /api/game-session endpoint)
 * @returns A tuple containing the current game state and a function to send requests
 */
export function useServerState(role: Role, session: Session): [GameState, (action: Request) => void] {
    const url = new URL(`ws/${role}/${session.id}`, process.env["WEBSOCKET_GAME_SERVER_URL"] ?? "ws://0.0.0.0:8080/ws")

    const {sendJsonMessage, lastJsonMessage, readyState} = useWebSocket(url.toString(), {
        shouldReconnect: () => true,
    })

    console.log(lastJsonMessage);

    const [gameState, setGameState] =
        useState<GameState>({state: "_LOADING"})

    useEffect(() => {
        if (readyState != ReadyState.OPEN) {
            setGameState({state: "_LOADING"})
        }
    }, [readyState])

    useEffect(() => {
        const msg = lastJsonMessage as any
        if (msg === null) return

        const state: string = msg.state
        if (state === "OPENED_QUESTION_HOST") {
            const q = msg.payload.question as any
            setGameState({
                state: state,
                board: msg.payload.board,
                question: {
                    row: q.row,
                    column: q.column,
                    question: q.question,
                    answer: q.answer,
                    hints: q.hints,
                    currentHintsNum: q["current_hints_num"],
                },
            })
        } else if (state === "OPENED_QUESTION_CLIENT") {
            const q = msg.payload.question as any
            setGameState({
                state: state,
                board: msg.payload.board,
                question: {
                    row: q.row,
                    column: q.column,
                    question: q.question,
                    currentHints: q["current_hints"],
                },
            })
        } else if (state === "OPENED_QUESTION_WITH_ANSWER") {
            const q = msg.payload.question as any
            setGameState({
                state: state,
                board: msg.payload.board,
                question: {
                    row: q.row,
                    column: q.column,
                    question: q.question,
                    answer: q.answer,
                },
            })
        } else if (state === "MAIN_BOARD") {
            setGameState({
                state: state,
                board: msg.payload.board,
            })
        }
    }, [lastJsonMessage])

    return [gameState, (action: Request) => {
        let request: any

        if (action.type == "OPEN_QUESTION") {
            request = {
                session: session,
                type: "OPEN_QUESTION",
                payload: {
                    row: action.row,
                    column: action.column,
                },
            }
        } else if (action.type == "SET_FIELD") {
            request = {
                session: session,
                type: "SET_FIELD",
                payload: {
                    row: action.row,
                    column: action.column,
                    mark: action.mark,
                },
            }
        } else if (action.type == "SHOW_ANSWER") {
            request = {
                session: session,
                type: "SHOW_ANSWER",
                payload: {
                    row: action.row,
                    column: action.column,
                },
            }
        } else if (action.type == "SHOW_NEXT_HINT") {
            request = {
                session: session,
                type: "SHOW_NEXT_HINT",
                payload: {
                    row: action.row,
                    column: action.column,
                    // "current_hints_num": action.currentHintsNum,
                },
            }
        } else {
            checkExhausted(action)
        }

        sendJsonMessage(request)
    }]
}
