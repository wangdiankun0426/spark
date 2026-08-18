import { defineConfig , loadEnv} from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import { resolve } from 'path'

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
            }
        }
    }
})

