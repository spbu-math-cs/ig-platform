import React, {FunctionComponent, JSX, useEffect, useReducer, useState} from 'react'
import {OIcon} from 'tic-tac-toe/OIcon'
import {XIcon} from 'tic-tac-toe/XIcon'


export type team = "X" | "O" | "None"


let players = Array(["Nick239", "X"], ["Fedor", "X"], ["Petr", "O"], ["AAA", "O"], ["SSSS", "O"], ["EEE", "X"])


interface ChooseTeamModalProps {
    ChooseTeam(t: team): void
}

interface PlayersProps {
    template: string[]
}

export const ChooseTeamModal = ({ChooseTeam}: ChooseTeamModalProps) => {
    return (
        <div className={"relative w-[1000px]"}>
            <div className={"blur-lg"}>
                <PlayersList/>
            </div>
            <div className={"absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2"}>
                <div
                    className={`flex flex-col items-center justify-center py-12 w-[500px] h-[20rem] rounded-2xl  bg-panel mt-6 space-y-8 ring-4 ring-txt`}>
                    <p className={`text-md text-txt uppercase font-extrabold  md:text-4xl space-y-12 `}>
                        CHOOSE TEAM
                    </p>
                    <div
                        className="bg-gray-800 flex items-center justify-evenly h-35 rounded-2xl p-2 ">
                        <button onClick={() => {
                            ChooseTeam("X")
                        }}
                                className={`focus:bg-gray-300 hover:bg-[#ffe1a9] transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6  text-3xl md:text-4xl font-extrabold mt-1 text-hostTxt `}>
                            <XIcon/>
                        </button>

                        <button onClick={() => {
                            ChooseTeam("O")
                        }}
                                className={`focus:bg-gray-300 hover:bg-[#ffe1a9] transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6 text-3xl md:text-4xl font-extrabold mt-1 text-playerTxt`}>
                            <OIcon/>
                        </button>
                    </div>
                </div>
            </div>
        </div>

    )
}


export function Players({template}: PlayersProps) {
    return (
        <div
            className="border-2 border-back w-[200px]  h-[200px] rounded-2xl py-2 px-4 flex flex-col items-center mb-2">
            <div className="rounded-2xl py-2 px-4 flex flex-row mb-1">
                <p className="font-bold text-3xl text-center">{template[0]}</p>

            </div>
            {
                template[1] == "X" ?
                    <XIcon/>
                    :
                    <OIcon/>

            }
        </div>
    )
}

interface LobbyProps {
    isHost: boolean
    sessionId: string
}

export const PlayersList = () => {
    return (
        <div className="flex flex-col items-center w-[1000px] rounded-2xl bg-square h-auto mt-8">
            <div className={`px-20 flex flex-row items-center w-[1000px] rounded-2xl bg-square space-x-96`}>
                <div className={"flex flex-col space-y-1"}>
                    <p className={`justify-items-start text-md text-JoinGameTxt uppercase font-extrabold py-4 text-4xl  `}>
                        JOINED PLAYERS
                    </p>
                    <hr className="w-[844px] h-1 mx-auto my-4 bg-JoinGameTxt border-0 rounded md:my-10 "/>
                </div>
            </div>
            <div
                className="
                    grid grid-cols-4 gap-4  justify-items-start py-10 rounded
                    mb-2 -scroll-ms-3 overflow-auto text-md text-JoinGameTxt
                    max-h-[60vh] bg-square">
                {
                    players?.map(quiz =>
                        <Players
                            template={quiz}
                        />
                    )
                }
            </div>
        </div>
    )
}

export const Lobby = ({isHost, sessionId}: LobbyProps) => {
    const [Team, setTeam] = useState<team>("None")

    function chooseTeam(t: team) {
        setTeam(t)
    }

    return (
        <div>
            {(Team == "None" && !isHost) ?
                <div>
                    <ChooseTeamModal
                        ChooseTeam={chooseTeam}
                    />
                </div>
                :
                <PlayersList/>
            }
        </div>)
}
