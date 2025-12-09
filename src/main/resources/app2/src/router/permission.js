// src/router/permission.js
import router from './index'

// 不需要登录的白名单路由
const whiteList = ['/', '/login', '/register']

router.beforeEach((to, from, next) => {
  // 检查是否有有效的token
  const token = localStorage.getItem('token')

  if (token) {
    // 有token的情况下
    if (to.path === '/' || to.path === '/login') {
      // 已登录用户访问登录页时重定向到首页
      next('/home')
    } else {
      // 访问其他需要权限的页面，允许通过
      next()
    }
  } else {
    // 没有token的情况下
    if (whiteList.includes(to.path)) {
      // 白名单路由可以直接访问
      next()
    } else {
      // 非白名单路由重定向到登录页
      next('/')
    }
  }
})
