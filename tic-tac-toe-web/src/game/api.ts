import {Session, QuizInfo, Quiz} from "@/game/types"

const API_ENDPOINT = new URL("http://0.0.0.0:8080/api/")

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
    const response = await fetch(new URL(`game-session/${quizId}`, API_ENDPOINT), {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
    })

    const json = await response.json()

    return json.session
}
