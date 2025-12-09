// src/services/authServices.js
import api from './api'

export const authServices = {
  // 通用登录接口（已有）
  async login(loginData) {
    try {
      const response = await api.post('/api/common/login', loginData)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // 新增：维修人员注册接口（核心注册功能）
  async repairerRegister(registerData) {
    try {
      // 构建符合后端RegisterRequest的数据结构
      const requestData = {
        username: registerData.username,
        password: registerData.password,
        userType: 'repairman',  // 固定为repairma 类型
        repairmanId: registerData.repairmanId
      }

      console.log('发送注册请求:', requestData)

      // 调用后端注册接口
      const response = await api.post('/api/common/register', requestData)

      console.log('注册响应:', response.data)
      return response.data
    } catch (error) {
      console.error('注册失败:', error)

      // 返回标准化的错误格式
      if (error.response?.data) {
        // 后端返回的错误
        throw error.response.data
      } else if (error.message) {
        // 网络或其他错误
        throw {
          code: 500,
          message: error.message || '网络错误'
        }
      } else {
        // 未知错误
        throw {
          code: 500,
          message: '注册失败，请重试'
        }
      }
    }
  }
}
