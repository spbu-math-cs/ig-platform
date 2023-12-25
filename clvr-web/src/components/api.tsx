import exp from "constants"


const API_ENDPOINT = new URL("http://0.0.0.0:8080/")

export type UserInfo = {
    name: string,
    id: string
}

export type UnauthorizedUser = "unauthorized"

export type User = UserInfo | UnauthorizedUser

// very very very bad but I do not know js
export function userEquals(userA: User, userB: User): boolean {
    if ((userA == "unauthorized" && userB != "unauthorized") ||
        (userA != "unauthorized" && userB == "unauthorized"))
        return false;
    if (userA == "unauthorized" && userB == "unauthorized") 
        return true;
    if (userA != "unauthorized" && userB != "unauthorized" && userA.name != userB.name) {
        return false;
    }
    return true;
}

export async function login(name: string, password: string): Promise<User> {
    try {
        const response = await fetch(new URL(`login`, API_ENDPOINT), {
            method: "POST",
            body: JSON.stringify({
                name: name,
                password: password
            }),
            headers: {
                "Content-Type": "application/json",
            },
            credentials: 'include'
        })
        
        if (response.status == 401) {
            alert("Incorrect password")
            return "unauthorized"
        }

        const userInfo = await response.json()
        return userInfo
    } catch(error) {
        return "unauthorized"
    }
}

export async function logout() {
    await fetch(new URL(`logout`, API_ENDPOINT), {
        method: "POST",
        credentials: `include`
    })
}

export async function register(name: string, password: string): Promise<User> {
    try {
        const response = await fetch(new URL(`register`, API_ENDPOINT), {
            method: "POST",
            body: JSON.stringify({
                name: name,
                password: password
            }),
            headers: {
                "Content-Type": "application/json",
            },
            credentials: 'include'
        })

        if (response.status == 409) {
            alert("User with name " + name + " already exists")
            return "unauthorized"
        }

        const userInfo = await response.json()
        return userInfo
    } catch(error) {
        return "unauthorized"
    }
}

export async function getUser(): Promise<User> {
    try {
        const response = await fetch(new URL('user', API_ENDPOINT), {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
            credentials: 'include'
        })

        if (response.status == 403) {
            console.warn("Failed to get user info")
            return "unauthorized"
        }

        const userInfo = await response.json()
        console.log(userInfo)
        return userInfo
    } catch(error) {
        return "unauthorized"
    }
}