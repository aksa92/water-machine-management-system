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
  },

  // 获取可抢工单
  async getAvailableOrders(areaId) {
    try {
      const response = await api.get(`/api/app/repairman/available-orders?areaId=${areaId}`)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // 抢单
  async grabOrder(orderId, repairmanId) {
    try {
      const response = await api.post('/api/app/repairman/grab-order', {
        orderId,
        repairmanId
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // 拒单
  async rejectOrder(orderId, repairmanId, reason) {
    try {
      const response = await api.post('/api/app/repairman/reject-order', {
        orderId,
        repairmanId,
        reason
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // 提交维修结果
  async submitRepairResult(orderId, repairmanId, dealNote, imgUrl = null) {
    try {
      const response = await api.post('/api/app/repairman/submit-result', {
        orderId,
        repairmanId,
        dealNote,
        imgUrl
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }
}
