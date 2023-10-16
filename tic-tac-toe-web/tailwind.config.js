const colors = require('tailwindcss/colors')

module.exports = {
    content: [
        './pages/**/*.{js,ts,jsx,tsx}',
        './components/**/*.{js,ts,jsx,tsx}',
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
    plugins: [],
}
