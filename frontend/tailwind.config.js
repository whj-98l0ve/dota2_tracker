/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        bgDark: '#0b0f19',
        cardDark: '#121929',
        cardBorder: 'rgba(255, 255, 255, 0.08)',
        neonGreen: '#00e599',
        neonRed: '#ff4d4f',
        neonGold: '#ffb703',
        neonBlue: '#3b82f6',
        neonPurple: '#8b5cf6',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'Menlo', 'monospace'],
      }
    },
  },
  plugins: [],
}