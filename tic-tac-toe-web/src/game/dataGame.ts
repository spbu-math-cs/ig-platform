import {QuizInfo} from '@/game/types'

export const quizzes: QuizInfo[] = [
    {
        name: "First Quiz",
        id: "11",
        comment: "Super-duper game"
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

