const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    // 前端开发服务器端口（后端为 8123，成对好记；若被占用可改成 8125 等）
    port: 8124
  }
})
