<template>
  <div class="home-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-title">首页</div>
    </div>

    <!-- 地图容器 -->
    <div class="map-container" @click="hideAllPopups">
      <!-- 地图模拟背景 -->
      <div class="map-background">
        <!-- 地图网格线 -->
        <div class="map-grid">
          <!-- 横向网格线 -->
          <div class="grid-line horizontal" v-for="i in 8" :key="'h' + i"
               :style="{ top: `${i * 12}%` }"></div>
          <!-- 纵向网格线 -->
          <div class="grid-line vertical" v-for="i in 6" :key="'v' + i"
               :style="{ left: `${i * 16}%` }"></div>
        </div>

        <!-- 校园建筑标记 -->
        <div class="campus-building building-1" :style="{ top: '30%', left: '30%' }">
          <div class="building-icon">🏫</div>
          <div class="building-name">教学楼</div>
        </div>

        <div class="campus-building building-2" :style="{ top: '60%', left: '60%' }">
          <div class="building-icon">🏢</div>
          <div class="building-name">学生公寓</div>
        </div>

        <div class="campus-building building-3" :style="{ top: '45%', left: '20%' }">
          <div class="building-icon">📚</div>
          <div class="building-name">图书馆</div>
        </div>

        <div class="campus-building building-4" :style="{ top: '70%', left: '40%' }">
          <div class="building-icon">🍽️</div>
          <div class="building-name">食堂</div>
        </div>

        <div class="campus-building building-5" :style="{ top: '20%', left: '50%' }">
          <div class="building-icon">⚽</div>
          <div class="building-name">体育馆</div>
        </div>

        <div class="campus-building building-6" :style="{ top: '40%', left: '70%' }">
          <div class="building-icon">🔬</div>
          <div class="building-name">实验室</div>
        </div>

        <!-- 道路 -->
        <div class="road road-1" style="top: 40%; left: 20%; width: 60%; height: 6px;"></div>
        <div class="road road-2" style="top: 60%; left: 30%; width: 6px; height: 40%;"></div>
        <div class="road road-3" style="top: 30%; left: 50%; width: 6px; height: 40%;"></div>

        <!-- 草地/绿化带 -->
        <div class="green-area area-1" style="top: 25%; left: 10%; width: 15%; height: 20%;"></div>
        <div class="green-area area-2" style="top: 65%; left: 75%; width: 15%; height: 20%;"></div>

        <!-- 饮水机标记1（教学楼） -->
        <div
            class="water-marker marker-1"
            :class="{ active: selectedMarker === 'TERM001' }"
            @click.stop="showMarkerInfo('TERM001')"
            style="top: 35%; left: 45%;"
        >
          <div class="marker-content">
            <!-- SVG水滴形状 -->
            <svg class="marker-svg" viewBox="0 0 22 32">
              <path
                  d="M22 11.2C22 19.2 15.4 24 11 32C6.6 24 0 19.2 0 11.2C0 7.04 2.2 0 11 0C19.8 0 22 7.04 22 11.2Z"
                  fill="#04d919"
                  stroke="white"
                  stroke-width="2"
              />
            </svg>
            <div class="marker-pulse"></div>
          </div>
          <div class="marker-label">TERM001</div>
        </div>

        <!-- 饮水机标记2（学生公寓） -->
        <div
            class="water-marker marker-2"
            :class="{ active: selectedMarker === 'TERM002' }"
            @click.stop="showMarkerInfo('TERM002')"
            style="top: 65%; left: 55%;"
        >
          <div class="marker-content">
            <svg class="marker-svg" viewBox="0 0 22 32">
              <path
                  d="M22 11.2C22 19.2 15.4 24 11 32C6.6 24 0 19.2 0 11.2C0 7.04 2.2 0 11 0C19.8 0 22 7.04 22 11.2Z"
                  fill="#04d919"
                  stroke="white"
                  stroke-width="2"
              />
            </svg>
            <div class="marker-pulse"></div>
          </div>
          <div class="marker-label">TERM002</div>
        </div>

        <!-- 饮水机标记3（图书馆） -->
        <div
            class="water-marker marker-3"
            :class="{ active: selectedMarker === 'TERM003' }"
            @click.stop="showMarkerInfo('TERM003')"
            style="top: 50%; left: 25%;"
        >
          <div class="marker-content">
            <svg class="marker-svg" viewBox="0 0 22 32">
              <path
                  d="M22 11.2C22 19.2 15.4 24 11 32C6.6 24 0 19.2 0 11.2C0 7.04 2.2 0 11 0C19.8 0 22 7.04 22 11.2Z"
                  fill="#aaaaaa"
                  stroke="white"
                  stroke-width="2"
              />
            </svg>
            <div class="marker-pulse"></div>
          </div>
          <div class="marker-label">TERM003</div>
        </div>

        <!-- 当前位置标记 -->
        <div class="current-location-marker" style="top: 50%; left: 50%;">
          <div class="location-icon">📍</div>
          <div class="location-label">我的位置</div>
        </div>
      </div>

      <!-- 地图控制按钮 -->
      <div class="map-controls">
        <button class="control-btn location-btn" @click="centerMap">
          <span class="control-icon">📍</span>
          <span class="control-text">定位</span>
        </button>
        <div class="zoom-controls">
          <button class="control-btn zoom-in-btn" @click="zoomIn">
            <span class="control-icon">+</span>
          </button>
          <button class="control-btn zoom-out-btn" @click="zoomOut">
            <span class="control-icon">-</span>
          </button>
        </div>
      </div>

      <!-- 设备信息弹窗1（教学楼饮水机） -->
      <div v-if="showDevicePopup && currentMarker === 'TERM001'" class="device-popup popup-1">
        <div class="popup-content">
          <!-- 标题栏 -->
          <div class="popup-title">
            <h3>{{ markers.TERM001.name }}</h3>
            <button class="close-btn" @click="hidePopup">✕</button>
          </div>

          <!-- 设备信息 -->
          <div class="device-info">
            <div class="info-row">
              <span class="info-label">{{ markers.TERM001.distance }} | ID: {{ markers.TERM001.id }} | 状态：</span>
              <span class="status" :class="markers.TERM001.status">{{ markers.TERM001.statusText }}</span>
            </div>

            <div class="info-row">
              <span class="info-label">当前水质：</span>
              <span class="quality">{{ markers.TERM001.quality }}</span>
            </div>

            <!-- TDS水质数据 -->
            <div class="tds-data-section">
              <div class="section-title">水质TDS数据：</div>
              <div class="tds-grid">
                <div class="tds-item">
                  <div class="tds-type">自来水</div>
                  <div class="tds-value">{{ markers.TERM001.qualityData.tapWater }}</div>
                  <div class="tds-unit">mg/L</div>
                </div>
                <div class="tds-item">
                  <div class="tds-type">纯净水</div>
                  <div class="tds-value">{{ markers.TERM001.qualityData.pureWater }}</div>
                  <div class="tds-unit">mg/L</div>
                </div>
                <div class="tds-item">
                  <div class="tds-type">矿化水</div>
                  <div class="tds-value">{{ markers.TERM001.qualityData.mineralWater }}</div>
                  <div class="tds-unit">mg/L</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <button class="action-btn primary" @click="showNavigationPopup">
              导航
            </button>
            <button class="action-btn secondary" @click="showWaterQuality">
              查看水质详细
            </button>
          </div>

          <!-- 离线设备提示 -->
          <div v-if="markers.TERM001.status === 'offline'" class="offline-notice">
            设备离线中，暂时无法使用
          </div>
        </div>
      </div>

      <!-- 设备信息弹窗2（学生公寓饮水机） -->
      <div v-if="showDevicePopup && currentMarker === 'TERM002'" class="device-popup popup-2">
        <div class="popup-content">
          <div class="popup-title">
            <h3>{{ markers.TERM002.name }}</h3>
            <button class="close-btn" @click="hidePopup">✕</button>
          </div>

          <div class="device-info">
            <div class="info-row">
              <span class="info-label">{{ markers.TERM002.distance }} | ID: {{ markers.TERM002.id }} | 状态：</span>
              <span class="status" :class="markers.TERM002.status">{{ markers.TERM002.statusText }}</span>
            </div>

            <div class="info-row">
              <span class="info-label">当前水质：</span>
              <span class="quality">{{ markers.TERM002.quality }}</span>
            </div>

            <!-- TDS水质数据 -->
            <div class="tds-data-section">
              <div class="section-title">水质TDS数据：</div>
              <div class="tds-grid">
                <div class="tds-item">
                  <div class="tds-type">自来水</div>
                  <div class="tds-value">{{ markers.TERM002.qualityData.tapWater }}</div>
                  <div class="tds-unit">mg/L</div>
                </div>
                <div class="tds-item">
                  <div class="tds-type">纯净水</div>
                  <div class="tds-value">{{ markers.TERM002.qualityData.pureWater }}</div>
                  <div class="tds-unit">mg/L</div>
                </div>
                <div class="tds-item">
                  <div class="tds-type">矿化水</div>
                  <div class="tds-value">{{ markers.TERM002.qualityData.mineralWater }}</div>
                  <div class="tds-unit">mg/L</div>
                </div>
              </div>
            </div>
          </div>

          <div class="action-buttons">
            <button class="action-btn primary" @click="showNavigationPopup">
              导航
            </button>
            <button class="action-btn secondary" @click="showWaterQuality">
              查看水质详细
            </button>
          </div>

          <!-- 离线设备提示 -->
          <div v-if="markers.TERM002.status === 'offline'" class="offline-notice">
            设备离线中，暂时无法使用
          </div>
        </div>
      </div>

      <!-- 设备信息弹窗3（图书馆饮水机） -->
      <div v-if="showDevicePopup && currentMarker === 'TERM003'" class="device-popup popup-3">
        <div class="popup-content">
          <div class="popup-title">
            <h3>{{ markers.TERM003.name }}</h3>
            <button class="close-btn" @click="hidePopup">✕</button>
          </div>

          <div class="device-info">
            <div class="info-row">
              <span class="info-label">{{ markers.TERM003.distance }} | ID: {{ markers.TERM003.id }} | 状态：</span>
              <span class="status" :class="markers.TERM003.status">{{ markers.TERM003.statusText }}</span>
            </div>

            <div class="info-row">
              <span class="info-label">当前水质：</span>
              <span class="quality">{{ markers.TERM003.quality }}</span>
            </div>

            <!-- TDS水质数据（离线设备显示-） -->
            <div class="tds-data-section">
              <div class="section-title">水质TDS数据：</div>
              <div class="tds-grid">
                <div class="tds-item">
                  <div class="tds-type">自来水</div>
                  <div class="tds-value">{{ markers.TERM003.qualityData.tapWater }}</div>
                  <div class="tds-unit">mg/L</div>
                </div>
                <div class="tds-item">
                  <div class="tds-type">纯净水</div>
                  <div class="tds-value">{{ markers.TERM003.qualityData.pureWater }}</div>
                  <div class="tds-unit">mg/L</div>
                </div>
                <div class="tds-item">
                  <div class="tds-type">矿化水</div>
                  <div class="tds-value">{{ markers.TERM003.qualityData.mineralWater }}</div>
                  <div class="tds-unit">mg/L</div>
                </div>
              </div>
            </div>
          </div>

          <div class="action-buttons">
            <button class="action-btn primary disabled" disabled>
              导航
            </button>
            <button class="action-btn secondary disabled" disabled>
              查看水质详细
            </button>
          </div>
          <div class="offline-notice">
            设备离线中，暂时无法使用
          </div>
        </div>
      </div>

      <!-- 水质详细弹窗 -->
      <div v-if="showWaterQualityPopup" class="water-quality-popup">
        <div class="popup-content">
          <div class="popup-title">
            <h3>水质详细数据</h3>
            <button class="close-btn" @click="showWaterQualityPopup = false">✕</button>
          </div>

          <!-- 水质数据卡片 -->
          <div class="quality-cards">
            <div class="quality-card">
              <div class="card-title">自来水TDS</div>
              <div class="card-value">285</div>
              <div class="card-unit">mg/L</div>
              <div class="card-trend up">偏高</div>
            </div>

            <div class="quality-card">
              <div class="card-title">纯净水TDS</div>
              <div class="card-value">13</div>
              <div class="card-unit">mg/L</div>
              <div class="card-trend good">优良</div>
            </div>

            <div class="quality-card">
              <div class="card-title">矿化水TDS</div>
              <div class="card-value">87</div>
              <div class="card-unit">mg/L</div>
              <div class="card-trend good">优良</div>
            </div>
          </div>

          <!-- 水质说明 -->
          <div class="quality-explanation">
            <div class="explanation-title">水质说明：</div>
            <div class="explanation-content">
              <div class="explanation-item">
                <span class="explanation-label">自来水TDS：</span>
                <span class="explanation-value">285 mg/L</span>
                <span class="explanation-status up">(偏高)</span>
              </div>
              <div class="explanation-item">
                <span class="explanation-label">纯净水TDS：</span>
                <span class="explanation-value">13 mg/L</span>
                <span class="explanation-status good">(优良)</span>
              </div>
              <div class="explanation-item">
                <span class="explanation-label">矿化水TDS：</span>
                <span class="explanation-value">87 mg/L</span>
                <span class="explanation-status good">(优良)</span>
              </div>
            </div>
            <div class="explanation-text">
              TDS值越低，水质越纯净。纯净水TDS&lt;50为优良，矿化水TDS 50-150为适宜范围。
            </div>
          </div>

          <button class="confirm-btn" @click="showWaterQualityPopup = false">
            确定
          </button>
        </div>
      </div>

      <!-- 导航选择弹窗 -->
      <div v-if="showNavigationSelectPopup" class="navigation-popup">
        <div class="popup-content">
          <div class="popup-title">
            <h3>选择导航应用</h3>
            <button class="close-btn" @click="showNavigationSelectPopup = false">✕</button>
          </div>

          <div class="nav-options">
            <button class="nav-option" @click="openMap('gaode')">
              <div class="nav-icon">🗺️</div>
              <div class="nav-name">高德地图</div>
              <div class="nav-desc">精确导航</div>
            </button>

            <button class="nav-option" @click="openMap('baidu')">
              <div class="nav-icon">📍</div>
              <div class="nav-name">百度地图</div>
              <div class="nav-desc">实时路况</div>
            </button>

            <button class="nav-option" @click="openMap('tencent')">
              <div class="nav-icon">🗺️</div>
              <div class="nav-name">腾讯地图</div>
              <div class="nav-desc">室内导航</div>
            </button>
          </div>

          <button class="cancel-btn" @click="showNavigationSelectPopup = false">
            取消
          </button>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-button active" @click="goToPage('home')">
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

    <!-- 全局遮罩层 -->
    <div v-if="showDevicePopup || showWaterQualityPopup || showNavigationSelectPopup"
         class="popup-overlay"
         @click="hideAllPopups">
    </div>

    <!-- 加载中提示 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { deviceService } from '@/services/deviceService'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 修改标记点配置
