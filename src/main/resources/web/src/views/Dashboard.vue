<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统概览</h2>
      <div class="breadcrumb">校园矿化水平台/仪表盘</div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stats-card">
        <div class="stats-icon">📊</div>
        <div class="stats-number">{{ stats.totalDevices }}</div>
        <div class="stats-label">设备总数</div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">🟢</div>
        <div class="stats-number">{{ stats.onlineDevices }}</div>
        <div class="stats-label">在线数量</div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">⚠️</div>
        <div class="stats-number">{{ stats.alertDevices }}</div>
        <div class="stats-label">告警设备</div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">📋</div>
        <div class="stats-number">{{ stats.pendingWorkOrders }}</div>
        <div class="stats-label">待处理工单</div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">🔴</div>
        <div class="stats-number">{{ stats.offlineDevices }}</div>
        <div class="stats-label">离线设备</div>
      </div>
    </div>

    <!-- 警告通知 -->
    <div v-if="latestAlert" class="alert-warning">
      <div class="alert-icon">⚠️</div>
      <div class="alert-content">
        <span>设备 {{ latestAlert.deviceId }} 异常，请关注！</span>
      </div>
      <div class="alert-close">×</div>
    </div>

    <!-- 主要内容区域 - 修改为单列布局 -->
    <div class="content-single-column">
      <!-- 最新告警 - 居中显示 -->
      <div class="content-card">
        <h3 class="card-title">最新告警</h3>
        <div class="alert-list">
          <div v-for="alert in recentAlerts" :key="alert.id" class="alert-item">
            <div class="alert-text">{{ alert.deviceId }}：{{ alert.message }}</div>
            <div :class="['alert-level', alert.level.toLowerCase()]">{{ formatAlertLevel(alert.level) }}</div>
          </div>
        </div>
      </div>

      <!-- 工单状态统计 -->
      <div class="content-card">
        <h3 class="card-title">工单状态统计</h3>
        <div class="chart-placeholder">
          <div class="placeholder-text">
            <div style="font-size: 16px; margin-bottom: 8px;">Axhub Charts</div>
            <div style="font-size: 24px; margin-bottom: 12px;">柱状图</div>
            <div style="color: #666; font-size: 12px;">
              通过Group内data和config中继器可更改数据及配置
            </div>
            <div style="color: #666; font-size: 12px;">
              详情访问：https://axhub.im/charts
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import type { ResultVO } from '@/api/types/auth'

// 定义数据结构
interface StatsData {
  totalDevices: number
  onlineDevices: number
  offlineDevices: number
  alertDevices: number
  pendingWorkOrders: number
}

interface Alert {
  id: string
  deviceId: string
  message: string
  level: string
  timestamp: string
}

// 响应式数据
const stats = ref<StatsData>({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0,
  alertDevices: 0,
  pendingWorkOrders: 0
})

const recentAlerts = ref<Alert[]>([])
const latestAlert = ref<Alert | null>(null)
const onlinePercentage = ref<number>(0)

// 获取路由和认证store实例
const router = useRouter()
const authStore = useAuthStore()

// 格式化告警级别
const formatAlertLevel = (level: string): string => {
  const levelMap: Record<string, string> = {
    'CRITICAL': '紧急',
    'ERROR': '错误',
    'WARNING': '警告'
  }
  return levelMap[level] || level
}

// 获取统计数据
const fetchStatsData = async () => {
  try {
    const token = authStore.token

    // 检查token是否存在
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 获取设备状态统计
    const statusCountResult = await request<ResultVO<Record<string, number>>>(
      '/api/web/device-status/status-count',
      { method: 'GET' }
    )

    if (statusCountResult.code === 200 && statusCountResult.data) {
      const data = statusCountResult.data
      stats.value.onlineDevices = data.online || 0
      stats.value.offlineDevices = data.offline || 0
      stats.value.alertDevices = data.fault || 0
      stats.value.totalDevices = (data.online || 0) + (data.offline || 0) + (data.fault || 0)

      // 计算在线设备百分比
      if (stats.value.totalDevices > 0) {
        onlinePercentage.value = Math.round((stats.value.onlineDevices / stats.value.totalDevices) * 100)
      } else {
        onlinePercentage.value = 0
      }
    }

    // 获取待处理工单数
    const workOrderResult = await request<ResultVO<any[]>>(
      '/api/work-orders/by-status?status=pending',
      { method: 'GET' }
    )

    if (workOrderResult.code === 200 && workOrderResult.data) {
      stats.value.pendingWorkOrders = workOrderResult.data.length
    }

  } catch (error: any) {
    console.error('获取统计数据失败:', error)
    const errorMsg = error.message?.includes('401')
        ? '登录已过期，请重新登录'
        : error.message?.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'

    // 如果是认证错误，登出并跳转到登录页
    if (error.message?.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 获取告警数据
const fetchAlertData = async () => {
  try {
    const token = authStore.token

    // 检查token是否存在
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 获取最新告警
    const alertResult = await request<ResultVO<Alert[]>>(
      '/api/alerts/pending',
      { method: 'GET' }
    )

    if (alertResult.code === 200 && alertResult.data) {
      recentAlerts.value = alertResult.data.slice(0, 5) // 只取前5条
      if (recentAlerts.value.length > 0) {
        latestAlert.value = recentAlerts.value[0] || null;
      }
    }
  } catch (error: any) {
    console.error('获取告警数据失败:', error)

    // 特别处理403权限错误
    if (error.message?.includes('403')) {
      console.warn('当前用户无权限访问告警数据')
      // 清空告警数据但不显示错误提示
      recentAlerts.value = []
      latestAlert.value = null
      return
    }

    const errorMsg = error.message?.includes('401')
        ? '登录已过期，请重新登录'
        : error.message?.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'

    // 如果是认证错误，登出并跳转到登录页
    if (error.message?.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}


// 组件挂载时获取数据
onMounted(() => {
  fetchStatsData()
  fetchAlertData()
})
</script>

<style scoped>
.dashboard {
  /* 移除 padding，因为 MainLayout 的 content-wrapper 已经有 padding */
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.breadcrumb {
  color: #666;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stats-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
}

.stats-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.stats-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 14px;
  color: #666;
}

.alert-warning {
  background: #fff8e6;
  border-left: 4px solid #ffc107;
  padding: 16px;
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  border-radius: 4px;
}

.alert-icon {
  font-size: 20px;
  margin-right: 12px;
  color: #ff9800;
}

.alert-content {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.alert-close {
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

/* 修改为单列布局 */
.content-single-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 16px;
  text-align: center; /* 标题居中 */
}

.alert-list {
  display: flex;
  flex-direction: column;
}

.alert-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.alert-text {
  font-size: 14px;
  color: #333;
}

.alert-level {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.alert-level.critical {
  background: #ffebee;
  color: #c62828;
}

.alert-level.error {
  background: #ffebee;
  color: #c62828;
}

.alert-level.warning {
  background: #fff8e1;
  color: #ff8f00;
}

.alert-divider {
  height: 1px;
  background: #f0f0f0;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 4px;
}

.placeholder-text {
  text-align: center;
  color: #666;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
