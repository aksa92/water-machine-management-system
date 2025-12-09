// src/services/workOrderService.js
import api from './api'

export const workOrderService = {
  // 获取我的工单
  async getMyOrders(repairmanId) {
    try {
      const response = await api.get(`/api/app/repairman/my-orders?repairmanId=${repairmanId}`)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }
}
