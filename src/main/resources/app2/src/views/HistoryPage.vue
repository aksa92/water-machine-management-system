<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 历史记录数据
const historyList = ref([])

// 从本地存储加载历史记录
const loadHistoryFromStorage = () => {
  const savedHistory = localStorage.getItem('waterHistory')
  console.log('从本地存储加载历史记录:', savedHistory)

  if (savedHistory) {
    try {
      const parsedHistory = JSON.parse(savedHistory)

      // 去重：只保留每个设备的最新一条记录
      const deviceMap = new Map()
      parsedHistory.forEach(record => {
        const deviceId = record.deviceId || 'unknown'
        // 只保留每个设备的最新记录
        if (!deviceMap.has(deviceId) ||
            new Date(record.timestamp) > new Date(deviceMap.get(deviceId).timestamp)) {
          deviceMap.set(deviceId, record)
        }
      })

      // 转换为数组并排序（按时间倒序）
      const uniqueHistory = Array.from(deviceMap.values())
          .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
          .map(record => ({
            id: record.id || Date.now(),
            date: record.date || formatDate(record.timestamp),
            deviceName: record.deviceName || getDeviceName(record.deviceId),
            deviceId: record.deviceId || '未知',
            time: record.time || formatTime(record.timestamp),
            amount: record.amount || '0ml',
            location: record.location || getDeviceLocation(record.deviceId)
          }))

      historyList.value = uniqueHistory
      console.log('处理后历史记录:', historyList.value)

    } catch (error) {
      console.error('加载历史记录失败:', error)
      loadDefaultHistory()
    }
  } else {
    loadDefaultHistory()
  }
}

// 根据deviceId获取设备名称
const getDeviceName = (deviceId) => {
  const deviceNames = {
    'TERM001': '教学楼饮水机',
    'TERM002': '学生公寓饮水机',
    'TERM003': '图书馆饮水机'
  }
  return deviceNames[deviceId] || `${deviceId}饮水机`
}

// 根据deviceId获取设备位置
const getDeviceLocation = (deviceId) => {
  const deviceLocations = {
    'TERM001': '教学楼1F大厅',
    'TERM002': '天马学生公寓1F',
    'TERM003': '图书馆2F'
  }
  return deviceLocations[deviceId] || '未知位置'
}

// 加载默认历史记录
const loadDefaultHistory = () => {
  historyList.value = []
  console.log('没有历史记录')
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '今日'

  const date = new Date(timestamp)
  const today = new Date()

  if (date.toDateString() === today.toDateString()) {
    return '今日'
  }

  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨日'
  }

  return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })

  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 计算属性：按日期分组
const dateGroupedRecords = computed(() => {
  const grouped = {}
  historyList.value.forEach(record => {
    if (record.date !== '今日' && record.date !== '昨日') {
      if (!grouped[record.date]) {
        grouped[record.date] = []
      }
      grouped[record.date].push(record)
    }
  })
  return grouped
})

// 计算属性：今日记录
const todayRecords = computed(() => {
  return historyList.value.filter(record => record.date === '今日')
})

// 计算属性：昨日记录
const yesterdayRecords = computed(() => {
  return historyList.value.filter(record => record.date === '昨日')
})

// 清空历史记录
const clearHistory = () => {
  if (confirm('确定要清空所有历史记录吗？')) {
    localStorage.removeItem('waterHistory')
    historyList.value = []
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
  loadHistoryFromStorage()
})
</script>

<template>
  <div class="history-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-title">历史记录</div>
      <button class="back-btn" @click="goBack">‹</button>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 统计信息和操作 -->
      <div class="summary-info">
        <div class="total-count">
          共{{ historyList.length }}条记录
        </div>
        <button class="clear-btn" @click="clearHistory" v-if="historyList.length > 0">
          清空记录
        </button>
      </div>

      <!-- 历史记录列表 -->
      <div class="history-list">
        <!-- 今日 -->
        <div class="date-group" v-if="todayRecords.length > 0">
          <div class="group-title">今日</div>
          <div class="records-container">
            <div
                v-for="record in todayRecords"
                :key="record.id"
                class="record-card"
            >
              <div class="record-header">
                <div class="device-info">
                  <div class="device-name">{{ record.deviceName }}</div>
                  <div class="device-location">{{ record.location }}</div>
                </div>
                <div class="record-time">{{ record.time }}</div>
              </div>
              <div class="record-details">
                <div class="water-amount">{{ record.amount }}</div>
                <div class="device-id">ID: {{ record.deviceId }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 昨日 -->
        <div class="date-group" v-if="yesterdayRecords.length > 0">
          <div class="group-title">昨日</div>
          <div class="records-container">
            <div
                v-for="record in yesterdayRecords"
                :key="record.id"
                class="record-card"
            >
              <div class="record-header">
                <div class="device-info">
                  <div class="device-name">{{ record.deviceName }}</div>
                  <div class="device-location">{{ record.location }}</div>
                </div>
                <div class="record-time">{{ record.time }}</div>
              </div>
              <div class="record-details">
                <div class="water-amount">{{ record.amount }}</div>
                <div class="device-id">ID: {{ record.deviceId }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 其他日期 -->
        <div
            class="date-group"
            v-for="(records, date) in dateGroupedRecords"
            :key="date"
        >
          <div class="group-title">{{ date }}</div>
          <div class="records-container">
            <div
                v-for="record in records"
                :key="record.id"
                class="record-card"
            >
              <div class="record-header">
                <div class="device-info">
                  <div class="device-name">{{ record.deviceName }}</div>
                  <div class="device-location">{{ record.location }}</div>
                </div>
                <div class="record-time">{{ record.time }}</div>
              </div>
              <div class="record-details">
                <div class="water-amount">{{ record.amount }}</div>
                <div class="device-id">ID: {{ record.deviceId }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 没有记录时显示 -->
        <div class="empty-state" v-if="historyList.length === 0">
          <div class="empty-icon">📊</div>
          <div class="empty-text">暂无历史记录</div>
          <div class="empty-hint">开始取水后会在这里显示记录</div>
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

      <div class="nav-button active" @click="goToPage('profile')">
        <div class="nav-icon">👤</div>
        <div class="nav-text">我的</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.history-page {
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
  padding: 20px 0;
  overflow-y: auto;
}

/* 统计信息 */
.summary-info {
  padding: 0 16px 16px;
}

.total-count {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

/* 历史记录列表 */
.history-list {
  padding: 0 16px;
}

.date-group {
  margin-bottom: 20px;
}

.group-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 4px solid #1890ff;
}

.records-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.record-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e8e8e8;
  transition: all 0.3s;
}

.record-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #1890ff;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.device-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  flex: 1;
  margin-right: 12px;
}

.record-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.record-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.water-amount {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
}

.device-id {
  font-size: 12px;
  color: #666;
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 12px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.3;
}

.empty-text {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: #999;
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
  .history-page {
    width: 100%;
    height: 100vh;
  }

  .main-content {
    padding: 16px 0;
  }

  .record-card {
    padding: 12px;
  }

  .device-name {
    font-size: 13px;
  }
}

.clear-btn {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.clear-btn:hover {
  background: #e8e8e8;
  color: #ff4d4f;
  border-color: #ff4d4f;
}

.summary-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px 16px;
}

/* 添加设备位置信息样式 */
.device-location {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}
</style>