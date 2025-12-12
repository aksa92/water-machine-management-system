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

    <!-- 注册表单 - 使用滚动容器包裹 -->
    <div class="scroll-container">
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

          <!-- 维修人员姓名 -->
          <div class="form-group">
            <label>维修人员姓名 *</label>
            <input
              v-model="form.repairmanName"
              type="text"
              placeholder="请输入维修人员姓名"
              :class="{ 'error': errors.repairmanName }"
              :disabled="loading"
            />
            <div v-if="errors.repairmanName" class="error-message">
              {{ errors.repairmanName }}
            </div>
          </div>

          <!-- 手机号 -->
          <div class="form-group">
            <label>手机号 *</label>
            <input
              v-model="form.phone"
              type="tel"
              placeholder="请输入手机号"
              :class="{ 'error': errors.phone }"
              :disabled="loading"
            />
            <div v-if="errors.phone" class="error-message">
              {{ errors.phone }}
            </div>
          </div>

          <!-- 负责区域 -->
          <div class="form-group">
            <label>负责区域 *</label>
            <input
              v-model="form.areaId"
              type="text"
              placeholder="请输入负责区域ID"
              :class="{ 'error': errors.areaId }"
              :disabled="loading"
            />
            <div v-if="errors.areaId" class="error-message">
              {{ errors.areaId }}
            </div>
          </div>

          <!-- 技能描述 -->
          <div class="form-group">
            <label>技能描述 *</label>
            <textarea
              v-model="form.skills"
              placeholder="请输入技能描述"
              :class="{ 'error': errors.skills }"
              :disabled="loading"
              rows="3"
            ></textarea>
            <div v-if="errors.skills" class="error-message">
              {{ errors.skills }}
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authServices } from '@/services/authServices'

const router = useRouter()

// 表单数据 - 补全所有字段
const form = reactive({
  username: '',
  repairmanId: '',
  repairmanName: '',
  phone: '',
  areaId: '',
  skills: '',
  password: '',
  confirmPassword: '',
  userType: 'repairman'
})

// 错误信息 - 补全所有字段
const errors = reactive({
  username: '',
  repairmanId: '',
  repairmanName: '',
  phone: '',
  areaId: '',
  skills: '',
  password: '',
  confirmPassword: ''
})