const markerConfigs = {
  TERM001: {
    position: { top: '35%', left: '45%' },
    deviceId: 'WM001',
    initialColor: '#04d919'
  },
  TERM002: {
    position: { top: '65%', left: '55%' },
    deviceId: 'WM002',
    initialColor: '#04d919'
  },
  TERM003: {
    position: { top: '50%', left: '25%' },
    deviceId: 'WM003',
    initialColor: '#aaaaaa'
  }
}

// 从后端获取的标记点数据 - 修复重复声明问题
const markers = reactive({
  TERM001: {
    name: '教学楼饮水机',
    distance: '128m',
    id: 'TERM001',
    deviceId: 'WM001',
    status: 'loading',
    statusText: '加载中...',
    quality: '加载中...',
    qualityData: {
      tapWater: '--',
      pureWater: '--',
      mineralWater: '--'
    },
    filterLife: '--',
    waterQualityLevel: '--',
    recordTime: '--'
  },
  TERM002: {
    name: '学生公寓饮水机',
    distance: '156m',
    id: 'TERM002',
    deviceId: 'WM002',
    status: 'loading',
    statusText: '加载中...',
    quality: '加载中...',
    qualityData: {
      tapWater: '--',
      pureWater: '--',
      mineralWater: '--'
    },
    filterLife: '--',
    waterQualityLevel: '--',
    recordTime: '--'
  },
  TERM003: {
    name: '图书馆饮水机',
    distance: '320m',
    id: 'TERM003',
    deviceId: 'WM003',
    status: 'loading',
    statusText: '加载中...',
    quality: '加载中...',
    qualityData: {
      tapWater: '--',
      pureWater: '--',
      mineralWater: '--'
    },
    filterLife: '--',
    waterQualityLevel: '--',
    recordTime: '--'
  }
})

