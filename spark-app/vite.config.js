import { defineConfig , loadEnv} from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'
import { readFileSync } from 'fs'

// 版本号与 package.json 同源，编译期注入为常量（与 PC 端 spark-ui 的 __APP_VERSION__ 一致）
const pkg = JSON.parse(readFileSync(resolve(__dirname, 'package.json'), 'utf-8'))

export default defineConfig((mode) => {
    const env = loadEnv(mode.mode, process.cwd());
    return {
        plugins: [uni(),],
        resolve: {
            alias: {
                '@': resolve(__dirname, 'src'), // 路径别名
            },
            extensions: ['.js', '.vue', '.json', '.css'] // 使用路径别名时想要省略的后缀名，可以自己 增减
        },
        server: {
            host: '127.0.0.1',
            port: 7102,
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
                'BASE_HTTP_API':  env.VITE_HTTP_BASE_API
            },
            '__APP_VERSION__': JSON.stringify(pkg.version)
        }
    }
})

