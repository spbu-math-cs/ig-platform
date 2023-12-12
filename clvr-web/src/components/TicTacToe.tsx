import {EditBoard} from "@/tic-tac-toe/EditBoard"
import React from "react"
import {Board} from "@/tic-tac-toe/Board"
import {checkExhausted} from "@/utils"
import {AppAction} from "@/pages/index"

export type TicTacToeState = {
    kind: "playing"
    sessionId: string
    role: "host" | "board"
} | {
    kind: "constructor"
}

interface TicTacTorProps {
    state: TicTacToeState
    dispatch: (action: AppAction) => void
}

export function TicTacToe({state, dispatch}: TicTacTorProps) {
    let content
    if (state.kind == "constructor") {
        content = <div className="mt-10 w-[1000px] items-center justify-center ">
            <EditBoard onCreate={() => dispatch({kind: "go_to_creating", game: "tic_tac_toe"})}/>
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