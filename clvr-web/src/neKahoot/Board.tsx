import React, {useEffect, useState} from 'react'
import {useServerState} from "@/tic-tac-toe/websockets"
import {GameState} from "@/neKahoot/types"


interface BoardProps {
    isHost: boolean
    sessionId: string
}

export const Board = ({isHost, sessionId}: BoardProps) => {
    // const [game, errors, sendMessage] = useServerState(isHost ? "host" : "client", {"id": sessionId})

    const [game, setGame] = useState<GameState>({
        state: "_LOADING",
    })

    useEffect(() => {
        setTimeout(() => setGame({
            state: "SHOW_QUESTION_ANSWER",
            question: "What is the best programming language?",
            answerOptions: [
                "JavaScript",
                "Java",
                "Python",
                "C++"
            ],
            timeLimit: new Date(Date.now() + 10000),
            answer: "JavaScript"
        }), 100)
    }, [])

    let content

    if (game.state == "_LOADING") {
        content = <p className="text-xl bold text-primary">Loading...</p>
    } else {
        console.error(game)
    }

    return (
        <div className="flex flex-col justify-center items-center h-full">
            {content}
        </div>
    )
}
