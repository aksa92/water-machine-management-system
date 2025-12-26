// src/services/studentDrinkStatsService.js
import apiClient from '@/services/api'

export const studentDrinkStatsService = {
  /**
   * 获取今日饮水统计
   */
  async getTodayStats(studentId) {
    try {
      const response = await apiClient.post('/api/student/drink-stats/today', {
        studentId: studentId
      })
      return response.data
    } catch (error) {
      console.error('获取今日饮水统计失败:', error)
      throw error
    }
  },

  /**
   * 获取本周饮水统计
   */
  async getThisWeekStats(studentId) {
    try {
      const response = await apiClient.post('/api/student/drink-stats/this-week', {
        studentId: studentId
      })
      return response.data
    } catch (error) {
      console.error('获取本周饮水统计失败:', error)
      throw error
    }
  },

  /**
   * 获取本月饮水统计
   */
  async getThisMonthStats(studentId) {
    try {
      const response = await apiClient.post('/api/student/drink-stats/this-month', {
        studentId: studentId
      })
      return response.data
    } catch (error) {
      console.error('获取本月饮水统计失败:', error)
      throw error
    }
  }
}
