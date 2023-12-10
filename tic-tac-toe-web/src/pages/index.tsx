import type {NextPage} from 'next'
import Head from 'next/head'
import React, {useEffect, useState} from 'react'
import {Board} from '@/components/Board'
import {nextTheme} from "@/state/themeSlice"
import {useDispatch} from "react-redux"
import {XIcon} from "@/components/XIcon"
import {OIcon} from "@/components/OIcon"
import {createGame, getQuizList, login} from "@/game/api"
import {QuizCard} from "@/components/QuizCard"
import {GameConfig, QuizInfo} from "@/game/types"
import {EditBoard} from "@/components/EditBoard"

type AppState = {
    kind: "main_page"
} | {
    kind: "playing"
    sessionId: string
    role: "host" | "board"
} | {
    kind: "joining"
    sessionId: string
} | {
    kind: "choosing_game"
} | {
    kind: "constructor"
} | {
    kind: "fatal"
    error: Node | string
}

const Home: NextPage = () => {
    const [state, setState] = useState<AppState>({kind: "main_page"})
    const [quizInfo, setQuizInfo] = useState<QuizInfo[] | undefined>()
    const [replaceMarksChecked, setReplaceMarks] = useState(false);
    const [openMultipleQuestionsChecked, setOpenMultipleQuestions] = useState(false);

    const dispatch = useDispatch()

    useEffect(() => {
        getQuizList().then(quizInfo => setQuizInfo(quizInfo))
        // TODO: do proper updates
        const handle = setInterval(() => getQuizList().then(quizInfo => setQuizInfo(quizInfo)), 10000)
        return () => clearInterval(handle)
    }, [])

    let content
    if (state.kind == "main_page") {
        content = <div>
            <div className="mt-10 md:mt-16 w-[500px] flex flex-col items-center justify-center mx-auto">
                <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                    <XIcon/>
                    <OIcon/>
                </div>
                <button onClick={async () => { await login() }}> LOGIN </button>
                <div
                    className={`flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl  bg-panel mt-6 space-y-8 md:space-y-8`}>
                    <p className={`text-md text-txt uppercase font-extrabold  md:text-3xl space-y-12 `}>
                        SELECT MODE
                    </p>
                    <div
                        className="bg-gray-800 flex items-center justify-evenly h-35 rounded-2xl p-2 ">
                        <button onClick={() => {
                            setState({kind: "choosing_game"})
                        }}
                                className={`focus:bg-gray-300 hover:bg-[#ffe1a9]  transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6  text-3xl md:text-4xl font-extrabold mt-1 text-hostTxt `}>
                            CREATE
                        </button>

                        <button onClick={() => {
                            setState({kind: "joining", sessionId: ""})
                        }}
                                className={`focus:bg-gray-300 hover:bg-[#ffe1a9] transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6 text-3xl md:text-4xl font-extrabold mt-1 text-playerTxt`}>
                            JOIN
                        </button>
                    </div>
                </div>
            </div>
            <div className="mt-106 md:mt-106  flex flex-col items-center justify-center mx-auto">
                <button onClick={() => dispatch(nextTheme())}
                        className={`button hover:ring-4 hover:ring-cyan-300 rounded-xl mt-8 px-6 py-3 bg-createcol hover:bg-panel`}>
                    Change Theme
                </button>
            </div>
        </div>
    } else if (state.kind == "joining") {
        content = <form onSubmit={e => {
            setState({kind: "playing", sessionId: state.sessionId, role: "board"})
            e.preventDefault()
        }}>
            <div
                className="mt-10 md:mt-16 w-[500px] flex flex-col items-center justofy-center mx-auto">
                <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                    <XIcon/>
                    <OIcon/>
                </div>
                <div
                    className={`flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl bg-panel mt-6 space-y-8 md:space-y-6`}>
                    <p className={`text-md text-JoinGameTxt uppercase font-extrabold  md:text-3xl space-y-12 `}>
                        ENTER GAME ID
                    </p>

                    <input
                        type="text"
                        className={`mt-1 border  w-80 h-24 rounded-xl px-2 py-3 bg-panel outline-0
                                    text-3xl md:text-4xl font-bold  text-center text-txt outline-none `}
                        value={state.sessionId}
                        onChange={e => {
                            setState({kind: "joining", sessionId: e.target.value})
                        }}
                    />
                </div>
            </div>
        </form>
    } else if (state.kind == "choosing_game") {
        content = <div>
            <div
                className="mt-10 w-[1000px] flex flex-col items-center justify-center">
                <div
                    className="flex flex-col items-center w-[1000px] md:w-[1000px] md:h-[650px] rounded-2xl bg-selectPanel p-6">
                    <div
                        className={`px-8 flex flex-row items-center w-[1000px] md:w-[1000px] rounded-2xl bg-panel space-x-96`}>
                        <p className={`justify-items-start text-md py-6  text-JoinGameTxt uppercase font-extrabold  md:text-2xl `}>
                            CHOOSE EXISTING GAME
                        </p>

                        <button onClick={() => setState({kind: "constructor"})}
                                className={`button hover:ring-4 py-2 hover:ring-cyan-300 rounded-xl px-6 bg-[#f3b236] hover:bg-panel`}>
                            CREATE NEW QUIZ
                        </button>
                    </div>

                    <div
                        className="flex flex-col justify-items-start py-10 px-100 rounded mb-2 -scroll-ms-3 overflow-auto
                                            text-md text-JoinGameTxt">
                        {
                            quizInfo === undefined
                                ? "Loading..."
                                : quizInfo.map(quiz =>
                                    <QuizCard
                                        quiz={quiz} key={quiz.id}
                                        handleSelect={async (id: string) => {
                                            const sessionId = (await createGame(id, {
                                                replaceMarks: replaceMarksChecked ? "ENABLED" : "DISABLED",
                                                openMultipleQuestions: openMultipleQuestionsChecked ? "ENABLED" : "DISABLED"
                                            })).id
                                            setState({kind: "playing", sessionId: sessionId, role: "host"})
                                        }}/>,
                                )}
                    </div>

                    <div style={{alignContent: "left", transform: "scale(1.5)"}}>
                        <ul>
                            <li>
                                <input 
                                    type="checkbox"
                                    checked={replaceMarksChecked}
                                    onChange={() => setReplaceMarks(!replaceMarksChecked)}
                                />
                                <span style={{color:"orange"}}>Enable replace marks</span>
                            </li>
                            <li>
                                <input 
                                    type="checkbox"
                                    checked={openMultipleQuestionsChecked}
                                    onChange={() => setOpenMultipleQuestions(!openMultipleQuestionsChecked)}
                                />
                                <span style={{color:"orange"}}>Enable open multiple questions</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    } else if (state.kind == "constructor") {
        content = <div className="mt-10 w-[1000px] items-center justify-center ">
            <EditBoard
                handleGameIsConstructed={() => {
                    getQuizList().then(quizInfo => setQuizInfo(quizInfo))
                    setState({kind: "choosing_game"})
                }}/>
        </div>
    } else if (state.kind == "playing") {
        content = 
        <div>
            <center>
                <h1 className={`text-100xl md:text-10000xl font-extrabold text-primary`}>
                    {"session: " + state.sessionId}
                </h1>
            </center>
            <Board sessionId={state.sessionId} isHost={state.role == "host"}/>
        </div>
    } else if (state.kind == "fatal") {
        content = <div>TODO</div>
    } else {
        checkExhausted(state)
    }

    return <div className={`flex min-h-screen bg-back flex-col items-center  justify-items-center   py-2`}>
        <Head>
            <title>Tic-Tac-Toe Game</title>
            <link rel="icon" href="/tictactoe.ico"/>
        </Head>


        <h1 className={`text-4xl md:text-5xl font-extrabold mt-4 text-primary`}>
            TIC{" "}<span className="text-createcol">TAC </span>{" "}TOE
        </h1>

        {content}
    </div>
}

export default Home
