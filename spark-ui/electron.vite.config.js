import { defineConfig, externalizeDepsPlugin } from 'electron-vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    // 主进程配置
    main: {
      plugins: [externalizeDepsPlugin()],
      build: {
        rollupOptions: {
          input: {
            index: resolve('electron/main/index.js'),
          },
        },
      },
    },
    // 预加载脚本配置
    preload: {
      plugins: [externalizeDepsPlugin()],
      build: {
        rollupOptions: {
          input: {
            index: resolve('electron/preload/index.js'),
          },
        },
      },
    },
    // 渲染进程配置（现有 Vue 项目）
    renderer: {
      plugins: [vue()],
      root: '.',
      build: {
        rollupOptions: {
          input: {
            index: resolve('index.html'),
          },
        },
      },
      resolve: {
        alias: {
          '@': resolve('src'),
        },
        extensions: ['.js', '.vue', '.json', '.scss'],
      },
      css: {
        preprocessorOptions: {
          scss: {
            additionalData: (content, filename) => {
              if (filename.replace(/\\/g, '/').endsWith('src/styles/variables.scss')) {
                return content
              }
              return `@use "@/styles/variables" as *;\n${content}`
            },
          },
        },
      },
      server: {
        host: '127.0.0.1',
        port: 80,
        proxy: {
          '/api/ws': {
            target: 'ws://127.0.0.1:8000/',
            changeOrigin: true,
            rewrite: path => path.replace(/^\/api/, ''),
          },
          '/api': {
            target: 'http://127.0.0.1:8000/',
            changeOrigin: true,
            rewrite: path => path.replace(/^\/api/, ''),
          },
        },
      },
      define: {
        'process.env': {
          'BASE_HTTP_API': env.VITE_HTTP_BASE_API,
        },
      },
    },
  }
})