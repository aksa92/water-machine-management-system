<!-- src/views/personnel/MaintenanceRecord.vue -->
<template>
  <div class="record-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>维修记录详情</h2>
      <div class="breadcrumb">校园矿化水平台 / 人员管理 / 维修人员 / 维修记录</div>
    </div>

    <!-- 返回按钮 -->
   <div class="back-button">
   <button @click="goBack" class="btn-back">
      ← 返回维修人员列表
    </button>
   </div>


    <!-- 维修人员信息 -->
    <div class="repairman-info card">
      <h3>维修人员信息</h3>
      <div class="info-content">
        <div class="info-item">
          <span class="label">姓名：</span>
          <span>{{ repairmanInfo.repairmanName }}</span>
        </div>
        <div class="info-item">
          <span class="label">联系电话：</span>
          <span>{{ repairmanInfo.phone }}</span>
        </div>
        <div class="info-item">
          <span class="label">维修片区：</span>
          <span>{{ repairmanInfo.areaId }}</span>
        </div>
        <div class="info-item">
          <span class="label">状态：</span>
          <span :class="`status-tag ${repairmanInfo.status}`">
            {{ getStatusText(repairmanInfo.status) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 工单统计 -->
    <div class="order-stats">
      <div class="stat-card">
        <div class="stat-number">{{ processingOrders.length }}</div>
        <div class="stat-label">处理中工单</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ completedOrders.length }}</div>
        <div class="stat-label">已完成工单</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ totalOrders.length }}</div>
        <div class="stat-label">总工单数</div>
      </div>
    </div>

    <!-- 工单分类标签 -->
    <div class="order-tabs">
      <button
        :class="{ active: activeTab === 'all' }"
        @click="activeTab = 'all'"
      >
        全部工单
      </button>
      <button
        :class="{ active: activeTab === 'processing' }"
        @click="activeTab = 'processing'"
      >
        处理中 ({{ processingOrders.length }})
      </button>
      <button
        :class="{ active: activeTab === 'completed' }"
        @click="activeTab = 'completed'"
      >
        已完成 ({{ completedOrders.length }})
      </button>
    </div>

    <!-- 工单表格 -->
    <div class="card">
      <table class="order-table">
        <thead>
          <tr>
            <th>工单号</th>
            <th>设备ID</th>
            <th>片区</th>
            <th>问题描述</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in displayedOrders" :key="order.orderId">
            <td>{{ order.orderId }}</td>
            <td>{{ order.deviceId }}</td>
            <td>{{ order.areaId }}</td>
            <td class="desc-cell">{{ order.description }}</td>
            <td>
              <span :class="`status-tag ${order.status}`">
                {{ formatOrderStatus(order.status) }}
              </span>
            </td>
            <td>{{ formatDate(order.createdTime) }}</td>
            <td class="operation-buttons">
              <button class="btn-view" @click="viewOrderDetail(order.orderId)">
                查看详情
              </button>
            </td>
          </tr>
          <tr v-if="displayedOrders.length === 0">
            <td colspan="7" class="no-data">暂无工单记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页控件 -->
    <div class="pagination">
      <button
        class="page-btn"
        :disabled="currentPage === 1"
        @click="currentPage--"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页 (共 {{ filteredOrders.length }} 条记录)
      </span>
      <button
        class="page-btn"
        :disabled="currentPage === totalPages"
        @click="currentPage++"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { request } from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import type { WorkOrder } from '@/api/types/workorder'

// 定义接口
interface RepairmanInfo {
  repairmanId: string
  repairmanName: string
  phone: string
  areaId: string
  status: RepairmanStatus // 明确指定枚举类型
}


// 枚举类型
type OrderStatus = 'pending' | 'processing' | 'reviewing' | 'completed' | 'timeout'
type RepairmanStatus = 'idle' | 'busy' | 'vacation'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const repairmanInfo = ref<RepairmanInfo>({
  repairmanId: '',
  repairmanName: '',
  phone: '',
  areaId: '',
  status: 'idle'
})

const allOrders = ref<WorkOrder[]>([])
const activeTab = ref<'all' | 'processing' | 'completed'>('all')
const currentPage = ref(1)
const pageSize = 10
const loading = ref(false)

