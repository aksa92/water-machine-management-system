// src/services/authServices.js
import api from './api'

export const authServices = {
    // 通用登录接口（已存在）
    async login(loginData) {
        try {
            const response = await api.post('/api/common/login', loginData)
            return response.data
        } catch (error) {
            throw error.response?.data || error.message
        }
    },

    // 新增：学生登录接口
    async studentLogin(studentId, password) {
        try {
            const loginData = {
                username: studentId,
                password: password,
                userType: 'user'  // 注意：后端中用户类型是"user"
            }
            const response = await api.post('/api/common/login', loginData)
            return response.data
        } catch (error) {
            throw error.response?.data || error.message
        }
    },

    // 新增：学生注册接口（如果后端有的话）
    async studentRegister(registerData) {
        try {
            // 这里需要根据后端的注册接口路径来调整
            const response = await api.post('/api/user/register', registerData)
            return response.data
        } catch (error) {
            throw error.response?.data || error.message
        }
    }
}