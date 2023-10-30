import React from 'react'
import {OIcon} from './OIcon'
import {XIcon} from './XIcon'

interface PlayerProp {
    handleHostMode(): void

    handleJoinGame(): void
}

interface ColorThemeProp {
    handleColorTheme(): void
}

export const ColorTheme = ({handleColorTheme}: ColorThemeProp) => {
    return (
        <div className="mt-106 md:mt-106  flex flex-col items-center justify-center mx-auto">
            <button onClick={handleColorTheme}
                    className={`button hover:ring-4 hover:ring-cyan-300 rounded-xl mt-8 px-6 py-3 bg-[#f3b236] hover:bg-panel`}>
                Change Theme
            </button>
        </div>
    )
}


export const ChooseMode = ({handleHostMode, handleJoinGame}: PlayerProp) => {
    return (
        <div className="mt-10 md:mt-16 w-[500px] flex flex-col items-center justify-center mx-auto">
            <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                <XIcon/>
                <OIcon/>
            </div>
            <div
                className={`flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl  bg-panel mt-6 space-y-8 md:space-y-8`}>
                <p className={`text-md text-txt uppercase font-extrabold  md:text-3xl space-y-12 `}>
                    SELECT MODE
                </p>
                <div className=" bg-gray-800  flex items-center justify-evenly h-35 rounded-2xl p-2 ">
                    <button onClick={handleHostMode}
                            className={`focus:bg-gray-300 hover:bg-[#ffe1a9] transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6  text-3xl md:text-4xl font-extrabold mt-1 text-hostTxt `}>
                        CREATE
                    </button>

                    <button onClick={handleJoinGame}
                            className={`focus:bg-gray-300 hover:bg-[#ffe1a9] transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6 text-3xl md:text-4xl font-extrabold mt-1 text-playerTxt`}>
                        JOIN
                    </button>
                </div>
            </div>
        </div>
    )
}