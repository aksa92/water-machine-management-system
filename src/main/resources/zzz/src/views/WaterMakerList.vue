<template>
  <div class="water-maker-list">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">设备巡检</div>
      <div class="header-right">
        <span class="nav-text">制水机</span>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 学校1 -->
      <div class="school-section">
        <div class="school-name">学校1</div>

        <div class="device-list">
          <div class="device-item">
            <div class="device-info">
              <div class="device-name">制水机#A102</div>
              <div class="device-location">A区教学楼 - 上次巡检时间2023-9-10</div>
            </div>
            <div class="device-actions">
              <button class="action-btn detail" @click="viewDeviceDetail('A102')">
                详情
              </button>
              <button class="action-btn inspect" @click="viewWaterSupplier('A102')">
                查看供水机
              </button>
            </div>
          </div>

          <div class="device-item">
            <div class="device-info">
              <div class="device-name">制水机#C103</div>
              <div class="device-location">C区宿舍 - 上次巡检时间2023-9-10</div>
            </div>
            <div class="device-actions">
              <button class="action-btn detail" @click="viewDeviceDetail('C103')">
                详情
              </button>
              <button class="action-btn inspect" @click="viewWaterSupplier('C103')">
                查看供水机
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 学校2 -->
      <div class="school-section">
        <div class="school-name">学校2</div>

        <div class="device-list">
          <div class="device-item">
            <div class="device-info">
              <div class="device-name">制水机#B101</div>
              <div class="device-location">B区操场 - 上次巡检时间2023-9-10</div>
            </div>
            <div class="device-actions">
              <button class="action-btn detail" @click="viewDeviceDetail('B101')">
                详情
              </button>
              <button class="action-btn inspect" @click="viewWaterSupplier('B101')">
                查看供水机
              </button>
            </div>
          </div>

          <div class="device-item">
            <div class="device-info">
              <div class="device-name">制水机#D104</div>
              <div class="device-location">D区食堂 - 上次巡检时间2023-9-10</div>
            </div>
            <div class="device-actions">
              <button class="action-btn detail" @click="viewDeviceDetail('D104')">
                详情
              </button>
              <button class="action-btn inspect" @click="viewWaterSupplier('D104')">
                查看供水机
              </button>
            </div>
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
  router.push(`/inspection/water-maker/${deviceId}`)
}

const viewWaterSupplier = (deviceId) => {
  router.push('/inspection/water-supplier')
}
</script>

<style scoped>
.water-maker-list {
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
  padding: 16px;
  overflow-y: auto;
}

/* 学校区块 */
.school-section {
  background: white;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.school-name {
  background: #f0f7ff;
  padding: 12px 16px;
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
  border-bottom: 1px solid #e8f4ff;
}

/* 设备列表 */
.device-list {
  padding: 16px;
}

.device-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.device-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.device-item:first-child {
  padding-top: 0;
}

.device-info {
  margin-bottom: 12px;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.device-location {
  font-size: 13px;
  color: #666;
}

/* 设备操作按钮 */
.device-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.detail {
  background: #1890ff;
  color: white;
}

.action-btn.inspect {
  background: white;
  color: #1890ff;
  border: 1px solid #1890ff;
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
