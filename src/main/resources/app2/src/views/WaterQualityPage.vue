<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { deviceService } from '@/services/deviceService'

const router = useRouter()
const route = useRoute()

// 设备信息
const deviceInfo = reactive({
  name: '饮水机',
  distance: '--',
  id: '--',
  status: 'loading',
  statusText: '加载中...',
  waterQuality: {
    tapWater: '--',
    pureWater: '--',
    mineralWater: '--'
  },
  filters: {
    preFilter: {
      name: '前置滤芯组',
      life: '--',
      percentage: 0
    },
    pureFilter: {
      name: '纯水滤芯组',
      life: '--',
      percentage: 0
    },
    mineralFilter: {
      name: '矿化滤芯组',
      life: '--',
      percentage: 0
    }
  },
  deviceId: '',
  terminalId: '',
  lastDetectionTime: '--',
  filterLife: '--',
  updateTime: '--'
})

const isLoading = ref(true)

// 从后端获取水质信息
const fetchWaterQualityInfo = async () => {
  try {
    isLoading.value = true

    const terminalId = route.query.terminalId
    const deviceId = route.query.deviceId

    if (!terminalId || !deviceId) {
      console.error('缺少设备参数')
      return
    }

    // 获取终端信息
    const terminalResult = await deviceService.getTerminalInfo(terminalId)
    if (terminalResult.code === 200 && terminalResult.data) {
      const terminalData = terminalResult.data

      deviceInfo.name = terminalData.terminalName || '饮水机'
      deviceInfo.id = terminalId
      deviceInfo.deviceId = deviceId
      deviceInfo.terminalId = terminalId
      deviceInfo.status = terminalData.status === 'active' ? 'online' : 'offline'
      deviceInfo.statusText = terminalData.status === 'active' ? '在线' : '离线'

      // 如果有距离信息
      if (terminalData.distance) {
        deviceInfo.distance = terminalData.distance
      }
    }

    // 获取水质信息
    const qualityResult = await deviceService.getWaterQualityInfo(deviceId)
    if (qualityResult.code === 200 && qualityResult.data) {
      const qualityData = qualityResult.data

      // 更新水质数据
      if (qualityData.rawWaterTds) {
        deviceInfo.waterQuality.tapWater = Math.round(qualityData.rawWaterTds)
      }
      if (qualityData.pureWaterTds) {
        deviceInfo.waterQuality.pureWater = Math.round(qualityData.pureWaterTds)
      }
      if (qualityData.mineralWaterTds) {
        deviceInfo.waterQuality.mineralWater = Math.round(qualityData.mineralWaterTds)
      }

      // 更新滤芯状态
      if (qualityData.filterLife) {
        const filterLife = parseInt(qualityData.filterLife)
        deviceInfo.filterLife = `${filterLife}%`

        // 模拟滤芯百分比（实际应根据滤芯类型分配）
        deviceInfo.filters.preFilter.percentage = filterLife
        deviceInfo.filters.preFilter.life = `${filterLife}%`
        deviceInfo.filters.pureFilter.percentage = Math.max(filterLife - 10, 0)
        deviceInfo.filters.pureFilter.life = `${Math.max(filterLife - 10, 0)}%`
        deviceInfo.filters.mineralFilter.percentage = Math.max(filterLife - 20, 0)
        deviceInfo.filters.mineralFilter.life = `${Math.max(filterLife - 20, 0)}%`
      }

      deviceInfo.lastDetectionTime = qualityData.lastDetectionTime || '--'
      deviceInfo.updateTime = qualityData.updateTime || '--'
      deviceInfo.waterQuality = qualityData.waterQuality || '--'
    }

  } catch (error) {
    console.error('获取水质信息失败:', error)
    deviceInfo.status = 'error'
    deviceInfo.statusText = '获取失败'
  } finally {
    isLoading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchWaterQualityInfo()
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面跳转
const goToPage = (page) => {
  switch(page) {
    case 'home':
      router.push('/home')
      break
    case 'scan':
      router.push('/scan')
      break
    case 'profile':
      router.push('/profile')
      break
  }
}
</script>

<template>
  <div class="water-quality-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-title">水质详情</div>
      <button class="back-btn" @click="goBack">‹</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-section">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载水质信息中...</div>
    </div>

    <!-- 主要内容区域 -->
    <div v-else class="main-content">
      <!-- 设备信息 -->
      <div class="device-info-section">
        <h2 class="device-name">{{ deviceInfo.name }}</h2>
        <div class="device-details">
          <span class="distance">{{ deviceInfo.distance }}</span>
          <span class="separator">|</span>
          <span class="device-id">ID: {{ deviceInfo.id }}</span>
          <span class="separator">|</span>
          <span class="device-id">设备: {{ deviceInfo.deviceId }}</span>
          <span class="separator">|</span>
          <span class="device-status" :class="deviceInfo.status">
            {{ deviceInfo.statusText }}
          </span>
        </div>
        <div class="update-time">
          最后更新: {{ deviceInfo.updateTime }}
        </div>
      </div>

      <!-- 水质检测标题 -->
      <div class="section-title">
        <span>TDS水质检测</span>
      </div>

      <!-- TDS水质数据卡片 -->
      <div class="quality-cards">
        <div class="quality-card tap-water">
          <div class="card-title">自来水TDS</div>
          <div class="card-value">{{ deviceInfo.waterQuality.tapWater }}</div>
          <div class="card-unit">mg/L</div>
        </div>

        <div class="quality-card pure-water">
          <div class="card-title">纯净水TDS</div>
          <div class="card-value">{{ deviceInfo.waterQuality.pureWater }}</div>
          <div class="card-unit">mg/L</div>
        </div>

        <div class="quality-card mineral-water">
          <div class="card-title">矿化水TDS</div>
          <div class="card-value">{{ deviceInfo.waterQuality.mineralWater }}</div>
          <div class="card-unit">mg/L</div>
        </div>
      </div>

      <!-- 滤芯状态标题 -->
      <div class="section-title">
        <span>滤芯状态</span>
      </div>

      <!-- 滤芯状态 -->
      <div class="filter-status-section">
        <!-- 前置滤芯组 -->
        <div class="filter-item">
          <div class="filter-info">
            <div class="filter-name">前置滤芯组</div>
            <div class="filter-life">{{ deviceInfo.filters.preFilter.life }}</div>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: deviceInfo.filters.preFilter.percentage + '%' }"></div>
          </div>
        </div>

        <!-- 纯水滤芯组 -->
        <div class="filter-item">
          <div class="filter-info">
            <div class="filter-name">纯水滤芯组</div>
            <div class="filter-life">{{ deviceInfo.filters.pureFilter.life }}</div>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: deviceInfo.filters.pureFilter.percentage + '%' }"></div>
          </div>
        </div>

        <!-- 矿化滤芯组 -->
        <div class="filter-item">
          <div class="filter-info">
            <div class="filter-name">矿化滤芯组</div>
            <div class="filter-life">{{ deviceInfo.filters.mineralFilter.life }}</div>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: deviceInfo.filters.mineralFilter.percentage + '%' }"></div>
          </div>
        </div>
      </div>

      <!-- 总体滤芯状态 -->
      <div class="overall-filter-status">
        <div class="filter-summary">
          <span class="summary-label">总体滤芯寿命：</span>
          <span class="summary-value">{{ deviceInfo.filterLife }}</span>
        </div>
      </div>

      <!-- 水质说明 -->
      <div class="quality-explanation">
        <div class="explanation-title">水质说明：</div>
        <div class="explanation-content">
          <div class="explanation-item">
            <span class="explanation-label">自来水TDS：</span>
            <span class="explanation-value">{{ deviceInfo.waterQuality.tapWater }} mg/L</span>
            <span class="explanation-status" :class="getTdsStatus(deviceInfo.waterQuality.tapWater, 'tap')">
              {{ getTdsStatusText(deviceInfo.waterQuality.tapWater, 'tap') }}
            </span>
          </div>
          <div class="explanation-item">
            <span class="explanation-label">纯净水TDS：</span>
            <span class="explanation-value">{{ deviceInfo.waterQuality.pureWater }} mg/L</span>
            <span class="explanation-status" :class="getTdsStatus(deviceInfo.waterQuality.pureWater, 'pure')">
              {{ getTdsStatusText(deviceInfo.waterQuality.pureWater, 'pure') }}
            </span>
          </div>
          <div class="explanation-item">
            <span class="explanation-label">矿化水TDS：</span>
            <span class="explanation-value">{{ deviceInfo.waterQuality.mineralWater }} mg/L</span>
            <span class="explanation-status" :class="getTdsStatus(deviceInfo.waterQuality.mineralWater, 'mineral')">
              {{ getTdsStatusText(deviceInfo.waterQuality.mineralWater, 'mineral') }}
            </span>
          </div>
        </div>
        <div class="explanation-text">
          TDS值越低，水质越纯净。纯净水TDS&lt;50为优良，矿化水TDS 50-150为适宜范围，自来水TDS通常为100-300mg/L。
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-button" @click="goToPage('home')">
        <div class="nav-icon">🗺️</div>
        <div class="nav-text">地图</div>
      </div>

      <div class="nav-button" @click="goToPage('scan')">
        <div class="nav-icon">📷</div>
        <div class="nav-text">扫码</div>
      </div>

      <div class="nav-button" @click="goToPage('profile')">
        <div class="nav-icon">👤</div>
        <div class="nav-text">我的</div>
      </div>
    </div>
  </div>
