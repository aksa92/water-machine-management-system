// src/services/deviceService.js
import api from './api'

export const deviceService = {
  // 获取维修人员辖区设备（按类型）
  async getAreaDevicesByType(deviceType) {
    try {
      const response = await api.get('/api/web/device/repairman/area-devices-by-type', {
        params: { deviceType }
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // 获取设备详情
  async getDeviceDetail(deviceId) {
    try {
      const response = await api.get(`/api/web/device/${deviceId}`)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // 获取设备状态数量统计
  async getDeviceStatusCount(areaId, deviceType) {
    try {
      const response = await api.get('/api/web/device-status/status-count', {
        params: { areaId, deviceType }
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // 按状态查询设备
  async getDevicesByStatus(status, areaId, deviceType) {
    try {
      const response = await api.get('/api/web/device-status/by-status', {
        params: { status, areaId, deviceType }
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }
}
