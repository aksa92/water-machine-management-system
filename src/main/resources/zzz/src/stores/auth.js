// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router/index.js'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref(null)
  const token = ref(localStorage.getItem('token'))
  const isAuthenticated = computed(() => !!token.value && !!user.value)

  // 登录
  const login = (userData, authToken) => {
    user.value = userData
    token.value = authToken

    // 存储到本地
    localStorage.setItem('token', authToken)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  // 获取用户信息
  const getUserInfo = () => {
    if (!user.value) {
      const storedUser = localStorage.getItem('user')
      if (storedUser) {
        user.value = JSON.parse(storedUser)
      }
    }
    return user.value
  }

  // 获取用户类型
  const getUserType = computed(() => {
    return user.value?.userType || localStorage.getItem('userType')
  })

  // 获取维修人员ID
  const getRepairmanId = computed(() => {
    return user.value?.userId || localStorage.getItem('userId') // 使用userId而不是repairmanId
  })

  // 获取区域ID
  const getAreaId = computed(() => {
    return user.value?.areaId || localStorage.getItem('areaId')
  })

  // 登出
  const logout = () => {
    user.value = null
    token.value = null

    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('userType')
    localStorage.removeItem('repairmanId')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('areaId') // 移除 areaId

    // 跳转到登录页
    router.push('/')
  }

  // 初始化时从本地存储恢复
  const initFromStorage = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')

    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = JSON.parse(storedUser)
    }
  }

  return {
    user,
    token,
    isAuthenticated,
    getUserType,
    getRepairmanId,
    getAreaId, // 导出 getAreaId
    login,
    logout,
    getUserInfo,
    initFromStorage
  }
})
