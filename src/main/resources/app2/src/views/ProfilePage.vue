<template>
  <div class="profile-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-title">个人主页</div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 用户信息卡片 -->
      <div class="user-card">
        <div class="avatar-section">
          <div class="avatar-circle">
            <div class="avatar-text">{{ userInfo.lastName }}</div>
          </div>
          <div class="user-name">{{ userInfo.fullName }}</div>
        </div>

        <div class="divider"></div>

        <!-- 用户详细信息 -->
        <div class="user-details">
          <div class="detail-item">
            <span class="detail-label">学号：</span>
            <span class="detail-value">{{ userInfo.studentId }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">学院：</span>
            <span class="detail-value">{{ userInfo.college }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">班级：</span>
            <span class="detail-value">{{ userInfo.class }}</span>
          </div>
        </div>
      </div>

      <!-- 数据统计卡片 -->
      <div class="stats-section">
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-value">{{ userStats.totalConsumption }}ml</div>
            <div class="stat-label">累计饮水量</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ userStats.drinkCount }}</div>
            <div class="stat-label">饮水次数</div>
          </div>
        </div>

        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-value">{{ userStats.avgDaily }}ml</div>
            <div class="stat-label">日均饮水量</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ userStats.days }}天</div>
            <div class="stat-label">饮水天数</div>
          </div>
        </div>
      </div>

      <!-- 时间切换 -->
      <div class="time-switch">
        <div
            class="time-option"
            :class="{ active: selectedPeriod === 'today' }"
            @click="selectPeriod('today')"
        >
          今日
        </div>
        <div
            class="time-option"
            :class="{ active: selectedPeriod === 'week' }"
            @click="selectPeriod('week')"
        >
          本周
        </div>
        <div
            class="time-option"
            :class="{ active: selectedPeriod === 'month' }"
            @click="selectPeriod('month')"
        >
          本月
        </div>
      </div>

      <!-- 饮水数据图表 -->
      <div class="chart-section">
        <div class="chart-title">饮水统计图表</div>
        <div class="chart-container">
          <!-- 柱状图容器 -->
          <div class="chart-placeholder" v-if="!showChart">
            <div class="placeholder-text">数据加载中...</div>
          </div>

          <!-- 模拟柱状图 -->
          <div class="mock-chart" v-else>
            <div class="chart-axis">
              <!-- Y轴 -->
              <div class="y-axis">
                <div class="y-label">{{ maxChartValue }}ml</div>
                <div class="y-label">{{ Math.round(maxChartValue * 0.75) }}ml</div>
                <div class="y-label">{{ Math.round(maxChartValue * 0.5) }}ml</div>
                <div class="y-label">{{ Math.round(maxChartValue * 0.25) }}ml</div>
                <div class="y-label">0ml</div>
              </div>

              <!-- 柱状图 -->
              <div class="bars-container">
                <div
                    v-for="(item, index) in chartData"
                    :key="index"
                    class="bar-item"
                >
                  <div
                      class="bar"
                      :style="{ height: calculateBarHeight(item.value) + 'px' }"
                      :class="{ active: item.active }"
                  >
                    <div class="bar-value">{{ item.value }}ml</div>
                  </div>
                  <div class="bar-label">{{ item.label }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 每日详情 -->
      <div class="daily-details-section">
        <div class="section-title">每日详情</div>
        <div class="daily-details-list">
          <div
            v-for="detail in dailyDetails"
            :key="detail.date"
            class="daily-detail-item"
          >
            <div class="detail-date">{{ formatDate(detail.date) }}</div>
            <div class="detail-consumption">{{ detail.consumption }}ml</div>
            <div class="detail-count">{{ detail.count }}次</div>
          </div>

          <div v-if="dailyDetails.length === 0" class="no-data">
            暂无饮水记录
          </div>
        </div>
      </div>

      <!-- 功能按钮 -->
      <div class="action-section">
        <button class="action-btn history-btn" @click="goToHistory">
          <span class="btn-icon">📊</span>
          <span class="btn-text">查看历史饮水记录</span>
          <span class="btn-arrow">›</span>
        </button>

        <button class="action-btn setting-btn" @click="goToSettings">
          <span class="btn-icon">⚙️</span>
          <span class="btn-text">设置</span>
          <span class="btn-arrow">›</span>
        </button>

        <button class="action-btn logout-btn" @click="handleLogout">
          <span class="btn-icon">🚪</span>
          <span class="btn-text">退出登录</span>
        </button>
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

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { studentDrinkStatsService } from '@/services/studentDrinkStatsService'

const router = useRouter()
const userStore = useUserStore()

// 用户信息
const userInfo = reactive({
  lastName: '',
  fullName: '',
  studentId: '',
  college: '信息科学与工程学院',
  class: '软件2301班'
})

// 饮水量统计数据
const userStats = reactive({
  totalConsumption: 0,
  drinkCount: 0,
  avgDaily: 0,
  days: 0,
  timeRange: '',
  timeDimension: ''
})

// 每日详情
const dailyDetails = ref([])

// 图表数据
const selectedPeriod = ref('today')
const showChart = ref(false)
const chartData = ref([])
const maxChartValue = ref(0)

// 获取饮水统计
const fetchDrinkStats = async (period) => {
  try {
    let response
    const studentId = userStore.studentId

    if (!studentId) {
      console.error('未获取到学生ID')
      return
    }

    // 根据选择的时间段调用不同的接口
    if (period === 'today') {
      response = await studentDrinkStatsService.getTodayStats(studentId)
    } else if (period === 'week') {
      response = await studentDrinkStatsService.getThisWeekStats(studentId)
    } else if (period === 'month') {
      response = await studentDrinkStatsService.getThisMonthStats(studentId)
    }

    if (response.code === 200) {
      const data = response.data

      // 更新统计信息
      userStats.totalConsumption = data.totalConsumption
      userStats.drinkCount = data.drinkCount
      userStats.avgDaily = data.avgDailyConsumption
      userStats.days = data.dailyDetails.length
      userStats.timeRange = data.timeRange
      userStats.timeDimension = data.timeDimension

      // 更新每日详情
      dailyDetails.value = data.dailyDetails || []

      // 更新图表数据
      updateChartData(data.dailyDetails, period)
    } else {
      console.error('获取饮水统计失败:', response.message)
    }
  } catch (error) {
    console.error('获取饮水统计异常:', error)
  }
}

// 更新图表数据
const updateChartData = (details, period) => {
  if (!details || details.length === 0) {
    chartData.value = []
    maxChartValue.value = 0
    return
  }

  // 计算最大值用于Y轴
  const values = details.map(item => item.consumption)
  maxChartValue.value = Math.max(...values, 100) // 最小值设为100

  // 根据时间段格式化数据
  if (period === 'today') {
    // 今日模式 - 显示最近几天的数据
    chartData.value = details.map((item, index) => ({
      label: formatDate(item.date).split('-').slice(1).join('/'),
      value: item.consumption,
      active: index === details.length - 1 // 最后一个为当前日期
    }))
  } else {
    // 本周/本月模式 - 显示所有日期数据
    chartData.value = details.map((item, index) => ({
      label: formatDate(item.date).split('-').slice(1).join('/'),
      value: item.consumption,
      active: index === details.length - 1 // 最后一个为当前日期
    }))
  }
}

// 计算柱状图高度
const calculateBarHeight = (value) => {
  if (maxChartValue.value === 0) return 0
  // 设置最大高度为120px，根据数值比例计算
  return (value / maxChartValue.value) * 120
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  // 如果是YYYY-MM-DD格式，直接返回
  if (dateString.includes('-')) {
    return dateString
  }
  // 如果是其他格式，转换为YYYY-MM-DD
  return dateString
}

// 选择时间段
const selectPeriod = async (period) => {
  selectedPeriod.value = period
  showChart.value = false

  // 获取对应时间段的统计信息
  await fetchDrinkStats(period)

  // 模拟加载效果
  setTimeout(() => {
    showChart.value = true
  }, 300)
}

// 跳转到历史记录
const goToHistory = () => {
  router.push('/history')
}

// 跳转到设置
const goToSettings = () => {
  alert('设置页面（待开发）')
}

// 退出登录
const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    // 清除用户状态
    userStore.clearUser()

    // 跳转到登录页
    router.push('/')
  }
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
      // 已经在个人中心
      break
  }
}