// 获取维修人员信息和工单数据
const fetchRepairmanData = async () => {
  loading.value = true
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    const repairmanId = route.params.id as string

    // 这里需要调用获取维修人员信息的接口（如果有的话）
    // 暂时先设置一个默认值
    repairmanInfo.value.repairmanId = repairmanId
    repairmanInfo.value.repairmanName = '维修人员姓名'
    repairmanInfo.value.phone = '13800138000'
    repairmanInfo.value.areaId = '市区'
    repairmanInfo.value.status = 'idle'

    // 获取该维修人员的所有工单
    const response = await request<{
      code: number
      msg: string
      data: WorkOrder[]
    }>(`/api/work-orders/my?repairmanId=${repairmanId}`, {
      method: 'GET'
    })

    if (response.code === 200) {
      allOrders.value = response.data || []
    } else {
      const errorMsg = response.msg || `获取失败（错误码：${response.code}）`
      console.error('获取工单列表失败:', errorMsg)
      alert(`获取工单列表失败：${errorMsg}`)
    }
  } catch (error: any) {
    console.error('请求异常:', error)
    const errorMsg = error.message.includes('401')
        ? '登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'
    alert(`获取数据失败：${errorMsg}`)

    if (error.message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 处理中的工单
const processingOrders = computed(() => {
  return allOrders.value.filter(order =>
    order.status === 'processing' ||
    order.status === 'pending' ||
    order.status === 'reviewing'
  )
})

// 已完成的工单
const completedOrders = computed(() => {
  return allOrders.value.filter(order => order.status === 'completed')
})

// 总工单数
const totalOrders = computed(() => {
  return allOrders.value
})

// 根据当前标签页筛选工单
const filteredOrders = computed(() => {
  switch (activeTab.value) {
    case 'processing':
      return processingOrders.value
    case 'completed':
      return completedOrders.value
    default:
      return totalOrders.value
  }
})

// 分页后的工单
const displayedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredOrders.value.slice(start, end)
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / pageSize)
})

// 获取维修人员状态文本
const getStatusText = (status: RepairmanStatus): string => {
  const statusMap: Record<RepairmanStatus, string> = {
    'idle': '空闲',
    'busy': '忙碌',
    'vacation': '休假'
  }
  return statusMap[status] || status
}

// 格式化工单状态显示
const formatOrderStatus = (status: OrderStatus): string => {
  const statusMap: Record<OrderStatus, string> = {
    'pending': '待抢单',
    'processing': '处理中',
    'reviewing': '待审核',
    'completed': '已完成',
    'timeout': '超时未抢'
  }
  return statusMap[status] || status
}

// 格式化日期
const formatDate = (dateString?: string): string => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 查看工单详情
const viewOrderDetail = (orderId: string) => {
  // 跳转到工单详情页面（需要根据实际路由调整）
  router.push(`/home/work-order/detail/${orderId}`)
}

// 返回上一页
const goBack = () => {
  router.back()
}


// 页面加载时获取数据
// MaintenanceRecord.vue
onMounted(() => {
  const repairmanId = route.params.id as string
  if (!repairmanId) {
    alert('维修人员ID不能为空')
    router.push('/home/personnel/maintenance')
    return
  }

  fetchRepairmanData()
})

</script>

<style scoped>
.record-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.breadcrumb {
  color: #666;
  font-size: 14px;
}

.card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.repairman-info h3 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
  color: #333;
}

.info-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  font-weight: 500;
  color: #666;
  margin-right: 8px;
  min-width: 80px;
}

.order-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #42b983;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.order-tabs {
  display: flex;
  gap: 1px;
  background: #ddd;
  border-radius: 4px;
  margin-bottom: 20px;
  overflow: hidden;
}

.order-tabs button {
  flex: 1;
  padding: 12px 16px;
  border: none;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.order-tabs button.active {
  background: #42b983;
  color: white;
}

.order-table {
  width: 100%;
  border-collapse: collapse;
}

.order-table th,
.order-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.order-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.order-table tbody tr:hover {
  background-color: #f8f9fa;
}

.desc-cell {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.pending {
  background-color: #fff7e6;
  color: #d48806;
}

.status-tag.processing {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-tag.reviewing {
  background-color: #f6f7ff;
  color: #667eea;
}

.status-tag.completed {
  background-color: #e6f7ee;
  color: #00875a;
}

.status-tag.timeout {
  background-color: #ffebe6;
  color: #cf1322;
}

.status-tag.idle {
  background-color: #e6f7ee;
  color: #00875a;
}

.status-tag.busy {
  background-color: #fffbe6;
  color: #d48806;
}

.status-tag.vacation {
  background-color: #f0f0f0;
  color: #8c8c8c;
}

.operation-buttons {
  display: flex;
  gap: 8px;
}

.btn-view {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  background-color: #e6f7ff;
  color: #1890ff;
  transition: opacity 0.3s;
}

.btn-view:hover {
  opacity: 0.9;
}

.no-data {
  text-align: center;
  padding: 40px 0;
  color: #8c8c8c;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  color: #666;
  font-size: 14px;
}

.page-btn {
  padding: 4px 12px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .order-stats {
    flex-direction: column;
    gap: 12px;
  }

  .info-content {
    grid-template-columns: 1fr;
  }

  .order-tabs {
    flex-direction: column;
  }

  .btn-back {
  background: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-back:hover {
  background: #e0e0e0;
  border-color: #bbb;
}


}
</style>
