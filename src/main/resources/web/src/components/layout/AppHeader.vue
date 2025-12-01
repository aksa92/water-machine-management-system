<template>
  <header class="app-header">
    <div class="header-left">
      <h1 class="system-title">校园矿化水系统</h1>
    </div>
    <div class="header-right">
      <div class="user-info">
        <div class="user-avatar">
          <img :src="userAvatar" alt="用户头像" />
        </div>
        <span class="user-name">张管理员</span>
        <div class="dropdown-arrow">▼</div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const userAvatar = ref('images/用户界面/u106.jpg') // 根据实际路径调整
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
}

.system-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
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
</style>