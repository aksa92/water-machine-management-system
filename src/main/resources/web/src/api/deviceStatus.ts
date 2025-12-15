// src/api/deviceStatus.ts
import axios from 'axios'

export const DeviceStatusApi = {
  // 获取设备状态列表 - 修改为匹配后端实际接口
    getDevicesByType: async (deviceType: string, areaId?: string) => {
        try {
            const params: any = { deviceType }
            if (areaId) params.areaId = areaId

            // 注意：这里是调用设备状态管理的接口
            const response = await axios.get('/api/web/device-status/by-type', { params })
            return response.data
        } catch (error) {
            throw new Error(`获取设备列表失败: ${error}`)
        }
    },

    // 按状态查询设备
    getDevicesByStatus: async (status: string, areaId?: string) => {
        try {
            const params: any = { status }
            if (areaId) params.areaId = areaId

            const response = await axios.get('/api/web/device-status/by-status', { params })
            return response.data
        } catch (error) {
            throw new Error(`获取设备列表失败: ${error}`)
        }
    },

  // 标记设备在线
  markDeviceOnline: async (deviceId: string) => {
    try {
      const response = await axios.post(`/api/web/device-status/${deviceId}/online`)
      return response.data
    } catch (error) {
      throw new Error(`设置设备在线失败: ${error}`)
    }
  },

  // 标记设备离线
  markDeviceOffline: async (deviceId: string, reason?: string) => {
    try {
      const params = reason ? { reason } : {}
      const response = await axios.post(`/api/web/device-status/${deviceId}/offline`, null, { params })
      return response.data
    } catch (error) {
      throw new Error(`设置设备离线失败: ${error}`)
    }
  },

  // 标记设备故障
  markDeviceFault: async (deviceId: string, faultType: string, description: string) => {
    try {
      const params = { faultType, description }
      const response = await axios.post(`/api/web/device-status/${deviceId}/fault`, null, { params })
      return response.data
    } catch (error) {
      throw new Error(`设置设备故障失败: ${error}`)
    }
  }
}
