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
      <!-- 告警表格 - 居中显示 -->
      <div class="content-card">
        <div class="card-header">
          <h3 class="card-title">告警信息表格</h3>
          <div class="table-controls">
            <!-- 搜索框 -->
            <input
              v-model="searchQuery"
              placeholder="搜索设备ID或告警信息..."
              class="search-input"
            />
            <!-- 筛选下拉框 -->
            <select v-model="filterStatus" class="filter-select">
              <option value="">全部状态</option>
              <option value="pending">待处理</option>
              <option value="processing">处理中</option>
              <option value="resolved">已解决</option>
              <option value="closed">已关闭</option>
            </select>
            <select v-model="filterLevel" class="filter-select">
              <option value="">全部级别</option>
              <option value="critical">紧急</option>
              <option value="error">错误</option>
              <option value="warning">警告</option>
              <option value="info">信息</option>
            </select>
          </div>
        </div>

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
        <div v-else-if="filteredAlerts.length === 0" class="empty-state">
          <div class="empty-icon">✅</div>
          <div class="empty-text">暂无告警信息</div>
        </div>

        <!-- 告警表格 -->
        <div v-else class="table-container">
          <table class="alert-table">
            <thead>
              <tr>
                <th>告警ID</th>
                <th>设备ID</th>
                <th>告警类型</th>
                <th>告警级别</th>
                <th>告警信息</th>
                <th>区域ID</th>
                <th>状态</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="alert in filteredAlerts" :key="alert.alertId">
                <td>{{ alert.alertId }}</td>
                <td>{{ alert.deviceId }}</td>
                <td>{{ alert.alertType }}</td>
                <td>
                  <span :class="['alert-level-badge', alert.alertLevel?.toLowerCase()]">
                    {{ formatAlertLevel(alert.alertLevel) }}
                  </span>
                </td>
                <td class="alert-message">{{ alert.alertMessage }}</td>
                <td>{{ alert.areaId }}</td>
                <td>
                  <span :class="['status-badge', alert.status?.toLowerCase()]">
                    {{ formatAlertStatus(alert.status) }}
                  </span>
                </td>
                <td>{{ formatDateTime(alert.timestamp) }}</td>
                <td>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- 分页组件 -->
          <div class="pagination-controls">
            <div class="page-size-selector">
              <label>每页显示：</label>
              <select v-model="pageSize" @change="handlePageSizeChange">
                <option :value="5">5条</option>
                <option :value="10">10条</option>
                <option :value="20">20条</option>
                <option :value="50">50条</option>
              </select>
            </div>

            <div class="pagination">
              <button
                @click="changePage(1)"
                :disabled="currentPage === 1 || totalPages === 0"
                class="pagination-btn"
              >
                首页
              </button>
              <button
                @click="changePage(currentPage - 1)"
                :disabled="currentPage === 1 || totalPages === 0"
                class="pagination-btn"
              >
                上一页
              </button>

              <span class="pagination-info">
                {{ currentPage }} / {{ totalPages }} (共 {{ totalAlerts }} 条)
              </span>

              <button
                @click="changePage(currentPage + 1)"
                :disabled="currentPage === totalPages || totalPages === 0"
                class="pagination-btn"
              >
                下一页
              </button>
              <button
                @click="changePage(totalPages)"
                :disabled="currentPage === totalPages || totalPages === 0"
                class="pagination-btn"
              >
                末页
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
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

const allAlerts = ref<Alert[]>([])
const latestAlert = ref<Alert | null | undefined>(null)
const loadingAlerts = ref(false)
const showAlertNotification = ref(true)
const alertPermissionError = ref(false)

// 表格筛选和搜索相关
const searchQuery = ref('')
const filterStatus = ref('')
const filterLevel = ref('')

// 分页相关变量
const currentPage = ref(1)
const pageSize = ref(10)

// 获取路由和认证store实例
const router = useRouter()
const authStore = useAuthStore()

// 计算筛选后的总告警数
const totalFilteredAlerts = computed(() => {
  return allAlerts.value.filter(alert => {
    // 搜索条件：设备ID或告警信息
    const matchesSearch = !searchQuery.value ||
      alert.deviceId.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      alert.alertMessage.toLowerCase().includes(searchQuery.value.toLowerCase())

    // 状态筛选
    const matchesStatus = !filterStatus.value || alert.status === filterStatus.value

    // 级别筛选 - 修改为直接比较原始值
    const matchesLevel = !filterLevel.value ||
      (alert.alertLevel && alert.alertLevel.toLowerCase() === filterLevel.value.toLowerCase())

    return matchesSearch && matchesStatus && matchesLevel
  })
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(totalFilteredAlerts.value.length / pageSize.value)
})

// 计算当前页数据
const filteredAlerts = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return totalFilteredAlerts.value.slice(startIndex, endIndex)
})

// 添加总告警数计算
const totalAlerts = computed(() => totalFilteredAlerts.value.length)

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

// 格式化告警状态
const formatAlertStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    'pending': '待处理',
    'processing': '处理中',
    'resolved': '已解决',
    'closed': '已关闭'
  }
  return statusMap[status?.toLowerCase()] || status
}

