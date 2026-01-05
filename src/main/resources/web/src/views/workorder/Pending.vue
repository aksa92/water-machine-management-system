<template>
  <div class="order-to-claim-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>待抢单工单</h2>
      <div class="breadcrumb">校园矿化水平台 / 工单管理 / 待抢单</div>
    </div>

    <!-- 检索筛选区 -->
    <div class="filter-bar">
      <!-- 工单号/设备ID搜索 -->
      <div class="filter-item search-item">
        <label>搜索：</label>
        <input
            type="text"
            v-model="searchKeyword"
            class="search-input"
            placeholder="输入工单号或设备ID搜索"
            @input="handleSearch"
        >
      </div>

      <!-- 校区筛选 -->
      <div class="filter-item">
        <label>所属校区：</label>
        <select v-model="filterForm.area" class="filter-select" @change="handleFilter">
          <option value="">全部校区</option>
          <option
            v-for="areaOption in areaOptions"
            :key="areaOption"
            :value="areaOption"
          >
            {{ areaOption }}
          </option>
        </select>
      </div>

      <!-- 日期筛选 -->
      <div class="filter-item">
        <label>创建日期：</label>
        <input
            type="date"
            v-model="filterForm.createDate"
            class="filter-input"
            @change="handleFilter"
        >
      </div>

      <!-- 重置按钮 -->
      <button class="btn-reset" @click="resetFilter">重置筛选</button>
    </div>

    <!-- 工单表格 -->
    <div class="card">
      <table class="order-table">
        <thead>
        <tr>
          <th>工单号</th>
          <th>设备</th>
          <th>校区</th>
          <th>问题描述</th>
          <th>状态</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="order in paginatedOrders" :key="order.id">
          <td>{{ order.orderNo }}</td>
          <td>
            <div class="device-info">
              <div class="device-id">{{ order.deviceId }}</div>
            </div>
          </td>
          <td>{{ order.area }}</td>
          <td class="desc-cell">{{ order.problemDesc }}</td>
          <td>
              <span :class="`status-tag ${order.status}`">
                {{ formatStatus(order.status) }}
              </span>
          </td>
          <td>{{ order.createTime }}</td>
          <td class="operation-buttons">
            <button class="btn-view" @click="viewOrderDetail(order)">查看详情</button>
          </td>
        </tr>
        <tr v-if="filteredOrders.length === 0">
          <td colspan="7" class="no-data">
            {{ loading ? '正在加载数据...' : '暂无待抢单工单' }}
          </td>
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

    <!-- 工单详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>工单详情</h3>
          <button class="modal-close" @click="closeDetailModal">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-item">
            <span class="detail-label">工单号：</span>
            <span class="detail-value">{{ currentOrder?.orderNo }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">建单时间：</span>
            <span class="detail-value">{{ currentOrder?.createTime }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">设备机型：</span>
            <span class="detail-value">{{ currentOrder?.deviceType }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">设备ID：</span>
            <span class="detail-value">{{ currentOrder?.deviceId }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">警告内容：</span>
            <span class="detail-value">{{ currentOrder?.problemDesc }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">设备最后上传时间：</span>
            <span class="detail-value">{{ currentOrder?.lastUploadTime }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">位置：</span>
            <span class="detail-value">{{ currentOrder?.location }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">当前状态：</span>
            <span :class="`detail-status status-tag ${currentOrder?.status}`">
              {{ currentOrder ? formatStatus(currentOrder.status) : '' }}
            </span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-close" @click="closeDetailModal">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
// 1. 导入必要的工具和状态管理
import { useRouter } from 'vue-router'
import { request } from '@/api/request'  // 导入项目封装的请求工具
import { useAuthStore } from '@/stores/auth'  // 导入 authStore

// 2. 实例化路由和 authStore
const router = useRouter()
const authStore = useAuthStore()

// 工单状态类型 - 与后端实体保持一致
type OrderStatus = 'pending' | 'grabbed' | 'processing' | 'completed' | 'closed' | 'timeout'

// 工单数据接口（适配后端WorkOrder实体）
interface ToClaimOrder {
  id: string
  orderNo: string
  deviceType: string // 设备机型（制水机/供水机）
  deviceId: string // 设备ID
  area: string // 所属校区
  problemDesc: string // 问题描述（警告内容）
  status: OrderStatus // 工单状态
  createTime: string // 创建时间（建单时间）
  lastUploadTime: string // 设备最后上传时间
  location: string // 设备位置
  description?: string // 详细描述
  priority?: string // 优先级
}

// 响应式数据
const orders = ref<ToClaimOrder[]>([])
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const loading = ref(false)

// 搜索关键词（工单号/设备ID）
const searchKeyword = ref('')

// 筛选表单数据
const filterForm = ref({
  area: '', // 校区筛选
  createDate: '' // 日期筛选
})

// 弹窗相关状态
const showDetailModal = ref(false)
const currentOrder = ref<ToClaimOrder | null>(null)

// 计算所有唯一的校区选项
const areaOptions = computed(() => {
  const areas = new Set<string>()
  orders.value.forEach(order => {
    if (order.area) {
      areas.add(order.area)
    }
  })
  return Array.from(areas).sort()
})

// 格式化状态显示 - 适配后端状态枚举
const formatStatus = (status: OrderStatus): string => {
  const statusMap = {
    pending: '待抢单',
    grabbed: '已抢单',
    processing: '处理中',
    completed: '已完成',
    closed: '已关闭',
    timeout: '超时未抢'
  }
  return statusMap[status] || status
}

// 加载待抢单工单数据
// 3. 重构 loadAvailableOrders 方法，与 Admin.vue 保持一致的 Token 处理逻辑
const loadAvailableOrders = async () => {
  loading.value = true
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 构建查询参数
    let url = ''
    const params = new URLSearchParams()

    // 判断是否启用时间筛选
    if (filterForm.value.createDate) {
      // 使用 by-time-range 接口进行更精确的时间筛选
      url = '/api/work-orders/by-time-range'

      // 设置起止时间为一天的开始和结束
      const startDate = new Date(filterForm.value.createDate)
      const endDate = new Date(startDate)
      endDate.setDate(endDate.getDate() + 1)

      params.append('startTime', startDate.toISOString())
      params.append('endTime', endDate.toISOString())
      params.append('status', 'pending') // 添加状态筛选
    } else {
      // 默认使用 available 接口，不传递校区参数
      url = '/api/work-orders/available'
      params.append('status', 'pending')
    }

    const queryString = params.toString()
    if (queryString) {
      url += `?${queryString}`
    }

    const response = await request<{
      code: number
      msg: string
      data: any[]
    }>(url, {
      method: 'GET',
    })

    if (response.code === 200) {
      orders.value = (response.data || []).map((order: any) => ({
        id: order.orderId || '',
        orderNo: order.orderId || '',
        deviceType: '未知设备',
        deviceId: order.deviceId || '',
        area: order.areaName || order.areaId || '', // 使用 areaName，如果不存在则使用 areaId
        problemDesc: order.description || '暂无描述',
        status: order.status || 'pending',
        createTime: order.createdTime ? new Date(order.createdTime).toLocaleString('zh-CN') : '未知时间',
        lastUploadTime: order.updatedTime ? new Date(order.updatedTime).toLocaleString('zh-CN') : '未知时间',
        location: '未知位置',
        description: order.description,
        priority: order.priority
      }))
    } else {
      const errorMsg = response.msg || `获取失败（错误码：${response.code}）`
      console.error('获取待抢单工单失败:', errorMsg)
      alert(`获取待抢单工单失败：${errorMsg}`)
    }
  } catch (error: any) {
    console.error('请求异常:', error)
    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'
    alert(`获取待抢单工单失败：${errorMsg}`)
  } finally {
    loading.value = false
  }
}


// 筛选后的工单列表
const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    // 工单号/设备ID搜索匹配
    const keywordMatch = searchKeyword.value.trim() === '' ||
        order.orderNo.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        order.deviceId.toLowerCase().includes(searchKeyword.value.toLowerCase())

    // 校区筛选（现在是校区名筛选）
    const areaMatch = filterForm.value.area === '' || order.area === filterForm.value.area

    return keywordMatch && areaMatch
  })
})

// 分页后的工单列表
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredOrders.value.slice(start, end)
})

