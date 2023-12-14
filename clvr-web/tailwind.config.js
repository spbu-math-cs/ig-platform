const colors = require('tailwindcss/colors')

module.exports = {
    content: [
        './src/**/*.{js,ts,jsx,tsx}',
    ],
    theme: {
        colors: {
            transparent: 'transparent',
            current: 'currentColor',
            'white': '#ffffff',
            'purple': '#3f3cbb',
            'midnight': '#121063',
            'tahiti': '#3ab7bf',
            'silver': '#ecebff',
            'bubble-gum': '#ff77e9',
            'bermuda': '#78dcca',
            'pastelPurple': '#c3b1e1',

            'metalX': '#565584',
            'metalText': '#f3b236',
            //'indigo': '#571e65',
            'indigo': '#f3b236',
            'metalBG': '#7C81AD',
            'metalTask': '#455677',
            'metalPanel': '#303759',

            'error' : '#a2203d',


            'purpleX': '#AE445A',
            // 'purpleO' : '#581845',
            //   'purpleO' : '#d931ab',
            'purpleO': '#f3b236',
            'purpleText': '#b93f73',
            'purpleBG': '#ffd3ef',
            'purpleTask': '#eda8ee',
            'purplePanel': '#571e65',

            //  'purpleO' : '#F39F5A',
            //  'purpleText' : '#F39F5A',
            //  'purpleBG' : '#662549',
            //  'purplePanel' : '#451952',

            '2048X': '#f59563',
            '2048O': '#edc850',
            '2048Cell': '#ede0c8',
            '2048BG': '#ecceb3',
            '2048Text': '#f67c5f',
            // '2048Panel' : '#5e4444',
            '2048Panel': '#835c5c',
            '2048Task': '#ffe1a9',


            //ok for text, bad for objects
            gray: colors.gray,
            emerald: colors.emerald,
            yellow: colors.yellow,
            pink: colors.pink,
        },
        extend: {
            animation: {
                appear: 'appear 0.2s'
            },
            keyframes: {
                appear: {
                    '0%': {
                        transform: "scale(0%)"
                    },
                    '100%': {
                        transform: "scale(100%)"
                    }
                }
            }
        },
    },
    plugins: [
        require('tailwindcss-themer')({
            defaultTheme: "metal",
            themes: [
                {
                    name: "metal",
                    selectors: [".theme-metal"],
                    extend: {
                        colors: {
                            'primary': '#f3b236',
                            'O': '#f3b236',
                            'X': '#565584',
                            'task': '#455677',
                            'answerPanel': '#303759',
                            'back': '#7C81AD',
                            'next': '#f3b236',
                            'quit': '#565584',
                            'txt': '#f3b236',
                            'panel': '#303759',
                            'selectPanel': '#303759',
                            'square' :  '#303759',
                            'boardHover': '#303759',
                            'answerTxt': '#f3b236',
                            'XO': '#303759',
                            'hostTxt': '#f3b236',
                            'JoinGameTxt' : '#f3b236',
                            'playerTxt': '#565584',
                            'createcol' : '#f3b236'
                        }
                    }
                },
                {
                    name: "2048",
                    selectors: [".theme-2048"],
                    extend: {
                        colors: {
                            'primary': '#303030',
                            'O': '#f3b236',
                            'X': '#f59563',
                            'task': '#ffe1a9',
                            'answerPanel': '#f59563',
                            'back': '#ffe1c9',
                            'next': '#f3b236',
                            'quit': '#f59563',
                            'txt': '#303030',
                            'panel': '#f59563',
                            'selectPanel': '#f59563',
                            'square' :  '#f59563',
                            'boardHover': '#f59563',
                            'answerTxt': '#303030',
                            'XO': '#303030',
                            'hostTxt': '#303030',
                            'JoinGameTxt' : '#303030',
                            'playerTxt': '#303030',
                            'createcol' : '#f3b236'
                        }
                    }
                },
                {
                    name: "purple",
                    selectors: [".theme-purple"],
                    extend: {
                        colors: {
                            'primary': '#f3b236',
                            'O': '#f3b236',
                            'X': '#AE445A',
                            'task': '#571e65',
                            'answerPanel': '#f3b236',
                            'back': '#f8e2f0',
                            'next': '#f3b236',
                            'quit': '#AE445A',
                            'txt': '#f3b236',
                            'panel': '#571e65',
                            'selectPanel': '#571e65',
                            'square' :  '#571e65',
                            'boardHover': '#f3b236',
                            'answerTxt': '#AE445A',
                            'XO': '#571e65',
                            'hostTxt': '#f3b236',
                            'JoinGameTxt': '#f3b236',
                            'playerTxt': '#AE445A',
                            'createcol' : '#f3b236'
                        },
                    }
                },
                {
                    name: "green",
                    selectors: [".theme-green"],
                    extend: {
                        colors: {
                            'primary': '#f8bb4b',
                          //  'O': '#186F65',
                            'O' : '#042f2b',
                            'X': '#e1836d',
                            'task': '#fcdda7',
                            'answerPanel': '#f8bb4b',
                            'back': '#F2FFE9',

                            'next': '#e17056',
                            'quit': '#ecceb3',
                            'txt': '#042f2b',
                            //   'panel': '#186F65'
                            'panel': '#1d8377',
                            'selectPanel': '#1d8377',
                            'square' :  '#1d8377',
                            'boardHover': '#e58a77',
                            'answerTxt': '#330b18',
                            'XO': '#6e3535',
                            //   'hostTxt': '#9a7e4a',
                            // 'hostTxt': '#c9a25f',
                            'hostTxt': '#f8bb4b',
                            'JoinGameTxt': '#f8bb4b',
                            'playerTxt': '#e1836d',
                            // 'createcol' : '#ec958b'
                            // 'createcol' : '#d78076'
                            'createcol': '#e7a481'
                        },
                    }
                },

                {
                    name: "tnkf",
                    selectors: [".theme-tnkf"],
                    extend: {
                        colors: {
                            'primary': '#6c6c6c',
                            'O': '#303030',
                            'X': '#303030',
                            'task': '#fde57d',
                            'answerPanel': '#a0a0a0',
                            'back': '#ededed',

                            'next': '#a0a0a0',
                            'quit': '#ecceb3',
                            'txt': '#414040',

                            'panel': '#ffdd2d',
                            'selectPanel': '#b3b3b3',
                            'square' :  '#a0a0a0',
                            'boardHover': '#a0a0a0',
                          //  'answerTxt': '#6c6c6c',
                            'answerTxt': '#414040',

                            'XO': '#6c6c6c',
                            'hostTxt': '#ffdd2d',
                            'JoinGameTxt': '#414040',
                            'playerTxt': '#a0a0a0',
                            'createcol' : '#b3b3b3'
                        },
                    }
                },
            ]
        }),
    ],
}
