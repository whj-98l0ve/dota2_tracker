import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 7171, // 把 8080 替换成你想监听的端口号
    host: '0.0.0.0', // 可选：加上这行可以通过本机局域网 IP 访问，不加则默认只能 localhost 访问
    // 将前端 /api 请求代理转发给同台机器上的后端 8088 端口，避免浏览器请求到访问者本机的 localhost:8088
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8088',
        changeOrigin: true
      }
    }
  }
})
