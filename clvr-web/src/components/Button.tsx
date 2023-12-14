import React from "react"

interface ButtonProps {
    onClick?: () => void
    children: React.ReactNode
}

export default function Button(props: ButtonProps) {
    return <button
        onClick={props.onClick}
        className="button hover:ring-4 hover:ring-cyan-300 rounded-xl px-6 py-3 bg-createcol hover:bg-primary">
        {props.children}
    </button>
}
