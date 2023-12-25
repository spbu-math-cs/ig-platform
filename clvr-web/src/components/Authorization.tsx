import {useState} from "react";
import {TemplateInfo} from "@/tic-tac-toe/types";
import {login, register, logout, User} from "@/components/api"

const API_ENDPOINT = new URL("http://127.0.0.1:8080/")

interface AuthProps {
    switchPage(val : any): void,
    setUser(val: User): void
}

interface UserIconProps {
    user: User,
    setUser(val: User): void
}

type LoginInfo = {
    user_name: string, 
    password: string, 
    password_x2: string
}

enum AuthorizationState {
    SignIn,
    SignUp,
    Done
} 

function validateLoginInfo(loginInfo: LoginInfo): boolean {
    if (loginInfo.user_name == "") {
        alert("User name cannot be empty")
        return false
    }

    if (loginInfo.password == "") {
        alert("Password cannot be empty")
        return false
    }

    return true
}

export function LogIn({switchPage, setUser}: AuthProps) {
    const [state, setState] = useState(AuthorizationState.SignIn);

    const [loginInfo, setLoginInfo] = useState<LoginInfo>({
        user_name: '',
        password: '',
        password_x2: ''
    })

    async function loginAction() {
        if (!validateLoginInfo(loginInfo)) {
            return
        }

        const user = login(loginInfo.user_name, loginInfo.password)
        user.then(user => {
            console.log("[login] " + user)
            if (user != "unauthorized") {
                setUser(user)
                switchPage({kind: "main_page", sessionId: "", modal: undefined})
            }
        })
    }

    function registerAction() {
        if (!validateLoginInfo(loginInfo)) {
            return
        }

        if (loginInfo.password != loginInfo.password_x2) {
            alert("Passwords have to be the same")
            return
        }

        const user = register(loginInfo.user_name, loginInfo.password)
        user.then(user => {
            console.log("[register] " + user)
            if (user != "unauthorized") {
                setUser(user)
                switchPage({kind: "main_page", sessionId: "", modal: undefined})
            }
        })
    }

    function switchToRegister() {
        setLoginInfo({
            user_name: '',
            password: '',
            password_x2: ''
        })

        setState(AuthorizationState.SignUp)
    }

    function switchToLogin() {
        setLoginInfo({
            user_name: '',
            password: '',
            password_x2: ''
        })

        setState(AuthorizationState.SignIn)
    }

    return (
        <div>
            {state == AuthorizationState.SignIn ? (
                    <div className="flex flex-col items-center justify-center px-6 mx-auto mt-20 ">
                        <div
                            className="w-full rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0 dark:bg-gray-800 dark:border-gray-700">
                            <div className="bg-panel p-6 space-y-4 md:space-y-6 sm:p-8">
                                <h1 className="text-primary text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl dark:text-white">
                                    Sign in
                                </h1>
                                <form className="space-y-4 md:space-y-6" action="#">
                                    <div>
                                        <label htmlFor="user name"
                                               className="text-primary block mb-2 text-sm font-medium text-gray-900 dark:text-white">Your user name</label>
                                        <textarea
                                            className="bg-square text-primary bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                                            placeholder="cool name"
                                            onChange={e => {setLoginInfo({...loginInfo, user_name: e.currentTarget.value})}}
                                            ></textarea>
                                    </div>
                                    <div>
                                        <label htmlFor="password"
                                               className="text-primary block mb-2 text-sm font-medium text-gray-900 dark:text-white">Password</label>
                                        <textarea
                                            className="bg-square text-primary bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                                            placeholder="cool password"
                                            onChange={e => {setLoginInfo({...loginInfo, password: e.currentTarget.value})}}
                                        ></textarea>
                                    </div>
                                    <button onClick={loginAction}
                                            className="text-primary w-full text-white bg-primary-600 hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800">
                                        Sign in
                                    </button>
                                    <button className="text-sm font-light text-gray-500 dark:text-gray-400"
                                            onClick={switchToRegister}>
                                        Don’t have an account yet? <a href="#"
                                                                      className="font-medium text-primary-600 hover:underline dark:text-primary-500">Sign
                                        up</a>
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                )
                :
                (
                    <div className="flex flex-col items-center justify-center px-6 py-8 mx-auto mt-10">
                        <div
                            className="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0 dark:bg-gray-800 dark:border-gray-700">
                            <div className="bg-panel p-6 space-y-4 md:space-y-6 sm:p-8">
                                <h1 className="text-primary text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl dark:text-white">
                                    Create and account
                                </h1>
                                <form className="space-y-4 md:space-y-6" action="#">
                                    <div>
                                        <label htmlFor="user name"
                                               className="text-primary block mb-2 text-sm font-medium text-gray-900 dark:text-white">Your
                                            user name</label>
                                        <textarea
                                            className="bg-square text-primary bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                                            placeholder="cool name"
                                            onChange={e => {setLoginInfo({...loginInfo, user_name: e.currentTarget.value})}}>
                                </textarea>
                                    </div>
                                    <div>
                                        <label htmlFor="password"
                                               className="text-primary block mb-2 text-sm font-medium text-gray-900 dark:text-white">Password</label>
                                        <textarea
                                            className="bg-square text-primary bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                                            placeholder="cool password"
                                            onChange={e => {setLoginInfo({...loginInfo, password: e.currentTarget.value})}}
                                        ></textarea>
                                    </div>
                                    <div>
                                        <label htmlFor="confirm-password"
                                               className="text-primary block mb-2 text-sm font-medium text-gray-900 dark:text-white">Confirm
                                            password</label>
                                        <textarea
                                            className="bg-square text-primary bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                                            placeholder="cool password"
                                            onChange={e => {setLoginInfo({...loginInfo, password_x2: e.currentTarget.value})}}
                                        ></textarea>
                                    </div>
                                    <button onClick={registerAction}
                                            className="w-full text-white bg-primary-600 hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800">Create
                                        an account
                                    </button>
                                    <button className="text-sm font-light text-gray-500 dark:text-gray-400"
                                            onClick={switchToLogin}>
                                        Already have an account? <a href="#"
                                                                    className="font-medium text-primary-600 hover:underline dark:text-primary-500">Login
                                        here</a>
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                )
            }
        </div>
    )
}

// TODO: make it beautiful
export function UserIcon({user, setUser}: UserIconProps) {
    function logoutAction() {
        logout()
        setUser("unauthorized")
    }

    let content 
    if (user == "unauthorized") {
        content = <div>
            <h2>unauthorized user</h2>
        </div>
    } else {
        content = <div>
            <h2>{user.name}</h2>
            <button onClick={logoutAction}>LOGOUT</button>
        </div>
    }

    return content
}