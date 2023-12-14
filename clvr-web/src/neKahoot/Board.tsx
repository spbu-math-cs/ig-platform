import React, {useEffect, useState} from 'react'
import {GameState} from "@/neKahoot/types"


interface BoardProps {
    isHost: boolean
    sessionId: string
}

function Countdown({timeLimit}: { timeLimit: Date }) {
    const [timeLeft, setTimeLeft] = useState<number>(0)

    useEffect(() => {
        const interval = setInterval(() => {
            setTimeLeft(Math.max(0, Math.round((timeLimit.getTime() - Date.now()) / 1000)))
        }, 100)
        return () => clearInterval(interval)
    }, [timeLimit])

    return (
        <p className="text-primary font-bold text-6xl">{timeLeft}</p>
    )
}

function Question({question}: { question: string }) {
    return (
        <div className="bg-panel p-16 rounded-xl h-96 flex items-center">
            <p className="text-primary font-bold text-6xl">{question}</p>
        </div>
    )
}

function AnswerOption({answer, onClick, selected, correct}: {
    answer: string,
    onClick: () => void,
    selected?: boolean,
    correct?: boolean
}) {
    return (
        <button className={`bg-panel p-8 rounded-xl grow m-8 hover:scale-110 duration-500 
            ${selected ? "bg-quit scale-105" : ""}
            ${correct ? "bg-next scale-110" : ""}`}>
            <p
                onClick={onClick}
                className={`text-primary font-bold text-6xl duration-500
                    ${correct ? "text-X" : ""}`}>
                {answer}
            </p>
        </button>
    )
}

export function Board({isHost, sessionId}: BoardProps) {
    // const [game, errors, sendMessage] = useServerState(isHost ? "host" : "client", {"id": sessionId})

    const [game, setGame] = useState<GameState>({
        state: "_LOADING",
    })

    useEffect(() => {
        const h1 = setTimeout(() => setGame({
            state: "OPENED_QUESTION",
            question: "What is the best programming language?",
            answerOptions: [
                "JavaScript",
                "Java",
                "Python",
                "C++",
            ],
            timeLimit: new Date(Date.now() + 10000),
        }), 0)
        const h2 = setTimeout(() => setGame({
            state: "SHOW_QUESTION_ANSWER",
            question: "What is the best programming language?",
            answerOptions: [
                "JavaScript",
                "Java",
                "Python",
                "C++",
            ],
            answer: "JavaScript",
            givenAnswer: game.state == "OPENED_QUESTION" ? game.givenAnswer : undefined,
            timeLimit: new Date(Date.now() + 5000),
        }), 10000)

        const h3 = setTimeout(() => setGame({
            state: "OPENED_QUESTION",
            question: "What's 9 + 10?",
            answerOptions: [
                "19",
                "21",
            ],
            timeLimit: new Date(Date.now() + 10000),
        }), 15000)

        const h4 = setTimeout(() => setGame({
            state: "SHOW_QUESTION_ANSWER",
            question: "What's 9 + 10?",
            answerOptions: [
                "19",
                "21",
            ],
            answer: "21",
            answerDescription: "<a href='https://youtu.be/UFu8UV2DRlU?si=9GD8JyC7OJfy98wp'>тык</a>",
            timeLimit: new Date(Date.now() + 15000),
        }), 25000)

        const h5 = setTimeout(() => setGame({
            state: "OPENED_QUESTION",
            question: "Сколько баллов надо поставить нашей команде?",
            answerOptions: [
                "0",
                "посмотрим",
                "5+5+5+3",
                "¯\\_(ツ)_/¯",
            ],
            timeLimit: new Date(Date.now() + 10000),
        }), 40000)

        const h6 = setTimeout(() => setGame({
            state: "SHOW_QUESTION_ANSWER",
            question: "Сколько баллов надо поставить нашей команде?",
            answerOptions: [
                "5+5+5+3",
                "5+5+5+3",
                "5+5+5+3",
                "5+5+5+3",
            ],
            answer: "5+5+5+3",
            timeLimit: new Date(Date.now() + 5553555355535553),
        }), 50000)

        return () => {
            clearTimeout(h1)
            clearTimeout(h2)
            clearTimeout(h3)
            clearTimeout(h4)
            clearTimeout(h5)
            clearTimeout(h6)
        }
    }, [])

    let content
    if (game.state == "_LOADING") {
        content = <p className="bold text-primary font-bold text-6xl">Loading...</p>
    } else if (game.state == "OPENED_QUESTION") {
        content = <div>
            <div className="flex justify-center">
                <Countdown timeLimit={game.timeLimit}/>
            </div>
            <Question question={game.question}/>
            <div className="flex">
                {game.answerOptions.map((answer, i) =>
                    <AnswerOption
                        onClick={() => setGame({
                            ...game,
                            givenAnswer: answer,
                        })}
                        key={i}
                        answer={answer}
                        selected={answer == game.givenAnswer}
                    />)}
            </div>
        </div>
    } else if (game.state == "SHOW_QUESTION_ANSWER") {
        content = <div>
            <div className="flex justify-center">
                <Countdown timeLimit={game.timeLimit}/>
            </div>
            <Question question={game.question}/>
            <div className="flex">
                {game.answerOptions.map((answer, i) =>
                    <AnswerOption
                        onClick={() => undefined}
                        key={i}
                        answer={answer}
                        selected={answer == game.givenAnswer}
                        correct={answer == game.answer}
                    />)}
            </div>
            {
                game.answerDescription &&
                <div className="p-8 rounded-xl text-4xl bg-panel text-primary"
                     dangerouslySetInnerHTML={{__html: game.answerDescription || ""}}></div>
            }
        </div>
    } else {
        console.error(game)
    }

    return (
        <div className="flex flex-col justify-center items-center h-full min-h-[500px]">
            {content}
        </div>
    )
}
