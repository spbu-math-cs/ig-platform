import { createSlice } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'

export type ThemeClass = "theme-metal" | "theme-green"
export type ThemeState = {
    value: ThemeClass
}

const initialState: ThemeState = {
    value: "theme-metal",
}

export const themeSlice = createSlice({
    name: 'theme',
    initialState,
    reducers: {
        setTheme: (state, action: PayloadAction<ThemeClass>) => {
            state.value = action.payload
        },
        nextTheme: (state) => {
            switch (state.value) {
                case "theme-metal":
                    state.value = "theme-green"
                    break
                case "theme-green":
                    state.value = "theme-metal"
                    break
            }
        }
    }
})

export const { setTheme, nextTheme } = themeSlice.actions

export default themeSlice.reducer
