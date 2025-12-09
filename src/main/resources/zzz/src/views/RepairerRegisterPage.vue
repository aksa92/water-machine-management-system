<!-- src/views/RepairerRegisterPage.vue -->
<template>
  <div class="register-page">
    <!-- 顶部返回按钮 -->
    <div class="header">
      <button class="back-btn" @click="goBack">
        <span>←</span> 返回
      </button>
      <h1 class="title">维修人员注册</h1>
    </div>

    <!-- 注册表单 -->
    <div class="form-container">
      <form @submit.prevent="handleRegister">
        <!-- 用户名 -->
        <div class="form-group">
          <label>用户名 *</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="请输入用户名"
            :class="{ 'error': errors.username }"
            :disabled="loading"
          />
          <div v-if="errors.username" class="error-message">
            {{ errors.username }}
          </div>
        </div>

        <!-- 维修人员ID -->
        <div class="form-group">
          <label>维修人员ID *</label>
          <input
            v-model="form.repairmanId"
            type="text"
            placeholder="请输入维修人员ID"
            :class="{ 'error': errors.repairmanId }"
            :disabled="loading"
          />
          <div v-if="errors.repairmanId" class="error-message">
            {{ errors.repairmanId }}
          </div>
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <label>密码 *</label>
          <div class="password-wrapper">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码（6-16位字母或数字）"
              :class="{ 'error': errors.password }"
              :disabled="loading"
            />
            <button
              type="button"
              class="toggle-password"
              @click="showPassword = !showPassword"
              :disabled="loading"
            >
              {{ showPassword ? '隐藏' : '显示' }}
            </button>
          </div>
          <div v-if="errors.password" class="error-message">
            {{ errors.password }}
          </div>
        </div>

        <!-- 确认密码 -->
        <div class="form-group">
          <label>确认密码 *</label>
          <div class="password-wrapper">
            <input
              v-model="form.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="请再次输入密码"
              :class="{ 'error': errors.confirmPassword }"
              :disabled="loading"
            />
            <button
              type="button"
              class="toggle-password"
              @click="showConfirmPassword = !showConfirmPassword"
              :disabled="loading"
            >
              {{ showConfirmPassword ? '隐藏' : '显示' }}
            </button>
          </div>
          <div v-if="errors.confirmPassword" class="error-message">
            {{ errors.confirmPassword }}
          </div>
        </div>

        <!-- 注册按钮 -->
        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '注册中...' : '立即注册' }}
        </button>

        <!-- 已有账号 -->
        <div class="login-link">
          已有账号？
          <button type="button" @click="goToLogin" :disabled="loading">
            去登录
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authServices } from '@/services/authServices'

const router = useRouter()

// 表单数据
const form = reactive({
  username: '',
  repairmanId: '',
  password: '',
  confirmPassword: ''
})

// 错误信息
const errors = reactive({
  username: '',
  repairmanId: '',
  password: '',
  confirmPassword: ''
})

// 状态
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// 表单验证
const validateForm = () => {
  // 清空错误信息
  Object.keys(errors).forEach(key => errors[key] = '')

  let isValid = true

  // 用户名验证
  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    isValid = false
  } else if (form.username.trim().length < 2) {
    errors.username = '用户名至少2个字符'
    isValid = false
  }

  // 维修人员ID验证
  if (!form.repairmanId.trim()) {
    errors.repairmanId = '请输入维修人员ID'
    isValid = false
  }

  // 密码验证
  if (!form.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (form.password.length < 6 || form.password.length > 16) {
    errors.password = '密码长度应为6-16位'
    isValid = false
  } else if (!/^[a-zA-Z0-9]+$/.test(form.password)) {
    errors.password = '密码只能包含字母和数字'
    isValid = false
  }

  // 确认密码验证
  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

// 注册处理
const handleRegister = async () => {
  if (!validateForm()) return

  loading.value = true

  try {
    // 准备注册数据
    const registerData = {
      username: form.username.trim(),
      password: form.password,
      repairmanId: form.repairmanId.trim()
    }

    // 调用注册接口
    const result = await authServices.repairerRegister(registerData)

    // 处理响应结果
    if (result && result.code === 200) {
      alert('注册成功！请使用用户名和密码登录')
      // 清空表单
      form.username = ''
      form.repairmanId = ''
      form.password = ''
      form.confirmPassword = ''
      // 跳转到登录页
      router.push('/')
    } else {
      alert('注册失败: ' + (result?.message || '未知错误'))
    }
  } catch (error) {
    // 处理错误
    console.error('注册过程错误:', error)

    let errorMessage = '注册失败'

    if (error.message) {
      if (error.message.includes('用户名已存在')) {
        errorMessage = '用户名已存在，请更换用户名'
      } else if (error.message.includes('维修人员ID已被注册')) {
        errorMessage = '维修人员ID已被注册'
      } else {
        errorMessage = error.message
      }
    }

    alert(errorMessage)
  } finally {
    loading.value = false
  }
}

// 跳转函数
const goBack = () => {
  router.back()
}

const goToLogin = () => {
  router.push('/')
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 16px;
  color: white;
}

.back-btn {
  background: none;
  border: none;
  color: white;
  font-size: 16px;
  padding: 8px 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  margin: 16px 0 0;
}

.form-container {
  padding: 30px 20px;
  background: white;
  margin-top: 20px;
  border-radius: 12px 12px 0 0;
  min-height: calc(100vh - 150px);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-group input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 15px;
  box-sizing: border-box;
  background: #fafafa;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input.error {
  border-color: #ff4d4f;
}

.form-group input::placeholder {
  color: #999;
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.password-wrapper {
  position: relative;
}

.toggle-password {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #667eea;
  font-size: 12px;
  cursor: pointer;
  padding: 4px 8px;
}

.toggle-password:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 30px;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.login-link button {
  background: none;
  border: none;
  color: #667eea;
  font-weight: 500;
  padding: 0 4px;
  cursor: pointer;
}

.login-link button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
