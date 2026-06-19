/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#102322",
        mist: "#eef2ea",
        pine: "#173f35",
        spruce: "#205347",
        moss: "#6f8f67",
        gold: "#f0c15d",
        coral: "#d97359",
      },
      boxShadow: {
        glow: "0 18px 60px rgba(16, 35, 34, 0.18)",
      },
      fontFamily: {
        sans: ['"Trebuchet MS"', '"Segoe UI"', "sans-serif"],
        display: ['Georgia', '"Times New Roman"', "serif"],
      },
    },
  },
  plugins: [],
};
