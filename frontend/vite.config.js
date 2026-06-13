import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import path from 'node:path'

const elementPlusLib = path.resolve(__dirname, 'node_modules/element-plus/lib/index.js')

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      // 兼容 element-plus 安装不完整时回退到 lib 入口
      'element-plus/es': path.resolve(__dirname, 'node_modules/element-plus/lib'),
      'element-plus': elementPlusLib
    }
  },
  optimizeDeps: {
    include: ['element-plus/lib/index.js', '@element-plus/icons-vue']
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
