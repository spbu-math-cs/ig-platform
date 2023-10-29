import React, {ChangeEvent, useState} from 'react'
import {OIcon} from './OIcon'
import {XIcon} from './XIcon'



interface JoiningProp {
    handleJoiningRequest(id : string) : void
    isError : boolean
}



export const JoinGame = ({handleJoiningRequest, isError}: JoiningProp) => {

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
                className={`flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl bg-panel mt-4 space-y-8 md:space-y-6`}>
                <p className={`text-md text-hostTxt uppercase font-extrabold  md:text-3xl space-y-12 `}>
                    ENTER GAME ID
                </p>

                <form onSubmit={() => handleJoiningRequest(value)}>
                    <input
                        type="text"
                        className={`mt-1 border  w-80 h-24 rounded-xl px-2 py-3 bg-panel outline-0
                                    text-3xl md:text-4xl font-bold  text-center text-txt outline-none `}
                        value = {value}
                        onChange={handleChange}
                    />

                </form>
                {isError ?
                    <p className={`text-center font-extrabold text-error text-2xl `}>
                        GAME DOESN'T EXIST
                    </p>
                    :
                    ""
                }

            </div>

            <button onClick={() => handleJoiningRequest(value)}
                    className={`button hover:ring-4 hover:ring-cyan-300 rounded-xl mt-8 px-6 py-3 bg-[#f3b236] hover:bg-panel`}>
                START GAME
            </button>

        </div>
    )
}