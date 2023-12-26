import {GameConfig, Session, TemplateInfo} from "@/tic-tac-toe/types"
import {GAME_SERVER_URL} from "@/config"

const API_ENDPOINT = new URL(GAME_SERVER_URL)

export async function getTemplateList(): Promise<TemplateInfo[]> {
    const response = await fetch(new URL("nekahoot/template", API_ENDPOINT))
    const json = await response.json()

    return json["templates"]
}

export async function createGame(quizId: string, config: GameConfig): Promise<Session> {
    const response = await fetch(new URL(`nekahoot/game`, API_ENDPOINT), {
        method: "POST",
        body: JSON.stringify({
            template_id: quizId,
        }),
        headers: {
            "Content-Type": "application/json",
        },
    })

    const json = await response.json()

    return json.session
}
