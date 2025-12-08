
<template>
  <div class="student-login-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-title">学生服务平台</div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 登录表单 -->
      <div class="login-form" v-if="!showRegister">
        <div class="welcome-text">
          <h2>欢迎回来</h2>
          <p>请登录您的账户</p>
        </div>

        <div class="form-group">
          <label for="studentId">学号</label>
          <input
              type="text"
              id="studentId"
              v-model="loginForm.studentId"
              placeholder="请输入学号"
              @keyup.enter="handleLogin"
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <div class="password-input">
            <input
                :type="showPassword ? 'text' : 'password'"
                id="password"
                v-model="loginForm.password"
                placeholder="请输入密码"
                @keyup.enter="handleLogin"
            />
            <button
                type="button"
                class="password-toggle"
                @click="showPassword = !showPassword"
            >
              <span v-if="showPassword">隐藏</span>
              <span v-else>显示</span>
            </button>
          </div>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" v-model="loginForm.rememberMe" />
            <span>记住我</span>
          </label>
          <button class="forgot-password" @click="handleForgotPassword">
            忘记密码?
          </button>
        </div>

        <button class="login-btn primary" @click="handleLogin" :disabled="loading">
          <span v-if="loading">登录中...</span>
          <span v-else>登录</span>
        </button>

        <div class="register-prompt">
          还没有账户？
          <button class="register-link" @click="showRegister = true">
            立即注册
          </button>
        </div>
      </div>

      <!-- 注册表单 -->
      <div class="register-form" v-else>
        <div class="welcome-text">
          <h2>创建账户</h2>
          <p>注册学生账户</p>
        </div>

        <div class="form-group">
          <label for="regStudentId">学号</label>
          <input
              type="text"
              id="regStudentId"
              v-model="registerForm.studentId"
              placeholder="请输入学号"
              :class="{ 'has-error': errors.studentId }"
          />
          <div v-if="errors.studentId" class="error-message">
            {{ errors.studentId }}
          </div>
        </div>

        <div class="form-group">
          <label for="regName">姓名</label>
          <input
              type="text"
              id="regName"
              v-model="registerForm.name"
              placeholder="请输入真实姓名"
              :class="{ 'has-error': errors.name }"
          />
          <div v-if="errors.name" class="error-message">
            {{ errors.name }}
          </div>
        </div>

        <div class="form-group">
          <label for="regPhone">手机号</label>
          <input
              type="tel"
              id="regPhone"
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              :class="{ 'has-error': errors.phone }"
          />
          <div v-if="errors.phone" class="error-message">
            {{ errors.phone }}
          </div>
        </div>

        <div class="form-group">
          <label for="regPassword">密码</label>
          <div class="password-input">
            <input
                :type="showRegPassword ? 'text' : 'password'"
                id="regPassword"
                v-model="registerForm.password"
                placeholder="请输入密码（6-20位）"
                :class="{ 'has-error': errors.password }"
            />
            <button
                type="button"
                class="password-toggle"
                @click="showRegPassword = !showRegPassword"
            >
              <span v-if="showRegPassword">隐藏</span>
              <span v-else>显示</span>
            </button>
          </div>
          <div v-if="errors.password" class="error-message">
            {{ errors.password }}
          </div>
        </div>

        <div class="form-group">
          <label for="regConfirmPassword">确认密码</label>
          <div class="password-input">
            <input
                :type="showConfirmPassword ? 'text' : 'password'"
                id="regConfirmPassword"
                v-model="registerForm.confirmPassword"
                placeholder="请再次输入密码"
                :class="{ 'has-error': errors.confirmPassword }"
            />
            <button
                type="button"
                class="password-toggle"
                @click="showConfirmPassword = !showConfirmPassword"
            >
              <span v-if="showConfirmPassword">隐藏</span>
              <span v-else>显示</span>
            </button>
          </div>
          <div v-if="errors.confirmPassword" class="error-message">
            {{ errors.confirmPassword }}
          </div>
        </div>

        <div class="form-group">
          <label for="regEmail">邮箱（选填）</label>
          <input
              type="email"
              id="regEmail"
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              :class="{ 'has-error': errors.email }"
          />
          <div v-if="errors.email" class="error-message">
            {{ errors.email }}
          </div>
        </div>

        <div class="agreement">
          <label>
            <input type="checkbox" v-model="registerForm.agreed" />
            <span>我已阅读并同意</span>
            <button class="agreement-link" @click="showAgreement">
              《用户协议》
            </button>
            和
            <button class="agreement-link" @click="showPrivacy">
              《隐私政策》
            </button>
          </label>
          <div v-if="errors.agreement" class="error-message">
            {{ errors.agreement }}
          </div>
        </div>

        <div class="action-buttons">
          <button class="register-btn primary" @click="handleRegister" :disabled="loading">
            <span v-if="loading">注册中...</span>
            <span v-else>注册</span>
          </button>
          <button class="register-btn secondary" @click="showRegister = false">
            返回登录
          </button>
        </div>
      </div>
    </div>

    <!-- 底部信息 -->
    <div class="footer">
      <!-- 这里只保留空白，没有文字内容 -->
    </div>
  </div>