onMounted(async () => {
  // 从用户状态获取信息
  if (userStore.isLoggedIn) {
    userInfo.studentId = userStore.studentId
    userInfo.fullName = userStore.username
    userInfo.lastName = userStore.username.charAt(0)
  }

  // 获取今日饮水统计
  await fetchDrinkStats('today')

  // 显示图表
  setTimeout(() => {
    showChart.value = true
  }, 500)
})
</script>

<style scoped>
.profile-page {
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

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 20px 16px;
  overflow-y: auto;
}

/* 用户信息卡片 */
.user-card {
  background: white;
  border-radius: 16px;
  padding: 24px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.avatar-section {
  text-align: center;
  margin-bottom: 20px;
}

.avatar-circle {
  width: 70px;
  height: 70px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  border: 3px solid #f2f2f2;
}

.avatar-text {
  font-size: 28px;
  font-weight: 600;
  color: white;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e8e8e8, transparent);
  margin: 20px 0;
}

/* 用户详细信息 */
.user-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.detail-label {
  color: #666;
  min-width: 60px;
}

.detail-value {
  color: #333;
  font-weight: 500;
}

/* 数据统计卡片 */
.stats-section {
  margin-bottom: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #e8e8e8;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

/* 时间切换 */
.time-switch {
  display: flex;
  background: white;
  border-radius: 25px;
  padding: 4px;
  margin-bottom: 20px;
  border: 1px solid #e8e8e8;
}

.time-option {
  flex: 1;
  text-align: center;
  padding: 10px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-radius: 21px;
  transition: all 0.3s;
}

.time-option.active {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  font-weight: 500;
}

/* 图表区域 */
.chart-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-left: 4px;
  border-left: 4px solid #1890ff;
}

