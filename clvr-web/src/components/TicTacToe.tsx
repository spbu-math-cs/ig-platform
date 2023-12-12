import {EditBoard} from "@/tic_tac_toe/EditBoard"
import {getQuizList} from "@/game/api"
import React from "react"
import {JSX} from "react"
import {Board} from "@/tic_tac_toe/Board"

export type TicTacToeState = {
    kind: "playing"
    sessionId: string
    role: "host" | "board"
} | {
    kind: "constructor"
}

export function TicTacToe({state}: {state: TicTacToeState}) {
    let content
    if (state.kind == "constructor") {
        content = <div className="mt-10 w-[1000px] items-center justify-center ">
            <EditBoard/>
        </div>
    } else if (state.kind == "playing") {
        content = <div>
            <center>
                <h1 className={`text-100xl md:text-10000xl font-extrabold text-primary`}>
                    {"session: " + state.sessionId}
                </h1>
            </center>
            <Board sessionId={state.sessionId} isHost={state.role == "host"}/>
        </div>
    } else {
        checkExhausted(state)
    }

    return content
}