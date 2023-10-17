import type {NextPage} from 'next'
import Head from 'next/head'
import React, {useEffect, useState} from 'react'
import {Board} from '../components/Board'
import {ChooseMode, ColorTheme} from '../components/ChooseMode'
import {WinnerModal} from '../components/WinnerModal'


const TextColor = ["text-metalText", "text-2048Text", "text-purpleText"]
const BGColor = ["bg-metalBG", "bg-2048BG", "bg-purpleBG"]

const Home: NextPage = () => {
    const rows = 3
    const cols = 3
    const [isX, setIsX] = useState<boolean>(true)
    const [sessionId, setSessionId] = useState<string>("")
    const [newGame, setNewGame] = useState<boolean>(false)
    const [squares, setSquares] = useState<Array<any>>(Array(cols * rows).fill(null))
    const [themeNumber, setColorTheme] = useState<number>(1)

    useEffect(() => {
        fetch("http://0.0.0.0:8080/api/game-session", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                "quiz": {
                    "id": "ABCD",
                },
            })
        }).then((response) => {
            return response.json()
        }).then((data) => {
            setSessionId(data.session.id)
        })
    }, [])

    const [isHost, setIsHost] = useState<boolean>(true)

    // let winner = calculateWinner(squares)
    let winner = ""

    function handleHostMode() {
        setIsHost(true)
    }

    function handlePlayerMode() {
        setIsHost(false)
    }

    function handleColorTheme() {
        setColorTheme((themeNumber + 1) % 3)
    }

    function PlayerGag() {
    }

    function handleRestartGame() {
        setIsX(true)
        // @ts-ignore
        setSquares(Array(cols * rows).fill(null))
    }


    function handleNewGame() {


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

        setNewGame(true)
    }

    function handleQuitGame() {
        setIsX(true)
        // @ts-ignore
        setSquares(Array(cols * rows).fill(null))
        setNewGame(false)
    }

    return (
        <div className={`flex min-h-screen ${BGColor[themeNumber]} flex-col items-center py-2`}>
            <Head>
                <title>Tic-Tac-Toe Game</title>
                <link rel="icon" href="/tictactoe.ico"/>
            </Head>


            <h1 className={`text-4xl md:text-5xl font-extrabold mt-4 ${TextColor[themeNumber]} `}>
                TIC{" "}<span className="text-[#f3b236]">TAC </span>{" "}TOE
            </h1>

            {!newGame
                ?
                (
                    <div>
                        <ChooseMode
                            themeNumber={themeNumber}
                            handleNewGame={handleNewGame}
                            handleHostMode={handleHostMode}
                            handlePlayerMode={handlePlayerMode}
                        />
                        <ColorTheme
                            themeNumber={themeNumber}
                            handleColorTheme={handleColorTheme}
                        />
                    </div>)
                :
                (isHost ?
                    <Board
                        themeNumber={themeNumber}
                        winner={winner}
                        playerX={isX}
                        sessionId={sessionId}
                        handleRestartGame={handleRestartGame}
                        isHost={isHost}/>
                    :
                    <Board
                        themeNumber={themeNumber}
                        winner={winner}
                        playerX={isX}
                        sessionId={sessionId}
                        handleRestartGame={PlayerGag}
                        isHost={isHost}
                    />)
            }
            {
                winner &&
                <WinnerModal
                    themeNumber={themeNumber}
                    winner={winner}
                    handleQuitGame={handleQuitGame}
                    handleNewGame={handleNewGame}
                />
            }
        </div>
    )
}

export default Home
