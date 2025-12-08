// src/router/permission.js
import router from './index'
import { useAuthStore } from '@/stores/auth'

// 需要登录的白名单
const whiteList = ['/', '/repairer-register']

router.beforeEach((to, from, next) => {
  console.log('路由守卫执行:', to.path)

  const authStore = useAuthStore()

  // 如果目标路由在白名单中，直接放行
  if (whiteList.includes(to.path)) {
    return next()
  }

  // 检查是否已登录
  if (!authStore.isAuthenticated) {
    console.warn('未登录，跳转到登录页')
    next('/')
  } else {
    // 已登录，验证用户权限（如果需要）
    const userType = authStore.getUserType
    console.log('当前用户类型:', userType)

    // 这里可以添加权限验证逻辑
    // 例如：只有特定用户类型可以访问某些页面
    next()
  }
})

// 路由守卫后置钩子
router.afterEach((to, from) => {
  console.log('路由切换完成:', from.path, '->', to.path)
})
