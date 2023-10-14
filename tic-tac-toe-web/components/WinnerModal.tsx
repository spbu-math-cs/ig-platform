import React from 'react'
import {OIcon} from './OIcon';
import {XIcon} from './XIcon'

const TextColor = ["text-metalText", "text-2048Text", "text-purpleText"];
const PanelColor = ["bg-metalPanel", "bg-2048Panel", "bg-purplePanel"];
const NEXTColor = ["bg-indigo", "bg-2048O", "bg-purpleO"];
const QUITColor = ["bg-metalX", "bg-2048X", "bg-purpleX"];


interface GameProps {
    themeNumber: number
    winner: string
    handleQuitGame(): void;
    handleNewGame(): void;
}

export const WinnerModal = ({themeNumber, winner, handleQuitGame, handleNewGame}: GameProps) => {
    return (
        <div className="bg-gray-900/90 z-10 min-h-screen w-full absolute top-0 left-0">
            <div
                className="w-[500px] h-[250px] rounded-xl bg-[#1f3540] space-y-10 px-6 py-4 mx-auto mt-52 flex items-center justify-center flex-col">
                <h2 className="flex flex-col items-center justify-center space-y-6 text-2xl md:text-4xl font-bold">
                    {winner === "X"
                        ? <XIcon themeNum={themeNumber}/>
                        : <OIcon themeNum={themeNumber}/>}
                    <p className={`uppercase ${TextColor[themeNumber]}`}>Takes the Round</p>
                </h2>

                <div className="flex items-center justify-center space-x-16">
                    <button onClick={handleQuitGame}
                            className={`button px-4 rounded-md py-1 ${QUITColor[themeNumber]} hover:${PanelColor[themeNumber]} hover:ring-4 hover:ring-gray-400`}>Quit
                    </button>
                    <button onClick={handleNewGame}
                            className={`button px-4 rounded-md py-1 ${NEXTColor[themeNumber]} hover:${PanelColor[themeNumber]} hover:ring-4 hover:ring-cyan-300`}>Next
                        Round
                    </button>
                </div>
            </div>
        </div>
    )
}
