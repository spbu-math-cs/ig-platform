import {useState} from "react";
import {GameConfig, QuizInfo} from "@/game/types"

interface QuizProps {
    quiz: QuizInfo
    handleSelect(id: string) : Promise<void>
}

export function QuizCard({quiz, handleSelect}: QuizProps) {
    return (
        <div className="border-2 border-back w-[950px] rounded-2xl py-2 px-4 flex flex-col items-start mb-2">
            <div className="rounded-2xl flex flex-row">
                <div className="rounded-2xl py-2 px-4 flex flex-row mb-1 space-x-30 w-[810px]">
                    <p className="font-bold text-2xl">{quiz.name}</p>
                    <div>
                        <p className="font-bold text-xl px-10">{quiz.comment}</p>
                    </div>
                </div>
                <button className="font-extrabold text-xl text-JoinGameTxt space-30-px flex button hover:ring-4 py-2 hover:ring-cyan-300 rounded-xl px-6 bg-[#f3b236] hover:bg-panel"
                        onClick={() => handleSelect(quiz.id)}
                >
                    PLAY!
                </button>
            </div>
        </div>
    )
}