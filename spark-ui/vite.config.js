import { defineConfig , loadEnv} from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import pkg from './package.json'

// 开发服务器启动横幅，把当前 mode 与实际加载的接口基址显式打印，env 文件改动需重启才能生效
function envBannerPlugin(mode, env) {
  return {
    name: 'spark-env-banner',
    configureServer(server) {
      server.httpServer?.once('listening', () => {
        const address = server.httpServer.address()
        const port = address && typeof address === 'object' ? address.port : ''
        const api = env.VITE_HTTP_BASE_API || '(未配置)'
        const hotUpdate = env.VITE_HOT_UPDATE_URL || '未启用'
        const isRemoteApi = !/127\.0\.0\.1|localhost/i.test(api)
        const c = { cyan: '\x1b[36m', yellow: '\x1b[33m', gray: '\x1b[90m', reset: '\x1b[0m' }
        const lines = [
          '',
          `${c.cyan}  ── 星火云开发服务器 ──────────────────────────────${c.reset}`,
          `  运行模式: ${mode}`,
          `  接口基址: ${api}`,
          `  热更清单: ${hotUpdate}`,
          `  监听地址: http://127.0.0.1:${port}`,
        ]
        if (mode === 'dev' && isRemoteApi) {
          lines.push(`${c.yellow}  [警告] dev 模式接口基址指向远程环境，请检查 .env.dev 并重启 dev 服务${c.reset}`)
        }
        lines.push(`${c.gray}  ─────────────────────────────────────────────────${c.reset}`, '')
        server.config.logger.info(lines.join('\n'), { clear: false })
      })
    },
  }
}

export default defineConfig((mode) => {
  const env = loadEnv(mode.mode, process.cwd());
  return {
    plugins: [vue(), envBannerPlugin(mode.mode, env)],
    resolve: {
      alias: {
        "@": resolve(__dirname, 'src'), // 路径别名
        // 桌面端专属前端代码（Tauri 壳内才生效），与 Rust 侧代码同放 src-tauri，便于成套维护
        "@desktop": resolve(__dirname, 'src-tauri/desktop'),
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
      strictPort: true, // 端口被占用时直接报错，避免 Tauri devUrl 指向失效端口
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
      },
      __APP_VERSION__: JSON.stringify(pkg.version)
    }
  }
})