// 格式化时间
const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取统计数据
const fetchStatsData = async () => {
  try {
    const token = authStore.token

    // 检查token是否存在
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    // 尝试使用备用统计方法
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const areaId = userInfo.areaId || ''

      // 构建区域参数
      const areaParam = areaId ? `?areaId=${areaId}` : ''

      // 分别获取各状态设备数量
      const [onlineResult, offlineResult, faultResult] = await Promise.allSettled([
        request<ResultVO<any[]>>(`/api/web/device-status/by-status?status=online${areaParam}`, { method: 'GET' }),
        request<ResultVO<any[]>>(`/api/web/device-status/by-status?status=offline${areaParam}`, { method: 'GET' }),
        request<ResultVO<any[]>>(`/api/web/device-status/by-status?status=fault${areaParam}`, { method: 'GET' })
      ])

      // 处理在线设备
      if (onlineResult.status === 'fulfilled' && onlineResult.value.code === 200) {
        stats.value.onlineDevices = onlineResult.value.data?.length || 0
      } else {
        stats.value.onlineDevices = 0
      }

      // 处理离线设备
      if (offlineResult.status === 'fulfilled' && offlineResult.value.code === 200) {
        stats.value.offlineDevices = offlineResult.value.data?.length || 0
      } else {
        stats.value.offlineDevices = 0
      }

      // 处理故障设备
      if (faultResult.status === 'fulfilled' && faultResult.value.code === 200) {
        stats.value.alertDevices = faultResult.value.data?.length || 0
      } else {
        stats.value.alertDevices = 0
      }

      // 计算设备总数
      stats.value.totalDevices = stats.value.onlineDevices + stats.value.offlineDevices + stats.value.alertDevices
    } catch (statusError) {
      console.error('获取设备状态统计失败，使用默认值:', statusError)
      // 设置默认值
      stats.value.onlineDevices = 0
      stats.value.offlineDevices = 0
      stats.value.alertDevices = 0
      stats.value.totalDevices = 0
    }

    // 获取待处理工单数
    const workOrderResult = await request<ResultVO<any[]>>(
        '/api/work-orders/by-status?status=pending',
        { method: 'GET' }
    )

    if (workOrderResult.code === 200 && workOrderResult.data) {
      stats.value.pendingWorkOrders = workOrderResult.data.length
    } else {
      stats.value.pendingWorkOrders = 0
    }

  } catch (error: any) {
    console.error('获取统计数据失败:', error)
    error.message?.includes('401')
        ? '登录已过期，请重新登录'
        : error.message?.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试';
// 如果是认证错误，登出并跳转到登录页
    if (error.message?.includes('401')) {
      authStore.logout()
      await router.push('/login')
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
      await router.push('/login')
      return
    }

    // 获取所有告警 - 使用现有的pending接口，或需要后端提供全量接口
    const alertResult = await request<ResultVO<Alert[]>>(
        '/api/alerts/pending',
        { method: 'GET' }
    )

    console.log('告警接口返回:', alertResult)

    if (alertResult.code === 200 && alertResult.data) {
      // 确保数据是数组
      const alerts = Array.isArray(alertResult.data) ? alertResult.data : []

      // 按时间倒序排序（最新的在前面）
      const sortedAlerts = [...alerts].sort((a, b) => {
        // 将时间字符串转换为Date对象进行比较
        const timeA = new Date(a.timestamp).getTime()
        const timeB = new Date(b.timestamp).getTime()
        return timeB - timeA // 降序排列（最新的在前）
      })

      allAlerts.value = sortedAlerts

      // 设置最新告警（如果存在告警数据）
      if (sortedAlerts.length > 0) {
        latestAlert.value = sortedAlerts[0] // 最新的告警
      } else {
        latestAlert.value = null
      }
    } else {
      latestAlert.value = null
    }
  } catch (error: any) {
    console.error('获取告警数据失败:', error)

    // 特别处理403权限错误
    if (error.message?.includes('403')) {
      console.warn('当前用户无权限访问告警数据')
      alertPermissionError.value = true
      allAlerts.value = []
      latestAlert.value = null
      return
    }
    error.message?.includes('401')
        ? '登录已过期，请重新登录'
        : error.message?.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试';
// 如果是认证错误，登出并跳转到登录页
    if (error.message?.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
    latestAlert.value = null
  } finally {
    loadingAlerts.value = false
  }
}

// 查看告警详情
// 处理告警
// 分页控制方法
const changePage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const changePageSize = (size: number) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
}

// 添加类型安全的页面大小变更处理方法
const handlePageSizeChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  if (target) {
    const size = Number(target.value)
    changePageSize(size)
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.table-controls {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 200px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
}

/* 表格样式 */
.table-container {
  overflow-x: auto;
}

.alert-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  margin-top: 16px;
}

.alert-table th,
.alert-table td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.alert-table th {
  background-color: #f5f5f5;
  font-weight: 600;
  color: #333;
  position: sticky;
  top: 0;
}

.alert-table tbody tr:hover {
  background-color: #f9f9f9;
}

.alert-message {
  max-width: 200px;
  word-wrap: break-word;
}

.alert-level-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  min-width: 50px;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  min-width: 60px;
}

/* 分页样式 */
.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-selector select {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.pagination-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 4px;
  font-size: 14px;
}

.pagination-btn:hover:not(:disabled) {
  background: #f0f0f0;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  margin: 0 15px;
  font-size: 14px;
  color: #666;
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

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .card-header {
    flex-direction: column;
    align-items: stretch;
  }

  .table-controls {
    width: 100%;
  }

  .search-input {
    min-width: unset;
    width: 100%;
  }

  .pagination-controls {
    flex-direction: column;
    gap: 15px;
  }
}
</style>
