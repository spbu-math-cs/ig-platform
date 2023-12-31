import {GameConfig, ProtoQuiz, Quiz, TemplateInfo, Session} from "@/tic-tac-toe/types"

const API_ENDPOINT = new URL("http://0.0.0.0:8080/")

export async function getTemplateList(): Promise<TemplateInfo[]> {
    const response = await fetch(new URL("tic-tac-toe/template-list", API_ENDPOINT))
    const json = await response.json()

    return json["template-list"]
}

export async function getTemplate(quizId: string): Promise<Quiz> {
    const response = await fetch(new URL(`tic-tac-toe/template/${quizId}`, API_ENDPOINT))
    const json = await response.json()
    return json as Quiz
}

export async function createGame(quizId: string, config: GameConfig): Promise<Session> {
    const response = await fetch(new URL(`tic-tac-toe/game`, API_ENDPOINT), {
        method: "POST",
        body: JSON.stringify({
            template_id: quizId,
            game_configuration: {
                replace_marks: config.replaceMarks ? "ENABLED" : "DISABLED",
                open_multiple_questions: config.openMultipleQuestions ? "ENABLED" : "DISABLED",
            }
        }),
        headers: {
            "Content-Type": "application/json",
        },
    })

    const json = await response.json()

    return json.session
}

export async function createTemplate(quiz: ProtoQuiz): Promise<string> {
    const response = await fetch(new URL(`tic-tac-toe/template`, API_ENDPOINT), {
        method: "POST",
        body: JSON.stringify(quiz),
        headers: {
            "Content-Type": "application/json",
        },
    })

    return (await response.json()).id as string
}
