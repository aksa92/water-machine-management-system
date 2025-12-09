<!-- src/views/profile/Profile.vue -->
<template>
  <div class="profile-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>个人信息</h2>
      <div class="breadcrumb">校园矿化水平台 / 个人中心 / 个人信息</div>
    </div>

    <div class="profile-container">
      <!-- 左侧头像和基本信息 -->
      <div class="profile-sidebar">
        <div class="avatar-container">
          <img :src="userInfo.avatar || defaultAvatar" alt="用户头像" class="user-avatar">
          <button class="change-avatar-btn" @click="triggerAvatarUpload">更换头像</button>
          <input 
            type="file" 
            ref="avatarInput" 
            class="avatar-input" 
            accept="image/*"
            @change="handleAvatarUpload"
          >
        </div>

        <div class="basic-info">
          <div class="info-item">
            <span class="label">用户名：</span>
            <span class="value">{{ userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">真实姓名：</span>
            <span class="value">{{ userInfo.realName }}</span>
          </div>
          <div class="info-item">
            <span class="label">身份：</span>
            <span class="value role-tag">{{ formatRole(userInfo.role) }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系电话：</span>
            <span class="value">{{ userInfo.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">账号状态：</span>
            <span class="value status-tag" :class="userInfo.status">
              {{ userInfo.status === 'active' ? '启用' : '禁用' }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">最后登录：</span>
            <span class="value">{{ formatDate(userInfo.lastLoginTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧信息编辑和密码修改 -->
      <div class="profile-content">
        <!-- 个人信息编辑表单 -->
        <div class="profile-card">
          <div class="card-header">
            <h3>编辑个人信息</h3>
          </div>
          <div class="card-body">
            <form @submit.prevent="updateProfile">
              <div class="form-row">
                <div class="form-item">
                  <label>真实姓名：</label>
                  <input 
                    type="text" 
                    v-model="profileForm.realName" 
                    placeholder="请输入真实姓名"
                    required
                  >
                </div>
                <div class="form-item">
                  <label>联系电话：</label>
                  <input 
                    type="tel" 
                    v-model="profileForm.phone" 
                    placeholder="请输入联系电话"
                    pattern="^1[3-9]\d{9}$"
                  >
                </div>
              </div>
              
              <div class="form-row">
                <div class="form-item full-width">
                  <label>备注信息：</label>
                  <textarea 
                    v-model="profileForm.remark" 
                    placeholder="请输入备注信息（选填）"
                    rows="3"
                  ></textarea>
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-save">保存修改</button>
              </div>
            </form>
          </div>
        </div>

        <!-- 密码修改表单 -->
        <div class="profile-card password-card">
          <div class="card-header">
            <h3>修改密码</h3>
          </div>
          <div class="card-body">
            <form @submit.prevent="changePassword">
              <div class="form-row">
                <div class="form-item full-width">
                  <label>当前密码：</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.oldPassword" 
                    placeholder="请输入当前密码"
                    required
                  >
                </div>
              </div>
              
              <div class="form-row">
                <div class="form-item full-width">
                  <label>新密码：</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.newPassword" 
                    placeholder="请输入新密码（至少8位，包含字母和数字）"
                    pattern="^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$"
                    required
                  >
                </div>
              </div>
              
              <div class="form-row">
                <div class="form-item full-width">
                  <label>确认新密码：</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.confirmPassword" 
                    placeholder="请再次输入新密码"
                    required
                  >
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-change-pwd">修改密码</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作成功提示 -->
    <div class="toast" v-if="showToast">
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
// 若使用Element Plus，可替换为原生提示

// 默认头像
const defaultAvatar = 'images/avatar/default-avatar.png'

// 用户信息接口
interface UserInfo {
  id: string
  username: string
  realName: string
  role: 'student' | 'teacher' | 'visitor' | 'admin' | 'maintenance'
  phone?: string
  avatar?: string
  status: 'active' | 'disabled'
  lastLoginTime: string
  remark?: string
}

// 响应式数据
const avatarInput = ref<HTMLInputElement | null>(null)
const showToast = ref(false)
const toastMessage = ref('')

// 用户信息（实际项目中从接口获取）
const userInfo = ref<UserInfo>({
  id: '1',
  username: 'admin01',
  realName: '管理员',
  role: 'admin',
  phone: '13800138000',
  avatar: '',
  status: 'active',
  lastLoginTime: '2025-12-05 09:23:45',
  remark: '系统超级管理员'
})

// 个人信息表单
const profileForm = reactive({
  realName: '',
  phone: '',
  remark: ''
})

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 初始化表单数据
onMounted(() => {
  profileForm.realName = userInfo.value.realName
  profileForm.phone = userInfo.value.phone || ''
  profileForm.remark = userInfo.value.remark || ''
})

// 格式化角色显示
const formatRole = (role: string) => {
  const roleMap: Record<string, string> = {
    student: '学生',
    teacher: '老师',
    visitor: '游客',
    admin: '管理员',
    maintenance: '维修人员'
  }
  return roleMap[role] || '未知身份'
}

// 格式化日期显示
const formatDate = (dateStr: string) => {
  if (!dateStr) return '暂无记录'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 触发头像上传
const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

// 处理头像上传
const handleAvatarUpload = (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    // 验证文件类型和大小
    const isImage = file.type.startsWith('image/')
    const isLt2M = file.size / 1024 / 1024 < 2

    if (!isImage) {
      showToastMessage('请上传图片格式的文件！')
      return
    }
    
    if (!isLt2M) {
      showToastMessage('头像大小不能超过2MB！')
      return
    }

    // 预览并上传头像（实际项目中调用上传接口）
    const reader = new FileReader()
    reader.onload = (e) => {
      userInfo.value.avatar = e.target?.result as string
      showToastMessage('头像上传成功！')
    }
    reader.readAsDataURL(file)

    // 清空input值，以便重新选择同一文件
    target.value = ''
  }
}

// 更新个人信息
const updateProfile = () => {
  // 表单验证
  if (!profileForm.realName.trim()) {
    showToastMessage('真实姓名不能为空！')
    return
  }

  // 手机号验证
  if (profileForm.phone && !/^1[3-9]\d{9}$/.test(profileForm.phone)) {
    showToastMessage('请输入正确的手机号码！')
    return
  }

  // 更新用户信息（实际项目中调用接口）
  userInfo.value.realName = profileForm.realName
  userInfo.value.phone = profileForm.phone
  userInfo.value.remark = profileForm.remark

  showToastMessage('个人信息修改成功！')
}

// 修改密码
const changePassword = () => {
  // 表单验证
  if (!passwordForm.oldPassword) {
    showToastMessage('请输入当前密码！')
    return
  }

  if (passwordForm.newPassword.length < 8) {
    showToastMessage('新密码长度不能少于8位！')
    return
  }

  if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/.test(passwordForm.newPassword)) {
    showToastMessage('新密码必须包含字母和数字！')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showToastMessage('两次输入的新密码不一致！')
    return
  }

  // 调用密码修改接口（实际项目中实现）
  showToastMessage('密码修改成功，请重新登录！')
  
  // 重置密码表单
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 显示提示消息
const showToastMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
  
  // 3秒后关闭提示
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}
</script>

<style scoped>
/* 基础样式 */
.profile-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.breadcrumb {
  color: #666;
  font-size: 14px;
}

/* 布局容器 */
.profile-container {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

/* 左侧侧边栏 */
.profile-sidebar {
  width: 300px;
  flex-shrink: 0;
}

.avatar-container {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.user-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 12px;
}

.change-avatar-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.change-avatar-btn:hover {
  background: #556cd6;
}

.avatar-input {
  display: none;
}

.basic-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  width: 80px;
  color: #666;
  flex-shrink: 0;
}

.value {
  color: #333;
  flex: 1;
}

/* 角色和状态标签 */
.role-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.active {
  background-color: #e6f7ee;
  color: #00875a;
}

.status-tag.disabled {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

/* 右侧内容区 */
.profile-content {
  flex: 1;
  min-width: 500px;
}

.profile-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  overflow: hidden;
}

.password-card {
  margin-bottom: 0;
}

.card-header {
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.card-body {
  padding: 20px;
}

/* 表单样式 */
.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.form-item {
  flex: 1;
  min-width: 200px;
}

.form-item.full-width {
  flex: 1 1 100%;
  min-width: 100%;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.form-item input,
.form-item textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-item input:focus,
.form-item textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.btn-save, .btn-change-pwd {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-save:hover, .btn-change-pwd:hover {
  background: #359e75;
}

.btn-change-pwd {
  background: #667eea;
}

.btn-change-pwd:hover {
  background: #556cd6;
}

/* 提示框样式 */
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 12px 20px;
  border-radius: 4px;
  z-index: 1000;
  animation: fadeIn 0.3s, fadeOut 0.3s 2.7s;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeOut {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(-20px); }
}

/* 响应式调整 */
@media (max-width: 900px) {
  .profile-container {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
  
  .profile-content {
    min-width: 100%;
  }
}

@media (max-width: 576px) {
  .form-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .form-item {
    min-width: 100%;
  }
}
</style>