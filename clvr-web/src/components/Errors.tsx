import {Error} from "@/tic-tac-toe/types"

interface ErrorProps {
    errors: Error[]
}

export const ErrorSnackbar = ({errors}: ErrorProps) => {
    return <div className='absolute bottom-2 right-4'>
        {errors.map(e =>
            <div
                key={e.id}
                className="bg-error p-4 rounded-xl text-yellow-300 text-xl font-bold m-2">
                {"ERROR: " + e.error_message}
            </div>
        )}
    </div>
}