// 计算属性：获取离线状态的标记
const offlineMarkers = computed(() => {
  return Object.values(markers).filter(marker => marker.status === 'offline')
})

// 状态
const showDevicePopup = ref(false)
const showWaterQualityPopup = ref(false)
const showNavigationSelectPopup = ref(false)
const currentMarker = ref('')
const selectedMarker = ref('')
const isLoading = ref(false)

// 从后端获取设备信息
const fetchDeviceInfo = async (terminalId) => {
  try {
    isLoading.value = true
    const result = await deviceService.getTerminalInfo(terminalId)

    if (result.code === 200 && result.data) {
      const data = result.data
      const marker = markers[terminalId]

      if (marker) {
        // 更新标记点信息
        marker.status = data.status === 'active' ? 'online' : 'offline'
        marker.statusText = data.status === 'active' ? '在线' : '离线'
        marker.quality = data.waterQuality || '--'

        // 更新水质数据
        if (data.rawWaterTds) marker.qualityData.tapWater = Math.round(data.rawWaterTds)
        if (data.pureWaterTds) marker.qualityData.pureWater = Math.round(data.pureWaterTds)
        if (data.mineralWaterTds) marker.qualityData.mineralWater = Math.round(data.mineralWaterTds)

        marker.filterLife = data.filterLife || '--'
        marker.waterQualityLevel = data.waterQuality || '--'
        marker.recordTime = data.updateTime || '--'

        // 更新标记点颜色
        updateMarkerColor(terminalId, data.status)
      }
    } else {
      console.error(`获取设备 ${terminalId} 信息失败:`, result.message)
    }
  } catch (error) {
    console.error(`获取设备 ${terminalId} 信息异常:`, error)
    markers[terminalId].status = 'error'
    markers[terminalId].statusText = '获取失败'
  } finally {
    isLoading.value = false
  }
}

