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
        extend: {},
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
                            'boardHover': '#303759',
                            'answerTxt': '#f3b236',
                            'XO': '#303759',
                            'hostTxt': '#f3b236',
                            'playerTxt': '#565584',
                        }
                    }
                },
                {
                    name: "2048",
                    selectors: [".theme-2048"],
                    extend: {
                        colors: {
                            'primary': '#f67c5f',
                            'O': '#edc850',
                            'X': '#f59563',
                            'task': '#ffe1a9',
                            'answerPanel': '#f59563',
                            'back': '#ecceb3',
                            'next': '#edc850',
                            'quit': '#f59563',
                            'txt': '#f67c5f',
                            'panel': '#835c5c',
                            'boardHover': '#f59563',
                            'answerTxt': '#ffe1a9',
                            'XO': '#303030',
                            'hostTxt': '#edc850',
                            'playerTxt': '#f59563',
                        }
                    }
                },
                {
                    name: "purple",
                    selectors: [".theme-purple"],
                    extend: {
                        colors: {
                            'primary': '#b93f73',
                            'O': '#f3b236',
                            'X': '#AE445A',
                            'task': '#eda8ee',
                            'answerPanel': '#f3b236',
                            'back': '#ffd3ef',
                            'next': '#f3b236',
                            'quit': '#AE445A',
                            'txt': '#b93f73',
                            'panel': '#571e65',
                            'boardHover': '#f3b236',
                            'answerTxt': '#AE445A',
                            'XO': '#571e65',
                            'hostTxt': '#f3b236',
                            'playerTxt': '#AE445A',
                        },
                    }
                },
            ]
        }),
    ],
}
