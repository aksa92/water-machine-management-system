import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'LoginPage', // 改为多单词
      component: () => import('../views/LoginPage.vue')
    },
    {
      path: '/home',
      name: 'HomePage', // 改为多单词
      component: () => import('../views/HomePage.vue')
    }
  ],
})

export default router
