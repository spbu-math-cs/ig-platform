import {useEffect, useState} from "react"
import useWebSocket, {ReadyState} from "react-use-websocket"
import {GameState, Session} from "./state"
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
 * FIXME: Due to how the API is designed, there is no way to get the indication that the request was
 *  successfully fulfilled. This means that differentiating between request in progress and connection
 *  lost is... annoying.
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
        if (msg.state === "OPENED_QUESTION") {
            setGameState({
                state: msg.state,
                board: msg.payload.board,
                question: msg.payload.question,
            }) // TODO: validate
        } else if (msg.state === "MAIN_BOARD") {
            setGameState({
                state: msg.state,
                board: msg.payload.board,
            }) // TODO: validate
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
        }

        sendJsonMessage(request)
    }]
}
