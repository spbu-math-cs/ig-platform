import React from 'react'
import {OIcon} from './OIcon'
import {XIcon} from './XIcon'

const TextColorPlayer = ["text-metalX", "text-2048X", "text-purpleX"]
const TextColorHost = ["text-indigo", "text-2048O", "text-purpleX"]
const PanelColor = ["bg-metalPanel", "bg-2048Panel", "bg-purplePanel"]
const HoverPanelColor = ["hover:bg-metalPanel", "hover:bg-2048Panel", "hover:bg-purplePanel"]
const TextColor = ["text-metalText", "text-2048Text", "text-purpleText"]

interface PlayerProp {
    themeNumber: number
    handleHostMode(): void
    handlePlayerMode(): void
    handleNewGame(): void
}

interface ColorThemeProp {
    themeNumber: number
    handleColorTheme(): void
}

export const ColorTheme = ({themeNumber, handleColorTheme}: ColorThemeProp) => {
    return (
        <div className="mt-106 md:mt-106  flex flex-col items-center justofy-center mx-auto">
            <button onClick={handleColorTheme}
                    className={`button hover:ring-4 hover:ring-cyan-300 rounded-xl mt-8 px-6 py-3 bg-[#f3b236] ${HoverPanelColor[themeNumber]}`}>
                Change Theme
            </button>
        </div>
    )
}


export const ChooseMode = ({themeNumber, handleHostMode, handleNewGame, handlePlayerMode}: PlayerProp) => {
    return (
        <div className="mt-10 md:mt-16 w-[500px] flex flex-col items-center justofy-center mx-auto">
            <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                <XIcon themeNum={themeNumber}/>
                <OIcon themeNum={themeNumber}/>
            </div>
            <div
                className={`flex flex-col items-center py-12 w-[500px] md:w-[450px] h-64 md:h-72 rounded-2xl ${PanelColor[themeNumber]} mt-6 space-y-8 md:space-y-8`}>
                <p className={`text-md ${TextColor[themeNumber]} uppercase font-extrabold  md:text-3xl space-y-12 `}>
                    SELECT MODE
                </p>
                <div className=" bg-gray-800  flex items-center justify-evenly h-35 rounded-2xl p-2 ">
                    <button onClick={handleHostMode}
                            className={`focus:bg-gray-300 hover:bg-[#ffe1a9] transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6  text-3xl md:text-4xl font-extrabold mt-1 ${TextColorPlayer[themeNumber]} `}>
                        HOST
                    </button>

                    <button onClick={handlePlayerMode}
                            className={`focus:bg-gray-300 hover:bg-[#ffe1a9] transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6 text-3xl md:text-4xl font-extrabold mt-1 ${TextColorHost[themeNumber]}  `}>
                        PLAYER
                    </button>
                </div>
            </div>
            <button onClick={handleNewGame}
                    className={`button hover:ring-4 hover:ring-cyan-300 rounded-xl mt-8 px-6 py-3 bg-[#f3b236] ${HoverPanelColor[themeNumber]}`}>
                START GAME
            </button>
        </div>
    )
}