// 获取水质信息
const fetchWaterQualityInfo = async (deviceId) => {
  try {
    const result = await deviceService.getWaterQualityInfo(deviceId)
    if (result.code === 200 && result.data) {
      return result.data
    }
  } catch (error) {
    console.error(`获取水质信息失败:`, error)
  }
  return null
}

// 更新标记点颜色
const updateMarkerColor = (terminalId, status) => {
  const markerElement = document.querySelector(`.water-marker.marker-${terminalId} .marker-svg path`)
  if (markerElement) {
    const color = status === 'active' ? '#04d919' : '#aaaaaa'
    markerElement.setAttribute('fill', color)
  }
}

// 显示标记点信息
const showMarkerInfo = async (markerId) => {
  currentMarker.value = markerId
  selectedMarker.value = markerId

  // 如果数据还在加载中，重新获取
  if (markers[markerId].status === 'loading') {
    await fetchDeviceInfo(markerId)
  }

  showDevicePopup.value = true
}

// 隐藏弹窗
const hidePopup = () => {
  showDevicePopup.value = false
  currentMarker.value = ''
  selectedMarker.value = ''
}

// 隐藏所有弹窗
const hideAllPopups = () => {
  hidePopup()
  showWaterQualityPopup.value = false
  showNavigationSelectPopup.value = false
}

