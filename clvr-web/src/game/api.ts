import {GameConfig, ProtoQuiz, Quiz, TemplateInfo, Session} from "@/game/types"

const API_ENDPOINT = new URL("http://0.0.0.0:8080/")

export async function getQuizList(): Promise<TemplateInfo[]> {
    const response = await fetch(new URL("tic-tac-toe/quiz-list", API_ENDPOINT))
    const json = await response.json()

    return json["quiz-list"]
}

export async function getQuiz(quizId: string): Promise<Quiz> {
    const response = await fetch(new URL(`tic-tac-toe/quiz-list/${quizId}`, API_ENDPOINT))
    const json = await response.json()
    return json as Quiz
}

export async function createGame(quizId: string, config: GameConfig): Promise<Session> {
    const response = await fetch(new URL(`tic-tac-toe/api/game-session`, API_ENDPOINT), {
        method: "POST",
        body: JSON.stringify({
            quiz_id: quizId,
            game_configuration: {
                replace_marks: config.replaceMarks,
                open_multiple_questions: config.openMultipleQuestions
            }
        }),
        headers: {
            "Content-Type": "application/json",
        },
    })

    const json = await response.json()

    return json.session
}

export async function createQuiz(quiz: ProtoQuiz): Promise<string> {
    const response = await fetch(new URL(`tic-tac-toe/api/quiz`, API_ENDPOINT), {
        method: "POST",
        body: JSON.stringify(quiz),
        headers: {
            "Content-Type": "application/json",
        },
    })

    return (await response.json()).id as string
}
