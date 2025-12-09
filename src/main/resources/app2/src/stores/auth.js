// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue' // 添加 computed 导入

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token'))

  const login = (userData, authToken) => {
    user.value = userData
    token.value = authToken
    localStorage.setItem('token', authToken)
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }

  const isAuthenticated = computed(() => !!token.value) // 使用 computed 包装

  return {
    user,
    token,
    isAuthenticated,
    login,
    logout
  }
})
