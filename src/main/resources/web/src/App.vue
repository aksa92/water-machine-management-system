<template>
  <div id="app">
    <!-- 全局提示组件（可复用） -->
    <div v-if="showToast" class="toast" :class="toastType">
      {{ toastMessage }}
    </div>

    <!-- 路由视图出口 -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 全局提示状态管理
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref<'success' | 'error' | 'warning'>('success')

// 提供全局提示方法
const showGlobalToast = (message: string, type: 'success' | 'error' | 'warning' = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true

  // 3秒后自动关闭
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

// 将提示方法挂载到全局
const app = document.querySelector('#app')
if (app) {
  (app as any).showToast = showGlobalToast
}

// 路由守卫补充：处理登录状态过期等场景
const router = useRouter()
onMounted(() => {
  router.beforeEach((to, from, next) => {
    // 已经登录但访问登录页时重定向到首页
    const isLoggedIn = localStorage.getItem('token') || sessionStorage.getItem('token')
    if (to.path === '/login' && isLoggedIn) {
      next('/home')
    } else {
      next()
    }
  })
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f8f9fa;
  line-height: 1.6;
  color: #333;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 全局组件样式 */
.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
  transition: box-shadow 0.3s ease;
}

.card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stats-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  text-align: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

/* 提示框样式 */
.alert-warning {
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 6px;
  padding: 12px 16px;
  color: #856404;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 全局提示样式 */
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 4px;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  transition: all 0.3s ease;
  animation: slideIn 0.3s forwards;
}

.toast.success {
  background-color: #52c41a;
}

.toast.error {
  background-color: #f5222d;
}

.toast.warning {
  background-color: #faad14;
}

/* 动画效果 */
@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 基础按钮样式 */
.btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  font-weight: 500;
}

.btn-primary {
  background-color: #42b983;
  color: white;
}

.btn-primary:hover {
  background-color: #359e75;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #666;
}

.btn-secondary:hover {
  background-color: #e8e8e8;
}

/* 表格基础样式 */
.base-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

.base-table th,
.base-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.base-table th {
  background-color: #fafafa;
  font-weight: 600;
  color: #666;
}

.base-table tr:hover {
  background-color: #f9f9f9;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .card, .stats-card {
    padding: 16px;
  }

  .toast {
    width: calc(100% - 40px);
    text-align: center;
  }
}
</style>