// 显示水质详细
const showWaterQuality = async () => {
  if (currentMarker.value && markers[currentMarker.value]) {
    const marker = markers[currentMarker.value]

    // 从后端获取详细水质信息
    const waterQualityData = await fetchWaterQualityInfo(marker.deviceId)
    if (waterQualityData) {
      // 可以在这里设置水质详细弹窗的数据
      console.log('水质详细数据:', waterQualityData)
    }

    // 跳转到水质详情页面
    router.push({
      path: '/water-quality',
      query: {
        terminalId: marker.id,
        deviceId: marker.deviceId
      }
    })

    // 关闭当前弹窗
    hidePopup()
  }
}

// 显示导航选择
const showNavigationPopup = () => {
  showNavigationSelectPopup.value = true
  showDevicePopup.value = false
}

// 打开地图应用
const openMap = (mapType) => {
  const mapNames = {
    gaode: '高德地图',
    baidu: '百度地图',
    tencent: '腾讯地图'
  }

  alert(`将在${mapNames[mapType]}中打开导航`)
  showNavigationSelectPopup.value = false

  // 这里应该实际调用地图应用的URL Scheme
  // 例如：window.location.href = `amapuri://route/...`
}

// 地图控制
const centerMap = () => {
  alert('定位到当前位置')
}

