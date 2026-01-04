<template>
  <header class="app-header">
    <div class="header-left">
      <h1 class="system-title">校园矿化水系统</h1>
    </div>
    <div class="header-right">
      <!-- 用户信息下拉菜单 -->
      <div class="user-dropdown">
        <div class="user-info" @click="toggleDropdown">
          <div class="user-avatar">
            <img :src="userAvatar" alt="用户头像" />
          </div>
          <span class="user-name">{{ currentUser?.username || '未登录' }}</span>
          <div class="dropdown-arrow">▼</div>
        </div>

        <!-- 下拉菜单 -->
        <div v-if="showDropdown" class="dropdown-menu" @click="closeDropdown">
          <div class="user-menu-header">
            <div class="user-menu-avatar">
              <img :src="userAvatar" alt="用户头像" />
            </div>
            <div class="user-menu-info">
              <div class="user-menu-name">{{ currentUser?.username || '未登录' }}</div>
              <div class="user-menu-role">{{ currentUser?.role || '普通用户' }}</div>
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <div class="dropdown-item" @click="switchUser">
            <i class="icon-switch"></i>
            <span>切换用户</span>
          </div>
          <div class="dropdown-item" @click="logout">
            <i class="icon-logout"></i>
            <span>退出登录</span>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 使用更美观的默认头像
const userAvatar = ref('https://ui-avatars.com/api/?name=User&background=667eea&color=fff&size=36')
const router = useRouter()
const authStore = useAuthStore()
const showDropdown = ref(false)

// 计算属性获取当前用户信息
const currentUser = computed(() => {
  const user = authStore.userInfo
  if (user) {
    // 更新头像URL
    const name = user.realName || user.username || 'User'
    userAvatar.value = `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=667eea&color=fff&size=36`
  }
  return user
})

// 切换下拉菜单显示状态
const toggleDropdown = (event: Event) => {
  event.stopPropagation()
  showDropdown.value = !showDropdown.value
}

// 关闭下拉菜单
const closeDropdown = (event: Event) => {
  event.stopPropagation()
  // 不立即关闭，让子元素点击事件处理完
}

// 点击文档关闭下拉菜单
const handleClickOutside = (event: Event) => {
  const target = event.target as HTMLElement
  if (!target.closest('.user-dropdown')) {
    showDropdown.value = false
  }
}

// 切换用户（跳转到登录页面）
const switchUser = () => {
  showDropdown.value = false
  authStore.logout() // 先清除当前登录状态
  router.push('/')
}

// 退出登录
const logout = () => {
  showDropdown.value = false
  authStore.logout()
  router.push('/')
}

// 监听点击外部区域关闭下拉菜单
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.app-header {
  grid-area: header;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  position: relative;
}

.system-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.user-dropdown {
  position: relative;
  display: inline-block;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.dropdown-arrow {
  font-size: 12px;
  opacity: 0.8;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.04);
  min-width: 240px;
  z-index: 1001;
  margin-top: 8px;
  overflow: hidden;
}

.user-menu-header {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.user-menu-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f0f0;
  overflow: hidden;
}

.user-menu-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-menu-info {
  flex: 1;
}

.user-menu-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.user-menu-role {
  font-size: 12px;
  color: #666;
}

.dropdown-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 4px 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  color: #333;
  font-size: 14px;
}

.dropdown-item:hover {
  background: #f5f5f5;
}

.dropdown-item i {
  width: 16px;
  height: 16px;
  display: inline-block;
}

.icon-switch::before {
  content: "🔄";
}

.icon-logout::before {
  content: "🚪";
}
</style>
