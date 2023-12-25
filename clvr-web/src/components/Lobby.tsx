import React, {useState} from 'react'
import {OIcon} from 'tic-tac-toe/OIcon'
import {XIcon} from 'tic-tac-toe/XIcon'
import {GameId} from "@/pages"

export type Team = "X" | "O" | undefined
type Player = {
    name: string,
    team: Team,
}

interface ChooseTeamModalProps {
    ChooseTeam(t: Team): void

    isHost: boolean
    sessionId: string
    game: string
    players: Player[]

    sendMessage: () => void
}

export const ChooseTeamModal = ({ChooseTeam, isHost, sessionId, players, game, sendMessage}: ChooseTeamModalProps) => {
    return (
        <div className={"relative w-[1000px]"}>
            <div className={"blur-lg"}>
                <PlayersList game={game} sessionId={sessionId} players={players} isHost={isHost} startGame={sendMessage}/>
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
    player: Player
    game: string
}

export function Players({player, game}: PlayersProps) {
    return (
        game == "tic_tac_toe" ? (
                <div
                    className="border-2 border-back w-[200px]  h-[200px] rounded-2xl py-2 px-4 flex flex-col items-center mb-2">
                    <div className={"items-center flex flex-col justify-items-center"}>
                        <div className="rounded-2xl py-2 px-4 flex flex-row mb-1">
                            <p className="font-bold text-3xl text-center">{player.name}</p>
                        </div>
                        {
                            player.team == "X" ? <XIcon/>
                                : player.team == "O" ? <OIcon/>
                                : <div/>
                        }
                    </div>
                </div>
            )
            :
            <div className="border-2 border-back w-[844px] rounded-2xl py-2 px-4 flex flex-col items-start mb-2">
                <div className="rounded-2xl flex flex-row">
                    <div className="rounded-2xl py-2 px-4 flex flex-row mb-1 space-x-30 w-[810px]">
                        <p className="font-bold text-2xl">{player.name}</p>
                    </div>
                </div>
            </div>
    )
}


interface PlayersListProps {
    isHost: boolean
    sessionId: string
    game: string
    players: Player[]

    startGame: () => void
}

export const PlayersList = ({isHost, sessionId, game, players, startGame}: PlayersListProps) => {
    return (
        <div className="flex flex-col items-center w-[1000px] rounded-2xl bg-square h-auto mt-8 pb-10 pt-4">
            <div className={`px-20 flex items-center w-[1000px] rounded-2xl bg-square space-x-96`}>
                <div className={"flex flex-col space-y-1 mt-0"}>
                    <div className={"flex flex-row justify-between pb-2"}>
                        <div className = {`flex flex-row justify-center space-x-2`}>
                            <p className={`justify-items-start text-md text-JoinGameTxt font-extrabold py-2 text-4xl  `}>
                                JOINED PLAYERS
                            </p>
                            <p className={`justify-items-start text-JoinGameTxt mt-4 font-extrabold py-2 text-l`}> to game:
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
            {game == "tic_tac_toe" ?
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
                                game={game}
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
                        players?.map(player =>
                            <Players
                                key={player.name}
                                player={player}
                                game={game}
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
    game: GameId
    players: Player[]

    startGame: () => void
}


export const Lobby = ({isHost, sessionId, game, players, startGame}: LobbyProps) => {
    const [Team, setTeam] = useState<Team>(undefined)

    function chooseTeam(t: Team) {
        setTeam(t)
    }

    return (
        <div>
            {(Team === undefined && !isHost && game == "tic_tac_toe") ?
                <div>
                    <ChooseTeamModal
                        ChooseTeam={chooseTeam}
                        players={players}
                        game={game} sessionId={sessionId} isHost={isHost}
                        sendMessage={startGame}
                    />
                </div>
                :
                <PlayersList
                    game={game}
                    sessionId={sessionId}
                    isHost={isHost}
                    players={players}
                    startGame={startGame}
                />
            }
        </div>)
}
