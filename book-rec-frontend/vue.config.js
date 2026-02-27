const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: { '^/api': '' }
      },
      // ✨ 新增这个配置：只要是访问 /covers 的，都转给 5001 端口的 Flask
      '/covers': {
        target: 'http://localhost:5001', 
        changeOrigin: true,
        // 这里不需要 pathRewrite，因为 Flask 本身就需要 /covers 这个路径
      }
    }
  }
})
