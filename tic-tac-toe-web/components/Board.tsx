import React from 'react'
import {OIcon} from './OIcon'
import {XIcon} from './XIcon'

const TextColor = ["text-metalText", "text-2048Text", "text-purpleText"];
const HoverPanelColor = ["hover:bg-metalPanel", "hover:bg-2048X", "hover:bg-purpleO"];

interface PlayerProp {
    themeNumber: number
    winner: string,
    playerX: boolean,
    squares: Array<any>,

    handlePlayer(i: number): void,

    handleRestartGame(): void,
}

interface SquareProp {
    value: any,

    onClick(): void,
}

export const Board = ({themeNumber, winner, playerX, handlePlayer, handleRestartGame, squares}: PlayerProp) => {

    function Square({value, onClick}: SquareProp) {
        return (
            <button className="square" onClick={onClick} disabled={winner ? true : false}>
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
            value = <p className={`text-md ${TextColor[themeNumber]} uppercase font-bold  md:text-m space-y-12`}>
                {squares[i]}
            </p>
        }
        return value;

    }

    const renderSquare = (i: number) => {
        return <Square value={value(i)} onClick={() => handlePlayer(i)}/>
    }


    return (
        <div>
            <div className="board">
                <div className=" w-[300px] md:[w-400px] rounded-lg flex items-center justify-center space-x-10">
                    <div>
                        {playerX
                            ?
                            <div
                                className="text-white bg-gray-700 text-xl px-4 py-1 w-28 rounded-lg font-medium uppercase">
                                <span className={`${TextColor[themeNumber]} text-2xl font-bold`}>
                                X </span> {" "} Turn
                            </div>
                            :
                            <div
                                className="text-white bg-gray-700 text-xl px-4 py-1 w-28 rounded-lg font-medium  uppercase">
                                <span className={`${TextColor[themeNumber]} text-2xl  font-bold`}>
                                O</span>{" "} Turn
                            </div>
                        }
                    </div>


                    <button onClick={handleRestartGame}
                            className={`group button px-2 py-1 hover:ring-4 hover:ring-cyan-300 rounded-md bg-[#f3b236] ${HoverPanelColor[themeNumber]}`}>
                        <svg xmlns="http://www.w3.org/2000/svg"
                             className="h-8 w-8 group-hover:rotate-180 transition duration-300 eas-in  " fill="none"
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
        </div>
    )
}
