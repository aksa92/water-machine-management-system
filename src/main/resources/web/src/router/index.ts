// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainLayout from '../components/layout/MainLayout.vue' // 导入布局组件

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView
    },
    {
      path: '/home',
      component: MainLayout, // 使用 MainLayout 作为布局
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('../views/Dashboard.vue') // Dashboard 作为子路由
        }
      ]
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue')
    },
  ],
})

export default router