import React, {useState} from 'react'
import {createTemplate} from "@/tic-tac-toe/api"


interface SquareProp {
    tsk: string
    ans: string
    topic: string

    onClick(): void
}

type SquareState = {
    question: string
    answer: string
    hints: string[]
    topic: string
    ok: boolean
}

type BoardState = {
    squares: SquareState[]
    gameName: string
    gameDetails: string
}

function Square({tsk, ans, topic, onClick}: SquareProp) {
    return (
        <button
            className="flex-col py-3 px-3 space-y-2 h-[190px] w-[190px] items-start place-items-start text-txt font-bold bg-square rounded-2xl hover:bg-[#18272e]"
            onClick={onClick}>
            <div className="w-full text-center h-5">{topic}</div>
            <p className="place-items-start text-left text-txt h-[80px] w-[170px] text-clip overflow-hidden hyphens-auto"> {tsk} </p>
            <p className="place-items-start text-left text-txt h-[80px] w-[170px] text-clip overflow-hidden hyphens-auto"> {ans} </p>
        </button>
    )
}

export function EditBoard({onCreate}: {onCreate: () => void}) {
    const [state, setState] = useState<BoardState>({
        squares: Array(9).fill({
            question: '',
            answer: '',
            hints: ['', ''],
            topic: '',
        }),
        gameName: '',
        gameDetails: '',
    })

    const [editingNum, setEditingNum] = useState<number | undefined>(undefined)

    const renderSquare = (i: number) => {
        // TODO: adaptivity
        return <Square
            onClick={() => setEditingNum(i)}
            topic={state.squares[i].topic}
            tsk={'Q ' + state.squares[i].question}
            ans={'A ' + state.squares[i].answer}
        />
    }
    return <div>
        <div className="flex-row w-max rounded-lg mx-auto flex justify-center items-start space-x-36">
            <div
                className="mt-24 flex h-[450px] w-[350px] md:mt-16 md:h-[500px] flex-col items-center justify-center space-y-4 rounded-xl bg-back">
                <div className="board-row">
                    {renderSquare(0)}
                    {renderSquare(1)}
                    {renderSquare(2)}
                </div>

                <div className="board-row">
                    {renderSquare(3)}
                    {renderSquare(4)}
                    {renderSquare(5)}
                </div>

                <div className="board-row">
                    {renderSquare(6)}
                    {renderSquare(7)}
                    {renderSquare(8)}
                </div>
            </div>
            <div className="flex-col h-[600px] overflow-y-auto space-y-5 w-[650px] rounded-lg flex items-center">
                <form
                    onChange={e => e.preventDefault()}
                    onSubmit={e => e.preventDefault()}>
                    <div className=" w-[500px] h-[150px] flex flex-col items-center justify-center mx-auto">
                        <p className="md:text-2xl font-bold text-center text-createcol outline-none">TOPIC</p>
                        <input
                            type="text"
                            value={editingNum === undefined ? "" : state.squares[editingNum].topic}
                            className={`breakWord break-words border w-[500px] h-[150px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                            onChange={e => {
                                setState({
                                    ...state,
                                    squares: state.squares.map((square, index) =>
                                        index === editingNum
                                            ? {...square, topic: e.currentTarget.value}
                                            : square),
                                })
                            }}
                        />
                    </div>
                    <div className=" w-[500px] h-[150px] flex flex-col items-center justify-center mx-auto">
                        <p className="md:text-2xl font-bold text-center text-createcol outline-none">QUESTION</p>
                        <input
                            type="text"
                            className={`breakWord break-words border  w-[500px] h-[150px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                            value={editingNum === undefined ? "" : state.squares[editingNum].question}
                            onChange={e => {
                                setState({
                                    ...state,
                                    squares: state.squares.map((square, index) =>
                                        index === editingNum
                                            ? {...square, question: e.currentTarget.value}
                                            : square),
                                })
                            }}
                        />
                    </div>
                    <div className=" w-[600px] h-[150px] flex flex-col items-center justify-center mx-auto">
                        <p className="md:text-2xl font-bold text-center text-createcol outline-none">ANSWER</p>
                        <input
                            type="text"
                            className={`breakWord break-words border  w-[500px] h-[150px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                            value={editingNum === undefined ? "" : state.squares[editingNum].answer}
                            onChange={e => {
                                setState({
                                    ...state,
                                    squares: state.squares.map((square, index) =>
                                        index === editingNum
                                            ? {...square, answer: e.currentTarget.value}
                                            : square),
                                })
                            }}
                        />
                    </div>
                    {editingNum != undefined && state.squares[editingNum]?.hints.map((hint, index) =>
                        <div
                            className=" w-[600px] h-[100px] flex flex-col items-center justify-center mx-auto"
                            key={index}>
                            <p className="md:text-2xl font-bold text-center text-createcol outline-none">
                                HINT {index + 1}
                                <span>
                                    <button
                                        className="text-xs align-super"
                                        onClick={() => {
                                            setState({
                                                ...state,
                                                squares: state.squares.map((square, squareIndex) =>
                                                    squareIndex === editingNum
                                                        ? {
                                                            ...square,
                                                            hints: square.hints.filter((_, hintIndex) =>
                                                                hintIndex !== index),
                                                        }
                                                        : square),
                                            })
                                        }}
                                    >
                                        delete
                                    </button>
                                </span>
                            </p>
                            <input
                                type="text"
                                value={hint}
                                className={`breakWord break-words border  w-[500px] h-[100px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                                onChange={e => {
                                    setState({
                                        ...state,
                                        squares: state.squares.map((square, squareIndex) =>
                                            squareIndex === editingNum
                                                ? {
                                                    ...square,
                                                    hints: square.hints.map((hint, hintIndex) =>
                                                        hintIndex === index
                                                            ? e.currentTarget.value
                                                            : hint),
                                                }
                                                : square),
                                    })
                                }}
                            />
                        </div>,
                    )}
                    {editingNum !== undefined && state.squares[editingNum].hints.length < 10
                        && <div className="flex justify-center py-4">
                            <button
                                className="px-8 button hover:ring-4 py-3  mx-auto text-center rounded-2xl bg-[#f3b236] hover:bg-panel"
                                onClick={() => setState({
                                    ...state,
                                    squares: state.squares.map((square, index) =>
                                        index === editingNum
                                            ? {...square, hints: [...square.hints, ""]}
                                            : square),
                                })}>
                                ADD HINT
                            </button>
                        </div>}
                </form>
            </div>
        </div>
        <div className="pt-8 flex-col space-y-10 w-[1000px] rounded-lg flex items-center justify-center">
            <form
                onChange={e => e.preventDefault()}
                onSubmit={e => e.preventDefault()}>
                <div
                    className=" w-[300px] h-[150px] flex flex-col items-center justofy-center space-y-4 mx-auto">
                    <p className="text-3xl font-bold text-center text-createcol outline-none"> ENTER GAME
                        NAME </p>
                    <input
                        type="text"
                        className={`breakWord border w-[500px] h-[150px] rounded-xl px-2 bg-panel outline-0
                                    text-3xl font-bold text-center break-words text-txt outline-none `}
                        onChange={e => {
                            setState({
                                ...state,
                                gameName: e.currentTarget.value,
                            })
                        }}
                    />
                </div>
                <div
                    className=" w-[300px] h-[200px] flex flex-col items-center justofy-center mx-auto space-y-4">
                    <p className="text-3xl font-bold w-[500px] text-center text-createcol outline-none"> ENTER
                        GAME
                        DESCRIPTION </p>
                    <textarea
                        className={`breakWord border w-[500px] h-[200px] rounded-xl p-2 bg-panel outline-0
                                    text-2xl font-bold text-center break-words text-txt outline-none `}
                        onChange={e => {
                            setState({
                                ...state,
                                gameDetails: e.currentTarget.value,
                            })
                        }}
                    />
                </div>
            </form>

            <button onClick={() => {
                createTemplate({
                    name: state.gameName,
                    comment: state.gameDetails,
                    board: state.squares.map((square, index) => ({
                        row: Math.floor(index / 3),
                        column: index % 3,
                        question: square.question,
                        answer: square.answer,
                        hints: square.hints,
                        topic: square.topic,
                    })),
                }).then(onCreate)
            }}
                    className={`px-8 button hover:ring-4 py-3  mx-auto text-center rounded-2xl bg-[#f3b236] hover:bg-panel`}>
                CREATE GAME
            </button>
        </div>
    </div>
}
