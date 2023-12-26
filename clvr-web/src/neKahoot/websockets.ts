import {Error, GameState, Session, Request, Role} from "@/neKahoot/types"
import useWebSocket, {ReadyState} from "react-use-websocket"
import {useEffect, useState} from "react"
import {checkExhausted} from "@/utils"

export function useServerState(role: Role, session: Session): [GameState, Error[], (action: Request) => void] {
    const url = new URL(`ws/nekahoot/${role}/${session.id}`, process.env["WEBSOCKET_GAME_SERVER_URL"] ?? "ws://0.0.0.0:8080/ws")

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

    const [errors, setErrors] = useState<Error[]>([])
    const [nextErrorId, setNextErrorId] = useState(0)

    useEffect(() => {
        const msg = lastJsonMessage as any
        if (msg === null) return

        const state: string = msg.state
        if (state === "OPENED_QUESTION") {
            const q = msg.payload.question as any
            if (q.answer !== undefined) {
                setGameState({
                    state: state,
                    info: {
                        answer: q.answer,
                        answerDescription: q["answer_description"],
                        answered: q.answered,
                    },
                    timeLimit: new Date(Date.now() + q.time),
                    answerOptions: q["answer_options"],
                    question: q.question
                })
            } else {
                setGameState({
                    state: state,
                    timeLimit: new Date(Date.now() + q.time),
                    answerOptions: q["answer_options"],
                    question: q.question,
                    givenAnswer: q.given_answer,
                })
            }
        } else if (state === "SHOW_QUESTION_ANSWER") {
            const q = msg.payload.question as any
            setGameState({
                state: state,
                question: q.question,
                answer: q.answer,
                answerDescription: q["answer_description"],
                answerOptions: q["answer_options"],
                timeLimit: new Date(Date.now() + q.time),
                givenAnswer: (gameState.state == "OPENED_QUESTION" ? gameState.givenAnswer : undefined)
            })
        } else if (state == "ERROR") {
            console.log("ERROR: " + msg.payload.message)

            const newErrors = [...errors]
            newErrors.push({error_message: msg.payload.message, id: nextErrorId})
            setNextErrorId(nextErrorId + 1)
            setErrors(newErrors)
            setTimeout(() => {
                setErrors(errors => {
                    const newErrors = [...errors]
                    newErrors.shift()
                    return newErrors
            })
            }, 5000)
        } else if (state == "PREPARING") {
            setGameState({
                state: state,
                players: msg.payload.players
            })
        } else if (state == "RESULT") {
            setGameState({
                state: state,
                players: msg.payload.results
            })
        }
    }, [lastJsonMessage])

    return [gameState, errors, (action: Request) => {
        let request: any

        if (action.kind == "NEXT_QUESTION") {
            request = {
                session: session,
                type: action.kind
            }
        } else if (action.kind == "GIVE_ANSWER") {
            request = {
                session: session,
                type: action.kind,
                payload: {
                    answer: action.answer
                }
            }
        } else if (action.kind == "START_GAME") {
            request = {
                session: session,
                type: action.kind,
            }
        } else {
            checkExhausted(action)
        }

        sendJsonMessage(request)
    }]
}
