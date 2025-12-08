// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'LoginPage',
      component: () => import('../views/LoginPage.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/repairer-register',
      name: 'RepairerRegisterPage',
      component: () => import('../views/RepairerRegisterPage.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/home',
      name: 'HomePage',
      component: () => import('../views/HomePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/work-orders',
      name: 'WorkOrderList',
      component: () => import('../views/WorkOrderList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'ProfilePage',
      component: () => import('../views/ProfilePage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/work-orders/:id',
      name: 'WorkOrderDetail',
      component: () => import('../views/WorkOrderDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/inspection',
      name: 'InspectionPage',
      component: () => import('../views/InspectionPage.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/inspection/scan',
      name: 'InspectionScan',
      component: () => import('../views/InspectionScan.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/inspection/water-maker',
      name: 'WaterMakerList',
      component: () => import('../views/WaterMakerList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/inspection/water-maker/:id',
      name: 'WaterMakerDetail',
      component: () => import('../views/WaterMakerDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/inspection/water-supplier',
      name: 'WaterSupplierList',
      component: () => import('../views/WaterSupplierList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/inspection/water-supplier/:id',
      name: 'WaterSupplierDetail',
      component: () => import('../views/WaterSupplierDetail.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/inspection/form',
      name: 'InspectionForm',
      component: () => import('../views/InspectionForm.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

export default router
