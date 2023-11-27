import {Error} from "@/game/types"

interface ErrorProps {
    errors: Error[]
}

export const ErrorBadge = ({errors}: ErrorProps) => {
    function ErrorMessage(error: Error): JSX.Element {
        return  <div className='error-message'>{"ERROR: " + error.error_message}</div>
    }

    function ErrorsBlock(): JSX.Element {
        let errorsBlock: JSX.Element[] = [] 
        const err = errors[0]
        for (let error of errors) {
            errorsBlock.push(ErrorMessage(error))
        }
        return <div className='errors-block'>{errorsBlock}</div>
    }

    return (
        <ErrorsBlock/>
    )
}