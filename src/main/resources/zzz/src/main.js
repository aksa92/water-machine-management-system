// main.js
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import '@/router/permission'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 挂载前初始化
app.mount('#app')

// 全局属性（可选）
app.config.globalProperties.$auth = {
  isAuthenticated: () => {
    const token = localStorage.getItem('token')
    return !!token
  },
  getUserType: () => localStorage.getItem('userType')
}
