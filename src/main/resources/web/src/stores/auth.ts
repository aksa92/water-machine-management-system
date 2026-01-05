// src/stores/auth.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/modules/auth'
import type { LoginRequest, LoginVO, ResultVO } from '@/api/types/auth'

interface UserInfo {
    id: number
    username: string
    realName?: string
    role?: string
    avatar?: string
    areaId?: string
}

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string>('')
    const userInfo = ref<UserInfo | null>(null)
    const isLoggedIn = ref(false)

    // 真实登录接口调用
    const login = async (loginData: LoginRequest) => {
    try {
        const response: ResultVO<LoginVO> = await authApi.login(loginData)

        if (response.code !== 200 || !response.data) {
            throw new Error(response.message || '登录失败')
        }

        // 直接使用后端返回的数据结构
        const responseData = response.data

        if (!responseData.token) {
            throw new Error('登录响应数据不完整')
        }

        // 转换后端数据结构为前端期望的格式
        token.value = responseData.token
        userInfo.value = {
            id: parseInt(responseData.userId) || 0,
            username: responseData.username,
            role: responseData.userType,
            areaId: responseData.areaId
        }
        isLoggedIn.value = true

        // 存储到 localStorage（如果用户选择记住我）
        if (loginData.rememberMe) {
            localStorage.setItem('token', responseData.token)
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
            localStorage.setItem('rememberMe', 'true')
        } else {
            sessionStorage.setItem('token', responseData.token)
            sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))
            localStorage.removeItem('rememberMe')
        }

        return response
    } catch (error: any) {
        console.error('登录失败:', error)
        throw error
    }
}


    // 退出登录
    const logout = () => {
        // 可以调用后端登出接口
        // authApi.logout().catch(console.error)

        // 清除本地存储
        token.value = ''
        userInfo.value = null
        isLoggedIn.value = false

        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('rememberMe')
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('userInfo')
    }

    // 初始化时从存储恢复状态
    const initialize = () => {
        const rememberMe = localStorage.getItem('rememberMe')
        const storage = rememberMe ? localStorage : sessionStorage

        const savedToken = storage.getItem('token')
        const savedUserInfo = storage.getItem('userInfo')

        if (savedToken && savedUserInfo) {
            try {
                token.value = savedToken
                userInfo.value = JSON.parse(savedUserInfo)
                isLoggedIn.value = true
            } catch (e) {
                console.error('恢复登录状态失败:', e)
                logout()
            }
        }
    }

    // 检查是否已登录（用于路由守卫）
    const checkAuth = (): boolean => {
        return isLoggedIn.value && !!token.value
    }

    return {
        token,
        userInfo,
        isLoggedIn,
        login,
        logout,
        initialize,
        checkAuth,
    }
})
