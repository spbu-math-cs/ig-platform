import {QuizInfo} from '@/game/types'

export const quizzes: QuizInfo[] = [
    {
        name: "Philosophy Quiz",
        id: "ABCD",
        comment: "Peter Abelard (1079-1142) was the preeminent philosopher of the twelfth century."
    },
    {
        name: "History",
        id: "22",
        comment: "Who crossed the Rubicon"
    },
    {
        name: "Biology",
        id: "33",
        comment: "Hummingbird eats sugar"
    },
    {
        name: "Chemistry",
        id: "44",
        comment: "CH3COOH"
    },
    {
        name: "Trigonometry",
        id: "55",
        comment: "sin^2 x + cos^2 x = 1"
    },
    {
        name: "Sixth Quiz",
        id: "66",
        comment: "Cool Game"
    }
]

export interface QuizProps {
    quiz: QuizInfo
    handleSelect(id: string): void
}