.chart-container {
  min-height: 200px;
  position: relative;
}

.chart-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px dashed #e8e8e8;
}

.placeholder-text {
  color: #999;
  font-size: 14px;
}

/* 模拟柱状图 */
.mock-chart {
  height: 200px;
  position: relative;
}

.chart-axis {
  display: flex;
  height: 100%;
  padding-left: 40px;
  position: relative;
}

.y-axis {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  width: 40px;
}

.y-label {
  font-size: 10px;
  color: #999;
  transform: translateY(50%);
}

.bars-container {
  flex: 1;
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 100%;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  width: 30px;
}

.bar {
  width: 20px;
  background: linear-gradient(to top, #409eff, #66b1ff);
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: height 0.5s ease;
  margin-bottom: 4px;
}

.bar.active {
  background: linear-gradient(to top, #ff6b6b, #ff8e8e);
}

.bar-value {
  position: absolute;
  top: -24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #666;
  white-space: nowrap;
}


.bar-label {
  font-size: 10px;
  color: #666;
  margin-top: 4px;
}

/* 每日详情区域 */
.daily-details-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.daily-details-section .section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-left: 4px;
  border-left: 4px solid #1890ff;
}

.daily-details-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.daily-detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.detail-date {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.detail-consumption {
  font-size: 14px;
  color: #1890ff;
  font-weight: 600;
}

.detail-count {
  font-size: 12px;
  color: #666;
  background: #e8f4fc;
  padding: 4px 8px;
  border-radius: 10px;
}

.no-data {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

/* 功能按钮区域 */
.action-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-icon {
  font-size: 20px;
  margin-right: 12px;
}

.btn-text {
  flex: 1;
  text-align: left;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.btn-arrow {
  color: #999;
  font-size: 18px;
}

.history-btn:hover {
  border-color: #409eff;
  background: #f0f5ff;
}

.setting-btn:hover {
  border-color: #67c23a;
  background: #f0f9eb;
}

.logout-btn:hover {
  border-color: #f56c6c;
  background: #fef0f0;
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
  .profile-page {
    width: 100%;
    height: 100vh;
  }

  .main-content {
    padding: 16px 12px;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .user-card {
    padding: 20px 16px;
  }

  .bars-container {
    gap: 4px;
  }

  .bar-item {
    width: 25px;
  }
}
</style>
