import {GameConfig, Session, TemplateInfo} from "@/tic-tac-toe/types"

const API_ENDPOINT = new URL("http://0.0.0.0:8080/")

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
