import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'StudentLoginPage',
            component: () => import('../views/StudentLoginPage.vue')
        },
        {
            path: '/home',
            name: 'HomePage',
            component: () => import('../views/HomePage.vue')
        },
        {
            path: '/water-quality',
            name: 'WaterQuality',
            component: () => import('../views/WaterQualityPage.vue')
        },
        {
            path: '/realtime-data',
            name: 'RealtimeData',
            component: () => import('../views/RealtimeDataPage.vue')
        },
        {
            path: '/scan',
            name: 'ScanPage',
            component: () => import('../views/ScanPage.vue')
        },
        {
            path: '/profile',
            name: 'ProfilePage',
            component: () => import('../views/ProfilePage.vue')
        },
        {
            path: '/history',
            name: 'HistoryPage',
            component: () => import('../views/HistoryPage.vue')
        }
    ]
})

export default router