// 分页计算
const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / pageSize)
})

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1 // 搜索后重置到第一页
  // 如果需要根据搜索关键词调用后端接口，可以在这里添加
  // 目前是前端筛选，如果数据量大，建议调用后端搜索接口
}

// 处理筛选（校区/日期）
const handleFilter = () => {
  currentPage.value = 1 // 筛选后重置到第一页
  // 如果是日期筛选，需要重新加载数据；如果是校区筛选，只需重新计算筛选结果
  if (filterForm.value.createDate) {
    loadAvailableOrders() // 日期筛选需要重新加载数据
  }
  // 校区筛选不需要重新加载数据，因为是在前端筛选
}

// 重置筛选条件
const resetFilter = () => {
  searchKeyword.value = '' // 清空搜索关键词
  filterForm.value = {
    area: '',
    createDate: ''
  }
  currentPage.value = 1
  loadAvailableOrders() // 重置后重新加载所有数据
}

// 查看工单详情（打开弹窗）
const viewOrderDetail = (order: ToClaimOrder) => {
  currentOrder.value = order
  showDetailModal.value = true
}

// 关闭详情弹窗
const closeDetailModal = () => {
  showDetailModal.value = false
  currentOrder.value = null
}

// 页面加载时获取数据
onMounted(() => {
  console.log('Token:', authStore.token)
  loadAvailableOrders()
})
</script>