const zoomIn = () => {
  alert('放大地图')
}

const zoomOut = () => {
  alert('缩小地图')
}

// 页面跳转
const goToPage = (page) => {
  switch(page) {
    case 'home':
      // 已经在首页
      break
    case 'scan':
      router.push('/scan')
      break
    case 'profile':
      router.push('/profile')
      break
  }
}

// 页面加载时初始化设备信息
onMounted(() => {
  // 初始化所有设备信息
  Object.keys(markers).forEach(terminalId => {
    fetchDeviceInfo(terminalId)
  })
})
</script>

<style scoped>
.home-page {
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
  z-index: 20;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: white;
  letter-spacing: 1px;
}

/* 地图容器 */
.map-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #e8f4fc;
}

.map-background {
  width: 100%;
  height: 100%;
  position: relative;
  background: linear-gradient(135deg, #a8d8ff 0%, #e3f2fd 100%);
  overflow: hidden;
}

/* 地图网格 */
.map-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  opacity: 0.3;
}

.grid-line {
  position: absolute;
  background: rgba(144, 202, 249, 0.5);
}

.grid-line.horizontal {
  width: 100%;
  height: 1px;
}

.grid-line.vertical {
  height: 100%;
  width: 1px;
}

/* 校园建筑 */
.campus-building {
  position: absolute;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 5;
}

.building-icon {
  font-size: 24px;
  margin-bottom: 4px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.building-name {
  font-size: 10px;
  color: #1976d2;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.8);
  padding: 2px 6px;
  border-radius: 10px;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 道路 */
.road {
  position: absolute;
  background: #78909c;
  border-radius: 3px;
  z-index: 2;
}

/* 绿化带 */
.green-area {
  position: absolute;
  background: linear-gradient(135deg, #a5d6a7 0%, #c8e6c9 100%);
  border-radius: 8px;
  z-index: 1;
  opacity: 0.4;
}

/* 水滴形标记 */
.water-marker {
  position: absolute;
  cursor: pointer;
  transform: translate(-50%, -50%);
  transition: all 0.3s ease;
  z-index: 10;
}

.water-marker:hover,
.water-marker.active {
  transform: translate(-50%, -50%) scale(1.2);
  z-index: 20;
}

.marker-content {
  position: relative;
  width: 24px;
  height: 32px;
}

.marker-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(1px 1px 2.5px rgba(0, 0, 0, 0.7));
}

.marker-pulse {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.water-marker.marker-1 .marker-pulse,
.water-marker.marker-2 .marker-pulse {
  background: rgba(4, 217, 25, 0.3);
}

.water-marker.marker-3 .marker-pulse {
  background: rgba(170, 170, 170, 0.3);
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.5); opacity: 0.2; }
  100% { transform: scale(1); opacity: 0.5; }
}

.marker-label {
  position: absolute;
  top: -24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  font-weight: bold;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  padding: 2px 6px;
  border-radius: 10px;
  white-space: nowrap;
  display: none;
}

.water-marker:hover .marker-label {
  display: block;
}

/* 当前位置标记 */
.current-location-marker {
  position: absolute;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 15;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.1); }
}

.location-icon {
  font-size: 32px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.location-label {
  font-size: 10px;
  color: #1976d2;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 6px;
  border-radius: 10px;
  margin-top: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 地图控制按钮 */
.map-controls {
  position: absolute;
  bottom: 100px;
  right: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 15;
}

.control-btn {
  width: 44px;
  height: 44px;
  background: white;
  border: none;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 16px;
  color: #333;
}

.control-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  background: #f8f9fa;
}

.zoom-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: white;
  border-radius: 22px;
  padding: 8px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.zoom-in-btn,
.zoom-out-btn {
  width: 36px;
  height: 36px;
  align-self: center;
}

.location-btn .control-icon {
  font-size: 20px;
}

.control-text {
  font-size: 10px;
  margin-top: 2px;
  color: #666;
}

/* 弹窗样式 */
.device-popup,
.water-quality-popup,
.navigation-popup {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.25);
  z-index: 100;
  width: 340px;
  animation: slideUp 0.3s ease;
  border: 1px solid #e0e0e0;
}

