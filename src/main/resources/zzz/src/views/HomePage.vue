<template>
  <div class="home-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <div class="user-info">
          <div class="user-name">{{ userInfo?.username || '未登录' }}</div>
          <div class="user-type">{{ userInfo?.userType === 'repairer' ? '维修人员' : userInfo?.userType }}</div>
        </div>
      </div>
      <div class="header-title">运维工作台</div>
      <div class="header-right"></div>
    </div>
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-number">5</div>
          <div class="stat-label">待处理工单</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">28</div>
          <div class="stat-label">辖区设备</div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button class="action-btn primary" @click="goToInspection">
          <span>去巡检</span>
        </button>
        <button class="action-btn secondary" @click="goToWorkOrders">
          <span>看工单</span>
        </button>
      </div>

      <!-- 待处理工单列表 -->
      <div class="work-order-section">
        <div class="section-header">
          <h3>待处理工单</h3>
        </div>
        <div class="work-order-list">
          <div class="work-order-item urgent">
            <div class="order-badge urgent">紧急</div>
            <div class="order-content">
              <div class="order-title">制水机#A201 - TDS超标</div>
              <div class="order-location">教学楼A区 - 10分钟前</div>
            </div>
          </div>
          <div class="work-order-item normal">
            <div class="order-badge normal">一般</div>
            <div class="order-content">
              <div class="order-title">供水机#B105 - 水位异常</div>
              <div class="order-location">图书馆 - 25分钟前</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item active" @click="goToHome">
        <span>首页</span>
      </div>
      <div class="nav-item" @click="goToInspection">
        <span>巡检</span>
      </div>
      <div class="nav-item" @click="goToWorkOrders">
        <span>工单</span>
      </div>
      <div class="nav-item" @click="goToProfile">
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const userInfo = authStore.getUserInfo()
const router = useRouter()

const goToInspection = () => {
  console.log('跳转到巡检页面')
  router.push('/inspection')
}

const goToWorkOrders = () => {
  console.log('跳转到工单页面')
  router.push('/work-orders')
}

const goToHome = () => {
  if (router.currentRoute.value.path !== '/home') {
    router.push('/home')
  }
}

const goToProfile = () => {
  console.log('跳转到我的页面')
  router.push('/profile')
}
</script>

<style scoped>
.home-page {
  width: 375px;
  height: 667px;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  position: relative;
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
  padding: 20px 16px;
  overflow-y: auto;
}

/* 统计卡片 */
.stats-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.action-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.primary {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
}

.action-btn.secondary {
  background: white;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

/* 工单区域 */
.work-order-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-header {
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.work-order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.work-order-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  border-left: 3px solid #ccc;
}

.work-order-item.urgent {
  border-left-color: #ff4d4f;
}

.work-order-item.normal {
  border-left-color: #52c41a;
}

.order-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  color: white;
  white-space: nowrap;
}

.order-badge.urgent {
  background: #ff4d4f;
}

.order-badge.normal {
  background: #52c41a;
}

.order-content {
  flex: 1;
}

.order-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.order-location {
  font-size: 12px;
  color: #666;
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
  -webkit-tap-highlight-color: transparent;
}

.nav-item.active {
  color: #1890ff;
  position: relative;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: #1890ff;
  border-radius: 1px;
}

.nav-item:hover {
  color: #1890ff;
}

.user-info {
  font-size: 12px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.user-type {
  color: #666;
  font-size: 11px;
}
</style>
