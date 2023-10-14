import type {NextPage} from 'next';
import Head from 'next/head';
import React, {useState} from 'react';
import {Board} from '../components/Board';
import {ChooseMode, ColorTheme} from '../components/ChooseMode';
import {WinnerModal} from '../components/WinnerModal';

var TextColor = ["text-metalText", "text-2048Text", "text-purpleText"];
var BGColor = ["bg-metalBG", "bg-2048BG", "bg-purpleBG"]

const Home: NextPage = () => {
    const rows = 3;
    const cols = 3;
    const [isX, setIsX] = useState<boolean>(true);
    const [newGame, setNewGame] = useState<boolean>(false);
    // @ts-ignore
    const [squares, setSquares] = useState<Array<any>>(Array(cols * rows).fill(null));
    const [themeNumber, setColorTheme] = useState<number>(1);

    const [isHost, setIsHost] = useState<boolean>(true);


    let winner = calculateWinner(squares);

    function handlePlayerX() {
        setIsX(true);
    }

    function handleRoleHost() {
        setIsHost(true);
    }

    function handleRolePlayer() {
        setIsHost(false);
    }
    function handlePlayerO() {
        setIsX(false);
    }

    function handleColorTheme() {
        setColorTheme((themeNumber + 1) % 3);
    }

    function handlePlayer(i: number) {
        if (calculateWinner(squares) || squares[i]) {
            return;
        }
        squares[i] = isX ? "X" : "O";
        setSquares(squares);
        setIsX(!isX);
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
        setNewGame(true);
    }

    function handleQuitGame() {
        setIsX(true);
        // @ts-ignore
        setSquares(Array(cols * rows).fill(null));
        setNewGame(false);
    }

    function calculateWinner(squares: Array<any>) {
        for (let i = 0; i < rows; ++i) { //vertical
            if (squares[i] && squares[i] == squares[i + rows] && squares[i + rows] == squares[i + 2 * rows]) {
                return squares[i];
            }
        }

        for (let i = 0; i < cols; ++i) { //horizontal
            if (squares[i * rows] && squares[i * rows] == squares[i * rows + 1] && squares[i * rows + 1] == squares[i * rows + 2]) {
                return squares[i * rows];
            }
        }


        if (squares[0] && squares[0] == squares[1 + rows] && squares[0] == squares[2 + 2 * rows]) {
            return squares[0]
        }

        if (squares[2] && squares[2] == squares[1 + rows] && squares[2] == squares[2 * rows]) {
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
                TIC
                {" "}
                <span className="text-[#f3b236]">TAC </span>
                {" "}
                TOE
            </h1>

            {!newGame
                ?

                <div>
                    <ChooseMode
                        themeNumber={themeNumber}
                        handleNewGame={handleNewGame}
                        handleHostMode={handleRoleHost}
                        handlePlayerMode={handleRolePlayer}
                    />
                    <ColorTheme
                        themeNumber={themeNumber}
                        handleColorTheme={handleColorTheme}
                    />
                </div>
                :
                <Board
                    themeNumber={themeNumber}
                    winner={winner}
                    playerX={isX}
                    squares={squares}
                    handlePlayer={handlePlayer}
                    handleRestartGame={handleRestartGame}
                />
            }
            {winner &&
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
