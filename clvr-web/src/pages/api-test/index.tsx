"use client"
import {useServerState} from "@/tic-tac-toe/websockets"
import {useEffect, useRef, useState} from "react"
import {getQuiz, getQuizList} from "@/tic-tac-toe/api"
import {Quiz, TemplateInfo} from "@/tic-tac-toe/types"

/**
 * Testing page for the websocket API, and an example of how to use it.
 */
export default function ApiTest() {
    const [quiz, setQuiz] = useState<Quiz>()
    const [allQuizzes, setAllQuizzes] = useState<TemplateInfo[]>()

    useEffect(() => {
        getQuiz("ABCD").then(q => { console.log(q); setQuiz(q) })
        getQuizList().then(q => setAllQuizzes(q))
    }, [])

    return <>
        <div>Quiz by id ABCD:</div>
        <pre>
            {JSON.stringify(quiz, null, 4)}
        </pre>
        <div>All quizzes:</div>
        <pre>
            {JSON.stringify(allQuizzes, null, 4)}
        </pre>
    </>
}
