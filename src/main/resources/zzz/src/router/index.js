import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'LoginPage',
      component: () => import('../views/LoginPage.vue')
    },
    // 添加维修人员注册页路由
    {
      path: '/repairer-register',
      name: 'RepairerRegisterPage',
      component: () => import('../views/RepairerRegisterPage.vue')
    },
    // 其他现有路由保持不变...
    {
      path: '/home',
      name: 'HomePage',
      component: () => import('../views/HomePage.vue')
    },
    {
      path: '/work-orders',
      name: 'WorkOrderList',
      component: () => import('../views/WorkOrderList.vue')
    },
    {
      path: '/profile',
      name: 'ProfilePage',
      component: () => import('../views/ProfilePage.vue')
    },
    {
      path: '/work-orders/:id',
      name: 'WorkOrderDetail',
      component: () => import('../views/WorkOrderDetail.vue')
    },
    {
      path: '/inspection',
      name: 'InspectionPage',
      component: () => import('../views/InspectionPage.vue')
    },
    {
      path: '/inspection/scan',
      name: 'InspectionScan',
      component: () => import('../views/InspectionScan.vue')
    },
    {
      path: '/inspection/water-maker',
      name: 'WaterMakerList',
      component: () => import('../views/WaterMakerList.vue')
    },
    {
      path: '/inspection/water-maker/:id',
      name: 'WaterMakerDetail',
      component: () => import('../views/WaterMakerDetail.vue')
    },
    {
      path: '/inspection/water-supplier',
      name: 'WaterSupplierList',
      component: () => import('../views/WaterSupplierList.vue')
    },
    {
      path: '/inspection/water-supplier/:id',
      name: 'WaterSupplierDetail',
      component: () => import('../views/WaterSupplierDetail.vue')
    },
    {
      path: '/inspection/form',
      name: 'InspectionForm',
      component: () => import('../views/InspectionForm.vue')
    }
  ]
})

export default router