<style scoped>
.order-to-claim-page {
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

/* 筛选区样式 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #4e5969;
  font-weight: 500;
}

/* 搜索输入框样式 */
.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 240px;
}

.filter-select, .filter-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 160px;
}

.btn-reset {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.btn-reset:hover {
  background-color: #f0f0f0;
}

/* 表格样式 */
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

/* 设备信息样式 */
.device-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-type {
  font-weight: 500;
  color: #333;
}

.device-id {
  font-size: 12px;
  color: #666;
}

/* 问题描述单元格 */
.desc-cell {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}

.desc-cell:hover {
  white-space: normal;
  overflow: visible;
  background-color: white;
  z-index: 10;
  position: relative;
}

/* 状态标签样式 - 适配后端状态 */
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

.status-tag.grabbed {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-tag.processing {
  background-color: #d6e4ff;
  color: #1677ff;
}

.status-tag.completed {
  background-color: #e6f7ee;
  color: #00875a;
}

.status-tag.closed {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

.status-tag.timeout {
  background-color: #ffebe6;
  color: #cf1322;
}

/* 操作按钮样式 */
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
  transition: opacity 0.3s;
  background-color: #e6f7ff;
  color: #1890ff;
}

.btn-view:hover {
  opacity: 0.9;
}

/* 空数据样式 */
.no-data {
  text-align: center;
  padding: 40px 0;
  color: #8c8c8c;
}

/* 分页样式 */
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

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  max-width: 500px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #666;
  padding: 0;
  line-height: 1;
}

.modal-body {
  padding: 20px;
}

.detail-item {
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
}

.detail-label {
  flex: 0 0 120px;
  color: #666;
  font-size: 14px;
}

.detail-value {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.detail-status {
  margin-top: 2px;
}

.modal-footer {
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
}

.btn-close {
  padding: 6px 16px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.btn-close:hover {
  background-color: #e9ecef;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-item {
    width: 100%;
  }

  .search-input, .filter-select, .filter-input {
    width: 100%;
  }

  .modal-content {
    width: 90%;
  }

  .detail-label {
    flex: 0 0 100px;
  }
}
</style>
