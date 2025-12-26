<template>
  <div class="realtime-data-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-title">实时数据详情</div>
      <button class="back-btn" @click="goBack">‹</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-section">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载实时数据中...</div>
    </div>

    <!-- 主要内容区域 -->
    <div v-else class="main-content">
      <!-- 终端信息 -->
      <div class="section-card">
        <h2 class="section-title">终端设备信息</h2>
        <div class="data-grid">
          <div class="data-item">
            <span class="data-label">终端ID</span>
            <span class="data-value">{{ realtimeData.terminalInfo.terminalId }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">终端名称</span>
            <span class="data-value">{{ realtimeData.terminalInfo.terminalName }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">终端状态</span>
            <span class="data-value status" :class="realtimeData.terminalInfo.terminalStatus.toLowerCase()">{{ realtimeData.terminalInfo.terminalStatus }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">安装日期</span>
            <span class="data-value">{{ realtimeData.terminalInfo.installDate }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">数据更新时间</span>
            <span class="data-value">{{ realtimeData.updateTime }}</span>
          </div>
        </div>
      </div>

      <!-- 制水机信息 -->
      <div class="section-card" v-if="realtimeData.makerDevice">
        <h2 class="section-title">制水机实时数据</h2>
        <div class="data-grid">
          <div class="data-item">
            <span class="data-label">设备ID</span>
            <span class="data-value">{{ realtimeData.makerDevice.deviceId }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">设备类型</span>
            <span class="data-value">{{ realtimeData.makerDevice.realtimeData.deviceType }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">在线状态</span>
            <span class="data-value status" :class="realtimeData.makerDevice.realtimeData.onlineStatus.toLowerCase()">{{ realtimeData.makerDevice.realtimeData.onlineStatus }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">TDS值</span>
            <span class="data-value">{{ realtimeData.makerDevice.realtimeData.tdsValue }} mg/L</span>
          </div>
          <div class="data-item">
            <span class="data-label">pH值</span>
            <span class="data-value">{{ realtimeData.makerDevice.realtimeData.phValue }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">出水温度</span>
            <span class="data-value">{{ realtimeData.makerDevice.realtimeData.temperature }}℃</span>
          </div>
          <div class="data-item">
            <span class="data-label">滤芯寿命</span>
            <span class="data-value">{{ realtimeData.makerDevice.realtimeData.filterLife }}%</span>
          </div>
          <div class="data-item">
            <span class="data-label">流量</span>
            <span class="data-value">{{ realtimeData.makerDevice.realtimeData.flowRate }} L/min</span>
          </div>
          <div class="data-item">
            <span class="data-label">累计用水量</span>
            <span class="data-value">{{ realtimeData.makerDevice.realtimeData.totalUsage }} L</span>
          </div>
          <div class="data-item">
            <span class="data-label">故障码</span>
            <span class="data-value">{{ realtimeData.makerDevice.realtimeData.faultCode }}</span>
          </div>
        </div>
      </div>

      <!-- 供水机信息 -->
      <div class="section-card" v-if="realtimeData.supplyDevice">
        <h2 class="section-title">供水机实时数据</h2>
        <div class="data-grid">
          <div class="data-item">
            <span class="data-label">设备ID</span>
            <span class="data-value">{{ realtimeData.supplyDevice.deviceId }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">设备类型</span>
            <span class="data-value">{{ realtimeData.supplyDevice.realtimeData.deviceType }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">在线状态</span>
            <span class="data-value status" :class="realtimeData.supplyDevice.realtimeData.onlineStatus.toLowerCase()">{{ realtimeData.supplyDevice.realtimeData.onlineStatus }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">水压</span>
            <span class="data-value">{{ realtimeData.supplyDevice.realtimeData.waterPressure }} MPa</span>
          </div>
          <div class="data-item">
            <span class="data-label">水箱水位</span>
            <span class="data-value">{{ realtimeData.supplyDevice.realtimeData.waterLevel }}%</span>
          </div>
          <div class="data-item">
            <span class="data-label">水泵状态</span>
            <span class="data-value">{{ realtimeData.supplyDevice.realtimeData.pumpStatus }}</span>
          </div>
          <div class="data-item">
            <span class="data-label">工作电压</span>
            <span class="data-value">{{ realtimeData.supplyDevice.realtimeData.voltage }} V</span>
          </div>
          <div class="data-item">
            <span class="data-label">累计运行时长</span>
            <span class="data-value">{{ realtimeData.supplyDevice.realtimeData.runHours }} h</span>
          </div>
          <div class="data-item">
            <span class="data-label">故障码</span>
            <span class="data-value">{{ realtimeData.supplyDevice.realtimeData.faultCode }}</span>
          </div>
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
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { deviceService } from '@/services/deviceService'

export default {
  name: 'RealtimeDataPage',
  setup() {
    const router = useRouter()
    const route = useRoute()

    const isLoading = ref(true)
    const realtimeData = ref({})

    // 获取实时数据
    const fetchRealtimeData = async () => {
      try {
        const terminalId = route.query.terminalId
        if (!terminalId) {
          console.error('缺少终端ID参数')
          return
        }

        const response = await deviceService.getRealtimeData(terminalId)

        if (response.code === 200) {
          realtimeData.value = response
        } else {
          console.error('获取实时数据失败:', response.msg)
          // 显示错误信息或使用默认值
          realtimeData.value = {
            terminalInfo: {},
            makerDevice: {},
            supplyDevice: {}
          }
        }
      } catch (error) {
        console.error('获取实时数据异常:', error)
        // 可以设置默认值或错误提示
      } finally {
        isLoading.value = false
      }
    }

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

    onMounted(() => {
      fetchRealtimeData()
    })

    return {
      isLoading,
      realtimeData,
      goBack,
      goToPage
    }
  }
}
</script>

<style scoped>
.realtime-data-page {
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

/* 加载状态 */
.loading-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1890ff;
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

/* 数据卡片 */
.section-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.data-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.data-item {
  display: flex;
  flex-direction: column;
  padding: 8px 0;
}

.data-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.data-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.status {
  font-weight: 600;
}

.status.online {
  color: #52c41a;
}

.status.offline {
  color: #f5222d;
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
  background: #f5f5f5;
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
  line-height: 1;
}
</style>
