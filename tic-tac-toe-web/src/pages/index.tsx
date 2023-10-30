import type {NextPage} from 'next'
import Head from 'next/head'
import React, {useEffect, useState} from 'react'
import {Board} from '@/components/Board'
import {WinnerModal} from '@/components/WinnerModal'
import {nextTheme} from "@/state/themeSlice"
import {useDispatch} from "react-redux"
import {XIcon} from "@/components/XIcon"
import {OIcon} from "@/components/OIcon"


const Home: NextPage = () => {
    const rows = 3
    const cols = 3
    const [isX, setIsX] = useState<boolean>(true)
    const [sessionId, setSessionId] = useState<string>("239")
    const [newGame, setNewGame] = useState<boolean>(false)
    const [squares, setSquares] = useState<Array<any>>(Array(cols * rows).fill(null))
    const [isHost, setIsHost] = useState<boolean>(true)
    const [isJoining, setIsJoining] = useState<boolean>(false)
    const [isError, setIsError] = useState<boolean>(false)
    const [joiningGameId, setJoiningGameId] = useState('')

    const dispatch = useDispatch()

    // let winner = calculateWinner(squares)
    let winner = ""

    const handleNewGame = () => {
        setIsX(true)
        // @ts-ignore
        setSquares(Array(cols * rows).fill(null))
        squares[0] = "ML"
        squares[1] = "DB"
        squares[2] = "Algos"

        squares[3] = "CS"
        squares[4] = "OOP"
        squares[5] = "DEV"

        squares[6] = "FP"
        squares[7] = "DB"
        squares[8] = "ACOS"

        setSquares(squares)

        if (isHost) {
            fetch("http://0.0.0.0:8080/api/game-session", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    "quiz": {
                        "id": "ABCD",
                    },
                }),
            }).then((response) => {
                return response.json()
            }).then((data) => {
                console.log(data)
                setNewGame(true)
                setIsJoining(false)
                setSessionId(data.session.id)
            }).catch(e => {
                console.error(e)
            })
        } else {
            setSessionId(joiningGameId)
            setNewGame(true)
            setIsJoining(false)
        }
    }

    return (
        <div className={`flex min-h-screen bg-back flex-col items-center py-2`}>
            <Head>
                <title>Tic-Tac-Toe Game</title>
                <link rel="icon" href="/tictactoe.ico"/>
            </Head>


            <h1 className={`text-4xl md:text-5xl font-extrabold mt-4 text-primary`}>
                TIC{" "}<span className="text-[#f3b236]">TAC </span>{" "}TOE
            </h1>

            {!newGame
                ?
                (!isJoining ?
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
                                            setIsHost(true)
                                            handleNewGame()
                                        }}
                                                className={`focus:bg-gray-300 hover:bg-[#ffe1a9] transition duration-300 ease-in flex items-center justify-center rounded-xl px-6 py-6  text-3xl md:text-4xl font-extrabold mt-1 text-hostTxt `}>
                                            CREATE
                                        </button>

                                        <button onClick={() => {
                                            setIsHost(false)
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
                                        className={`button hover:ring-4 hover:ring-cyan-300 rounded-xl mt-8 px-6 py-3 bg-[#f3b236] hover:bg-panel`}>
                                    Change Theme
                                </button>
                            </div>
                        </div>)
                        :

                        <form onSubmit={e => {
                            if (joiningGameId === 'Hello world') {
                                setIsError(true)
                                setIsHost(false)
                                setIsJoining(true)
                            } else {
                                handleNewGame()
                            }
                            e.preventDefault()
                        }}>
                            <div className="mt-10 md:mt-16 w-[500px] flex flex-col items-center justofy-center mx-auto">
                                <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                                    <XIcon/>
                                    <OIcon/>
                                </div>
                                <div
                                    className={`flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl bg-panel mt-4 space-y-8 md:space-y-6`}>
                                    <p className={`text-md text-hostTxt uppercase font-extrabold  md:text-3xl space-y-12 `}>
                                        ENTER GAME ID
                                    </p>

                                    <input
                                        type="text"
                                        className={`mt-1 border  w-80 h-24 rounded-xl px-2 py-3 bg-panel outline-0
                                    text-3xl md:text-4xl font-bold  text-center text-txt outline-none `}
                                        value={joiningGameId}
                                        onChange={e => { console.log(e.currentTarget.value); return setJoiningGameId(e.currentTarget.value); }}
                                    />

                                    <button onClick={e => { console.log("Joining game " + joiningGameId) }}
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
                        </form>

                )
                :
                <div>
                    <center>
                        <h1 className={`text-100xl md:text-10000xl font-extrabold mt-0 text-primary`}>
                            session: {sessionId}{" "}
                        </h1>
                    </center>
                    {(isHost ?
                    <Board
                        playerX={isX}
                        sessionId={sessionId}
                        handleRestartGame={() => {
                            setIsX(true)
                            setSquares(Array(cols * rows).fill(null))
                        }}
                        isHost={isHost}/>
                    :
                    <Board
                        playerX={isX}
                        sessionId={sessionId}
                        handleRestartGame={() => {
                        }}
                        isHost={isHost}
                    />)}
                </div>
            }
            {
                winner &&
                <WinnerModal
                    winner={winner}
                    handleQuitGame={() => {
                        setIsX(true)
                        setSquares(Array(cols * rows).fill(null))
                        setNewGame(false)
                    }}
                    handleNewGame={handleNewGame}
                />
            }
        </div>
    )
}

export default Home
