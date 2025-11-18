/** @type {import('tailwindcss').Config} */
module.exports = {
    theme: {
        extend: {
            // You can add your custom font-family here later
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },
        },
    },
    plugins: [],

    // This is the most important part:
    // It tells the plugin where your HTML files are
    content: [
        "./src/main/resources/templates/**/*.html"
    ]
}