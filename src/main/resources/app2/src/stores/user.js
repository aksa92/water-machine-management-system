// src/stores/user.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userType = ref(localStorage.getItem('userType') || '')
  const studentId = ref(localStorage.getItem('studentId') || '')

  // 计算属性：是否已登录
  const isLoggedIn = computed(() => !!token.value)

  // 设置用户信息
  const setUser = (userInfo) => {
    token.value = userInfo.token
    userId.value = userInfo.userId
    username.value = userInfo.username
    userType.value = userInfo.userType
    studentId.value = userInfo.studentId

    // 保存到本地存储
    localStorage.setItem('token', userInfo.token)
    localStorage.setItem('userId', userInfo.userId)
    localStorage.setItem('username', userInfo.username)
    localStorage.setItem('userType', userInfo.userType)
    localStorage.setItem('studentId', userInfo.studentId)
  }

  // 清除用户信息
  const clearUser = () => {
    token.value = ''
    userId.value = ''
    username.value = ''
    userType.value = ''
    studentId.value = ''

    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('userType')
    localStorage.removeItem('studentId')
  }

  // 从本地存储初始化用户信息
  const initFromStorage = () => {
    token.value = localStorage.getItem('token') || ''
    userId.value = localStorage.getItem('userId') || ''
    username.value = localStorage.getItem('username') || ''
    userType.value = localStorage.getItem('userType') || ''
    studentId.value = localStorage.getItem('studentId') || ''
  }

  return {
    token,
    userId,
    username,
    userType,
    studentId,
    isLoggedIn,
    setUser,
    clearUser,
    initFromStorage
  }
})
