"use client"
import {useServerState} from "../../game/websockets"
import {useRef} from "react"

/**
 * Testing page for the websocket API, and an example of how to use it.
 */
export default function ApiTest() {
    const [game, sendMessage] = useServerState({
        id: "ABCD",
    })

    const openQuestionRowNumberRef = useRef<HTMLInputElement | null>(null)
    const openQuestionColumnNumberRef = useRef<HTMLInputElement | null>(null)

    const setFieldRowNumberRef = useRef<HTMLInputElement | null>(null)
    const setFieldColumnNumberRef = useRef<HTMLInputElement | null>(null)
    const setMarkRef = useRef<HTMLInputElement | null>(null)

    return (
        <div>
      <pre>
        {JSON.stringify(game, null, 4)}
      </pre>

            <form>
                <div>Open question</div>
                <div>
                    <label>Question row:</label>
                    <input type="number" name="row" ref={openQuestionRowNumberRef}/>
                </div>
                <div>
                    <label>Question column:</label>
                    <input type="number" name="column" ref={openQuestionColumnNumberRef}/>
                </div>
                <div>
                    <button onClick={e => {
                        sendMessage({
                            type: "OPEN_QUESTION",
                            row: openQuestionRowNumberRef.current?.valueAsNumber!,
                            column: openQuestionColumnNumberRef.current?.valueAsNumber!,
                        })
                        e.preventDefault()
                    }}>Open question
                    </button>
                </div>
            </form>

            <form>
                <div>Set field</div>
                <div>
                    <label>Field row:</label>
                    <input type="number" name="row" ref={setFieldRowNumberRef}/>
                </div>
                <div>
                    <label>Field column:</label>
                    <input type="number" name="column"/>
                </div>
                <div>
                    <label>Mark:</label>
                    <input type="text" name="mark"/>
                </div>
                <button onClick={e => {
                    sendMessage({
                        type: "SET_FIELD",
                        row: setFieldRowNumberRef.current?.valueAsNumber!,
                        column: setFieldColumnNumberRef.current?.valueAsNumber!,
                        mark: setMarkRef.current?.value! as "X" | "O",
                    })
                    e.preventDefault()
                }}>Set field
                </button>
            </form>
        </div>
    )
}
