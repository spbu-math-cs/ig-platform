import {Error} from "@/tic-tac-toe/types"

interface ErrorProps {
    errors: Error[]
}

export const ErrorSnackbar = ({errors}: ErrorProps) => {
    return <div className='absolute bottom-2 right-4'>
        {errors.map(e => {
                let bg_color
                let prefix
                if (e.is_error) {
                    bg_color = "bg-error"
                    prefix = "ERROR: "
                } else {
                    prefix = ""
                    bg_color = "bg-noterror"
                }
                return <div
                    key={e.id}
                    className={bg_color + " p-4 rounded-xl text-yellow-300 text-xl font-bold m-2"}>
                    {prefix + e.error_message}
                </div>  
            }
        )}
    </div>
}
