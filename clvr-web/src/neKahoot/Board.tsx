import React, {useEffect, useState} from 'react'
import {GameState} from "@/neKahoot/types"
import {useServerState} from "@/neKahoot/websockets"
import {checkExhausted} from "@/utils"
import {Lobby} from "@/components/Lobby"


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
            ${correct ? "bg-next scale-120" : ""}`}
            onClick={onClick}>
            <p
                className={`text-primary font-bold text-6xl duration-500
                    ${correct ? "text-X" : ""}`}>
                {answer}
            </p>
        </button>
    )
}

export function Board({isHost, sessionId}: BoardProps) {
    const [game, errors, sendMessage] = useServerState(isHost ? "host" : "player", {"id": sessionId})

    function nextQuestionAction() {
        sendMessage({
            kind: "NEXT_QUESTION"
        })
    }

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
                        onClick={() => {
                            console.log("Giving answer")
                            sendMessage({
                                kind: "GIVE_ANSWER",
                                answer: answer,
                            })
                        }}
                        key={i}
                        answer={answer}
                        selected={answer == game.givenAnswer}
                    />)}
            </div>
        </div>
    } else if (game.state == "SHOW_QUESTION_ANSWER") {
        content = <div>
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
            {
                isHost && 
                <div>
                    <div className="flex justify-center">
                        <button className={`bg-panel p-8 rounded-xl grow m-8 hover:scale-120 scale-105`} onClick={nextQuestionAction}>
                            <p className={`text-primary font-bold text-6xl duration-500`}>Next question</p>
                        </button>
                    </div>
                </div>
            }
        </div>
    } else if (game.state == "PREPARING") {
        content = <Lobby
            isHost={isHost}
            sessionId={sessionId}
            game="nekahoot"
            players={game.players.map(p => ({name: p.name, team: undefined}))}
            startGame={() => sendMessage({kind: "START_GAME"})}
            sendTeamToServer={(_) => {}}
        />
    } else if (game.state == "RESULT") {
        const players: Player[] = game.players.map(player => {
            return {
            name: player.player_name,
            score: player.score,
            answered: player.correct_questions
        }})
        content = <FinalBoard sessionId={sessionId} players={players}></FinalBoard>
    } else {
        checkExhausted(game)
    }

    return (
        <div className="flex flex-col justify-center items-center h-full min-h-[500px]">
            {content}
        </div>
    )
}

type Player = {
    name: string,
    score: number,
    answered: number
}

interface PlayerProps {
    player: Player
}

interface PlayersListProps {
    sessionId: string
    players: Player[]
}


export const FinalBoard = ({sessionId, players}: PlayersListProps) => {
    return (
        <div className="flex flex-col items-center w-[1000px] rounded-2xl bg-square h-auto mt-8 pb-10 pt-4">
            <div className={`px-20 flex items-center w-[1000px] rounded-2xl bg-square space-x-96`}>
                <div className={"flex flex-col space-y-1 mt-0"}>
                    <div className={"flex flex-row justify-between pb-2"}>
                        <div className = {`flex flex-row justify-center space-x-2`}>
                            <p className={`justify-items-start text-md text-JoinGameTxt font-extrabold py-2 text-4xl  `}>
                                Game Result
                            </p>
                            <p className={`justify-items-start text-JoinGameTxt mt-4 font-extrabold py-2 text-l`}> of game:
                                {sessionId}
                            </p>
                        </div>
                    </div>
                    <hr className="w-[844px] h-1 mx-auto my-4 bg-JoinGameTxt border-0 rounded md:my-10 "/>
                </div>
            </div>
            <div
                className="
                flex flex-col justify-items-start py-10 px-100 rounded
                mb-2 -scroll-ms-3 overflow-auto text-md text-JoinGameTxt
                max-h-[60vh] bg-square">
                {
                    players?.map(player =>
                        <PlayerResult player={player}/>
                    )
                }
            </div>
        </div>

    )
}

export function PlayerResult({player}: PlayerProps) {
    return (
        <div className="border-2 border-back w-[844px] rounded-2xl py-2 px-4 flex flex-col items-start mb-2">
            <div className="rounded-2xl flex flex-row">
                <div className="rounded-2xl py-2 px-4 flex justify-between flex-row mb-1 space-x-30 w-[810px]">
                    <p className="font-bold text-2xl">{player.name}</p>
                    <p className="font-bold text-2xl">Score: {player.score}</p>
                    <p className="font-bold text-2xl">Answered: {player.answered}</p>
                </div>
            </div>
        </div>
    )
}
