// src/services/api.js
import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://120.46.151.248:8080',
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    // 从本地存储获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      // token过期或无效，清除用户信息并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('userType')
      localStorage.removeItem('studentId')
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
)

export default apiClient
