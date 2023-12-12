import type {NextPage} from 'next'
import Head from 'next/head'
import React, {useEffect, useState} from 'react'
import {XIcon} from "@/tic_tac_toe/XIcon"
import {OIcon} from "@/tic_tac_toe/OIcon"
import {createGame, getQuizList} from "@/game/api"
import {TemplateCard} from "@/components/TemplateCard"
import {TemplateInfo} from "@/game/types"
import {TicTacToe, TicTacToeState} from "@/components/TicTacToe"
import Button from "@/components/Button"

type AppState = {
    kind: "main_page"
    modal: undefined | "tic_tac_toe"
    sessionId: string
} | {
    kind: "joining"
    sessionId: string
} | {
    kind: "fatal"
    error: Node | string
} | {
    kind: "tic_tac_toe"
    state: TicTacToeState
}

const Home: NextPage = () => {
    const [state, setState] = useState<AppState>({
        kind: "main_page",
        sessionId: "",
        modal: undefined,
    })
    const [quizInfo, setQuizInfo] = useState<TemplateInfo[] | undefined>()
    const [replaceMarksChecked, setReplaceMarks] = useState(false)
    const [openMultipleQuestionsChecked, setOpenMultipleQuestions] = useState(false)

    useEffect(() => {
        getQuizList().then(quizInfo => setQuizInfo(quizInfo))
        // TODO: do proper updates
        const handle = setInterval(() => getQuizList().then(quizInfo => setQuizInfo(quizInfo)), 10000)
        return () => clearInterval(handle)
    }, [])

    let content
    if (state.kind == "main_page") {
        content = <div>
            <div className="mt-10 md:mt-16 w-[1000px] flex flex-col items-center justify-center mx-auto">
                <div className="w-full flex gap-4 m-8">
                    <ul className="grow space-y-4">
                        <div className="text-3xl text-txt font-bold w-full text-center mb-4">
                            HOST A GAME
                        </div>

                        <li className="bg-panel px-8 py-4 rounded-xl w-full">
                            <h2
                                className="text-3xl md:text-5xl font-extrabold m-4 text-primary">
                                TIC TAC TOE
                            </h2>

                            <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                                <XIcon/>
                                <OIcon/>
                            </div>

                            <div className="w-full flex justify-end">
                                <Button onClick={() => setState({...state, modal: "tic_tac_toe"})}>
                                    BROWSE GAMES
                                </Button>
                            </div>
                        </li>

                        <li className="bg-panel px-8 py-4 rounded-xl w-full">
                            <h2
                                className="text-3xl md:text-5xl font-extrabold m-4 text-primary">
                                ALSO TIC TAC TOE, SHUT UP
                            </h2>

                            <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                                <XIcon/>
                                <OIcon/>
                            </div>

                            <div className="w-full flex justify-end">
                                <Button onClick={() => setState({...state, modal: "tic_tac_toe"})}>
                                    BROWSE GAMES
                                </Button>
                            </div>
                        </li>
                    </ul>

                    <div>
                        <div className="text-3xl text-txt font-bold w-full text-center mb-4">
                            JOIN A GAME
                        </div>
                        <form
                            className="flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl bg-panel space-y-8 md:space-y-6">

                            <p className="text-md text-JoinGameTxt uppercase font-extrabold  md:text-3xl space-y-12">
                                ENTER GAME ID
                            </p>

                            <input
                                type="text"
                                className="mt-1 border  w-80 h-24 rounded-xl px-2 py-3 bg-panel outline-0 text-3xl md:text-4xl font-bold  text-center text-txt outline-none"
                                value={state.sessionId}
                                onChange={e => {
                                    setState({kind: "main_page", modal: undefined, sessionId: e.target.value})
                                }}
                            />

                            <Button>
                                JOIN!
                            </Button>
                        </form>
                    </div>
                </div>
                {/*
                <div
                    className={`flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl  bg-panel mt-6 space-y-8 md:space-y-8`}>
                    <p className={`text-md text-txt uppercase font-extrabold md:text-3xl space-y-12 `}>
                        SELECT MODE
                    </p>
                    <div
                        className="bg-gray-800 flex items-center justify-evenly h-35 rounded-2xl p-2 ">
                        <button onClick={() => {
                            // setState({kind: "tic_tac_toe", state: {kind: "creating_game"}})
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
                */}
            </div>
        </div>
    } else if (state.kind == "joining") {
        content = <form onSubmit={e => {
            setState({
                kind: "tic_tac_toe",
                state: {kind: "playing", sessionId: state.sessionId, role: "board"},
            })
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
    } else if (state.kind == "fatal") {
        content = <div>TODO</div>
    } else if (state.kind == "tic_tac_toe") {
        content = <TicTacToe state={state.state}/>
    } else {
        checkExhausted(state)
    }

    let modalContent
    if (state.kind !== "main_page" || state.modal === undefined) {
        // do nothing
    } else if (state.modal === "tic_tac_toe") {
        modalContent = <div
            className="flex flex-col items-center w-[1000px] rounded-2xl bg-selectPanel p-6">
            <div
                className={`px-8 flex flex-row items-center w-[1000px] rounded-2xl bg-panel space-x-96`}>
                <p className={`justify-items-start text-md py-6  text-JoinGameTxt uppercase font-extrabold  md:text-2xl `}>
                    CHOOSE EXISTING GAME
                </p>

                <button onClick={() => setState({kind: "tic_tac_toe", state: {kind: "constructor"}})}
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
                            <TemplateCard
                                template={quiz} key={quiz.id}
                                handleSelect={async (id: string) => {
                                    const sessionId = (await createGame(id, {
                                        replaceMarks: replaceMarksChecked ? "ENABLED" : "DISABLED",
                                        openMultipleQuestions: openMultipleQuestionsChecked ? "ENABLED" : "DISABLED",
                                    })).id
                                    setState({kind: "tic_tac_toe", state: {kind: "playing", sessionId: sessionId, role: "host"}})
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
                        <span style={{color: "orange"}}>Enable replace marks</span>
                    </li>
                    <li>
                        <input
                            type="checkbox"
                            checked={openMultipleQuestionsChecked}
                            onChange={() => setOpenMultipleQuestions(!openMultipleQuestionsChecked)}
                        />
                        <span style={{color: "orange"}}>Enable open multiple questions</span>
                    </li>
                </ul>
            </div>
        </div>
    } else {
        checkExhausted(state.modal)
    }

    return <div className={`flex min-h-screen bg-back flex-col items-center  justify-items-center   py-2`}>
        <Head>
            <title>
                {state.kind === "tic_tac_toe"
                    ? <>CLVR: Tic-Tac-Toe</>
                    : <>CLVR</>}
            </title>
            <link rel="icon" href="/tictactoe.ico"/>
        </Head>

        {state.kind === "main_page" && state.modal !== undefined &&
            <div className="
                overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 justify-center
                items-center w-full md:inset-0 max-h-full
                flex bg-opacity-40 bg-gray-950"
                 onClick={() => setState({...state, modal: undefined})}>
                <div className="rounded-xl p-8 bg-panel" onClick={e => e.stopPropagation()}>
                    {modalContent}
                </div>
            </div>
        }

        <h1 className={`text-4xl md:text-5xl font-extrabold mt-4 text-primary`}>
            C<span className="text-createcol">L</span>V<span className="text-createcol">R</span>
        </h1>

        {content}
    </div>
}

export default Home