</template>

<!-- 只修改 <script> 部分，<template> 和 <style> 保持不变 -->
<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authServices } from '@/services/authServices'  // 引入认证服务

const router = useRouter()

// 登录/注册切换
const showRegister = ref(false)

// 登录表单数据
const loginForm = reactive({
  studentId: '',
  password: '',
  rememberMe: false
})

// 注册表单数据
const registerForm = reactive({
  studentId: '',
  name: '',
  phone: '',
  password: '',
  confirmPassword: '',
  email: '',
  agreed: false
})

// 错误信息
const errors = reactive({
  studentId: '',
  name: '',
  phone: '',
  password: '',
  confirmPassword: '',
  email: '',
  agreement: ''
})

// 状态
const loading = ref(false)
const showPassword = ref(false)
const showRegPassword = ref(false)
const showConfirmPassword = ref(false)

// 表单验证规则
const validateLogin = () => {
  if (!loginForm.studentId.trim()) {
    alert('请输入学号')
    return false
  }
  if (!loginForm.password) {
    alert('请输入密码')
    return false
  }
  return true
}

const validateRegister = () => {
  let isValid = true
  Object.keys(errors).forEach(key => errors[key] = '')

  if (!registerForm.studentId.trim()) {
    errors.studentId = '请输入学号'
    isValid = false
  }

  if (!registerForm.name.trim()) {
    errors.name = '请输入姓名'
    isValid = false
  }

  if (!registerForm.phone.trim()) {
    errors.phone = '请输入手机号'
    isValid = false
  }

  if (!registerForm.password) {
    errors.password = '请输入密码'
    isValid = false
  } else if (registerForm.password.length < 6) {
    errors.password = '密码长度至少6位'
    isValid = false
  }

  if (registerForm.password !== registerForm.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  if (!registerForm.agreed) {
    errors.agreement = '请阅读并同意用户协议和隐私政策'
    isValid = false
  }

  return isValid
}

// 真实的登录处理
const handleLogin = async () => {
  if (!validateLogin()) return

  loading.value = true

  try {
    console.log('开始学生登录:', loginForm.studentId)

    // 调用后端登录接口
    const result = await authServices.studentLogin(
        loginForm.studentId,
        loginForm.password
    )

    console.log('登录响应:', result)

    if (result.code === 200) {
      // 登录成功
      const loginData = result.data

      // 保存用户信息到本地存储
      localStorage.setItem('token', loginData.token)
      localStorage.setItem('userId', loginData.userId)
      localStorage.setItem('username', loginData.username)
      localStorage.setItem('userType', loginData.userType)
      localStorage.setItem('studentId', loginForm.studentId)

      // 如果选择了记住我，保存登录信息
      if (loginForm.rememberMe) {
        localStorage.setItem('rememberedStudentId', loginForm.studentId)
      } else {
        localStorage.removeItem('rememberedStudentId')
      }

      alert('登录成功！')

      // 跳转到首页
      router.push('/home')
    } else {
      // 登录失败
      alert('登录失败: ' + (result.message || '未知错误'))
    }
  } catch (error) {
    console.error('登录过程异常:', error)

    // 处理错误信息
    if (error.message && error.message.includes('Network Error')) {
      alert('网络连接失败，请检查网络或服务器状态')
    } else if (error.message && error.message.includes('timeout')) {
      alert('请求超时，请稍后重试')
    } else if (error.code === 500) {
      alert('服务器内部错误，请联系管理员')
    } else {
      alert('登录失败: ' + (error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}


// 注册处理
// 在 <script setup> 部分修改 handleRegister 函数
const handleRegister = async () => {
  if (!validateRegister()) return

  loading.value = true

  try {
    // 构建注册数据（匹配后端RegisterRequest结构）
    const registerData = {
      studentId: registerForm.studentId.trim(),
      name: registerForm.name.trim(),
      password: registerForm.password,
      // 注意：后端RegisterRequest目前不支持phone和email字段
      // 如果需要这些字段，需要扩展后端的RegisterRequest和User实体
    }

    console.log('前端注册数据:', registerData)

    // 调用注册接口
    const result = await authServices.studentRegister(registerData)

    console.log('注册完整响应:', result)

    // 根据后端ResultVO格式处理响应
    if (result && result.code === 200) {
      // 注册成功
      alert('注册成功！请使用您的学号和密码登录')
      showRegister.value = false

      // 清空注册表单
      Object.keys(registerForm).forEach(key => {
        if (key !== 'agreed') {
          registerForm[key] = ''
        }
      })
      registerForm.agreed = false

      // 清空错误信息
      Object.keys(errors).forEach(key => errors[key] = '')
    } else {
      // 注册失败，显示后端返回的错误信息
      const errorMsg = result?.message || '注册失败，请稍后重试'
      alert('注册失败: ' + errorMsg)
    }
  } catch (error) {
    console.error('注册过程异常:', error)

    // 处理错误信息
    let errorMessage = '注册失败，请稍后重试'

    if (error.code === 400) {
      errorMessage = error.message || '请求参数错误，请检查填写的信息'
    } else if (error.code === 409) {
      errorMessage = error.message || '用户已存在'
    } else if (error.message) {
      // 如果是对象，提取message字段
      if (typeof error.message === 'object' && error.message.message) {
        errorMessage = error.message.message
      } else if (typeof error.message === 'string') {
        errorMessage = error.message
      }
    }

    alert('注册失败: ' + errorMessage)
  } finally {
    loading.value = false
  }
}
// 其他辅助函数保持不变
const handleForgotPassword = () => {
  alert('请联系管理员重置密码：admin@example.com')
}

const showAgreement = () => {
  alert('用户协议内容...')
}

const showPrivacy = () => {
  alert('隐私政策内容...')
}

// 页面加载时检查是否有记住的学号
const initRememberedUser = () => {
  const rememberedStudentId = localStorage.getItem('rememberedStudentId')
  if (rememberedStudentId) {
    loginForm.studentId = rememberedStudentId
    loginForm.rememberMe = true
  }
}

// 初始化
initRememberedUser()
</script>

<style scoped>
.student-login-page {
  width: 375px;
  height: 667px;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow-y: auto;
}

/* 顶部标题栏 */
.header {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 16px;
  color: white;
  text-align: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 24px 16px;
  overflow-y: auto;
}

/* 欢迎文本 */
.welcome-text {
  text-align: center;
  margin-bottom: 32px;
}

.welcome-text h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.welcome-text p {
  font-size: 14px;
  color: #666;
}

/* 表单组 */
.form-group {
  margin-bottom: 20px;
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
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
  background: white;
}

.form-group input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-group input.has-error {
  border-color: #ff4d4f;
}

.form-group input::placeholder {
  color: #999;
}

/* 密码输入框 */
.password-input {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #1890ff;
  font-size: 12px;
  cursor: pointer;
  padding: 4px;
}

/* 错误消息 */
.error-message {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 14px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  cursor: pointer;
}

.remember-me input {
  width: 16px;
  height: 16px;
}

.forgot-password {
  background: none;
  border: none;
  color: #1890ff;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
}

/* 登录/注册按钮 */
.login-btn,
.register-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 16px;
}

.login-btn.primary,
.register-btn.primary {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
}

.login-btn.secondary,
.register-btn.secondary {
  background: white;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.login-btn:disabled,
.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-btn:hover:not(:disabled),
.register-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

/* 注册提示 */
.register-prompt {
  text-align: center;
  font-size: 14px;
  color: #666;
  margin-top: 24px;
}

.register-link {
  background: none;
  border: none;
  color: #1890ff;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
  font-weight: 500;
}

/* 协议同意 */
.agreement {
  margin: 24px 0;
  font-size: 12px;
  color: #666;
}

.agreement label {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  cursor: pointer;
}

.agreement input {
  margin-top: 2px;
  flex-shrink: 0;
}

.agreement-link {
  background: none;
  border: none;
  color: #1890ff;
  padding: 0;
  cursor: pointer;
}

/* 注册按钮组 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 底部信息 - 保持布局但无内容 */
.footer {
  padding: 8px;
  text-align: center;
  background: white;
  border-top: 1px solid #e8e8e8;
}

/* 响应式调整 */
@media (max-width: 420px) {
  .student-login-page {
    width: 100%;
    height: 100vh;
  }

  .main-content {
    padding: 20px 12px;
  }

  .welcome-text {
    margin-bottom: 24px;
  }

  .welcome-text h2 {
    font-size: 22px;
  }

  .login-btn,
  .register-btn {
    padding: 12px;
  }

  .footer {
    padding: 4px;
  }
}
</style>
