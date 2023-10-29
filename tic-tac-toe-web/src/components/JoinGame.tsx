import React, {ChangeEvent, useState} from 'react'
import {OIcon} from './OIcon'
import {XIcon} from './XIcon'

const PanelColor = ["bg-metalPanel", "bg-2048Panel", "bg-purplePanel"]
const HoverPanelColor = ["hover:bg-metalPanel", "hover:bg-2048Panel", "hover:bg-purplePanel"]
const TextColor = ["text-metalText", "text-2048Text", "text-purpleText"]
const BGColor = ["bg-metalBG", "bg-2048BG", "bg-purpleBG"]

interface JoiningProp {
    themeNumber: number
    handleJoiningRequest(id : string) : void
    handleSubmit(event: React.FormEvent) : void

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

export const JoinGame = ({themeNumber, handleJoiningRequest, handleSubmit}: JoiningProp) => {

    const [value, setValue] = useState('')
    const handleChange =(event: ChangeEvent<HTMLInputElement>) => {
        const element = event.currentTarget as HTMLInputElement
        const value = element.value
        setValue(value)
    }

    return (
        <div className="mt-10 md:mt-16 w-[500px] flex flex-col items-center justofy-center mx-auto">
            <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                <XIcon/>
                <OIcon/>
            </div>
            <div
                className={`flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl ${PanelColor[themeNumber]} mt-6 space-y-8 md:space-y-8`}>
                <p className={`text-md ${TextColor[themeNumber]} uppercase font-extrabold  md:text-3xl space-y-12 `}>
                    ENTER GAME ID
                </p>

                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        className={`border w-80 h-24 rounded-xl mt-8 px-2 py-3 ${BGColor[themeNumber]} ${HoverPanelColor[themeNumber]}
                                    rounded-xl py-3 text-3xl md:text-4xl font-bold ${TextColor[themeNumber]} outline-none `}
                        value = {value}
                        onChange={handleChange}
                    />

                </form>
            </div>

            <button onClick={() => handleJoiningRequest(value)}
                    className={`button hover:ring-4 hover:ring-cyan-300 rounded-xl mt-8 px-6 py-3 bg-[#f3b236] ${HoverPanelColor[themeNumber]}`}>
                START GAME
            </button>

        </div>
    )
}