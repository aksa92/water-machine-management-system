import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
    ],
    resolve: {
        alias: {
            '@': '/src'
        },
    },
    server: {
        proxy: {
            // 代理所有以 /api 开头的请求到后端
            '/api': {
                target: 'https://120.46.151.248:8081', // Spring Boot 后端地址
                changeOrigin: true, // 改变请求来源
                secure: false, // 如果是https，可能需要设置为false
                // 如果需要重写路径，可以取消下面的注释
                // rewrite: (path) => path.replace(/^\/api/, '')
            },
            // 如果需要代理其他路径，可以继续添加
            // '/ws': {
            //   target: 'ws://localhost:8080',
            //   ws: true
            // }
        },
        // 可选：设置端口
        port: 5173, // Vite默认端口
        // 可选：自动打开浏览器
        open: true
    }
})