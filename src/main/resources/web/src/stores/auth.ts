// src/stores/auth.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/modules/auth'
import type { LoginRequest, LoginVO, ResultVO } from '@/api/types/auth'

interface UserInfo {
    id: number
    username: string
    realName: string
    role: string
    avatar?: string
}

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string>('')
    const userInfo = ref<UserInfo | null>(null)
    const isLoggedIn = ref(false)

    // 真实登录接口调用
    const login = async (loginData: LoginRequest) => {
        try {
            // 调用真实后端接口
            const response: ResultVO<LoginVO> = await authApi.login(loginData)

            // 检查响应状态
            if (response.code !== 200) {
                throw new Error(response.message || '登录失败')
            }

            // 保存 token 和用户信息
            token.value = response.data.token
            userInfo.value = response.data.userInfo
            isLoggedIn.value = true

            // 存储到 localStorage（如果用户选择记住我）
            if (loginData.rememberMe) {
                localStorage.setItem('token', response.data.token)
                localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
                localStorage.setItem('rememberMe', 'true')
            } else {
                sessionStorage.setItem('token', response.data.token)
                sessionStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
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