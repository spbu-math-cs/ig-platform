import React from 'react'

export const XIcon = () => {
    return (
        <div className="relative h-16 w-16 cursor-pointer ">
            <div
                className={` absolute origin-top-left rotate-[44deg] ml-2 -mt-[1px] bg-X  h-4 w-20 rounded-l-full rounded-r-full `}>
            </div>
            <div
                className={`absolute origin-top-right -rotate-[42deg] -ml-[23px] bg-X h-4 w-20  rounded-l-full rounded-r-full `}>
            </div>
        </div>
    )
}
