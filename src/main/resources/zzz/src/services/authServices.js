// src/services/authServices.js
import api from './api'

export const authServices = {
  async login(username, password) {
    try {
      const response = await api.post('/api/common/login', {
        username: username,
        password: password
      })
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }
}
