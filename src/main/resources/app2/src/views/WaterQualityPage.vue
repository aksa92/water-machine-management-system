<template>
  <div class="water-quality-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-title">水质详情</div>
      <button class="back-btn" @click="goBack">‹</button>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 设备信息 -->
      <div class="device-info-section">
        <h2 class="device-name">{{ deviceInfo.name }}</h2>
        <div class="device-details">
          <span class="distance">{{ deviceInfo.distance }}</span>
          <span class="separator">|</span>
          <span class="device-id">ID: {{ deviceInfo.id }}</span>
          <span class="separator">|</span>
          <span class="device-status" :class="deviceInfo.status">
            {{ deviceInfo.statusText }}
          </span>
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

      <!-- 水质说明 -->
      <div class="quality-explanation">
        <div class="explanation-title">水质说明：</div>
        <div class="explanation-content">
          <div class="explanation-item">
            <span class="explanation-label">自来水TDS：</span>
            <span class="explanation-value">{{ deviceInfo.waterQuality.tapWater }} mg/L</span>
            <span class="explanation-status up">(偏高)</span>
          </div>
          <div class="explanation-item">
            <span class="explanation-label">纯净水TDS：</span>
            <span class="explanation-value">{{ deviceInfo.waterQuality.pureWater }} mg/L</span>
            <span class="explanation-status good">(优良)</span>
          </div>
          <div class="explanation-item">
            <span class="explanation-label">矿化水TDS：</span>
            <span class="explanation-value">{{ deviceInfo.waterQuality.mineralWater }} mg/L</span>
            <span class="explanation-status good">(优良)</span>
          </div>
        </div>
        <div class="explanation-text">
          TDS值越低，水质越纯净。纯净水TDS&lt;50为优良，矿化水TDS 50-150为适宜范围。
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

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 设备信息
const deviceInfo = reactive({
  name: '湖南大学教学楼1F饮水机',
  distance: '128m',
  id: 'A201',
  status: 'online',
  statusText: '在线',
  waterQuality: {
    tapWater: '285',
    pureWater: '13',
    mineralWater: '87'
  },
  filters: {
    preFilter: {
      name: '前置滤芯组',
      life: '100%',
      percentage: 100
    },
    pureFilter: {
      name: '纯水滤芯组',
      life: '80%',
      percentage: 80
    },
    mineralFilter: {
      name: '矿化滤芯组',
      life: '70%',
      percentage: 70
    }
  }
})

// 模拟从路由参数获取设备ID
onMounted(() => {
  // 这里可以获取路由参数来加载对应的设备信息
  const deviceId = router.currentRoute.value.query.deviceId || 'A201'
  // 实际应用中应该根据deviceId加载数据
  console.log('加载设备数据:', deviceId)
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
</style>