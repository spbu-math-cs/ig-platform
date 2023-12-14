import type {NextPage} from 'next'
import React, {useEffect, useState} from 'react'
import {XIcon} from "@/tic-tac-toe/XIcon"
import {OIcon} from "@/tic-tac-toe/OIcon"
import {createGame, getQuizList} from "@/tic-tac-toe/api"
import {TemplateCard} from "@/components/TemplateCard"
import {TemplateInfo} from "@/tic-tac-toe/types"
import {TicTacToe, TicTacToeState} from "@/components/TicTacToe"
import {NeKahoot, NeKahootState} from "@/components/NeKahoot"
import Button from "@/components/Button"
import {checkExhausted} from "@/utils"
import {LogIn} from "@/components/Authorization";
import {RootState, store} from "@/state/store";
import {useSelector} from "react-redux";
import {nextTheme} from "@/state/themeSlice"
import Head from "next/head"

type AppState = {
    kind: "main_page"
    modal: undefined | "tic_tac_toe" | "neKahoot"
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
} | {
    kind: "logging"
} | {
    kind: "neKahoot"
    state: NeKahootState
}

export type AppAction = {
    kind: "go_to_main_page"
} | {
    kind: "go_to_creating"
    game: "tic_tac_toe" | "neKahoot"
}

