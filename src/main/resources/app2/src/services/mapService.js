// src/services/mapService.js
import api from './api'

export const mapService = {
    // 获取所有设备位置
    async getTerminalLocations() {
        try {
            const response = await api.get('/api/student/terminal/location/all')
            return response.data
        } catch (error) {
            console.error('获取设备位置失败:', error)
            throw error.response?.data || error.message
        }
    },

    // 获取可用设备位置
    async getAvailableTerminalLocations() {
        try {
            const response = await api.get('/api/student/terminal/location/available')
            return response.data
        } catch (error) {
            console.error('获取可用设备位置失败:', error)
            throw error.response?.data || error.message
        }
    },

    // 根据坐标获取附近设备
    async getNearbyTerminals(lng, lat, radius = 1000) {
        try {
            const response = await api.post('/api/student/terminal/nearby', {
                longitude: lng,
                latitude: lat,
                radius: radius
            })
            return response.data
        } catch (error) {
            console.error('获取附近设备失败:', error)
            throw error.response?.data || error.message
        }
    }
}