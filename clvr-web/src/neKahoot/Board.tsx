import React, {useEffect, useState} from 'react'
import {GameState} from "@/neKahoot/types"
import {useServerState} from "@/neKahoot/websockets"
import {checkExhausted} from "@/utils"
import {Lobby} from "@/components/Lobby"
import {ScoreTable} from "@/neKahoot/ScoreTable";


interface BoardProps {
    isHost: boolean
    sessionId: string
}

function Countdown({timeLimit}: { timeLimit: Date }) {
    const [timeLeft, setTimeLeft] = useState<number>(0)

    useEffect(() => {
        const interval = setInterval(() => {
            setTimeLeft(Math.max(0, Math.round((timeLimit.getTime() - Date.now()) / 1000)))
        }, 100)
        return () => clearInterval(interval)
    }, [timeLimit])

    return (
        <p className="text-primary font-bold text-6xl">{timeLeft}</p>
    )
}

function Question({question}: { question: string }) {
    return (
        <div className="bg-panel p-16 rounded-xl h-96 flex items-center">
            <p className="text-primary font-bold text-6xl">{question}</p>
        </div>
    )
}

function AnswerOption({answer, onClick, selected, correct}: {
    answer: string,
    onClick: () => void,
    selected?: boolean,
    correct?: boolean
}) {
    return (
        <button className={`bg-panel p-8 rounded-xl grow m-8 hover:scale-110 duration-500 
            ${selected ? "bg-quit scale-105" : ""}
            ${correct ? "bg-next scale-110" : ""}`}>
            <p
                onClick={onClick}
                className={`text-primary font-bold text-6xl duration-500
                    ${correct ? "text-X" : ""}`}>
                {answer}
            </p>
        </button>
    )
}

export function Board({isHost, sessionId}: BoardProps) {
    const [game, errors, sendMessage] = useServerState(isHost ? "host" : "player", {"id": sessionId})

    let content
    if (game.state == "_LOADING") {
        content = <p className="bold text-primary font-bold text-6xl">Loading...</p>
    } else if (game.state == "OPENED_QUESTION") {
        content = <div>
            <div className="flex justify-center">
                <Countdown timeLimit={game.timeLimit}/>
            </div>
            <Question question={game.question}/>
            <div className="flex">
                {game.answerOptions.map((answer, i) =>
                    <AnswerOption
                        onClick={() => sendMessage({
                            kind: "GIVE_ANSWER",
                            answer: answer,
                        })}
                        key={i}
                        answer={answer}
                        selected={answer == game.givenAnswer}
                    />)}
            </div>
        </div>
    } else if (game.state == "SHOW_QUESTION_ANSWER") {
        content = <div>
            <div className="flex justify-center">
                <Countdown timeLimit={game.timeLimit}/>
            </div>
            <Question question={game.question}/>
            <div className="flex">
                {game.answerOptions.map((answer, i) =>
                    <AnswerOption
                        onClick={() => undefined}
                        key={i}
                        answer={answer}
                        selected={answer == game.givenAnswer}
                        correct={answer == game.answer}
                    />)}
            </div>
            {
                game.answerDescription &&
                <div className="p-8 rounded-xl text-4xl bg-panel text-primary"
                     dangerouslySetInnerHTML={{__html: game.answerDescription || ""}}></div>
            }
        </div>
    } else if (game.state == "PREPARING") {
        content = <Lobby
            isHost={isHost}
            sessionId={sessionId}
            game="nekahoot"
            players={game.players.map(p => ({name: p.name, team: undefined}))}
            startGame={() => sendMessage({kind: "START_GAME"})}
        />
    } else if (game.state == "RESULTS") {
        content = <ScoreTable
            isHost={isHost}
            sessionId={sessionId}
            game="nekahoot"
            players={game.players.map(p => ({name: p.playerName, score: p.score, correctAnswers : p.correctAnswers, team: undefined}))}
        />
    } else {
        checkExhausted(game)
    }

    return (
        <div className="flex flex-col justify-center items-center h-full min-h-[500px]">
            {content}
        </div>
    )
}
