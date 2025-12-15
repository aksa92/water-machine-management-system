// src/router/index.ts
import { createRouter, createWebHistory, type NavigationGuardNext, type RouteLocationNormalized } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainLayout from '../components/layout/MainLayout.vue'
import { useAuthStore } from '@/stores/auth'


const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'login',
            component: LoginView,
            meta: {
                title: '登录',
                requiresAuth: false // 不需要认证
            }
        },
        {
            path: '/home',
            component: MainLayout,
            meta: {
                requiresAuth: true // 需要认证
            },
            children: [
                {
                    path: '',
                    name: 'home',
                    component: () => import('../views/Dashboard.vue'),
                    meta: {
                        title: '首页'
                    }
                },
                // 设备监控相关路由
                {
                    path: 'equipment',
                    name: 'equipment',
                    component: () => import('../views/equipment/EquipmentView.vue'),
                    meta: {
                        title: '设备监控'
                    }
                },
                {
                    path: '/home/equipment/water-maker/:id',
                    name: 'WaterMakerDetail',
                    component: () => import('@/views/equipment/WaterMakerDetail.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: 'equipment/water-maker',
                    name: 'water-maker',
                    component: () => import('../views/equipment/WaterMaker.vue'),
                    meta: {
                        title: '制水设备'
                    }
                },
                {
                path: '/home/equipment/water-supplier/:id',
                name: 'WaterSupplierDetail',
                component: () => import('@/views/equipment/WaterSupplierDetail.vue'),
                meta: { requiresAuth: true }
                },

                {
                    path: 'equipment/water-supplier',
                    name: 'water-supplier',
                    component: () => import('../views/equipment/WaterSupplier.vue'),
                    meta: {
                        title: '供水设备'
                    }
                },
                // 工单管理相关路由
                {
                    path: 'work-order',
                    name: 'work-order',
                    component: () => import('../views/workorder/WorkOrderView.vue'),
                    meta: {
                        title: '工单管理'
                    }
                },
                {
                    path: 'work-order/pending',
                    name: 'work-order-pending',
                    component: () => import('../views/workorder/Pending.vue'),
                    meta: {
                        title: '待处理工单'
                    }
                },
                {
                    path: 'work-order/timeout',
                    name: 'work-order-timeout',
                    component: () => import('../views/workorder/Timeout.vue'),
                    meta: {
                        title: '超时工单'
                    }
                },
                {
                    path: 'work-order/processing',
                    name: 'work-order-processing',
                    component: () => import('../views/workorder/Processing.vue'),
                    meta: {
                        title: '处理中工单'
                    }
                },
                {
                    path: 'work-order/review',
                    name: 'work-order-review',
                    component: () => import('../views/workorder/Review.vue'),
                    meta: {
                        title: '待审核工单'
                    }
                },
                {
                    path: 'work-order/review/:id',
                    name: 'ReviewDetail',
                    component: () => import('../views/workorder/ReviewDetail.vue'),
                    meta: {
                        title: '工单审核详情'
                    }
                },
                {
                    path: 'work-order/completed',
                    name: 'work-order-completed',
                    component: () => import('../views/workorder/Completed.vue'),
                    meta: {
                        title: '已完成工单'
                    }
                },
                {
                    path: '/home/work-order/completed/:id',
                    name: 'CompletedDetail',
                    component: () => import('@/views/workorder/CompletedDetail.vue'),
                    meta: {
                        title: '结单信息'
                    }
                },
                // 人员管理相关路由
                // 在 personnel/admin 路由下添加子路由
                {
                path: 'personnel/admin',
                name: 'personnel-admin',
                component: () => import('../views/personnel/Admin.vue'),
                meta: {
                       title: '管理员管理'
                },
                children: [
                {
                      path: 'add',
                      name: 'admin-add',
                      component: () => import('../views/personnel/addAdmin.vue'),
                      meta: {
                      title: '新增管理员'
            }
        }
    ]
},
                {
                    path: 'personnel/maintenance',
                    name: 'personnel-maintenance',
                    component: () => import('@/views/personnel/Maintenance.vue'),
                    meta: {
                        title: '维修人员管理'
                    }
                },
                {
                    path: 'personnel/maintenance/records/:id',
                    name: 'MaintenanceRecord',
                    component: () => import('@/views/personnel/MaintenanceRecord.vue'),
                    meta: {
                        title: '维修记录详情'
                    }
                }
,
                {
                    path: 'personnel/user',
                    name: 'personnel-user',
                    component: () => import('../views/personnel/User.vue'),
                    meta: {
                        title: '用户管理'
                    }
                },
                // 片区相关路由
                {
                    path: 'area/urban',
                    name: 'area-urban',
                    component: () => import('../views/area/Urban.vue'),
                    meta: {
                        title: '城市片区'
                    }
                },
                {
                    path: 'equipment/water-maker/:id',
                    name: 'water-maker-detail',
                    component: () => import('../views/equipment/WaterMakerDetail.vue'),
                    meta: {
                        title: '制水设备详情'
                    }
                },
                {
                    path: 'area/campus',
                    name: 'area-campus',
                    component: () => import('../views/area/Campus.vue'),
                    meta: {
                        title: '校园片区'
                    }
                },
                // 个人信息路由
                {
                    path: 'profile',
                    name: 'profile',
                    component: () => import('../views/Profile.vue'),
                    meta: {
                        title: '个人信息',
                        requiresAuth: true
                    }
                }
            ]
        },
        {
            path: '/about',
            name: 'about',
            component: () => import('../views/AboutView.vue'),
            meta: {
                title: '关于',
                requiresAuth: false
            }
        },
      /*  // 404 页面
        {
            path: '/:pathMatch(.*)*',
            name: 'NotFound',
            component: () => import('../views/NotFound.vue'),
            meta: {
                title: '页面不存在',
                requiresAuth: false
            }
        }*/
    ]
})

// 路由守卫
router.beforeEach(async (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    // 设置页面标题
    const title = to.meta?.title as string || '校园直饮水管理系统'
    document.title = title

    // 获取认证状态
    const authStore = useAuthStore()

    // 初始化登录状态
    if (!authStore.isLoggedIn) {
        authStore.initialize()
    }

    // 判断是否需要认证
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    // 如果路由需要认证但用户未登录
    if (requiresAuth && !authStore.isLoggedIn) {
        // 重定向到登录页面，并保存当前想要访问的路径
        next({
            name: 'login',
            query: { redirect: to.fullPath }
        })
    }
    // 如果用户已登录但访问登录页面
    else if (to.name === 'login' && authStore.isLoggedIn) {
        // 检查是否有重定向路径
        const redirect = from.query.redirect as string || '/home'
        next(redirect)
    }
    // 其他情况正常放行
    else {
        next()
    }
})

// 路由后置守卫 - 可在这里添加一些统计或清理工作
router.afterEach((to: RouteLocationNormalized, from: RouteLocationNormalized) => {
    // 可以在这里添加页面访问统计等
    console.log(`路由跳转: ${from.fullPath} -> ${to.fullPath}`)
})

export default router