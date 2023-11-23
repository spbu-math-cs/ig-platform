import React, {useEffect, useState} from 'react'
import Head from "next/head";


interface SquareProp {
    tsk: any
    ans: any

    onClick(): void
}

interface EditorProp {
    handleGameIsConstructed(): void
}

export const EditBoard = ({handleGameIsConstructed}: EditorProp) => {

    const [txt, setTxt] = useState('AA')
    const [EditedTextType, setEditedTxt] = useState(0)
    const [editingNum, setEditingNum] = useState(10)
    const [squaresTask, setSquaresTask] = useState<Array<any>>(Array(9).fill('  '))
    const [squaresHint1, setSquaresHist1] = useState<Array<any>>(Array(9).fill('  '))
    const [squaresHint2, setSquaresHist2] = useState<Array<any>>(Array(9).fill('  '))
    const [squaresHint3, setSquaresHist3] = useState<Array<any>>(Array(9).fill('  '))
    const [squaresCover, setSquaresCover] = useState<Array<any>>(Array(9).fill('  '))

    const [squaresAns, setSquaresAns] = useState<Array<any>>(Array(9).fill('  '))
    const [isConstructing, setGameIsConstructing] = useState<boolean>(true)
    const [gameName, setGameName] = useState<string>('')
    const [gameDetails, setGameDetails] = useState<string>('')

    //TODO: когда-нибудь, когда появятся аккаунты, добавить плашку private


    function Square({tsk, ans, onClick}: SquareProp) {
        return (
            <button
                className={`flex-col py-3 px-3 space-y-2 h-[190px] w-[190px] items-start place-items-start text-txt font-bold bg-square rounded-2xl hover:bg-[#18272e]`}
                onClick={onClick}>
                <p className="place-items-start text-left text-txt  h-[50px] w-[170px]  text-clip overflow-hidden"> {tsk} </p>
                <p className="place-items-start text-left text-txt   h-[50px] w-[170px]  text-clip overflow-hidden"> {ans} </p>
            </button>
        )
    }

    function pressSquare(i: number) {
        setEditingNum(i)
        setEditedTxt((EditedTextType + 1) % 3);
        //TODO: сейчас не очищается вывод при переключении режимов/ячеек, пока не знаю, как это исправить
    }


    const renderSquare = (i: number) => {
        return <Square
            onClick={() => {
                pressSquare(i)
            }}
            tsk={'Q ' + squaresTask[i] + `\n`}
            ans={'A ' + squaresAns[i] + `\n`}
        />
    }
    return (
        isConstructing ?
            (<div>
                <button onClick={() => {
                    setGameIsConstructing(false)
                }}
                        className={`px-6 button hover:ring-4 py-2  mx-auto  text-center rounded-xl bg-[#f3b236] hover:bg-panel`}>
                    CREATE
                </button>
                <div className="flex-row w-max rounded-lg mx-auto flex justify-center items-start space-x-36">
                    <div className="board mt-2 ">
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
                    <div className="flex-col h-[600px] overflow-y-scroll space-y-5 w-[600px] rounded-lg flex items-center">
                        <form onChange={e => {
                            e.preventDefault()
                        }}>
                            <div className=" w-[500px] h-[150px] flex flex-col items-center justify-center mx-auto">
                                <p className="md:text-2xl font-bold text-center text-createcol outline-none"> COVER</p>
                                <input
                                    type="text"
                                    className={`breakWord break-words border w-[500px] h-[150px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                                    onChange={e => {
                                        squaresCover[editingNum] = (e.currentTarget.value)
                                    }}
                                />
                            </div>
                        </form>
                        <form onChange={e => {
                            e.preventDefault()
                        }}>
                            <div className=" w-[500px] h-[150px] flex flex-col items-center justify-center mx-auto">
                                <p className="md:text-2xl font-bold text-center text-createcol outline-none"> TASK </p>
                                <input
                                    type="text"
                                    className={`breakWord break-words border  w-[500px] h-[150px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                                    onChange={e => {
                                        squaresTask[editingNum] = (e.currentTarget.value)
                                    }}
                                />
                            </div>
                        </form>
                        <form onChange={e => {
                            e.preventDefault()
                        }}>
                            <div className=" w-[600px] h-[150px] flex flex-col items-center justify-center mx-auto">
                                <p className="md:text-2xl font-bold text-center text-createcol outline-none"> ANSWER</p>
                                <input
                                    type="text"
                                    className={`breakWord break-words border  w-[500px] h-[150px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                                    onChange={e => {
                                        squaresAns[editingNum] = (e.currentTarget.value)
                                    }}
                                />
                            </div>
                        </form>
                        <form onChange={e => {
                            e.preventDefault()
                        }}>
                            <div className=" w-[600px] h-[100px] flex flex-col items-center justify-center mx-auto">
                                <p className="md:text-2xl font-bold text-center text-createcol outline-none"> HINT1 </p>
                                <input
                                    type="text"
                                    className={`breakWord break-words border  w-[500px] h-[100px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                                    onChange={e => {
                                        squaresHint1[editingNum] = (e.currentTarget.value)
                                    }}
                                />
                            </div>
                        </form>
                        <form onChange={e => {
                            e.preventDefault()
                        }}>
                            <div className=" w-[600px] h-[100px] flex flex-col items-center justify-center mx-auto">
                                <p className="md:text-2xl font-bold text-center text-createcol outline-none"> HINT2 </p>
                                <input
                                    type="text"
                                    className={`breakWord break-words border  w-[500px] h-[100px] rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center text-txt outline-none `}
                                    onChange={e => {
                                        squaresHint2[editingNum] = (e.currentTarget.value)
                                    }}
                                />
                            </div>
                        </form>
                        <form onChange={e => {
                            e.preventDefault()
                        }}>
                            <div className=" w-[600px] h-[100px] flex flex-col items-center justofy-center mx-auto">
                                <p className="md:text-2xl font-bold text-center text-createcol outline-none"> HINT3 </p>
                                <input
                                    type="text"
                                    className={`breakWord border w-[500px] h-[100px]  rounded-xl px-3 bg-panel outline-0
                                    text-3xl font-bold text-center break-words text-txt outline-none `}
                                    onChange={e => {
                                        squaresHint3[editingNum] = (e.currentTarget.value)
                                    }}
                                />
                            </div>
                        </form>
                    </div>
                </div>
            </div>)
            :


            (<div>
                <div className="flex-col space-y-10 w-[1000px] rounded-lg flex items-center justify-center">

                    <form onChange={e => {
                        e.preventDefault()
                    }}>
                        <div
                            className=" w-[300px] h-[150px] flex flex-col items-center justofy-center space-y-4 mx-auto">
                            <p className="text-3xl font-bold text-center text-createcol outline-none"> ENTER GAME
                                NAME </p>
                            <input
                                type="text"
                                className={`breakWord border w-[500px] h-[150px] rounded-xl px-2 bg-panel outline-0
                                    text-3xl font-bold text-center break-words text-txt outline-none `}
                                onChange={e => {
                                    setGameName(e.currentTarget.value)

                                }}
                            />
                        </div>
                    </form>
                    <form onChange={e => {
                        e.preventDefault()
                    }}>
                        <div
                            className=" w-[300px] h-[200px] flex flex-col items-center justofy-center mx-auto space-y-4">
                            <p className="text-3xl font-bold w-[500px] text-center text-createcol outline-none"> ENTER
                                GAME
                                DESCRIPTION </p>
                            <input
                                type="text"
                                className={`breakWord border w-[500px] h-[200px] rounded-xl px-2 bg-panel outline-0
                                    text-2xl font-bold text-center break-words text-txt outline-none `}
                                onChange={e => {
                                    setGameDetails(e.currentTarget.value)
                                }}
                            />
                        </div>
                    </form>

                    <button onClick={() => {
                        handleGameIsConstructed()
                    }}
                            className={`px-8 button hover:ring-4 py-3  mx-auto text-center rounded-2xl bg-[#f3b236] hover:bg-panel`}>
                        CREATE GAME
                    </button>
                </div>
            </div>)

    )
}
