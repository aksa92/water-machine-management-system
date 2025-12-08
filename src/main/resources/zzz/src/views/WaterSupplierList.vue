<template>
  <div class="water-supplier-list">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">设备巡检</div>
      <div class="header-right">
        <span class="nav-text">供水机</span>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="device-list">
        <div class="device-item">
          <div class="device-info">
            <div class="device-name">供水机#A105</div>
            <div class="device-status">上次巡检时间2023-9-10</div>
          </div>
          <div class="device-actions">
            <button class="action-btn detail" @click="viewDeviceDetail('A105')">
              详情
            </button>
            <button class="action-btn inspect" @click="startInspection('A105')">
              开始巡检
            </button>
          </div>
        </div>

        <div class="device-item">
          <div class="device-info">
            <div class="device-name">供水机#A106</div>
            <div class="device-status">上次巡检时间2023-9-10</div>
          </div>
          <div class="device-actions">
            <button class="action-btn detail" @click="viewDeviceDetail('A106')">
              详情
            </button>
            <button class="action-btn inspect" @click="startInspection('A106')">
              开始巡检
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goToHome">首页</div>
      <div class="nav-item active" @click="goToInspection">巡检</div>
      <div class="nav-item" @click="goToWorkOrders">工单</div>
      <div class="nav-item" @click="goToProfile">我的</div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}

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
  router.push('/profile')
}

const viewDeviceDetail = (deviceId) => {
  router.push(`/inspection/water-supplier/${deviceId}`)
}

const startInspection = (deviceId) => {
  console.log(`开始巡检供水机 ${deviceId}`)
  router.push('/inspection/form')
}
</script>

<style scoped>
.water-supplier-list {
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.header-left {
  width: 80px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
  flex: 1;
}

.header-right {
  width: 80px;
  text-align: right;
}

.back-btn {
  font-size: 14px;
  color: #1890ff;
  cursor: pointer;
  transition: color 0.3s;
}

.back-btn:hover {
  color: #096dd9;
}

.nav-text {
  font-size: 14px;
  color: #1890ff;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 20px 16px;
  overflow-y: auto;
}

/* 设备列表 */
.device-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.device-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.device-item:last-child {
  border-bottom: none;
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.device-status {
  font-size: 13px;
  color: #666;
}

/* 设备操作按钮 */
.device-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.detail {
  background: #f0f7ff;
  color: #1890ff;
}

.action-btn.inspect {
  background: #1890ff;
  color: white;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
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
</style>
