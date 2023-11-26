import type {NextPage} from 'next'
import Head from 'next/head'
import React, {useEffect, useState} from 'react'
import {Board} from '@/components/Board'
import {nextTheme} from "@/state/themeSlice"
import {useDispatch} from "react-redux"
import {XIcon} from "@/components/XIcon"
import {OIcon} from "@/components/OIcon"
import {createGame, getQuizList} from "@/game/api"
import {QuizCard} from "@/components/QuizCard"
import {QuizInfo} from "@/game/types"
import {EditBoard} from "@/components/EditBoard"

type AppState = {
    kind: "main_page"
} | {
    kind: "playing"
    id: string
    role: "host" | "board"
} | {
    kind: "joining"
    id: string
} | {
    kind: "choosing_game"
} | {
    kind: "constructor"
} | {
    kind: "fatal"
    error: Node | string
}

const Home: NextPage = () => {
    const [appState, setAppState] = useState<AppState>({kind: "main_page"})

    const [sessionId, setSessionId] = useState<string>("239")
    const [newGame, setNewGame] = useState<boolean>(false)
    const [isHost, setIsHost] = useState<boolean>(true)
    const [isJoining, setIsJoining] = useState<boolean>(false)
    const [isCreating, setIsCreating] = useState<boolean>(false)
    const [isError, setIsError] = useState<boolean>(false)
    const [joiningGameId, setJoiningGameId] = useState('')
    const [quizInfo, setQuizInfo] = useState<QuizInfo[] | undefined>()
    const [isConstructor, setGameConstructor] = useState<boolean>(false)

    const dispatch = useDispatch()

    useEffect(() => {
        getQuizList().then(quizInfo => setQuizInfo(quizInfo))
    }, [])

    return <div className={`flex min-h-screen bg-back flex-col items-center  justify-items-center   py-2`}>
        <Head>
            <title>Tic-Tac-Toe Game</title>
            <link rel="icon" href="/tictactoe.ico"/>
        </Head>


        <h1 className={`text-4xl md:text-5xl font-extrabold mt-4 text-primary`}>
            TIC{" "}<span className="text-createcol">TAC </span>{" "}TOE
        </h1>

        {!newGame ?
            ((!isJoining && !isCreating) ?
                (<div>
                    <div className="mt-10 md:mt-16 w-[500px] flex flex-col items-center justify-center mx-auto">
                        <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                            <XIcon/>
                            <OIcon/>
                        </div>
                        <div
                            className={`flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl  bg-panel mt-6 space-y-8 md:space-y-8`}>
                            <p className={`text-md text-txt uppercase font-extrabold  md:text-3xl space-y-12 `}>
                                SELECT MODE
                            </p>
                            <div
                                className="bg-gray-800 flex items-center justify-evenly h-35 rounded-2xl p-2 ">
                                <button onClick={() => {
                                    // handleCreateGame()
                                    setIsCreating(true)
                                }}
                                        className={`focus:bg-gray-300 hover:bg-[#ffe1a9]  transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6  text-3xl md:text-4xl font-extrabold mt-1 text-hostTxt `}>
                                    CREATE
                                </button>

                                <button onClick={() => {
                                    setIsJoining(true)
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
                </div>)
                : (isJoining) ?
                    (<form onSubmit={e => {
                        if (joiningGameId === 'Hello world') {
                            setIsError(true)
                            setIsHost(false)
                            setIsJoining(true)
                        } else {
                            setIsHost(false)
                            setSessionId(joiningGameId)
                            setNewGame(true)
                            setIsJoining(false)
                        }
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
                                    value={joiningGameId}
                                    onChange={e => {
                                        console.log(e.currentTarget.value)
                                        return setJoiningGameId(e.currentTarget.value)
                                    }}
                                />

                                <button onClick={e => {
                                    console.log("Joining game " + joiningGameId)
                                }}
                                        type="submit"
                                        className={`button hover:ring-4 hover:ring-cyan-300 rounded-xl mt-8 px-6 py-3 bg-[#f3b236] hover:bg-panel`}>
                                    START GAME
                                </button>

                                {isError ?
                                    <p className={`text-center font-extrabold text-error text-2xl `}>
                                        GAME DOESN'T EXIST
                                    </p>
                                    :
                                    ""
                                }
                            </div>
                        </div>
                    </form>)
                    : (!isConstructor ?
                            (<div>
                                <div
                                    className="mt-10 w-[1000px] flex flex-col items-center justify-center">
                                    <div
                                        className={`flex flex-col items-center w-[1000px] md:w-[1000px] md:h-[650px] rounded-2xl bg-selectPanel`}>

                                        <div
                                            className={`px-8 flex flex-row items-center w-[1000px] md:w-[1000px] rounded-2xl bg-panel space-x-96`}>
                                            <p className={`justify-items-start text-md py-6  text-JoinGameTxt uppercase font-extrabold  md:text-2xl `}>
                                                CHOOSE EXISTING GAME
                                            </p>

                                            <button onClick={() => setGameConstructor(true)}
                                                    className={`button hover:ring-4 py-2 hover:ring-cyan-300 rounded-xl px-6 bg-[#f3b236] hover:bg-panel`}>
                                                CREATE NEW GAME
                                            </button>
                                        </div>

                                        <div
                                            className="flex flex-col justify-items-start py-10 px-100 rounded mb-2 -scroll-ms-3 overflow-auto
                                            text-md text-JoinGameTxt">
                                            {
                                                quizInfo === undefined
                                                    ? "Loading..."
                                                    : quizInfo.map(quiz =>
                                                        <QuizCard quiz={quiz} key={quiz.id}
                                                                  handleSelect={async (id: string) => {
                                                                      setIsHost(true)

                                                                      const session = await createGame(id)
                                                                      console.log(id)
                                                                      setNewGame(true)
                                                                      setIsJoining(false)
                                                                      setSessionId(session.id)
                                                                  }}/>,
                                                    )}
                                        </div>
                                    </div>
                                </div>
                            </div>)
                            : <div className="mt-10 w-[1000px] items-center justify-center ">
                                <EditBoard
                                    handleGameIsConstructed={() => {
                                        setGameConstructor(false)
                                        setNewGame(false)
                                        setIsJoining(false)
                                        setIsCreating(false)
                                    }}></EditBoard>
                            </div>
                    ))
            :
            (
                <div>
                    <center>
                        <h1 className={`text-100xl md:text-10000xl font-extrabold mt-0 text-primary`}>
                            session: {sessionId}{" "}
                        </h1>
                    </center>
                    {
                        (isHost ?
                            <Board
                                sessionId={sessionId}
                                isHost={isHost}/>
                            :
                            <Board
                                sessionId={sessionId}
                                isHost={isHost}
                            />)
                    }
                </div>
            )
        }
    </div>
}

export default Home
