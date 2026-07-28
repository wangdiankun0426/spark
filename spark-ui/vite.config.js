import { defineConfig , loadEnv} from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig((mode) => {
  const env = loadEnv(mode.mode, process.cwd());
  return {
    plugins: [vue(),],
    resolve: {
      alias: {
        "@": resolve(__dirname, 'src'), // 路径别名
      },
      extensions: ['.js', '.vue', '.json', '.scss'] // 使用路径别名时想要省略的后缀名，可以自己 增减
    },
    css: {
      preprocessorOptions: {
        scss: {
          // 全局注入 variables.scss，所有 scss 文件与 .vue 的 <style lang="scss"> 均可直接使用变量，无需逐个 @use
          additionalData: (content, filename) => {
            // 排除 variables.scss 自身，避免循环引用
            if (filename.replace(/\\/g, '/').endsWith('src/styles/variables.scss')) {
              return content;
            }
            return `@use "@/styles/variables" as *;\n${content}`;
          }
        }
      }
    },
    optimizeDeps: {
      entries: ['./index.html']
    },
    server: {
      host: '127.0.0.1',
      port: 7101,
      proxy: {
        // 在此处为需要解决跨域的 API 配置代理
        '/api/ws': {
          target: 'ws://127.0.0.1:8000/',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/api/, '') // 去掉 /api 前缀
        },
        // 在此处为需要解决跨域的 API 配置代理
        '/api': {
          target: 'http://127.0.0.1:8000/',
          changeOrigin: true,
          rewrite: path => path.replace(/^\/api/, '') // 去掉 /api 前缀
        },
      }
    },
    define: {
      'process.env': {
        'BASE_HTTP_API': env.VITE_HTTP_BASE_API
      }
    }
  }
})