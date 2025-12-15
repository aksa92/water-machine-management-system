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
    <div v-if="latestAlert && showAlertNotification" class="alert-warning">
      <div class="alert-icon">⚠️</div>
      <div class="alert-content">
        <span>设备 {{ latestAlert.deviceId }} 异常，请关注！</span>
        <div class="alert-detail">{{ latestAlert.alertMessage }}</div>
      </div>
      <div class="alert-close" @click="showAlertNotification = false">×</div>
    </div>

    <!-- 主要内容区域 - 修改为单列布局 -->
    <div class="content-single-column">
      <!-- 最新告警 - 居中显示 -->
      <div class="content-card">
        <h3 class="card-title">最新告警</h3>

        <!-- 加载状态 -->
        <div v-if="loadingAlerts" class="loading-state">
          <div class="loading-spinner"></div>
          <div class="loading-text">加载告警数据中...</div>
        </div>

        <!-- 权限错误提示 -->
        <div v-else-if="alertPermissionError" class="permission-error">
          <div class="error-icon">🔒</div>
          <div class="error-content">
            <div class="error-title">权限受限</div>
            <div class="error-text">当前用户无告警查看权限</div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="recentAlerts.length === 0" class="empty-state">
          <div class="empty-icon">✅</div>
          <div class="empty-text">暂无告警信息</div>
        </div>

        <!-- 告警列表 -->
        <div v-else class="alert-list">
          <div v-for="alert in recentAlerts" :key="alert.alertId" class="alert-item">
            <div class="alert-text">{{ alert.deviceId }}：{{ alert.alertMessage }}</div>
            <div class="alert-meta">
              <div :class="['alert-level', alert.alertLevel?.toLowerCase()]">
                {{ formatAlertLevel(alert.alertLevel) }}
              </div>
              <div v-if="alert.areaId" class="alert-area">{{ alert.areaId }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 工单状态统计 -->
      <div class="content-card">
        <h3 class="card-title">工单状态统计</h3>
        <div class="chart-placeholder">
          <div class="placeholder-text">
            <div style="font-size: 16px; margin-bottom: 8px;">待处理工单数: {{ stats.pendingWorkOrders }}</div>
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

// 定义数据结构 - 修正为与后端实体匹配
interface StatsData {
  totalDevices: number
  onlineDevices: number
  offlineDevices: number
  alertDevices: number
  pendingWorkOrders: number
}

// 修正：与后端 Alert 实体字段保持一致
interface Alert {
  alertId: number
  deviceId: string
  alertType: string
  alertLevel: string  // 'info' | 'warning' | 'error' | 'critical'
  alertMessage: string
  areaId: string
  status: string      // 'pending' | 'processing' | 'resolved' | 'closed'
  timestamp: string
  resolvedTime?: string
  resolvedBy?: string
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
const loadingAlerts = ref(false)
const showAlertNotification = ref(true)
const alertPermissionError = ref(false)

// 获取路由和认证store实例
const router = useRouter()
const authStore = useAuthStore()

// 格式化告警级别
const formatAlertLevel = (level: string): string => {
  const levelMap: Record<string, string> = {
    'critical': '紧急',
    'error': '错误',
    'warning': '警告',
    'info': '信息'
  }
  return levelMap[level?.toLowerCase()] || level
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
  loadingAlerts.value = true
  alertPermissionError.value = false

  try {
    const token = authStore.token

    // 检查token是否存在
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 获取最新告警 - 使用正确的接口
    const alertResult = await request<ResultVO<Alert[]>>(
        '/api/alerts/pending',
        { method: 'GET' }
    )

    console.log('告警接口返回:', alertResult)

    if (alertResult.code === 200 && alertResult.data) {
      // 确保数据是数组
      const alerts = Array.isArray(alertResult.data) ? alertResult.data : []

      // 只取前5条
      recentAlerts.value = alerts.slice(0, 5)

      // 如果有告警，设置最新告警
      if (recentAlerts.value.length > 0) {
  const sortedAlerts = [...recentAlerts.value].sort((a, b) => {
    const priorityMap: Record<string, number> = {
      'critical': 4,
      'error': 3,
      'warning': 2,
      'info': 1
    };
    return (priorityMap[b.alertLevel?.toLowerCase()] || 0) - (priorityMap[a.alertLevel?.toLowerCase()] || 0);
  });
  latestAlert.value = sortedAlerts[0] ?? null; // 明确处理 undefined 情况
} else {
  latestAlert.value = null;
}

    }
  } catch (error: any) {
    console.error('获取告警数据失败:', error)

    // 特别处理403权限错误
    if (error.message?.includes('403')) {
      console.warn('当前用户无权限访问告警数据')
      alertPermissionError.value = true
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
  } finally {
    loadingAlerts.value = false
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

.alert-detail {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.alert-close {
  font-size: 20px;
  cursor: pointer;
  color: #999;
  padding: 0 8px;
}

.alert-close:hover {
  color: #666;
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
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
  text-align: center;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 14px;
  color: #666;
}

/* 权限错误提示 */
.permission-error {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 6px;
  margin: 10px 0;
}

.error-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #6c757d;
}

.error-content {
  flex: 1;
}

.error-title {
  font-size: 14px;
  font-weight: 600;
  color: #495057;
  margin-bottom: 4px;
}

.error-text {
  font-size: 12px;
  color: #6c757d;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
  color: #666;
}

/* 告警列表样式 */
.alert-list {
  display: flex;
  flex-direction: column;
}

.alert-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.alert-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-level {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 500;
  min-width: 50px;
  text-align: center;
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

.alert-level.info {
  background: #e8f5e9;
  color: #2e7d32;
}

.alert-area {
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
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

  .alert-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>