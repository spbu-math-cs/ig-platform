import React, {useState} from 'react'
import {GameId} from "@/pages"


type Player = {
    name: string,
    score: number,
    correctAnswers: number

}

interface PlayersProps {
    player: Player
    game: string
}

export function Players({player}: PlayersProps) {
    return (
        <div className="border-2 border-back w-[844px] rounded-2xl py-2 px-4 flex flex-col items-start mb-2">
            <div className="rounded-2xl flex flex-row">
                <div className="rounded-2xl py-2 px-4 flex flex-row justify-between mb-1 space-x-30 w-[810px]">
                    <p className="font-bold text-2xl">{player.name}</p>
                    <div className={`flex flex-row space-x-2`}>
                        <p className="font-bold text-2xl">{player.score.toString()}</p>
                        <p className="font-bold text-2xl">{player.correctAnswers.toString()}</p>
                    </div>
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

}

export const PlayersList = ({isHost, sessionId, game, players}: PlayersListProps) => {
    return (
        <div className="flex flex-col items-center w-[1000px] rounded-2xl bg-square h-auto mt-8 pb-10 pt-4">
            <div className={`px-20 flex items-center w-[1000px] rounded-2xl bg-square space-x-96`}>
                <div className={"flex flex-col space-y-1 mt-0"}>
                    <div className={"flex flex-row justify-between pb-2"}>
                        <div className={`flex flex-row justify-center space-x-2`}>
                            <p className={`justify-items-start text-md text-JoinGameTxt font-extrabold py-2 text-4xl  `}>
                                RESULTS
                            </p>
                            <p className={`justify-items-start text-JoinGameTxt mt-4 font-extrabold py-2 text-l`}> of
                                game:
                                {sessionId}
                            </p>
                        </div>
                    </div>
                    <hr className="w-[844px] h-1 mx-auto my-4 bg-JoinGameTxt border-0 rounded md:my-10 "/>
                </div>
            </div>
            <div
                className="
                    flex flex-col justify-items-start py-10 px-100 rounded
                    mb-2 -scroll-ms-3 overflow-auto text-md text-JoinGameTxt
                    max-h-[60vh] bg-square">
                {
                    // players?.sort((a, b) => a.score - b.score))
                    players?.map(player =>
                        <Players
                            key={player.name}
                            player={player}
                            game={game}
                        />
                    )
                }
            </div>
        </div>

    )
}

interface ScoreTableProps {
    isHost: boolean
    sessionId: string
    game: GameId
    players: Player[]
}


export const ScoreTable = ({isHost, sessionId, game, players}: ScoreTableProps) => {
    return (
        <div>
            <PlayersList
                game={game}
                sessionId={sessionId}
                isHost={isHost}
                players={players}
            />
        </div>)
}
