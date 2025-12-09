// src/main.js
import './assets/main.css'
import './assets/mobile.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './router/permission' // 确保引入了路由守卫

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
