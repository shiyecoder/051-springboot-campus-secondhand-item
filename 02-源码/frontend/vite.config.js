import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 通道3：前端 5172，后端 8082（端口来自 queue.py 输出，不要改成别的通道）
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 5172,
    strictPort: true,
    proxy: {
      '/api': { target: 'http://127.0.0.1:8082', changeOrigin: true },
      '/uploads': { target: 'http://127.0.0.1:8082', changeOrigin: true }
    }
  },
  build: { outDir: 'dist', chunkSizeWarningLimit: 2000 }
})
