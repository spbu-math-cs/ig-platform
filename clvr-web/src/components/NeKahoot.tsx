import {EditBoard} from "@/neKahoot/EditBoard"
import React from "react"
import {Board} from "@/neKahoot/Board"
import {checkExhausted} from "@/utils"
import {AppAction} from "@/pages"

export type NeKahootState = {
    kind: "playing"
    sessionId: string
    role: "host" | "player"
} | {
    kind: "constructor"
}

interface NeKahootProps {
    state: NeKahootState
    dispatch: (action: AppAction) => void
}

export function NeKahoot({state, dispatch}: NeKahootProps) {
    let content
    if (state.kind == "constructor") {
        content = <div className="mt-10 w-[1000px] items-center justify-center ">
            <EditBoard onCreate={() => dispatch({kind: "go_to_creating", game: "neKahoot"})}/>
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