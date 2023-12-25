import React, {useState} from 'react'
import {OIcon} from 'tic-tac-toe/OIcon'
import {XIcon} from 'tic-tac-toe/XIcon'
import {GameId} from "@/pages"
import {Team} from "@/tic-tac-toe/types"

type Player = {
    name: string,
}

interface ChooseTeamModalProps {
    chooseTeam: (t: Team) => void
}

export const ChooseTeamModal = ({chooseTeam}: ChooseTeamModalProps) => {
    return (
        <div className={"relative w-[1000px]"}>
            <div className={"absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2"}>
                <div
                    className={`flex flex-col items-center justify-center py-12 w-[500px] h-[20rem] rounded-2xl  bg-panel mt-6 space-y-8 ring-4 ring-txt`}>
                    <p className={`text-md text-txt uppercase font-extrabold  md:text-4xl space-y-12 `}>
                        CHOOSE TEAM
                    </p>
                    <div
                        className="bg-gray-800 flex items-center justify-evenly h-35 rounded-2xl p-2 ">
                        <button onClick={() => {
                            chooseTeam("X")
                        }}
                                className={`focus:bg-gray-300 hover:bg-[#ffe1a9] transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6  text-3xl md:text-4xl font-extrabold mt-1 text-hostTxt `}>
                            <XIcon/>
                        </button>

                        <button onClick={() => {
                            chooseTeam("O")
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
    player: Player
}

export function Players({player}: PlayersProps) {
    return <div className="border-2 border-back w-[844px] rounded-2xl py-2 px-4 flex flex-col items-start mb-2">
        <div className="rounded-2xl flex flex-row">
            <div className="rounded-2xl py-2 px-4 flex flex-row mb-1 space-x-30 w-[810px]">
                <p className="font-bold text-2xl">{player.name}</p>
            </div>
        </div>
    </div>
}


interface PlayersListProps {
    isHost: boolean
    sessionId: string
    players: Player[]

    startGame: () => void
}

export const PlayersList = ({isHost, sessionId, players, startGame}: PlayersListProps) => {
    return (
        <div className="flex flex-col items-center w-[1000px] rounded-2xl bg-square h-auto mt-8 pb-10 pt-4">
            <div className={`px-20 flex items-center w-[1000px] rounded-2xl bg-square space-x-96`}>
                <div className={"flex flex-col space-y-1 mt-0"}>
                    <div className={"flex flex-row justify-between pb-2"}>
                        <div className={`flex flex-row justify-center space-x-2`}>
                            <p className={`justify-items-start text-md text-JoinGameTxt font-extrabold py-2 text-4xl  `}>
                                JOINED PLAYERS
                            </p>
                            <p className={`justify-items-start text-JoinGameTxt mt-4 font-extrabold py-2 text-l`}> to
                                game:
                                {sessionId}
                            </p>
                        </div>
                        {isHost ?
                            <button onClick={startGame}
                                    className={`button hover:ring-4 py-1 hover:ring-cyan-300 rounded-xl px-6 bg-[#f3b236] hover:bg-square`}>
                                START GAME
                            </button>
                            : <div/>
                        }
                    </div>
                    <hr className="w-[844px] h-1 mx-auto my-4 bg-JoinGameTxt border-0 rounded md:my-10 "/>
                </div>
            </div>
            <div
                className="
                grid grid-cols-4 gap-4  justify-items-start py-10 rounded
                mb-2 -scroll-ms-3 overflow-auto text-md text-JoinGameTxt
                max-h-[60vh] bg-square">
                {
                    players?.map(player =>
                        <Players
                            key={player.name}
                            player={player}
                        />,
                    )
                }
            </div>
        </div>

    )
}

interface LobbyProps {
    isHost: boolean
    sessionId: string
    players: Player[]

    startGame: () => void
    joinTeam: undefined | ((t: Team) => void)
}


export const Lobby = ({isHost, sessionId, players, startGame, joinTeam}: LobbyProps) => {
    const [chosenTeam, setChosenTeam] = useState<boolean>(false)

    return (
        <div>
            <PlayersList
                sessionId={sessionId}
                isHost={isHost}
                players={players}
                startGame={startGame}
            />
            { // TODO: move this to a separate component for TTT only
                (!chosenTeam && joinTeam !== undefined) &&
                <ChooseTeamModal
                    chooseTeam={team => {
                        joinTeam(team)
                        setChosenTeam(true)
                    }}
                />
            }
        </div>)
}
