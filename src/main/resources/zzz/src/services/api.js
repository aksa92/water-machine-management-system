// src/services/api.js - 最小版本
import axios from 'axios'

// 创建最基本的axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
})

// 只添加一个请求日志，方便调试
apiClient.interceptors.request.use(
  (config) => {
    console.log(`请求: ${config.method.toUpperCase()} ${config.url}`)
    return config
  }
)

export default apiClient
