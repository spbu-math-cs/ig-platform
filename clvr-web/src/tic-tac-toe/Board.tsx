import React, {useEffect, useState} from 'react'
import {OIcon} from './OIcon'
import {XIcon} from './XIcon'
import {useServerState} from "@/tic-tac-toe/websockets"
import {ErrorSnackbar} from '@/components/Errors'
import {Lobby} from "@/components/Lobby"

const rows = 3
const cols = 3

interface BoardProps {
    isHost: boolean
    sessionId: string
}

interface SquareProp {
    value: any
    selected: boolean

    onClick(): void
}

function Square({value, onClick, selected}: SquareProp) {
    return (
        <button
            className={`
                flex h-[190px] w-[190px] md:h-[160px] md:w-[160px] items-center justify-center bg-square rounded-2xl
                shadow-md active:scale-125 hover:bg-[#18272e] shadow-gray-400/30
                transition-all duration-200 ease-in
                ${selected && 'bg-[#18272e] scale-110'}
            `}
            onClick={onClick}>
            {value}
        </button>
    )
}

export const Board = ({isHost, sessionId}: BoardProps) => {
    const [currentPlayer, setCurrentPlayer] = useState<"X" | "O" | undefined>(undefined)
    const [game, errors, sendMessage] = useServerState(isHost ? "host" : "client", {"id": sessionId})

    function value(i: number) {
        let value
        let board
        if (game.state !== "_LOADING" && game.state !== "PREPARING") {
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
        return <Square
            value={value(i)}
            selected={
                (game.state == "OPENED_QUESTION_CLIENT"
                    || game.state == "OPENED_QUESTION_HOST"
                    || game.state == "OPENED_QUESTION_WITH_ANSWER")
                && i == game.question.row * 3 + game.question.column
            }
            onClick={() => {
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

    if (game.state == "PREPARING") {
        return <Lobby
            isHost={isHost}
            sessionId={sessionId}
            game="tic_tac_toe"
            sendMessage={sendMessage}
        />
    }

    return (
        <div className={``}>
            <div className={`space-y-32 space-x-44 items-center`}>
                <div
                    className="w-[800px] md:[w-300px] rounded-lg flex items-center justify-center space-y-10  space-x-36">
                    <div className="flex-row w-max rounded-lg mx-auto flex justify-center items-start space-x-36">
                        <div
                            className=" mt-24 flex h-[450px] w-[350px] md:mt-16 md:h-[500px] flex-col items-center justify-center space-y-4 rounded-xl bg-back">
                            <div className="grow"></div>
                            {isHost ?
                                <div
                                    className="w-[700px] md:[w-500px] rounded-lg flex items-center justify-center space-x-40">
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
                                    </div>
                                </div>
                                : <div className={"py-4"}></div>
                            }

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

                        <div className="flex-col w-[600px] rounded-lg flex items-center">
                            <div
                                className={`mt-24 w-[500px] min-h-[400px] h-auto md:[w-400px] bg-task rounded-lg flex items-top justify-center`}>
                                <button className={`rounded-xl py-10 px-10 text-3xl font-extrabold text-txt`}
                                        dangerouslySetInnerHTML={{
                                            __html:
                                                game.state === "OPENED_QUESTION_CLIENT"
                                                || game.state === "OPENED_QUESTION_HOST"
                                                || game.state === "OPENED_QUESTION_WITH_ANSWER"
                                                    ? game.question.question
                                                    : "",
                                        }}>
                                </button>
                            </div>
                            {game.state == "OPENED_QUESTION_HOST" || game.state == "OPENED_QUESTION_WITH_ANSWER" ?
                                <div
                                    className={`w-[500px] min-h-[100px] h-auto mt-4  bg-answerPanel rounded-lg flex items-top justify-center`}>
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
                                                 className="w-[500px] pt-3 mt-4 min-h-[100px] h-auto rounded-xl font-extrabold text-xl text-answerTxt bg-answerPanel pb-2">
                                                <div className="grow px-4"
                                                     dangerouslySetInnerHTML={{__html: hint}}></div>
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
                                <div className={" mt-4 space-y-4"}>
                                    <button
                                        className={`px-4 rounded-2xl text-4xl font-extrabold justify-center  hover:bg-task text-center w-[500px] min-h-[100px] h-auto text-answerTxt bg-answerPanel`}
                                        onClick={() => {
                                        }}>
                                        ANSWER
                                    </button>
                                    {game.question.currentHints.length ? <div
                                        className="px-4 p-3 rounded-2xl w-[500px] min-h-[100px] py-3 font-extrabold h-auto text-xl  text-answerTxt bg-answerPanel">
                                        {
                                            game.question.currentHints.map(hint =>
                                                <div className="grow" key={hint}
                                                     dangerouslySetInnerHTML={{__html: hint}}>
                                                </div>,
                                            )
                                        }
                                    </div> : <div></div>
                                    }
                                </div>
                            }
                        </div>
                    </div>
                </div>
                <ErrorSnackbar errors={errors}/>
            </div>
        </div>
    )
}
