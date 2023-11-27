import {ProtoQuiz, Quiz, QuizInfo, Session} from "@/game/types"

const API_ENDPOINT = new URL("http://0.0.0.0:8080/")

export async function getQuizList(): Promise<QuizInfo[]> {
    const response = await fetch(new URL("quiz-list", API_ENDPOINT))
    const json = await response.json()

    return json["quiz-list"]
}

export async function getQuiz(quizId: string): Promise<Quiz> {
    const response = await fetch(new URL(`quiz-list/${quizId}`, API_ENDPOINT))
    const json = await response.json()
    return json as Quiz
}

export async function createGame(quizId: string): Promise<Session> {
    const response = await fetch(new URL(`api/game-session`, API_ENDPOINT), {
        method: "POST",
        body: JSON.stringify({
            quiz: {
                id: quizId,
            },
        }),
        headers: {
            "Content-Type": "application/json",
        },
    })

    const json = await response.json()

    return json.session
}

export async function createQuiz(quiz: ProtoQuiz): Promise<string> {
    const response = await fetch(new URL(`api/quiz`, API_ENDPOINT), {
        method: "POST",
        body: JSON.stringify(quiz),
        headers: {
            "Content-Type": "application/json",
        },
    })

    return (await response.json()).id as string
}
