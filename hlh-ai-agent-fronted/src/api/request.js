import axios from 'axios'
import { Message } from '@arco-design/web-vue'

// 后端接口地址前缀
export const API_BASE = 'http://localhost:8123/api'

// axios 实例：统一的请求配置与错误处理
const request = axios.create({
  baseURL: API_BASE,
  timeout: 60000
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response && error.response.status
    if (status) {
      Message.error(`请求失败（${status}），请稍后重试`)
    } else if (error.code === 'ECONNABORTED') {
      Message.error('请求超时，请检查后端服务是否启动')
    } else {
      Message.error('网络异常，无法连接后端服务')
    }
    return Promise.reject(error)
  }
)

export default request
