import type {NextPage} from 'next'
import React, {FunctionComponent, JSX, useEffect, useReducer, useState} from 'react'
import {XIcon} from "@/tic-tac-toe/XIcon"
import {OIcon} from "@/tic-tac-toe/OIcon"
import {createGame as createTicTacToeGame, getTemplateList as getTicTacToeTemplateList} from "@/tic-tac-toe/api"
import {TemplateCard} from "@/components/TemplateCard"
import {GameConfig, TemplateInfo} from "@/tic-tac-toe/types"
import Button from "@/components/Button"
import {checkExhausted} from "@/utils"
import {LogIn} from "@/components/Authorization"
import {store} from "@/state/store"
import {nextTheme} from "@/state/themeSlice"
import Head from "next/head"
import {createGame as createNekahootGame, getTemplateList as getNekahootTemplateList} from "@/neKahoot/api"
import {Session} from "@/neKahoot/types"
import {Board as TicTacToeBoard} from "@/tic-tac-toe/Board"
import {Board as NekahootBoard} from "@/neKahoot/Board"
import {EditBoard} from "@/tic-tac-toe/EditBoard"

type GameId = "tic_tac_toe" | "nekahoot"

type GameComponentProps = {
    sessionId: string
    isHost: boolean
}

type GameConstructorProps = {
    onCreate: () => void
}

type Game<Options> = {
    id: GameId
    name: string | JSX.Element
    getTemplates: () => Promise<TemplateInfo[]>
    defaultOptions: Options
    optionCaptions: { [key in keyof Options]: string }
    createGame: (templateId: string, options: Options) => Promise<Session>
    gameComponent: FunctionComponent<GameComponentProps>
    constructorComponent?: FunctionComponent<GameConstructorProps>
    gameIconIco?: string
    gameIconPng?: string
}

const games: { [key in GameId]: Game<any> } = {
    tic_tac_toe: {
        id: "tic_tac_toe",
        name: "Tic-Tac-Toe",
        getTemplates: getTicTacToeTemplateList,
        defaultOptions: {
            replaceMarks: true,
            openMultipleQuestions: true,
        },
        optionCaptions: {
            replaceMarks: "Replace marks",
            openMultipleQuestions: "Open multiple questions",
        },
        createGame: createTicTacToeGame,
        gameComponent: TicTacToeBoard,
        constructorComponent: EditBoard,
    } as Game<GameConfig>,
    nekahoot: {
        id: "nekahoot",
        name: "neKahoot",
        getTemplates: getNekahootTemplateList,
        defaultOptions: {},
        optionCaptions: {},
        createGame: createNekahootGame,
        gameComponent: NekahootBoard,
    } as Game<{}>,
}

type AppState = {
    kind: "main_page"
    modal: undefined | GameId
    sessionId: string
} | {
    kind: "joining"
    sessionId: string
} | {
    kind: "fatal"
    error: Node | string
} | {
    kind: "logging"
} | {
    kind: "playing"
    game: GameId
    sessionId: string
    isHost: boolean
} | {
    kind: "constructor"
    game: GameId
}

export type AppAction = {
    kind: "go_to_main_page"
} | {
    kind: "go_to_creating"
    game: GameId
}

type OptionsOf<GameId> = GameId extends keyof typeof games ? typeof games[GameId]["defaultOptions"] : never

