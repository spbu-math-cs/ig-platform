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
            <p className="place-items-start text-left text-txt h-[80px] w-[170px] text-clip text-xl overflow-hidden hyphens-auto"> {tsk} </p>
            <p className="place-items-start text-left text-txt h-[80px] w-[170px] text-clip text-xl overflow-hidden hyphens-auto"> {ans} </p>
        </button>
    )
}

interface EditBoardProps {
    setState(): void
}


export function EditBoard({setState}: EditBoardProps) {
    const [defState, setDefState] = useState<BoardState>({
        squares: Array(9).fill({
            question: '',
            answer: '',
            hints: ['', ''],
            topic: '',
        }),
        gameName: '',
        gameDetails: '',
    })
    const [isCreated, setIsCreated] = useState<boolean>(false)
    const [editingNum, setEditingNum] = useState<number | undefined>(undefined)

    const renderSquare = (i: number) => {
        // TODO: adaptivity
        return <Square
            onClick={() => setEditingNum(i)}
            topic={defState.squares[i].topic}
            tsk={'Q ' + defState.squares[i].question}
            ans={'A ' + defState.squares[i].answer}
        />
    }
    return (
        !isCreated ?
            <div className={`flex flex-col items-center py-0`}>
                <div className="flex-row mt-6 w-screen rounded-lg mx-auto flex justify-center items-start space-x-16">
                    <div
                        className="mt-20 flex h-[450px] ml-16 md:mt-16 md:h-[500px] flex-col items-center justify-center space-y-4 rounded-2xl bg-back">
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
                    <div
                        className="flex-col h-[600px] overflow-y-auto space-y-5 w-[650px] rounded-lg flex items-center">
                        <form
                            onChange={e => e.preventDefault()}
                            onSubmit={e => e.preventDefault()}>
                            <div className=" w-[500px] h-[150px] flex flex-col items-center justify-center mx-auto">
                                <p className="md:text-2xl font-bold text-center text-createcol outline-none">TOPIC</p>
                                <input
                                    type="text"
                                    value={editingNum === undefined ? "" : defState.squares[editingNum].topic}
                                    className={`breakWord break-words border w-[500px] h-[150px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                                    onChange={e => {
                                        setDefState({
                                            ...defState,
                                            squares: defState.squares.map((square, index) =>
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
                                    value={editingNum === undefined ? "" : defState.squares[editingNum].question}
                                    onChange={e => {
                                        setDefState({
                                            ...defState,
                                            squares: defState.squares.map((square, index) =>
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
                                    value={editingNum === undefined ? "" : defState.squares[editingNum].answer}
                                    onChange={e => {
                                        setDefState({
                                            ...defState,
                                            squares: defState.squares.map((square, index) =>
                                                index === editingNum
                                                    ? {...square, answer: e.currentTarget.value}
                                                    : square),
                                        })
                                    }}
                                />
                            </div>
                            {editingNum != undefined && defState.squares[editingNum]?.hints.map((hint, index) =>
                                <div
                                    className=" w-[600px] h-[100px] flex flex-col items-center justify-center mx-auto"
                                    key={index}>
                                    <p className="md:text-2xl font-bold text-center text-createcol outline-none">
                                        HINT {index + 1}
                                        <span>
                                    <button
                                        className="text-xs align-super"
                                        onClick={() => {
                                            setDefState({
                                                ...defState,
                                                squares: defState.squares.map((square, squareIndex) =>
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
                                            setDefState({
                                                ...defState,
                                                squares: defState.squares.map((square, squareIndex) =>
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
                            {editingNum !== undefined && defState.squares[editingNum].hints.length < 10
                                && <div className="flex justify-center py-4">
                                    <button
                                        className="px-8 button hover:ring-4 py-3  mx-auto text-center rounded-2xl bg-[#f3b236] hover:bg-panel"
                                        onClick={() => setDefState({
                                            ...defState,
                                            squares: defState.squares.map((square, index) =>
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
                <button
                    className={`items-center mt-8 button hover:ring-4 py-1 w-[200px] h-16 hover:ring-cyan-300 rounded-2xl px-6 bg-[#f3b236] hover:bg-square`}
                    onClick={() => setIsCreated(true)}>
                    CREATE GAME!
                </button>
            </div> :
            <GameInfo setState1={setState}
                      defState={defState}
            ></GameInfo>
    )
}

interface GameInfoProps {
    defState: BoardState

    setState1(): void
}

export function GameInfo({defState, setState1}: GameInfoProps) {
    const [state, setState] = useState<BoardState>(defState)
    return (
        <div className={`relative w-[1000px]`}>
            <div className={"blur-2xl pointer-events-none"}>
                <EditBoard setState={setState1}/>
            </div>
            <div
                className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 py-8 flex-col space-y-8 w-[600px] h-auto rounded-2xl flex items-center justify-start bg-back">
                <form
                    onChange={e => e.preventDefault()}
                    onSubmit={e => e.preventDefault()}
                    className={`flex-col space-y-8 flex`}>
                    <div className=" w-[300px] h-auto flex flex-col items-center justify-center space-y-8 mx-auto">
                        <div>
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
                    </div>
                    <div className=" w-[300px] h-auto flex flex-col items-center justofy-center mx-auto space-y-4">
                        <div>
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
                    }).then(setState1())
                }}
                        className={`px-8 button hover:ring-4 mt-0 py-3  mx-auto text-center rounded-2xl bg-[#f3b236] hover:bg-panel`}>
                    CREATE GAME
                </button>
            </div>
        </div>
    )
}