// 状态
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const validateForm = () => {
  // 清空错误信息
  Object.keys(errors).forEach(key => errors[key] = '')

  let isValid = true

  // 统一使用安全的方式获取和验证字段值
  const getValue = (field) => {
    const value = form[field]
    return value === null || value === undefined ? '' : String(value).trim()
  }

  // 用户名验证
  const username = getValue('username')
  if (!username) {
    errors.username = '请输入用户名'
    isValid = false
  } else if (username.length < 2) {
    errors.username = '用户名至少2个字符'
    isValid = false
  }

  // 维修人员ID验证
  const repairmanId = getValue('repairmanId')
  if (!repairmanId) {
    errors.repairmanId = '请输入维修人员ID'
    isValid = false
  }

  // 维修人员姓名验证
  const repairmanName = getValue('repairmanName')
  if (!repairmanName) {
    errors.repairmanName = '请输入维修人员姓名'
    isValid = false
  }

  // 手机号验证
  const phone = getValue('phone')
  if (!phone) {
    errors.phone = '请输入手机号'
    isValid = false
  } else if (!/^1[3-9]\d{9}$/.test(phone)) {
    errors.phone = '手机号格式不正确'
    isValid = false
  }

  // 负责区域验证
  const areaId = getValue('areaId')
  if (!areaId) {
    errors.areaId = '请输入负责区域'
    isValid = false
  }

  // 技能描述验证
  const skills = getValue('skills')
  if (!skills) {
    errors.skills = '请输入技能描述'
    isValid = false
  }

  // 密码验证
  const password = form.password || ''
  if (!password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (password.length < 6 || password.length > 16) {
    errors.password = '密码长度应为6-16位'
    isValid = false
  } else if (!/^[a-zA-Z0-9]+$/.test(password)) {
    errors.password = '密码只能包含字母和数字'
    isValid = false
  }

  // 确认密码验证
  const confirmPassword = form.confirmPassword || ''
  if (!confirmPassword) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (password !== confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

// 重置表单
const resetForm = () => {
  // 重置表单数据
  form.username = ''
  form.repairmanId = ''
  form.repairmanName = ''
  form.phone = ''
  form.areaId = ''
  form.skills = ''
  form.password = ''
  form.confirmPassword = ''
  form.userType = 'repairman'

  // 重置错误信息
  Object.keys(errors).forEach(key => errors[key] = '')

  // 重置显示状态
  showPassword.value = false
  showConfirmPassword.value = false
}

// 注册处理
const handleRegister = async () => {
  if (!validateForm()) return

  loading.value = true

  try {
    // 安全地准备注册数据
    const registerData = {
      username: (form.username || '').trim(),
      password: form.password || '',
      repairmanId: (form.repairmanId || '').trim(),
      repairmanName: (form.repairmanName || '').trim(),
      phone: (form.phone || '').trim(),
      areaId: (form.areaId || '').trim(),
      skills: (form.skills || '').trim(),
      userType: form.userType || 'repairman'
    }

    // 调用注册接口
    const result = await authServices.repairerRegister(registerData)

    // 处理响应结果
    if (result && result.code === 200) {
      alert('注册成功！请使用用户名和密码登录')
      resetForm()  // 使用重置函数
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
/* 基础页面样式 */
.register-page {
  height: 100%; /* 使用100%而不是100vh，避免全局冲突 */
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 头部样式 */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 16px;
  color: white;
  position: relative;
  z-index: 2;
  flex-shrink: 0; /* 防止头部被压缩 */
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

/* 滚动容器 - 解决内容显示不全的核心 */
.scroll-container {
  flex: 1;
  overflow-y: auto; /* 允许垂直滚动 */
  -webkit-overflow-scrolling: touch; /* 优化移动端滚动体验 */
  position: relative;
  min-height: 0; /* 关键：允许flex容器内的滚动 */
}

/* 表单容器 */
.form-container {
  padding: 30px 20px 40px; /* 调整底部内边距 */
  background: white;
  border-radius: 12px 12px 0 0;
  margin-top: -10px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  min-height: fit-content; /* 确保容器高度适应内容 */
}

/* 表单样式 */
form {
  display: flex;
  flex-direction: column;
}

/* 表单项样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group textarea {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 15px;
  box-sizing: border-box;
  background: #fafafa;
  resize: vertical;
  max-height: 200px;
  min-height: 80px;
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

/* 密码框样式 */
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

/* 错误信息 */
.error-message {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
  min-height: 18px; /* 保持错误信息区域高度稳定 */
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 24px;
  margin-bottom: 16px;
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

/* 登录链接 */
.login-link {
  text-align: center;
  margin-top: 8px;
  color: #666;
  font-size: 14px;
  padding-bottom: 10px; /* 确保底部有足够空间 */
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

/* 适配小屏幕设备 */
@media (max-height: 667px) {
  .header {
    padding: 16px;
  }

  .form-container {
    padding: 20px 16px 30px;
  }

  .form-group {
    margin-bottom: 16px;
  }

  .submit-btn {
    padding: 12px;
    margin-top: 20px;
    margin-bottom: 12px;
  }

  .form-group textarea {
    min-height: 70px;
    padding: 12px 14px;
  }

  .form-group input {
    padding: 12px 14px;
  }
}

/* 防止iOS Safari的弹性滚动问题 */
@supports (-webkit-touch-callout: none) {
  .scroll-container {
    overflow-y: scroll;
    -webkit-overflow-scrolling: touch;
  }
}
</style>
