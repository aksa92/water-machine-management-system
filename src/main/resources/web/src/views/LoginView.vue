<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">校园直饮水管理系统</h1>
        <p class="login-subtitle">请登录您的账户</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <!-- 用户类型选择 -->
        <div class="form-group">
          <label for="userType">用户类型</label>
          <select
              id="userType"
              v-model="loginForm.userType"
              class="form-input"
              :disabled="loading"
              required
          >
            <option value="admin">管理员</option>
            <option value="user">普通用户</option>
            <option value="repairer">维修人员</option>
          </select>
        </div>

        <div class="form-group">
          <label for="username">用户名</label>
          <input
              id="username"
              v-model="loginForm.username"
              type="text"
              placeholder="请输入用户名"
              required
              class="form-input"
              :disabled="loading"
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
              id="password"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              required
              class="form-input"
              :disabled="loading"
          />
        </div>

        <div class="form-options">
          <label class="checkbox-label">
            <input type="checkbox" v-model="loginForm.rememberMe" />
            <span class="checkbox-text">记住我</span>
          </label>
          <a href="#" class="forgot-password">忘记密码？</a>
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <!-- 调试信息 -->
        <div v-if="debugInfo" class="debug-info">
          <h4>调试信息:</h4>
          <pre>{{ debugInfo }}</pre>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const debugInfo = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  userType: 'admin', // 添加默认用户类型
  rememberMe: false
})

// 在 handleLogin 方法中
const handleLogin = async () => {
  if (!loginForm.username.trim() || !loginForm.password.trim()) {
    alert('请输入用户名和密码')
    return
  }

  loading.value = true

  try {
    // 调用真实的登录接口，传递完整的参数
    await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
      userType: loginForm.userType, // 添加用户类型参数
      rememberMe: loginForm.rememberMe
    })

    // 登录成功，处理重定向
    const redirect = router.currentRoute.value.query.redirect as string
    if (redirect) {
      router.push(redirect)
    } else {
      router.push('/home')
    }

  } catch (error: any) {
    console.error('登录失败:', error)
    // 更友好的错误提示
    const errorMessage = error.message.includes('Network')
        ? '网络连接失败，请检查网络设置'
        : error.message.includes('401')
            ? '用户名或密码错误'
            : error.message || '登录失败，请稍后重试'

    alert(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.login-header {
  padding: 30px 30px 20px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
}

.login-title {
  margin: 0 0 10px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.login-subtitle {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.login-form {
  padding: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  font-size: 14px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #666;
}

.checkbox-label input {
  margin-right: 6px;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  transition: color 0.3s;
}

.forgot-password:hover {
  color: #764ba2;
  text-decoration: underline;
}

.login-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.login-button:disabled {
  opacity: 0.8;
  cursor: not-allowed;
}

.debug-info {
  margin-top: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  font-size: 12px;
  font-family: 'Courier New', monospace;
}

.debug-info h4 {
  margin: 0 0 8px 0;
  color: #6c757d;
}

.debug-info pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
