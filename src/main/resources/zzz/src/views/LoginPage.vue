<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authServices } from '@/services/authServices'

const router = useRouter()
const usertype = ref('repairer')
const username = ref('')
const password = ref('')
const loading = ref(false)

// 在 handleLogin 函数中添加更多日志
// 在 LoginPage.vue 中添加更详细的调试
const handleLogin = async () => {
  console.log('开始登录流程');
  if (!username.value || !password.value) {
    alert('请输入账号和密码');
    return;
  }

  loading.value = true;
  try {
    console.log('调用 authServices.login', {
      username: username.value,
      password: password.value,
      userType: usertype.value
    });

    const loginPromise = authServices.login({
      username: username.value,
      password: password.value,
      userType: usertype.value
    });
    console.log('loginPromise 创建成功:', loginPromise);

    const result = await loginPromise;

    // 添加对结果的验证
    if (result && result.code === 200) {
      console.log('登录成功:', result);

      // 保存登录信息到本地存储
      localStorage.setItem('token', result.data.token)
      localStorage.setItem('userId', result.data.userId)
      localStorage.setItem('username', result.data.username)
      localStorage.setItem('userType', result.data.userType)
      localStorage.setItem('repairmanId', username.value)

      alert('登录成功！')
      router.push('/home');
    } else {
      alert('登录失败: ' + (result?.message || '未知错误'));
    }
  } catch (error) {
    console.error('登录过程异常:', error);
    console.error('错误类型:', typeof error);
    console.error('错误堆栈:', error.stack);
    alert('登录失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 跳转到注册页面
const goToRegister = () => {
  router.push('/repairer-register');
};
</script>

<template>
  <div class="login-background">
    <div class="login-header">
      <h1 class="login-title">运维人员登录</h1>
      <p class="welcome-text">欢迎使用运维APP</p>
    </div>

    <div class="login-box">
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label class="form-label">账号</label>
          <input
            v-model="username"
            type="text"
            placeholder="请输入账号"
            class="form-input"
            :disabled="loading"
          />
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            class="form-input"
            :disabled="loading"
          />
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <!-- 添加注册按钮 -->
        <div class="register-section">
          <p class="register-text">还没有账户？</p>
          <button type="button" class="register-button" @click="goToRegister" :disabled="loading">
            立即注册
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-background {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  padding: 60px 30px 40px;
  box-sizing: border-box;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
  color: white;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.welcome-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  margin: 0;
}

.login-box {
  background: white;
  border-radius: 12px;
  padding: 30px 25px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  flex: 1;
}

.login-form {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.form-group {
  margin-bottom: 25px;
}

.form-label {
  display: block;
  color: #333;
  font-weight: 500;
  font-size: 15px;
  margin-bottom: 10px;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 15px;
  transition: all 0.3s;
  box-sizing: border-box;
  background: #fafafa;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input::placeholder {
  color: #999;
  font-size: 14px;
}

.form-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 20px;
  margin-bottom: 15px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

/* 注册部分样式 */
.register-section {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.register-text {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.register-button {
  width: 100%;
  padding: 12px;
  background: white;
  color: #667eea;
  border: 1px solid #667eea;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.register-button:hover:not(:disabled) {
  background: #f8f9ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.register-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.register-button:active:not(:disabled) {
  transform: translateY(0);
}
</style>
