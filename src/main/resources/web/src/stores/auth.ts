import { defineStore } from 'pinia'
import { ref } from 'vue'
import { realLoginApi } from '@/api/auth'
import type { LoginVO } from '@/api/types/auth'

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

  // 登录方法
  const login = async (loginData: { username: string; password: string; rememberMe?: boolean }) => {
    try {
      const response: LoginVO = await realLoginApi(loginData)
      
      if (response.code === 200) {
        token.value = response.data.token
        userInfo.value = response.data.userInfo
        isLoggedIn.value = true
        
        // 存储到localStorage
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo))
        
        return response
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error: any) {
      throw new Error(error.message || '登录失败，请检查网络连接')
    }
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 初始化时从localStorage恢复状态
  const initialize = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')
    
    if (savedToken && savedUserInfo) {
      token.value = savedToken
      userInfo.value = JSON.parse(savedUserInfo)
      isLoggedIn.value = true
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    logout,
    initialize
  }
})