const Home: NextPage = () => {
    const [state, setState] = useState<AppState>({
        kind: "main_page",
        sessionId: "",
        modal: undefined,
    })
    const dispatch = store.dispatch

    type TemplateMap = { [key in GameId]?: TemplateInfo[] }
    const [templates, updateTemplates] = useReducer<
        (templates: TemplateMap, newTemplates: { [game in GameId]?: TemplateInfo[] }) => TemplateMap
    >((templates, newTemplates): TemplateMap => {
        return {...templates, ...newTemplates}
    }, {})

    type OptionsMap = { [key in GameId]: typeof games[key]["defaultOptions"] }
    const [options, updateOptions] = useReducer<
        (options: OptionsMap, newOptions: { [game in GameId]?: OptionsOf<game> }) => OptionsMap
    >((options, newOptions) => {
        return {...options, ...newOptions}
    }, Object.fromEntries(Object.keys(games).map(game => [game, games[game as GameId].defaultOptions])) as {
        [key in GameId]: typeof games[key]["defaultOptions"]
    })

    useEffect(() => {
        for (const game of Object.values(games)) {
            game.getTemplates().then(info =>
                updateTemplates({...templates, [game.id]: info}),
            )
        }
    }, [])

    const runAction = (action: AppAction) => {
        if (action.kind == "go_to_main_page") {
            setState({kind: "main_page", sessionId: "", modal: undefined})
        } else if (action.kind == "go_to_creating") {
            games[action.game].getTemplates().then(info =>
                updateTemplates({[action.game]: info}),
            )
        } else {
            checkExhausted(action)
        }
    }

    let content
    if (state.kind == "main_page") {
        content = <div>
            <div className="mt-10 md:mt-16 w-[1000px] flex flex-col items-center justify-center mx-auto">
                <div className="w-full flex flex-row space-x-10 gap-4 m-8">
                    <div className="grow space-y-4">
                        <div
                            className="text-3xl text-txt font-bold text-center rounded-xl outline-1 px-6 py-3 ring-4 ring-txt  ">
                            HOST A GAME
                        </div>

                        <div className="flex flex-col space-y-4 bg-panel px-8 py-4 rounded-xl w-full h-60 items-center">
                            <h2 className="text-3xl md:text-5xl font-extrabold m-4 text-primary">
                                TIC TAC TOE
                            </h2>

                            <div className="flex flex-row rounded-xl px-6 items-center justify-center space-x-2">
                                <a href="/tictactoe.ico" className="flex items-center">
                                    <img src="/tictactoe.ico" className=" mt-2 mr-3 h-10 sm:h-12" alt={""}/>
                                </a>

                                <div className="mt-2 w-full flex justify-end">
                                    <Button onClick={() => setState({...state, modal: "tic_tac_toe"})}>
                                        BROWSE GAMES
                                    </Button>
                                </div>
                            </div>
                        </div>

                        <div className="flex flex-col space-y-4 bg-panel px-8 py-4 rounded-xl w-full h-60 items-center">
                            <h2
                                className="text-3xl md:text-5xl font-extrabold m-4 text-primary">
                                !KAHOOT
                            </h2>

                            <div className="flex flex-row rounded-xl px-6 items-center justify-center space-x-2">
                                <a href="/kahoot.ico" className="flex items-center">
                                    <img src="/kahoot.ico" className=" mt-2 mr-3 h-10 sm:h-12 rounded-xl" alt={""}/>
                                </a>

                                <div className="mt-2 w-full flex justify-end">
                                    <Button onClick={() => setState({...state, modal: "nekahoot"})}>
                                        BROWSE GAMES
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="flex flex-col space-y-4">
                        <div
                            className="text-3xl text-txt font-bold w-full text-center rounded-xl outline-1 px-6 py-3 ring-4 ring-txt">
                            JOIN A GAME
                        </div>
                        <form
                            onSubmit={e => {
                                setState({
                                    kind: "playing",
                                    game: "tic_tac_toe",
                                    sessionId: state.sessionId,
                                    isHost: false,
                                })
                                e.preventDefault()
                            }}
                            className="flex flex-col items-center w-[700px] md:w-[450px] h-[90py] rounded-2xl bg-panel py-4 space-y-8 md:space-y-6">
                            <p className="text-2xl md:text-5xl font-extrabold m-4 text-primary">
                                ENTER GAME ID
                            </p>
                            <input
                                type="text"
                                className="mt-1 border  w-80 h-24 rounded-xl px-2 py-3 bg-panel outline-0 text-3xl md:text-4xl font-bold  text-center text-txt outline-none"
                                value={state.sessionId}
                                onChange={e => {
                                    setState({kind: "main_page", sessionId: e.target.value, modal: undefined})
                                }}
                            />
                            <Button>
                                JOIN!
                            </Button>
                        </form>
                        <div
                            className="flex flex-row items-center justify-center py-4 space-x-8 rounded-xl h-40 outline-1 px-6 ring-4 ring-txt">

                            <div className="flex justify-end">
                                <Button onClick={() => dispatch(nextTheme())}>
                                    CHANGE THEME
                                </Button>
                            </div>
                            <div className="flex justify-end">
                                <Button onClick={() => setState({kind: "logging"})}>
                                    LOG IN
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    } else if (state.kind == "joining") {
        content = <form onSubmit={e => {
            setState({
                kind: "playing",
                game: "tic_tac_toe",
                sessionId: state.sessionId,
                isHost: false,
            })
            e.preventDefault()
        }}>
            <div
                className="mt-10 md:mt-16 w-[500px] flex flex-col items-center justofy-center mx-auto">
                <div className="flex rounded-xl px-6 py-2 items-center justify-center space-x-4">
                    <XIcon/>
                    <OIcon/>
                </div>
                <div
                    className={`flex flex-col items-center py-12 w-[700px] md:w-[450px] h-64 md:h-72 rounded-2xl bg-panel mt-6 space-y-8 md:space-y-6`}>
                    <p className={`text-md text-JoinGameTxt uppercase font-extrabold  md:text-3xl space-y-12 `}>
                        ENTER GAME ID
                    </p>

                    <input
                        type="text"
                        className={`mt-1 border  w-80 h-24 rounded-xl px-2 py-3 bg-panel outline-0
                                    text-3xl md:text-4xl font-bold  text-center text-txt outline-none `}
                        value={state.sessionId}
                        onChange={e => {
                            setState({kind: "joining", sessionId: e.target.value})
                        }}
                    />
                </div>
            </div>
        </form>
    } else if (state.kind == "fatal") {
        content = <div>TODO</div>
    } else if (state.kind == "playing") {
        content = React.createElement(games[state.game].gameComponent, {
            sessionId: state.sessionId,
            isHost: state.isHost,
        })
    } else if (state.kind == "logging") {
        content = <LogIn switchPage={setState}></LogIn>
    } else if (state.kind == "constructor") {
        let constructor = games[state.game].constructorComponent
        if (constructor === undefined) {
            console.error("No constructor available (should be unreachable)")
        } else {
            content = React.createElement(constructor, {
                onCreate: () => {
                    runAction({kind: "go_to_creating", game: state.game})
                },
            })
        }
    } else {
        checkExhausted(state)
    }

    let modalContent
    if (state.kind !== "main_page" || state.modal === undefined) {
        // do nothing
    } else {
        const game = state.modal
        modalContent = <div className="flex flex-col items-center w-[1000px] rounded-2xl bg-square">
            <div className={`px-8 flex flex-row items-center w-[1000px] rounded-2xl bg-square space-x-96`}>
                <p className={`justify-items-start text-md text-JoinGameTxt uppercase font-extrabold  md:text-2xl `}>
                    CHOOSE EXISTING GAME
                </p>

                { games[game].constructorComponent !== undefined &&
                    <button onClick={() => setState({
                        kind: "constructor",
                        game: game,
                    })}
                            className={`button hover:ring-4 py-2 hover:ring-cyan-300 rounded-xl px-6 bg-[#f3b236] hover:bg-square`}>
                        or CREATE NEW QUIZ
                    </button>
                }
            </div>
            <div
                className="
                    flex flex-col justify-items-start py-10 px-100 rounded
                    mb-2 -scroll-ms-3 overflow-auto text-md text-JoinGameTxt
                    max-h-[60vh] bg-square">
                {
                    templates[game] === undefined
                        ? "Loading..."
                        : templates[game]?.map(quiz =>
                            <TemplateCard
                                template={quiz} key={quiz.id}
                                handleSelect={async (id: string) => {
                                    const sessionId = (await games[game].createGame(id, options[game])).id
                                    setState({
                                        kind: "playing",
                                        game: game,
                                        sessionId: sessionId,
                                        isHost: true,
                                    })
                                }}/>,
                        )
                }
            </div>

            <div style={{alignContent: "left", transform: "scale(1.5)"}}>
                {
                    Object.keys(games[game].optionCaptions).map(option =>
                        <div className="flex flex-row items-center space-x-2" key={option}>
                            <input type="checkbox" checked={options[game][option]}
                                   onChange={() => {
                                       updateOptions({
                                           [game]: {
                                               ...options[game],
                                               [option]: !options[game][option],
                                           },
                                       })
                                   }}/>
                            <span className="text-txt font-bold">{games[game].optionCaptions[option]}</span>
                        </div>,
                    )
                }
            </div>
        </div>
    }

    return <div
        className={`flex min-h-screen bg-back flex-col items-center  justify-items-center  max-w-screen  py-2`}>
        <div className={`flex flex-row justify-between items-center`}>
            <div className="flex flex-row items-center ">
                <img src={
                    state.kind != "playing" && state.kind != "constructor" ? "/clover.PNG"
                        : games[state.game].gameIconPng || "/clover.PNG"}
                     className="h-20" alt={""}/>
                <a href={"/"}>
                    <h1 className={`ml-3 text-6xl md:text-6xl font-extrabold mt-8 text-primary`}>
                        C<span className="text-createcol">L</span>V<span className="text-createcol">R</span>
                    </h1>
                </a>
            </div>
        </div>
        {
            state.kind == "playing" &&
            <div className={`font-extrabold text-primary `}>
                {"session: " + state.sessionId}
            </div>
        }

        <Head>
            <title>
                {state.kind != "playing" && state.kind != "constructor" ? "CLVR" : games[state.game].name}
            </title>
            <link rel="icon" href={
                state.kind != "playing" && state.kind != "constructor" ? "/clover.ico"
                    : games[state.game].gameIconIco || "/clover.ico"
            }/>
        </Head>

        {state.kind === "main_page" && state.modal !== undefined &&
            <div className="
                overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 justify-center
                items-center w-full md:inset-0 max-h-full
                flex bg-opacity-40 bg-gray-950"
                 onClick={() => setState({...state, modal: undefined})}>
                <div className="rounded-xl p-8 bg-panel" onClick={e => e.stopPropagation()}>
                    {modalContent}
                </div>
            </div>
        }
        <link rel="icon" href="/tictactoe.ico"/>
        {content}
    </div>
}

export default Home