@keyframes slideUp {
  from {
    transform: translate(-50%, 100%);
    opacity: 0;
  }
  to {
    transform: translate(-50%, 0);
    opacity: 1;
  }
}

.popup-content {
  padding: 20px;
}

.popup-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.popup-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
  flex-shrink: 0;
  margin-left: 12px;
}

.close-btn:hover {
  background: #e8e8e8;
  color: #333;
}

/* 设备信息 */
.device-info {
  margin-bottom: 20px;
}

.info-row {
  font-size: 14px;
  margin-bottom: 12px;
  color: #333;
  display: flex;
  align-items: center;
}

.info-label {
  color: #666;
}

.status {
  font-weight: 600;
  margin-left: 4px;
}

.status.online {
  color: #04d919;
}

.status.offline {
  color: #aaaaaa;
}

.quality {
  font-weight: 600;
  margin-left: 4px;
}

.quality.good {
  color: #04d919;
}

.quality.unknown {
  color: #ff9800;
}

/* TDS水质数据区域 */
.tds-data-section {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 10px;
  border: 1px solid #e8e8e8;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.tds-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.tds-item {
  text-align: center;
  padding: 12px 8px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
}

.tds-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tds-type {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
  font-weight: 500;
}

.tds-value {
  font-size: 20px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 4px;
}

.tds-unit {
  font-size: 11px;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-btn {
  padding: 14px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-btn.primary {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
}

.action-btn.secondary {
  background: #f0f5ff;
  color: #1890ff;
  border: 1px solid #d6e4ff;
}

.action-btn.disabled {
  background: #f5f5f5;
  color: #999;
  border: 1px solid #e8e8e8;
  cursor: not-allowed;
}

.action-btn:hover:not(.disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.action-btn.primary:hover:not(.disabled) {
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.offline-notice {
  margin-top: 12px;
  padding: 8px 12px;
  background: #fff8e1;
  border: 1px solid #ffecb3;
  border-radius: 6px;
  font-size: 12px;
  color: #ff9800;
  text-align: center;
}

/* 水质详细弹窗 */
.quality-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.quality-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 16px 12px;
  text-align: center;
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
  margin-bottom: 6px;
}

.card-trend {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  display: inline-block;
}

.card-trend.good {
  background: rgba(4, 217, 25, 0.1);
  color: #04d919;
}

.card-trend.up {
  background: rgba(255, 152, 0, 0.1);
  color: #ff9800;
}

.quality-explanation {
  background: #f0f5ff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
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

.confirm-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.confirm-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

/* 导航选择弹窗 */
.nav-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.nav-option {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 16px 12px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.nav-option:hover {
  border-color: #1890ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: #f8faff;
}

.nav-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.nav-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
}

.nav-desc {
  font-size: 10px;
  color: #999;
}

.cancel-btn {
  width: 100%;
  padding: 14px;
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 16px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: #e8e8e8;
}

/* 底部导航栏 */
.bottom-nav {
  height: 60px;
  background: white;
  border-top: 1px solid #e8e8e8;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  z-index: 10;
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

.nav-button.active {
  color: #1890ff;
}

.nav-button:hover {
  background: #f8f9fa;
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.nav-text {
  font-size: 12px;
  font-weight: 500;
}

/* 遮罩层 */
.popup-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 50;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 响应式调整 */
@media (max-width: 420px) {
  .home-page {
    width: 100%;
    height: 100vh;
  }

  .device-popup,
  .water-quality-popup,
  .navigation-popup {
    width: calc(100% - 32px);
    bottom: 70px;
  }

  .nav-options {
    grid-template-columns: repeat(2, 1fr);
  }

  .tds-grid {
    grid-template-columns: 1fr;
  }
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
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
</style>