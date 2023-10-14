import React from 'react'

const XColor = ["bg-metalX", "bg-2048X", "bg-purpleX"];

interface XProp {
    themeNum: number,
}
export const XIcon = ({themeNum} : XProp) => {
    return (
        <div className="relative h-16 w-16 cursor-pointer ">
            <div className= {` absolute origin-top-left rotate-[44deg] ml-2 -mt-[1px] ${XColor[themeNum]}  h-4 w-20 rounded-l-full rounded-r-full `}>
            </div>
            <div className= {`absolute origin-top-right -rotate-[42deg] -ml-[23px] ${XColor[themeNum]}  h-4 w-20  rounded-l-full rounded-r-full `}>
            </div>
        </div>
    )
}


