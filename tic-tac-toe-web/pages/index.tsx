import type {NextPage} from 'next';
import Head from 'next/head';
import React, {useState} from 'react';
import {Board} from '../components/Board';
import {ChooseMode, ColorTheme} from '../components/ChooseMode';
import {WinnerModal} from '../components/WinnerModal';


const TextColor = ["text-metalText", "text-2048Text", "text-purpleText"];
const BGColor = ["bg-metalBG", "bg-2048BG", "bg-purpleBG"];

const Home: NextPage = () => {

    const rows = 3;
    const cols = 3;
    const [isX, setIsX] = useState<boolean>(true);
    const [newGame, setNewGame] = useState<boolean>(false);
    // @ts-ignore
    const [squares, setSquares] = useState<Array<any>>(Array(cols * rows).fill(null));
    const [themeNumber, setColorTheme] = useState<number>(1);

    const [isHost, setIsHost] = useState<boolean>(true);
    const [isMove, setIsMove] = useState<boolean>(false);



    let winner = calculateWinner(squares);

    function handleHostMode() {
        setIsHost(true);
    }

    function handlePlayerMode() {
        setIsHost(false);
    }

    function handleColorTheme() {
        setColorTheme((themeNumber + 1) % 3);
    }

    function PlayerGag() {
    }

    function handlePlayer(i: number) {
        if (i < rows * cols && isMove) {
            if (calculateWinner(squares)) {
                return;
            }
            squares[i] = isX ? "X" : "O";
            setSquares(squares);
            setIsX(!isX);
            setIsMove(false);
            return;
        }

        if (i == rows * cols + 1) {
            setIsX(true);
            setIsMove(true);
            return;
        }
        if (i == rows * cols + 2) {
            setIsX(false);
            setIsMove(true);
            return;
        }
    }

    function handleRestartGame() {
        setIsX(true);
        // @ts-ignore
        setSquares(Array(cols * rows).fill(null));
    }


    function handleNewGame() {


        setIsX(true);
        // @ts-ignore
        setSquares(Array(cols * rows).fill(null));
        squares[0] = "ML";
        squares[1] = "DB";
        squares[2] = "Algos";

        squares[3] = "CS";
        squares[4] = "OOP";
        squares[5] = "DEV";

        squares[6] = "FP";
        squares[7] = "DB";
        squares[8] = "ACOS";

        setSquares(squares);

        setNewGame(true);

        let k = new Audio("/LobbyMusic.mp3")

        k.addEventListener("canplaythrough", (event) => {
            k.play()
        }, true);
    }

    function handleQuitGame() {
        setIsX(true);
        // @ts-ignore
        setSquares(Array(cols * rows).fill(null));
        setNewGame(false);
    }

    function calculateWinner(squares: Array<any>) {
        for (let i = 0; i < rows; ++i) { //vertical
            if ((squares[i] == "X" || squares[i] == "O") && squares[i] == squares[i + rows] && squares[i + rows] == squares[i + 2 * rows]) {
                return squares[i];
            }
        }

        for (let i = 0; i < cols; ++i) { //horizontal
            if ((squares[i * rows] == "X" || squares[i * rows] == "O") && squares[i * rows] == squares[i * rows + 1] && squares[i * rows + 1] == squares[i * rows + 2]) {
                return squares[i * rows];
            }
        }

        if ((squares[0] == "X" || squares[0] == "O") && squares[0] == squares[1 + rows] && squares[0] == squares[2 + 2 * rows]) {
            return squares[0]
        }

        if ((squares[2] == "X" || squares[2] == "O") && squares[2] == squares[1 + rows] && squares[2] == squares[2 * rows]) {
            return squares[2]
        }
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
                        squares={squares}
                        handlePlayer={handlePlayer}
                        handleRestartGame={handleRestartGame}
                        isHost={isHost}/>
                    :
                    <Board
                        themeNumber={themeNumber}
                        winner={winner}
                        playerX={isX}
                        squares={squares}
                        handlePlayer={PlayerGag}
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
