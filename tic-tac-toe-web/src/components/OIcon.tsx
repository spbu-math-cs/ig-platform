import React from 'react'

const OColor = ["ring-indigo", "ring-2048O", "ring-purpleO"]

interface OProp {
    themeNum: number,
}

export const OIcon = ({themeNum}: OProp) => {
    return (
        <div className="flex items-center justify-center h-16 w-16 cursor-pointer ">
            <div className={` h-8 w-8 ring-[18px] ${OColor[themeNum]} rounded-full`}>
            </div>
        </div>
    )
}
