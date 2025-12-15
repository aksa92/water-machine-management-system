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
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <div>加载中...</div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <div class="error-icon">❌</div>
        <div class="error-message">{{ error }}</div>
        <button class="retry-btn" @click="fetchWaterSuppliers">重试</button>
      </div>

      <!-- 正常状态 -->
      <div v-else class="device-list">
        <div v-for="device in deviceList" :key="device.id" class="device-item">
          <div class="device-info">
            <div class="device-name">{{ device.name }}</div>
            <div class="device-location">{{ device.location }}</div>
            <div class="device-status" :class="getStatusClass(device.status)">
              状态: {{ formatStatus(device.status) }}
            </div>
            <div class="device-last-check">
              上次巡检: {{ device.lastInspectionTime }}
            </div>
          </div>
          <div class="device-actions">
            <button class="action-btn detail" @click="viewDeviceDetail(device.id)">
              详情
            </button>
            <button class="action-btn inspect" @click="startInspection(device.id)">
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { deviceService } from '@/services/deviceService'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 设备数据
const deviceList = ref([])
const loading = ref(true)
const error = ref(null)

// 获取供水机列表
const fetchWaterSuppliers = async () => {
  try {
    loading.value = true
    error.value = null

    const response = await deviceService.getAreaDevicesByType('water_supply')

    if (response.code === 200) {
      deviceList.value = response.data.map(device => ({
        id: device.deviceId,
        name: device.deviceName || `供水机#${device.deviceId}`,
        location: device.installLocation || '未指定位置',
        lastInspectionTime: device.lastCheckTime || '2023-9-10',
        status: device.status,
        waterLevel: device.waterLevel || 0,
        storageCapacity: device.storageCapacity || 0,
        areaId: device.areaId
      }))
    } else {
      error.value = response.message
    }
  } catch (err) {
    error.value = err.message || '获取设备列表失败'
    console.error('获取供水机列表失败:', err)
  } finally {
    loading.value = false
  }
}

// 状态格式化
const formatStatus = (status) => {
  const statusMap = {
    'online': '在线',
    'offline': '离线',
    'fault': '故障',
    'maintenance': '维护中'
  }
  return statusMap[status] || status
}

// 状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 'online':
      return 'status-online'
    case 'offline':
      return 'status-offline'
    case 'fault':
      return 'status-fault'
    default:
      return 'status-unknown'
  }
}

// 导航函数
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
  router.push(`/inspection/form?deviceId=${deviceId}&deviceType=water_supply`)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchWaterSuppliers()
})
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

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #ff4d4f;
}

.error-message {
  color: #666;
  margin-bottom: 16px;
}

.retry-btn {
  padding: 8px 24px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.device-location {
  font-size: 13px;
  color: #666;
  margin: 4px 0;
}

.device-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
  margin-top: 4px;
}

.status-online {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-offline {
  background: #fff2e8;
  color: #fa541c;
  border: 1px solid #ffbb96;
}

.status-fault {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.device-last-check {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
