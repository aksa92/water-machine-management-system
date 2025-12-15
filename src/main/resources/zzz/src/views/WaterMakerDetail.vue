<template>
  <div class="water-maker-detail">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">制水机详情</div>
      <div class="header-right"></div>
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
        <button class="retry-btn" @click="fetchDeviceDetail">重试</button>
      </div>

      <!-- 正常状态 -->
      <div v-else class="device-detail-container">
        <!-- 设备信息 -->
        <div class="info-section">
          <div class="section-title">设备信息</div>
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">设备ID</div>
              <div class="info-value">{{ deviceInfo.id }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">设备名称</div>
              <div class="info-value">{{ deviceInfo.name }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">安装位置</div>
              <div class="info-value">{{ deviceInfo.location }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">设备状态</div>
              <div class="info-value" :class="getStatusClass(deviceInfo.status)">
                {{ formatStatus(deviceInfo.status) }}
              </div>
            </div>
            <div class="info-item">
              <div class="info-label">安装日期</div>
              <div class="info-value">{{ deviceInfo.installDate }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">所属区域</div>
              <div class="info-value">{{ deviceInfo.areaId }}</div>
            </div>
          </div>
        </div>

        <!-- 水质监测 -->
        <div v-if="deviceInfo.waterQuality" class="info-section">
          <div class="section-title">水质监测</div>
          <div class="monitoring-grid">
            <div class="monitoring-item">
              <div class="monitoring-label">自来水TDS</div>
              <div class="monitoring-value">{{ deviceInfo.waterQuality.tapWaterTDS || 285 }}</div>
              <div class="monitoring-unit">mg/L</div>
            </div>
            <div class="monitoring-item">
              <div class="monitoring-label">纯净水TDS</div>
              <div class="monitoring-value">{{ deviceInfo.waterQuality.pureWaterTDS || 13 }}</div>
              <div class="monitoring-unit">mg/L</div>
            </div>
            <div class="monitoring-item">
              <div class="monitoring-label">矿化水TDS</div>
              <div class="monitoring-value">{{ deviceInfo.waterQuality.mineralWaterTDS || 87 }}</div>
              <div class="monitoring-unit">mg/L</div>
            </div>
          </div>
        </div>

        <!-- 设备备注 -->
        <div v-if="deviceInfo.remark" class="info-section">
          <div class="section-title">备注信息</div>
          <div class="remark-content">{{ deviceInfo.remark }}</div>
        </div>
      </div>

      <button
        v-if="!loading && !error"
        class="inspect-btn"
        @click="startInspection"
        :disabled="deviceInfo.status === 'fault'"
      >
        {{ deviceInfo.status === 'fault' ? '设备故障，无法巡检' : '开始巡检' }}
      </button>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goToHome">首页</div>
      <div class="nav-item" @click="goToInspection">巡检</div>
      <div class="nav-item" @click="goToWorkOrders">工单</div>
      <div class="nav-item" @click="goToProfile">我的</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { deviceService } from '@/services/deviceService'

const router = useRouter()
const route = useRoute()

// 设备详情数据
const deviceInfo = ref({})
const loading = ref(true)
const error = ref(null)

// 获取设备ID
const deviceId = computed(() => route.params.id)

// 获取设备详情
const fetchDeviceDetail = async () => {
  try {
    loading.value = true
    error.value = null

    const response = await deviceService.getDeviceDetail(deviceId.value)

    if (response.code === 200) {
      const data = response.data

      // 转换后端数据到前端格式
      deviceInfo.value = {
        id: data.deviceId,
        name: data.deviceName || `制水机#${data.deviceId}`,
        location: data.installLocation || '未指定位置',
        status: data.status || 'offline',
        areaId: data.areaId,
        installDate: data.installDate,
        remark: data.remark,
        lastCheckTime: data.lastCheckTime,
        createTime: data.createTime,
        deviceType: data.deviceType,
        // 水质数据（如果存在）
        waterQuality: data.tapWaterTDS !== undefined || data.pureWaterTDS !== undefined || data.mineralWaterTDS !== undefined ? {
          tapWaterTDS: data.tapWaterTDS,
          pureWaterTDS: data.pureWaterTDS,
          mineralWaterTDS: data.mineralWaterTDS
        } : undefined
      }
    } else {
      error.value = response.message || '获取设备详情失败'
    }
  } catch (err) {
    error.value = err.message || '获取设备详情失败'
    console.error('获取设备详情失败:', err)
  } finally {
    loading.value = false
  }
}

// 状态格式化
const formatStatus = (status) => {
  const statusMap = {
    'online': '在线',
    'offline': '离线',
    'fault': '故障'
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
      return ''
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

const startInspection = () => {
  console.log('开始巡检制水机', deviceInfo.value.id)
  router.push(`/inspection/form?deviceId=${deviceInfo.value.id}&deviceType=water_maker`)
}

// 组件挂载时获取数据
onMounted(() => {
  if (deviceId.value) {
    fetchDeviceDetail()
  }
})
</script>



<style scoped>
.water-maker-detail {
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

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.device-detail-container {
  flex: 1;
}

/* 信息区块 */
.info-section {
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

/* 设备信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
}

.info-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.status-normal {
  color: #52c41a;
}

.status-warning {
  color: #faad14;
}

/* 水质监测网格 */
.monitoring-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.monitoring-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  text-align: center;
}

.monitoring-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.monitoring-value {
  font-size: 20px;
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 2px;
}

.monitoring-unit {
  font-size: 11px;
  color: #999;
}

/* 滤芯状态网格 */
.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.filter-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  text-align: center;
}

.filter-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.filter-status {
  font-size: 12px;
  margin-bottom: 4px;
}

.filter-lifespan {
  font-size: 11px;
  color: #666;
}

/* 传感器监控网格 */
.sensor-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.sensor-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  text-align: center;
}

.sensor-name {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.sensor-value {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 2px;
}

.sensor-unit {
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
}

.sensor-extra {
  font-size: 11px;
  color: #666;
}

/* 巡检按钮 */
.inspect-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 20px;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.inspect-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.3);
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
  padding: 80px 0;
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
  padding: 80px 0;
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

.status-online {
  color: #52c41a;
}

.status-offline {
  color: #fa541c;
}

.status-fault {
  color: #ff4d4f;
}

.remark-content {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.inspect-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
