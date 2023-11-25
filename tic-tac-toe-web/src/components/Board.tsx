import React, {useEffect, useState} from 'react'
import {OIcon} from './OIcon'
import {XIcon} from './XIcon'
import {useServerState} from "@/game/websockets"

const rows = 3
const cols = 3

interface PlayerProp {
    playerX: boolean
    isHost: boolean
    sessionId: string

    handleRestartGame(): void
}

interface SquareProp {
    value: any

    onClick(): void
}

export const Board = ({playerX, handleRestartGame, isHost, sessionId}: PlayerProp) => {
    const [currentPlayer, setCurrentPlayer] = useState<"X" | "O" | undefined>(undefined)
    const [game, sendMessage] = useServerState(isHost ? "host" : "client", {"id": sessionId})

    useEffect(() => {
        let k = new Audio("/LobbyMusic.mp3")

        k.addEventListener("canplaythrough", (_) => {
            k.play().then()
        }, true)
    }, [])

    function Square({value, onClick}: SquareProp) {
        return (
            <button
                className={`flex h-[190px] w-[190px] md:h-[160px] md:w-[160px] items-center justify-center bg-square rounded-2xl shadow-md active:scale-125 transition duration-200 ease-in hover:bg-[#18272e] shadow-gray-400/30`}
                onClick={onClick}>
                {value}
            </button>
        )
    }

    function value(i: number) {
        let value
        let board
        if (game.state !== "_LOADING") {
            board = game.board
        }

        if (board === undefined) {
            return undefined
        } else if (board.cells[i].mark === "X") {
            value = <XIcon/>
        } else if (board.cells[i].mark === "O") {
            value = <OIcon/>
        } else if (board.cells[i].mark === "EMPTY" || board.cells[i].mark === "NOT_OPENED") {
            value =
                <p className={`text-md text-txt uppercase font-bold text-xl md:text-2xl space-y-12`}
                   dangerouslySetInnerHTML={{__html: board.cells[i].topic}}>
                </p>
        } else {
            console.log("Unexpected mark value " + board.cells[i].mark)
            value = ""
        }
        return value
    }

    const renderSquare = (i: number) => {
        return <Square value={value(i)} onClick={() => {
            if (!isHost) return
            if (currentPlayer !== undefined) {
                sendMessage({
                    type: "SET_FIELD",
                    mark: currentPlayer,
                    row: Math.floor(i / rows),
                    column: i % cols,
                })
            } else {
                sendMessage({
                    type: "OPEN_QUESTION",
                    row: Math.floor(i / rows),
                    column: i % cols,
                })
            }
            setCurrentPlayer(undefined)
        }}/>
    }

    function setX() {
        setCurrentPlayer("X")
    }

    function setO() {
        setCurrentPlayer("O")
    }

    return (
        <div className={"space-y-32 space-x-44"}>
            <div className="w-[800px] md:[w-300px] rounded-lg flex items-center justify-center space-y-10  space-x-36">
                <div className="flex-row w-max rounded-lg mx-auto flex justify-center items-start space-x-36">

                    <div className=" mt-24 flex h-[450px] w-[350px] md:mt-16 md:h-[500px] flex-col items-center justify-center space-y-4 rounded-xl bg-back">

                        <div className="w-[700px] md:[w-500px] rounded-lg flex items-center justify-center space-x-40">
                            <div>
                                {currentPlayer == "X" ?
                                    <div
                                        className={`text-white bg-square text-2xl px-6 py-1.5 w-36 space-y-8 rounded-lg font-medium uppercase`}>
                                <span className={`text-txt text-2xl font-bold`}>
                                X </span> {" "} Turn
                                    </div>
                                    :
                                    <div
                                        className={`text-white bg-square text-2xl px-6 py-1.5 w-36 space-y-8 rounded-lg font-medium  uppercase`}>
                                <span className={`text-txt text-2xl  font-bold`}>
                                O</span>{" "} Turn
                                    </div>
                                }
                            </div>

                            <div
                                className="md:[w-400px] rounded-lg flex items-center justify-center space-x-4 ml-4">
                                <button onClick={setX}
                                        className={`button px-4 py-2 ml-19 hover:ring-4 hover:ring-cyan-300 rounded-md bg-[#f3b236] space-y-16 hover:bg-boardHover`}>
                                    <span className={`text-XO text-2xl  font-bold`}> X </span>
                                </button>
                                <button onClick={setO}
                                        className={`group button px-4 py-2 hover:ring-4 hover:ring-cyan-300 rounded-md bg-[#f3b236] space-y-16 hover:bg-boardHover`}>
                                    <span className={`text-XO text-2xl  font-bold`}> O </span>
                                </button>


                                <button onClick={handleRestartGame}
                                        className={`group button px-4 py-2 hover:ring-4 hover:ring-cyan-300 rounded-md bg-[#f3b236] space-y-16 hover:bg-boardHover`}>
                                    <svg xmlns="http://www.w3.org/2000/svg"
                                         className="h-8 w-8 group-hover:rotate-180 transition duration-300 eas-in space-y-10  "
                                         fill="none"
                                         viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                                        <path strokeLinecap="round" strokeLinejoin="round"
                                              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                                    </svg>
                                </button>
                            </div>
                        </div>

                        <div className="board-row">
                            {renderSquare(0)}
                            {renderSquare(1)}
                            {renderSquare(2)}
                        </div>

                        <div className="board-row">
                            {renderSquare(3)}
                            {renderSquare(4)}
                            {renderSquare(5)}
                        </div>

                        <div className="board-row">
                            {renderSquare(6)}
                            {renderSquare(7)}
                            {renderSquare(8)}
                        </div>
                    </div>

                    <div className="flex-col space-y-5 w-[600px] rounded-lg flex items-center">
                        <div className={`mt-24 w-[500px] min-h-[400px] h-auto md:[w-400px] px-30 py-30 bg-task rounded-lg flex items-top justify-center`}>
                            <button className={`rounded-xl py-10 px-10 text-3xl font-extrabold text-txt`}
                                dangerouslySetInnerHTML={{
                                    __html:
                                        game.state === "OPENED_QUESTION_CLIENT"
                                        || game.state === "OPENED_QUESTION_HOST"
                                            ? game.question.question
                                            : "",
                                }}>
                            </button>
                        </div>
                        {game.state == "OPENED_QUESTION_HOST" || game.state == "OPENED_QUESTION_WITH_ANSWER" ?
                            <div
                                className={`w-[500px] min-h-[100px] h-auto  bg-answerPanel rounded-lg flex items-top justify-center`}>
                                <button
                                    className={`px-4 py-3 rounded-2xl text-2xl font-extrabold justify-center text-answerTxt`}
                                    onClick={() => sendMessage({
                                        type: "SHOW_ANSWER",
                                        row: game.question.row,
                                        column: game.question.column,
                                    })}>
                                    {game.question.answer}
                                </button>
                            </div>
                            : <div></div>
                        }

                        {game.state == "OPENED_QUESTION_HOST" &&
                            <>
                                {
                                    game.question.hints.map((hint, i) =>
                                        <div key={i}
                                             className="w-[500px] pt-3  min-h-[100px] h-auto rounded-xl font-extrabold text-xl text-answerTxt bg-answerPanel">
                                            <div className="grow px-4" dangerouslySetInnerHTML={{__html: hint}}></div>
                                            {
                                                game.question.currentHintsNum == i &&
                                                <button
                                                    onClick={() => sendMessage({
                                                        type: "SHOW_NEXT_HINT",
                                                        currentHintsNum: i,
                                                        row: game.question.row,
                                                        column: game.question.column,
                                                    })}
                                                    className="flex ml-3 px-3 mt-1 hover:ring-4 hover:ring-cyan-300 hover:bg-answerPanel text-answerTxt rounded-xl py-1 outline outline-offset-2 outline-1 ">
                                                    SHOW
                                                </button>
                                            }
                                        </div>,
                                    )
                                }
                            </>
                        }
                        {game.state == "OPENED_QUESTION_CLIENT" &&
                            <div className="px-4 p-3 rounded-2xl w-[500px] min-h-[100px] py-3 font-extrabold h-auto text-xl  text-answerTxt bg-answerPanel">
                                {
                                    game.question.currentHints.map(hint =>
                                        <div className="grow" key={hint} dangerouslySetInnerHTML={{__html: hint}}>
                                        </div>,
                                    )
                                }
                            </div>
                        }
                    </div>
                </div>
            </div>
        </div>
    )
}
