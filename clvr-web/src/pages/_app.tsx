import '@/styles/globals.css'
import type {AppProps} from 'next/app'
import {Provider, useSelector} from "react-redux"
import {RootState, store} from "@/state/store"
import {nextTheme} from "@/state/themeSlice"
import React from "react"
import Button from "@/components/Button"

function MyApp({Component, pageProps}: AppProps) {
    const dispatch = store.dispatch

    function ThemeAware() {
        const theme = useSelector((state: RootState) => state.theme)
        return <div className={theme.value}>
            <Component {...pageProps} />
        </div>
    }


    return <Provider store={store}>
        <ThemeAware/>
    </Provider>
}

export default MyApp
