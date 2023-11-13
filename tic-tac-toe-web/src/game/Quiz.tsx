import {useState} from "react";
import {QuizProps} from "@/game/dataGame"


export function Quiz({quiz, handleSelect}: QuizProps) {
    const [details, setDetails] = useState(false)

    return (
        <div className="border-2 border-back w-[950px] rounded-2xl py-2 px-4 flex flex-col items-start mb-2">
            <div className="rounded-2xl flex flex-row">
                <div className="rounded-2xl py-2 px-4 flex flex-row mb-1 space-x-30 w-[810px]">
                    <p className="font-bold text-2xl">{quiz.name}</p>

                    {details ? <div>
                            <p className="font-bold text-xl px-10">{quiz.comment}</p>
                        </div>
                        : ''}
                </div>
                <button className="bg-panel font-extrabold text-xl text-hostTxt space-30-px py-3 flex"
                        onClick={() => handleSelect(quiz.id)}
                >
                    SELECT
                </button>
            </div>

            <button
                className="px-4 bg-panel font-bold text-s text-back"
                onClick={() => setDetails(prev => !prev)}
            >
                {details ? 'HIDE DETAILS' : 'SHOW DETAILS'}
            </button>


        </div>
    )
}