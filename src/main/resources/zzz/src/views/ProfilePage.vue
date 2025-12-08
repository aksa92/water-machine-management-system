<template>
  <div class="profile-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-title">个人中心</div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="profile-header">
          <div class="user-avatar">
            <div class="avatar-icon">👤</div>
          </div>
          <div class="user-info">
            <div class="user-name">维修员 张三</div>
            <div class="user-id">工号：REP2023001</div>
            <div class="user-area">负责片区：教学楼A区、图书馆、宿舍C区</div>
          </div>
        </div>
      </div>

      <!-- 本月工作统计 -->
      <div class="stats-section">
        <div class="section-title">本月工作统计</div>
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number">23</div>
            <div class="stat-label">处理工单</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">45</div>
            <div class="stat-label">设备巡检</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">98%</div>
            <div class="stat-label">响应率</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">95%</div>
            <div class="stat-label">完成率</div>
          </div>
        </div>
      </div>

      <!-- 功能菜单 -->
      <div class="menu-section">
        <div class="section-title">功能菜单</div>
        <div class="menu-list">
          <div class="menu-item" @click="goToSettings">
            <div class="menu-left">
              <div class="menu-icon">⚙️</div>
              <div class="menu-text">系统设置</div>
            </div>
            <div class="menu-right">></div>
          </div>

          <div class="menu-item" @click="goToNotifications">
            <div class="menu-left">
              <div class="menu-icon">🔔</div>
              <div class="menu-text">消息通知</div>
              <div class="badge" v-if="unreadCount > 0">{{ unreadCount }}</div>
            </div>
            <div class="menu-right">></div>
          </div>

          <div class="menu-item" @click="goToFeedback">
            <div class="menu-left">
              <div class="menu-icon">📝</div>
              <div class="menu-text">问题反馈</div>
            </div>
            <div class="menu-right">></div>
          </div>

          <div class="menu-item" @click="goToHelp">
            <div class="menu-left">
              <div class="menu-icon">❓</div>
              <div class="menu-text">使用帮助</div>
            </div>
            <div class="menu-right">></div>
          </div>

          <div class="menu-item" @click="goToAbout">
            <div class="menu-left">
              <div class="menu-icon">ℹ️</div>
              <div class="menu-text">关于我们</div>
            </div>
            <div class="menu-right">></div>
          </div>
        </div>
      </div>

      <!-- 退出登录按钮 -->
      <div class="logout-section">
        <button class="logout-btn" @click="logout">
          退出登录
        </button>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goToHome">首页</div>
      <div class="nav-item" @click="goToInspection">巡检</div>
      <div class="nav-item" @click="goToWorkOrders">工单</div>
      <div class="nav-item active" @click="goToProfile">我的</div>
    </div>

    <!-- 退出确认弹窗 -->
    <div v-if="showLogoutConfirm" class="confirm-modal">
      <div class="modal-content">
        <div class="modal-header">
          <div class="modal-title">确认退出</div>
        </div>
        <div class="modal-body">
          <div class="modal-message">确定要退出登录吗？</div>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel" @click="cancelLogout">取消</button>
          <button class="modal-btn confirm" @click="confirmLogout">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 未读消息数量
const unreadCount = ref(3)
// 显示退出确认弹窗
const showLogoutConfirm = ref(false)

// 导航函数
const goToHome = () => {
  router.push('/home')
}

const goToInspection = () => {
  router.push('/inspection')
}

const goToWorkOrders = () => {
  router.push('/work-orders')
}

const goToProfile = () => {
  // 如果已经在个人中心页面，则刷新
  if (router.currentRoute.value.path === '/profile') {
    // 可以重新加载数据
    console.log('刷新个人中心')
  }
}

// 功能菜单跳转
const goToSettings = () => {
  console.log('跳转到系统设置')
  alert('系统设置页面开发中')
}

const goToNotifications = () => {
  console.log('跳转到消息通知')
  alert('消息通知页面开发中')
  // 可以在这里清空未读消息
  // unreadCount.value = 0
}

const goToFeedback = () => {
  console.log('跳转到问题反馈')
  alert('问题反馈页面开发中')
}

const goToHelp = () => {
  console.log('跳转到使用帮助')
  alert('使用帮助页面开发中')
}

const goToAbout = () => {
  console.log('跳转到关于我们')
  alert('关于我们页面开发中')
}

// 退出登录
const logout = () => {
  showLogoutConfirm.value = true
}

// 取消退出
const cancelLogout = () => {
  showLogoutConfirm.value = false
}

// 确认退出
const confirmLogout = () => {
  console.log('用户退出登录')
  showLogoutConfirm.value = false

  // 清除用户数据或token
  // localStorage.removeItem('userToken')
  // localStorage.removeItem('userInfo')

  // 跳转到登录页面
  router.push('/')
}
</script>

<style scoped>
.profile-page {
  width: 100%;
  height: 100%;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
}

/* 顶部标题栏 */
.header {
  background: white;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

/* 个人信息卡片 */
.profile-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.profile-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.user-avatar {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 32px;
  color: white;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.user-id {
  font-size: 14px;
  color: #666;
}

.user-area {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  margin-top: 2px;
}

/* 统计区域 */
.stats-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

/* 菜单区域 */
.menu-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.menu-list {
  display: flex;
  flex-direction: column;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  cursor: pointer;
  transition: all 0.3s;
}

.menu-item:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:hover {
  background: #fafafa;
  padding-left: 8px;
  padding-right: 8px;
  margin: 0 -8px;
  border-radius: 4px;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-icon {
  font-size: 20px;
}

.menu-text {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.badge {
  background: #ff4d4f;
  color: white;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

.menu-right {
  font-size: 16px;
  color: #999;
  transition: transform 0.3s;
}

.menu-item:hover .menu-right {
  transform: translateX(2px);
  color: #1890ff;
}

/* 退出登录按钮 */
.logout-section {
  padding: 16px;
}

.logout-btn {
  width: 100%;
  padding: 14px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.2);
}

.logout-btn:hover {
  background: #ff7875;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
}

/* 底部导航栏 */
.bottom-nav {
  display: flex;
  background: white;
  border-top: 1px solid #e8e8e8;
  padding: 8px 0;
}

.nav-item {
  flex: 1;
  text-align: center;
  padding: 8px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: color 0.3s;
}

.nav-item.active {
  color: #1890ff;
}

.nav-item:hover {
  color: #1890ff;
}

/* 确认弹窗 */
.confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 280px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.modal-header {
  padding: 20px 24px 12px;
  text-align: center;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.modal-body {
  padding: 0 24px 20px;
  text-align: center;
}

.modal-message {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  border-top: 1px solid #f0f0f0;
}

.modal-btn {
  flex: 1;
  padding: 14px;
  border: none;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.modal-btn.cancel {
  background: #f5f5f5;
  color: #666;
  border-right: 1px solid #f0f0f0;
}

.modal-btn.cancel:hover {
  background: #e8e8e8;
}

.modal-btn.confirm {
  background: #1890ff;
  color: white;
}

.modal-btn.confirm:hover {
  background: #096dd9;
}
</style>
