// src/services/authServices.js
// src/services/authServices.js
import api from './api'

export const authServices = {
  async login(loginData) {
    try {
      const response = await api.post('/api/common/login', loginData)
      return response.data
    } catch (error) {
      throw error.response?.data || error.message
    }
  }
}
