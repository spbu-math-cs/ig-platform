import React, {useState} from 'react'
import {OIcon} from './OIcon'
import {XIcon} from './XIcon'

const PanelColor = ["bg-metalPanel", "bg-2048Panel", "bg-purplePanel"]
const TextColor = ["text-metalText", "text-2048Text", "text-purpleText"]
const TextColorXO = ["text-metalPanel", "text-[#303030]", "text-purplePanel"]
const AnswerTextColor = ["text-metalText", "text-2048Task", "text-purpleX"]
const HoverPanelColor = ["hover:bg-metalPanel", "hover:bg-2048X", "hover:bg-purpleO"]
const AnswerColor = ["bg-metalPanel", "bg-2048X", "bg-purpleO"]
const TaskColor = ["bg-metalTask", "bg-2048Task", "bg-purpleTask"]

const task = ["Это условие задачи1", "Это условие задачи2", "Это условие задачи3", "Это условие задачи4", "Это условие задачи5",
    "Это условие задачи6", "Это условие задачи7", "Это условие задачи8", "Это условие задачи9", ""]

const answer = ["Это ответ задачи1", "Это ответ задачи2", "Это ответ задачи3", "Это ответ задачи4", "Это ответ задачи5",
    "Это ответ задачи6", "Это ответ задачи7", "Это ответ задачи8", "Это ответ задачи9", ""]

const rows = 3
const cols = 3


interface PlayerProp {
    themeNumber: number
    winner: string,
    playerX: boolean,
    squares: Array<any>,
    isHost: boolean,

    handlePlayer(i: number): void,

    handleRestartGame(): void,
}

interface SquareProp {
    value: any,

    onClick(): void,
}

export const Board = ({themeNumber, winner, playerX, handlePlayer, handleRestartGame, squares, isHost}: PlayerProp) => {
    const [cellNumber, setCell] = useState<number>(9)

    function Square({value, onClick}: SquareProp) {
        return (
            <button
                className={`flex h-[190px] w-[190px] md:h-[160px] md:w-[160px] items-center justify-center ${PanelColor[themeNumber]} rounded-2xl shadow-md active:scale-125 transition duration-200 ease-in hover:bg-[#18272e] shadow-gray-400/30`}
                onClick={onClick} disabled={!!winner}>
                {value}
            </button>
        )

    }

    function value(i: number) {
        let value
        if (squares[i] === "X") {
            value = <XIcon themeNum={themeNumber}/>
        } else if (squares[i] === "O") {
            value = <OIcon themeNum={themeNumber}/>
        } else if (squares[i] != null) {
            value =
                <p className={`text-md ${TextColor[themeNumber]} uppercase font-bold text-xl md:text-2xl space-y-12`}>
                    {squares[i]}
                </p>
        }
        return value

    }

    const renderSquare = (i: number) => {
        return <Square value={value(i)} onClick={() => {
            isHost ? (handlePlayer(i), setCell(i)) : {}
        }}/>
    }

    function setX() {
        handlePlayer(rows * cols + 1)
    }

    function setO() {
        handlePlayer(rows * cols + 2)
    }


    return (
        <div className={"space-y-32 space-x-44"}>
            <div className="w-[800px] md:[w-300px] rounded-lg flex items-center justify-center space-y-10  space-x-36">
                <div className="board">
                    <div className="w-[700px] md:[w-500px] rounded-lg flex items-center justify-center space-x-40">
                        <div>
                            {playerX
                                ?
                                <div
                                    className={`text-white ${PanelColor[themeNumber]} text-2xl px-6 py-1.5 w-36 space-y-8 rounded-lg font-medium uppercase`}>
                                <span className={`${TextColor[themeNumber]} text-2xl font-bold`}>
                                X </span> {" "} Turn
                                </div>
                                :
                                <div
                                    className={`text-white ${PanelColor[themeNumber]} text-2xl px-6 py-1.5 w-36 space-y-8 rounded-lg font-medium  uppercase`}>
                                <span className={`${TextColor[themeNumber]} text-2xl  font-bold`}>
                                O</span>{" "} Turn
                                </div>
                            }
                        </div>

                        <div className="md:[w-400px] rounded-lg flex items-center justify-center space-x-4 ml-4">
                            <button onClick={setX}
                                    className={`button px-4 py-2 ml-19 hover:ring-4 hover:ring-cyan-300 rounded-md bg-[#f3b236] space-y-16 ${HoverPanelColor[themeNumber]}`}>
                                <span className={`${TextColorXO[themeNumber]} text-2xl  font-bold`}> X </span>
                            </button>
                            <button onClick={setO}
                                    className={`group button px-4 py-2 hover:ring-4 hover:ring-cyan-300 rounded-md bg-[#f3b236] space-y-16 ${HoverPanelColor[themeNumber]}`}>
                                <span className={`${TextColorXO[themeNumber]} text-2xl  font-bold`}> O </span>
                            </button>


                            <button onClick={handleRestartGame}
                                    className={`group button px-4 py-2 hover:ring-4 hover:ring-cyan-300 rounded-md bg-[#f3b236] space-y-16 ${HoverPanelColor[themeNumber]}`}>
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
                <div className={"space-y-4"}>
                    <div
                        className={`mt-24 w-[500px] h-[400px] md:[w-400px] px-30 py-[100px] ${TaskColor[themeNumber]} rounded-lg flex items-top justify-center`}>
                        <button
                            className={` rounded-xl py-30 px-40 text-3xl md:text-4xl font-extrabold ${TextColor[themeNumber]}`}>
                            {task[cellNumber]}
                        </button>
                    </div>
                    {isHost ?
                        <div
                            className={`w-[500px] h-[100px] ${AnswerColor[themeNumber]} rounded-lg flex items-top justify-center`}>
                            <button
                                className={`px-4 rounded-2xl text-3xl md:text-3xl font-extrabold justify-center ${AnswerTextColor[themeNumber]}`}>
                                {answer[cellNumber]}
                            </button>
                        </div>
                        : <div></div>
                    }
                </div>
            </div>
        </div>
    )
}
