/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // 极光毛玻璃主题核心色板
        bgDark: '#050811',
        cardDark: 'rgba(15, 23, 42, 0.65)',
        cardBorder: 'rgba(255, 255, 255, 0.12)',
        neonGreen: '#10b981',
        neonRed: '#f43f5e',
        neonGold: '#f59e0b',
        neonBlue: '#22d3ee',
        neonPurple: '#a855f7',
      },
      fontFamily: {
        sans: ['Inter', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'Menlo', 'Consolas', 'monospace'],
      },
      boxShadow: {
        'aurora-cyan': '0 0 25px -5px rgba(34, 211, 238, 0.35), inset 0 1px 0 0 rgba(255, 255, 255, 0.2)',
        'aurora-emerald': '0 0 25px -5px rgba(16, 185, 129, 0.35), inset 0 1px 0 0 rgba(255, 255, 255, 0.2)',
        'aurora-rose': '0 0 25px -5px rgba(244, 63, 94, 0.35), inset 0 1px 0 0 rgba(255, 255, 255, 0.2)',
        'aurora-card': '0 20px 50px -12px rgba(0, 0, 0, 0.75), inset 0 1px 0 0 rgba(255, 255, 255, 0.12)',
      }
    },
  },
  plugins: [],
}
