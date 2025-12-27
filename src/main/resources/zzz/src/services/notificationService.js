// src/services/notificationService.js
import api from './api'

export const notificationService = {
  // 获取未读通知
  async getUnreadNotifications(repairmanId) {
    try {
      const response = await api.get('/api/app/repairman/notification/unread', {
        params: { repairmanId }
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // 获取所有通知
  async getAllNotifications(repairmanId) {
    try {
      const response = await api.get('/api/app/repairman/notification/all', {
        params: { repairmanId }
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  },

  // 标记通知为已读
  async markNotificationAsRead(notificationId) {
    try {
      const response = await api.post('/api/app/repairman/notification/read', null, {
        params: { notificationId }
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }
}
