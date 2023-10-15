import React, {useState} from 'react'
import {OIcon} from './OIcon'
import {XIcon} from './XIcon'

const PanelColor = ["bg-metalPanel", "bg-2048Panel", "bg-purplePanel"];
const TextColor = ["text-metalText", "text-2048Text", "text-purpleText"];
const HoverPanelColor = ["hover:bg-metalPanel", "hover:bg-2048X", "hover:bg-purpleO"];
const TaskColor = ["bg-metalTask", "bg-2048Task", "bg-purpleTask"]

const task = ["Это условие задачи1", "Это условие задачи2", "Это условие задачи3", "Это условие задачи4", "Это условие задачи5",
    "Это условие задачи6", "Это условие задачи7", "Это условие задачи8", "Это условие задачи9", ""]


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
                className={`flex h-[190px] w-[190px] md:h-[130px] md:w-[130px] items-center justify-center ${PanelColor[themeNumber]} rounded-2xl shadow-md active:scale-125 transition duration-200 ease-in hover:bg-[#18272e] shadow-gray-400/30`}
                onClick={onClick} disabled={!!winner}>
                {value}
            </button>
        )

    }

    function value(i: number) {
        let value;
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
        return value;

    }

    const renderSquare = (i: number) => {
        return <Square value={value(i)} onClick={() => {
            isHost ? (handlePlayer(i), setCell(i)) : {}
        }}/>
    }


    return (
        <div>
            <div className=" w-[800px] md:[w-800px] rounded-lg flex items-center justify-center space-y-32  space-x-36">
                <div className="board">
                    <div className=" w-[700px] md:[w-700px] rounded-lg flex items-center justify-center space-x-24">
                        <div>
                            {playerX
                                ?
                                <div
                                    className={`text-white ${PanelColor[themeNumber]} text-xl px-4 py-1 w-28 space-y-8 rounded-lg font-medium uppercase`}>
                                <span className={`${TextColor[themeNumber]} text-2xl font-bold`}>
                                X </span> {" "} Turn
                                </div>
                                :
                                <div
                                    className={`text-white ${PanelColor[themeNumber]} text-xl px-4 py-1 w-28 space-y-8 rounded-lg font-medium  uppercase`}>
                                <span className={`${TextColor[themeNumber]} text-2xl  font-bold`}>
                                O</span>{" "} Turn
                                </div>
                            }
                        </div>


                        <button onClick={handleRestartGame}
                                className={`group button px-2 py-1 hover:ring-4 hover:ring-cyan-300 rounded-md bg-[#f3b236] space-y-16 ${HoverPanelColor[themeNumber]}`}>
                            <svg xmlns="http://www.w3.org/2000/svg"
                                 className="h-8 w-8 group-hover:rotate-180 transition duration-300 eas-in space-y-10  "
                                 fill="none"
                                 viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
                                <path strokeLinecap="round" strokeLinejoin="round"
                                      d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                            </svg>
                        </button>
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
                <div
                    className={` w-[700px] md:[w-700px] px-300 py-300 ${TaskColor[themeNumber]} rounded-lg flex items-top justify-center`}>
                    <button
                        className={` rounded-xl px-40 py-40 text-3xl md:text-4xl font-extrabold ${TextColor[themeNumber]}`}>
                        {task[cellNumber]}
                    </button>
                </div>
            </div>
        </div>
    )
}