</template>

<script>
// 添加判断TDS状态的辅助函数
const getTdsStatus = (value, type) => {
  const numValue = parseInt(value)
  if (isNaN(numValue)) return 'unknown'

  switch(type) {
    case 'tap':
      return numValue > 300 ? 'up' : numValue < 100 ? 'low' : 'good'
    case 'pure':
      return numValue < 50 ? 'good' : numValue > 100 ? 'up' : 'normal'
    case 'mineral':
      return numValue >= 50 && numValue <= 150 ? 'good' : numValue > 150 ? 'up' : 'low'
    default:
      return 'unknown'
  }
}

const getTdsStatusText = (value, type) => {
  const status = getTdsStatus(value, type)
  switch(status) {
    case 'good': return '(优良)'
    case 'up': return '(偏高)'
    case 'low': return '(偏低)'
    case 'normal': return '(正常)'
    default: return '(--)'
  }
}
</script>

<style scoped>
.water-quality-page {
  width: 375px;
  height: 667px;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

/* 顶部标题栏 */
.header {
  height: 40px;
  background: linear-gradient(135deg, #1156b1 0%, #81d3f8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: white;
  letter-spacing: 1px;
}

.back-btn {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 20px 16px;
  overflow-y: auto;
}

/* 设备信息 */
.device-info-section {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.device-details {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.distance {
  color: #666;
}

.separator {
  color: #ccc;
}

.device-id {
  color: #1890ff;
  font-weight: 500;
}

.device-status {
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.device-status.online {
  background: rgba(4, 217, 25, 0.1);
  color: #04d919;
}

.device-status.offline {
  background: rgba(170, 170, 170, 0.1);
  color: #aaaaaa;
}

/* 分区标题 */
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 20px 0 16px;
  padding-left: 4px;
  border-left: 4px solid #1890ff;
}

/* TDS水质卡片 */
.quality-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.quality-card {
  background: white;
  border-radius: 10px;
  padding: 16px 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e8e8e8;
  transition: all 0.3s;
}

.quality-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 4px;
}

.card-unit {
  font-size: 11px;
  color: #999;
}

/* 滤芯状态 */
.filter-status-section {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-item {
  margin-bottom: 20px;
}

.filter-item:last-child {
  margin-bottom: 0;
}

.filter-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.filter-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.filter-life {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
}

.progress-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #1890ff, #52c41a);
  border-radius: 4px;
  transition: width 0.5s ease;
}

/* 水质说明 */
.quality-explanation {
  background: #f0f5ff;
  border-radius: 8px;
  padding: 16px;
  margin-top: 20px;
  border-left: 4px solid #1890ff;
}

.explanation-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.explanation-content {
  margin-bottom: 12px;
}

.explanation-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.explanation-label {
  color: #666;
  min-width: 80px;
}

.explanation-value {
  color: #333;
  font-weight: 500;
  margin: 0 8px;
}

.explanation-status {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
}

.explanation-status.good {
  background: rgba(4, 217, 25, 0.1);
  color: #04d919;
}

.explanation-status.up {
  background: rgba(255, 152, 0, 0.1);
  color: #ff9800;
}

.explanation-text {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  padding-top: 8px;
  border-top: 1px solid rgba(24, 144, 255, 0.2);
}

/* 底部导航栏 */
.bottom-nav {
  height: 60px;
  background: white;
  border-top: 1px solid #e8e8e8;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.nav-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  background: none;
  border: none;
  padding: 0;
}

.nav-button:hover {
  background: #f8f9fa;
}

.nav-button.active {
  color: #1890ff;
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.nav-text {
  font-size: 12px;
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 420px) {
  .water-quality-page {
    width: 100%;
    height: 100vh;
  }

  .quality-cards {
    grid-template-columns: 1fr;
  }

  .main-content {
    padding: 16px 12px;
  }
}

.loading-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
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

.loading-text {
  font-size: 14px;
  color: #666;
}

.update-time {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  text-align: center;
}

.overall-filter-status {
  background: white;
  border-radius: 8px;
  padding: 12px 16px;
  margin: 16px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.filter-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.summary-value {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.explanation-status.low {
  background: rgba(144, 202, 249, 0.1);
  color: #1890ff;
}

.explanation-status.normal {
  background: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.explanation-status.unknown {
  background: rgba(158, 158, 158, 0.1);
  color: #9e9e9e;
}
</style>