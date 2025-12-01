<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')

const handleLogin = () => {
  if (username.value && password.value) {
    console.log('登录成功，跳转到首页')
    router.push('/home')
  } else {
    alert('请输入账号和密码')
  }
}
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
          />
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            class="form-input"
          />
        </div>

        <button type="submit" class="login-button">登录</button>
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
  margin-top: auto;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.login-button:active {
  transform: translateY(0);
}
</style>
