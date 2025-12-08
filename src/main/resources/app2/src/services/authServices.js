// src/services/authServices.js
import api from './api'

export const authServices = {
    // 通用登录接口
    async login(loginData) {
        try {
            const response = await api.post('/api/common/login', loginData)
            return response.data
        } catch (error) {
            throw error.response?.data || error.message
        }
    },

    // 学生登录接口
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

    // 修改：学生注册接口 - 使用正确的路径和字段
    async studentRegister(registerData) {
        try {
            // 构建符合RegisterRequest的数据
            const requestData = {
                username: registerData.name,      // 用户名（学生姓名）
                password: registerData.password,   // 密码
                userType: 'user',                  // 固定为用户类型
                studentId: registerData.studentId, // 学号
                studentName: registerData.name     // 学生姓名
                // 注意：后端RegisterRequest目前只支持这些字段
                // 手机号、邮箱等需要扩展User实体和RegisterRequest
            }

            console.log('发送注册数据到后端:', requestData)
            const response = await api.post('/api/common/register', requestData)
            console.log('后端注册响应:', response.data)
            return response.data
        } catch (error) {
            console.error('注册API错误:', error)
            // 返回更详细的错误信息
            const errorData = error.response?.data || {
                code: 500,
                message: error.message || '网络错误'
            }
            throw errorData
        }
    }
}