const Home: NextPage = () => {
    const [state, setState] = useState<AppState>({
        kind: "main_page",
        sessionId: "",
        modal: undefined,
    })
    const dispatch = store.dispatch
    const theme = useSelector((state: RootState) => state.theme)

    const [quizInfo, setQuizInfo] = useState<TemplateInfo[] | undefined>()
    const [replaceMarksChecked, setReplaceMarks] = useState(false)
    const [openMultipleQuestionsChecked, setOpenMultipleQuestions] = useState(false)

    const runAction = (action: AppAction) => {
        if (action.kind == "go_to_main_page") {
            setState({kind: "main_page", sessionId: "", modal: undefined})
        } else if (action.kind == "go_to_creating") {
            if (action.game == "tic_tac_toe") {
                setState({kind: "main_page", sessionId: "", modal: "tic_tac_toe"})
                getQuizList().then(quizInfo => setQuizInfo(quizInfo))
            } else if (action.game == "neKahoot") {
                setState({kind: "main_page", sessionId: "", modal: "neKahoot"})
                getQuizList().then(quizInfo => setQuizInfo(quizInfo))
            } else {
                checkExhausted(action.game)
            }
        } else {
            checkExhausted(action)
        }
    }

    useEffect(() => {
        getQuizList().then(quizInfo => setQuizInfo(quizInfo))
    }, [])

    let content
    if (state.kind == "main_page") {
        content = <div>
            <div className="mt-10 md:mt-16 w-[1000px] flex flex-col items-center justify-center mx-auto">
                <div className="w-full flex flex-row space-x-10 gap-4 m-8">
                    <div className="grow space-y-4">
                        <div
                            className="text-3xl text-txt font-bold text-center rounded-xl outline-1 px-6 py-3 ring-4 ring-txt  ">
                            HOST A GAME
                        </div>

                        <div className="flex flex-col space-y-4 bg-panel px-8 py-4 rounded-xl w-full h-60 items-center">
                            <h2 className="text-3xl md:text-5xl font-extrabold m-4 text-primary">
                                TIC TAC TOE
                            </h2>

                            <div className="flex flex-row rounded-xl px-6 items-center justify-center space-x-2">
                                <a href="/tictactoe.ico" className="flex items-center">
                                    <img src="/tictactoe.ico" className=" mt-2 mr-3 h-10 sm:h-12" alt={""}/>
                                </a>

                                <div className="mt-2 w-full flex justify-end">
                                    <Button onClick={() => setState({...state, modal: "tic_tac_toe"})}>
                                        BROWSE GAMES
                                    </Button>
                                </div>
                            </div>
                        </div>

                        <div className="flex flex-col space-y-4 bg-panel px-8 py-4 rounded-xl w-full h-60 items-center">
                            <h2
                                className="text-3xl md:text-5xl font-extrabold m-4 text-primary">
                                !KAHOOT
                            </h2>

                            <div className="flex flex-row rounded-xl px-6 items-center justify-center space-x-2">
                                <a href="/kahoot.ico" className="flex items-center">
                                    <img src="/kahoot.ico" className=" mt-2 mr-3 h-10 sm:h-12 rounded-xl" alt={""}/>
                                </a>

                                <div className="mt-2 w-full flex justify-end">
                                    <Button onClick={() => setState({...state, modal: "neKahoot"})}>
                                        BROWSE GAMES
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="flex flex-col space-y-4">
                            <div className="text-3xl text-txt font-bold w-full text-center mb-4 rounded-xl outline-1 px-6 py-3 ring-4 ring-txt">
                                JOIN A GAME
                            </div>
                            <form
                                onSubmit={e => { setState({
                                    kind: "tic_tac_toe",
                                    state: {kind: "playing", sessionId: state.sessionId, role: "board"},
                                })
                                e.preventDefault()
                            }}
                                className="flex flex-col
                                 items-center w-[700px] md:w-[450px] h-[90py] rounded-2xl bg-panel py-4 space-y-8 md:space-y-6">
                                <p className="text-2xl md:text-5xl font-extrabold m-4 text-primary">
                                    ENTER GAME ID
                                </p>
                                <input
                                    type="text"
                                    className="mt-1 border  w-80 h-24 rounded-xl px-2 py-3 bg-panel outline-0 text-3xl md:text-4xl font-bold  text-center text-txt outline-none"
                                    value={state.sessionId}
                                    onChange={e => {
                                        setState({kind: "main_page", sessionId: e.target.value, modal: undefined})
                                    }}
                                />
                                <Button>
                                    JOIN!
                                </Button>
                            </form>
                        <div
                            className="flex flex-row items-center justify-center py-4 space-x-8 rounded-xl h-40 outline-1 px-6 ring-4 ring-txt">

                            <div className="flex justify-end">
                                <Button onClick={() => dispatch(nextTheme())}>
                                    CHANGE THEME
                                </Button>
                            </div>
                            <div className="flex justify-end">
                                <Button onClick={() => setState({kind: "logging"})}>
                                    LOG IN
                                </Button>
                            </div>
                        </div>
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
        content = <TicTacToe state={state.state} dispatch={runAction}/>
    } else if (state.kind == "logging") {
        content =
            <LogIn switchPage={setState}></LogIn>
    } else if (state.kind == "neKahoot") {
        content = <NeKahoot state={state.state} dispatch={runAction}/>
    } else {
        checkExhausted(state)
    }

    let modalContent
    if (state.kind !== "main_page" || state.modal === undefined) {
        // do nothing
    } else if (state.modal === "tic_tac_toe") {
        modalContent = <div className="flex flex-col items-center w-[1000px] rounded-2xl bg-square">
            <div className={`px-8 flex flex-row items-center w-[1000px] rounded-2xl bg-square space-x-96`}>
                <p className={`justify-items-start text-md text-JoinGameTxt uppercase font-extrabold  md:text-2xl `}>
                    CHOOSE EXISTING GAME
                </p>

                <button onClick={() => setState({kind: "tic_tac_toe", state: {kind: "constructor"}})}
                        className={`button hover:ring-4 py-2 hover:ring-cyan-300 rounded-xl px-6 bg-[#f3b236] hover:bg-square`}>
                    or CREATE NEW QUIZ
                </button>
            </div>
            <div
                className="
                    flex flex-col justify-items-start py-10 px-100 rounded
                    mb-2 -scroll-ms-3 overflow-auto text-md text-JoinGameTxt
                    max-h-[60vh] bg-square">
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
                                    setState({
                                        kind: "tic_tac_toe",
                                        state: {kind: "playing", sessionId: sessionId, role: "host"}
                                    })
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
                        <span className="text-txt font-bold">Enable replace marks</span>
                    </li>
                    <li>
                        <input
                            type="checkbox"
                            checked={openMultipleQuestionsChecked}
                            onChange={() => setOpenMultipleQuestions(!openMultipleQuestionsChecked)}
                        />
                        <span className="text-txt font-bold">Enable open multiple questions</span>
                    </li>
                </ul>
            </div>


        </div>
    } else if (state.modal === "neKahoot") {
        modalContent = <div className="flex flex-col items-center w-[1000px] rounded-2xl bg-square">
            <div className={`px-8 flex flex-row items-center w-[1000px] rounded-2xl bg-square space-x-96`}>
                <p className={`justify-items-start text-md text-JoinGameTxt uppercase font-extrabold  md:text-2xl `}>
                    CHOOSE EXISTING GAME
                </p>
            </div>
            <div
                className="
                    flex flex-col justify-items-start py-10 px-100 rounded
                    mb-2 -scroll-ms-3 overflow-auto text-md text-JoinGameTxt
                    max-h-[60vh] bg-square">
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
                                    setState({
                                        kind: "neKahoot",
                                        state: {kind: "playing", sessionId: sessionId, role: "host"}
                                    })
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
                        <span className="text-txt font-bold">Enable replace marks</span>
                    </li>
                    <li>
                        <input
                            type="checkbox"
                            checked={openMultipleQuestionsChecked}
                            onChange={() => setOpenMultipleQuestions(!openMultipleQuestionsChecked)}
                        />
                        <span className="text-txt font-bold">Enable open multiple questions</span>
                    </li>
                </ul>
            </div>


        </div>
    } else {
        checkExhausted(state.modal)
    }

    return <div
        className={`flex min-h-screen bg-back flex-col items-center  justify-items-center  max-w-screen  py-2`}>
        <div className={`flex flex-row justify-between items-center`}>
            <div className="flex flex-row items-center ">
                <img src={
                state.kind == "tic_tac_toe" ? "/tictactoe.ico"
                    : state.kind == "neKahoot" ? "/kahoot.ico"
                    : "/clover.PNG"} className="h-20" alt={""}/>
                <a href={"/"}>
                    <h1 className={`ml-3 text-6xl md:text-6xl font-extrabold mt-8 text-primary`}>
                        C<span className="text-createcol">L</span>V<span className="text-createcol">R</span>
                    </h1>
                </a>
            </div>
        </div>

        <Head>
            <title>
                {state.kind === "tic_tac_toe" ? "CLVR: Tic-Tac-Toe"
                    : state.kind == "neKahoot" ? "CLVR: neKahoot"
                    : "CLVR"}
            </title>
            <link rel="icon" href={
                state.kind == "tic_tac_toe" ? "/tictactoe.ico"
                    : state.kind == "neKahoot" ? "/kahoot.ico"
                    : "/clover.ico"
            }/>
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
        <link rel="icon" href="/tictactoe.ico"/>
        {content}
    </div>
}

export default Home
