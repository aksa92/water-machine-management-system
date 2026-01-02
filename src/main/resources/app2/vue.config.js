const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
    transpileDependencies: true,
    publicPath: './',  // 重要：使用相对路径
    outputDir: 'dist',
    devServer: {
        proxy: {
            '/api': {
                target: 'https://120.46.151.248:8081',  // 后端地址
                changeOrigin: true
            }
        }
    }
})