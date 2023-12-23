import React, {FunctionComponent, JSX, useEffect, useReducer, useState} from 'react'
import {OIcon} from 'tic-tac-toe/OIcon'
import {XIcon} from 'tic-tac-toe/XIcon'


export type team = "X" | "O" | "None"


let players = Array(["Nick239", "X"], ["Fedor", "X"], ["Petr", "O"], ["AAA", "O"], ["SSSS", "O"], ["EEE", "X"])
type GameId = "tic_tac_toe" | "nekahoot"
type AppState = {
    kind: "main_page"
    modal: undefined | GameId
    sessionId: string
} | {
    kind: "joining"
    sessionId: string
} | {
    kind: "fatal"
    error: Node | string
} | {
    kind: "logging"
} | {
    kind: "playing"
    game: GameId
    sessionId: string
    isHost: boolean
} | {
    kind: "constructor"
    game: GameId
} | {
    kind: "lobby"
    game: GameId
    sessionId: string
    isHost: boolean
}

interface ChooseTeamModalProps {
    ChooseTeam(t: team): void

    isHost: boolean
    sessionId: string
    game: string
    setState() : void
}

export const ChooseTeamModal = ({ChooseTeam, isHost, sessionId, game, setState}: ChooseTeamModalProps) => {
    return (
        <div className={"relative w-[1000px]"}>
            <div className={"blur-lg"}>
                <PlayersList game={game} sessionId={sessionId} isHost={isHost} setState={setState}/>
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

interface PlayersProps {
    template: string[]
    game : string
}

export function Players({template, game}: PlayersProps) {
    return (
        game == "tic_tac_toe" ? (
        <div className="border-2 border-back w-[200px]  h-[200px] rounded-2xl py-2 px-4 flex flex-col items-center mb-2">
            <div className={"items-center flex flex-col justify-items-center"}>
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
        </div>
        )
            :
            <div className="border-2 border-back w-[844px] rounded-2xl py-2 px-4 flex flex-col items-start mb-2">
                <div className="rounded-2xl flex flex-row">
                    <div className="rounded-2xl py-2 px-4 flex flex-row mb-1 space-x-30 w-[810px]">
                        <p className="font-bold text-2xl">{template[0]}</p>
                    </div>
                </div>
            </div>
    )
}


interface PlayersListProps {
    isHost: boolean
    sessionId: string
    game: string
    setState() : void
}

export const PlayersList = ({isHost, sessionId, game, setState}: PlayersListProps) => {
    return (
        <div className="flex flex-col items-center w-[1000px] rounded-2xl bg-square h-auto mt-8 pb-10 pt-4">
            <div className={`px-20 flex items-center w-[1000px] rounded-2xl bg-square space-x-96`}>
                <div className={"flex flex-col space-y-1 mt-0"}>
                    <div className={"flex flex-row justify-between pb-2"}>
                        <p className={`justify-items-start text-md text-JoinGameTxt uppercase font-extrabold py-2 text-4xl  `}>
                            JOINED PLAYERS
                        </p>
                        {isHost ?
                            <button onClick={() => setState()}
                                    className={`button hover:ring-4 py-1 hover:ring-cyan-300 rounded-xl px-6 bg-[#f3b236] hover:bg-square`}>
                                START GAME
                            </button>
                            : <div/>
                        }
                    </div>
                    <hr className="w-[844px] h-1 mx-auto my-4 bg-JoinGameTxt border-0 rounded md:my-10 "/>
                </div>
            </div>
            {game == "tic_tac_toe" ?
                <div
                    className="
                    grid grid-cols-4 gap-4  justify-items-start py-10 rounded
                    mb-2 -scroll-ms-3 overflow-auto text-md text-JoinGameTxt
                    max-h-[60vh] bg-square">
                    {
                        players?.map(quiz =>
                            <Players
                                template={quiz}
                                game = {game}
                            />
                        )
                    }
                </div>
                :
                <div
                    className="
                    flex flex-col justify-items-start py-10 px-100 rounded
                    mb-2 -scroll-ms-3 overflow-auto text-md text-JoinGameTxt
                    max-h-[60vh] bg-square">
                    {
                        players?.map(quiz =>
                            <Players
                                template={quiz}
                                game = {game}
                            />
                        )
                    }
                </div>
            }
        </div>

    )
}

interface LobbyProps {
    isHost: boolean
    sessionId: string
    game: string
    setState() :void
}


export const Lobby = ({isHost, sessionId, game, setState}: LobbyProps) => {
    const [Team, setTeam] = useState<team>("None")

    function chooseTeam(t: team) {
        setTeam(t)
    }

    return (
        <div>
            {(Team == "None" && !isHost && game == "tic_tac_toe") ?
                <div>
                    <ChooseTeamModal
                        ChooseTeam={chooseTeam}
                        game={game} sessionId={sessionId} isHost={isHost}
                        setState={setState}
                    />
                </div>
                :
                <PlayersList game={game} sessionId={sessionId} isHost={isHost} setState ={setState}/>
            }
        </div>)
}
