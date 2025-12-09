// src/services/deviceService.js
import api from './api'

export const deviceService = {
  // 获取终端设备信息
  async getTerminalInfo(terminalId) {
    try {
      const response = await api.get(`/api/water/terminal/${terminalId}`)
      return response.data
    } catch (error) {
      // 更好的错误处理
      if (error.response?.status === 403) {
        console.error('权限不足，请重新登录')
        // 可以在这里触发重新登录逻辑
      }
      throw error.response?.data || error.message
    }
  },

  // 获取水质信息
  async getWaterQualityInfo(deviceId) {
    try {
      const response = await api.get(`/api/water/quality/${deviceId}`)
      return response.data
    } catch (error) {
      if (error.response?.status === 403) {
        console.error('权限不足，无法获取水质信息')
      }
      throw error.response?.data || error.message
    }
  }
}
