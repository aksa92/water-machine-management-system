<template>
  <div class="order-processing-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>处理中工单</h2>
      <div class="breadcrumb">校园矿化水平台 / 工单管理 / 处理中</div>
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

      <!-- 片区筛选 -->
      <div class="filter-item">
        <label>所属片区：</label>
        <select v-model="filterForm.area" class="filter-select" @change="handleFilter">
          <option value="">全部片区</option>
          <option value="A">A</option>
          <option value="B">B</option>
          <option value="C">C</option>
          <option value="D">D</option>
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

    <!-- 工表格 -->
    <div class="card">
      <table class="order-table">
        <thead>
          <tr>
            <th>工单号</th>
            <th>设备</th>
            <th>片区</th>
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
                <div class="device-type">{{ order.deviceType }}</div>
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
              <button class="btn-view" @click="viewOrderDetail(order.id)">查看详情</button>
            </td>
          </tr>
          <tr v-if="filteredOrders.length === 0">
            <td colspan="7" class="no-data">{{ loading ? '正在加载数据...' : '暂无处理中工单' }}</td>
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

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-container" @click.stop>
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
            <span class="detail-label">接单师傅：</span>
            <span class="detail-value">{{ currentOrder?.maintenanceName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">师傅电话：</span>
            <span class="detail-value">{{ currentOrder?.maintenancePhone }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">当前状态：</span>
            <span :class="`detail-value status-tag ${currentOrder?.status}`">
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
import { useRouter } from 'vue-router'
import { request } from '@/api/request'
import { useAuthStore } from '@/stores/auth'

// 工单状态类型
type OrderStatus = 'timeout' | 'pending' | 'processing' | 'reviewing' | 'completed'

// 工单数据接口（扩展设备相关信息和维修人员信息）
interface ProcessingOrder {
  id: string
  orderNo: string
  deviceType: string // 设备机型（制水机/供水机）
  deviceId: string // 设备ID
  area: string // 所属片区
  problemDesc: string // 问题描述（警告内容）
  status: OrderStatus // 工单state
  createTime: string // 创建时间（建单时间）
  lastUploadTime: string // 设备最后上传时间
  location: string // 设备位置
  maintenanceName: string // 接单师傅姓名
  maintenancePhone: string // 接单师傅电话
}

// 响应式数据
const orders = ref<ProcessingOrder[]>([])
const currentPage = ref(1)
const pageSize = 10 // 每显示数量
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

// 搜索关键词（工单号/设备ID）
const searchKeyword = ref('')

// 筛选表单数据
const filterForm = ref({
  area: '', // 片区筛选
  createDate: '' // 日期筛选
})

// 弹窗相关状态
const showDetailModal = ref(false)
const currentOrder = ref<ProcessingOrder | null>(null)

// 格式化state显示
const formatStatus = (status: OrderStatus): string => {
  const statusMap = {
    timeout: '超时未抢',
    pending: '待抢单',
    processing: '处理中',
    reviewing: '待审核',
    completed: '已结单'
  }
  return statusMap[status]
}

// 加载处理中工单列表
// 修改 loadProcessingOrders 方法中的时间筛选逻辑
const loadProcessingOrders = async () => {
  loading.value = true
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未登录或缺少令牌')
      router.push('/login')
      return
    }

    // 判断是否启用时间筛选
    let url = ''
    const params = new URLSearchParams()

    if (filterForm.value.createDate) {
      // 使用 by-time-range 接口进行时间筛选
      url = '/api/work-orders/by-time-range'

      // 设置起止时间为一天的开始和结束
      const startDate = new Date(filterForm.value.createDate)
      const endDate = new Date(startDate)
      endDate.setDate(endDate.getDate() + 1)

      params.append('startTime', startDate.toISOString())
      params.append('endTime', endDate.toISOString())
      params.append('status', 'processing') // 添加状态筛选

      if (filterForm.value.area) {
        params.append('areaId', filterForm.value.area)
      }
    } else {
      // 默认使用 by-status 接口
      url = '/api/work-orders/by-status'
      params.append('status', 'processing')

      if (filterForm.value.area) {
        params.append('areaId', filterForm.value.area)
      }
    }

    const queryString = params.toString()
    if (queryString) {
      url += `?${queryString}`
    }

    // 调用后端接口获取处理中工单
    const response = await request<{
      code: number
      msg: string
      data: any[]
    }>(url, {
      method: 'GET'
    })

    if (response.code === 200) {
      // 适配后端返回的数据结构
      orders.value = response.data.map((order: any) => ({
        id: order.orderId,
        orderNo: order.orderId,
        deviceType: order.deviceType || '未知设备',
        deviceId: order.deviceId,
        area: order.areaId,
        problemDesc: order.description || '暂无描述',
        status: order.status,
        createTime: order.createdTime ? new Date(order.createdTime).toLocaleString('zh-CN') : '未知时间',
        lastUploadTime: order.updatedTime ? new Date(order.updatedTime).toLocaleString('zh-CN') : '未知时间',
        location: order.location || '未知位置',
        maintenanceName: order.assignedRepairmanName || '未分配',
        maintenancePhone: order.assignedRepairmanPhone || '未知'
      }))
    } else {
      console.error('获取处理中工单失败:', response.msg)
      alert('获取处理中工单失败：' + response.msg)
    }
  } catch (error) {
    console.error('请求异常:', error)
    alert('网络错误，请检查网络连接')
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

    // 片区筛选
    const areaMatch = filterForm.value.area === '' || order.area === filterForm.value.area

    // 日期筛选（匹配日期部分，忽略时间）
    //const dateMatch = filterForm.value.createDate === '' ||
    //  order.createTime.split(' ')[0] === filterForm.value.createDate

    return keywordMatch && areaMatch
  })
})

// 分页计算
const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / pageSize)
})

// 分页后的订单列表
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredOrders.value.slice(start, end)
})

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1 // 搜索后重置到第一页
}

// 处理筛选（片区/日期）
const handleFilter = () => {
  currentPage.value = 1 // 筛选后重置到第一页
  loadProcessingOrders()
}

// 重置筛选条件
const resetFilter = () => {
  searchKeyword.value = '' // 清空搜索关键词
  filterForm.value = {
    area: '',
    createDate: ''
  }
  currentPage.value = 1
  loadProcessingOrders()
}

// 查看工单详情（打开弹窗）
const viewOrderDetail = (id: string) => {
  const order = orders.value.find(item => item.id === id)
  if (order) {
    currentOrder.value = order
    showDetailModal.value = true
  }
}

// 关闭详情弹窗
const closeDetailModal = () => {
  showDetailModal.value = false
  currentOrder.value = null
}

// 页面加载时获取数据
onMounted(() => {
  loadProcessingOrders()
})
</script>

<style scoped>
/* 原有样式保持不变 */
.order-processing-page {
  padding: 20px;
}

.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; font-weight: 600; color: #333; margin-bottom: 8px; }
.breadcrumb { color: #666; font-size: 14px; }
.filter-bar { display: flex; align-items: center; gap: 20px; padding: 16px; background-color: #f8f9fa; border-radius: 4px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-item { display: flex;align-items: center; gap: 8px; }
.filter-item label { font-size: 14px; color: #4e5969; font-weight: 500; }
.search-input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; min-width: 240px; }
.filter-select, .filter-input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; min-width: 160px; }
.btn-reset { padding: 8px 16px; border: 1px solid #ddd; background-color: white; border-radius: 4px;cursor: pointer;font-size: 14px;color: #666; transition: all 0.3s; }
.btn-reset:hover { background-color: #f0f0f0; }
.order-table { width: 100%; border-collapse: collapse; }
.order-table th, .order-table td { padding: 12px 16px;text-align: left;border-bottom: 1px solid #f0f0f0; }
.order-table th { background-color: #f8f9fa; font-weight: 600; color: #4e5969; font-size: 14px; }
.order-table tbody tr:hover { background-color: #f8f9fa; }
.device-info { display: flex;flex-direction: column;gap: 4px; }
.device-type { font-weight: 500; color: #333; }
.device-id { font-size: 12px; color: #666; }
.desc-cell { max-width: 200px;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;cursor:pointer; }
.desc-cell:hover { white-space: normal; overflow: visible;background-color: white;z-index: 10;position: relative; }
.status-tag { display: inline-block;padding: 4px 8px;border-radius: 4px;font-size: 12px;font-weight: 500; }
.status-tag.timeout { background-color: #ffebe6; color: #cf1322; }
.status-tag.pending { background-color: #fff7e6; color: #d48806; }
.status-tag.processing { background-color: #e6f7ff; color: #1890ff; }
.status-tag.reviewing { background-color: #f6f7ff; color: #667eea; }
.status-tag.completed { background-color: #e6f7ee; color: #00875a; }
.operation-buttons { display:flex; gap: 8px; }
.btn-view { padding: 4px 8px; border-radius: 4px;font-size: 12px;cursor:pointer;border:none;transition: opacity 0.3s; background-color: #e6f7ff; color: #1890ff; }
.btn-view:hover { opacity: 0.9; }
.no-data { text-align: center; padding: 40px 0; color: #8c8c8c; }
.pagination { display:flex;justify-content: center;align-items: center; gap: 16px;margin-top: 24px; color: #666; font-size: 14px; }
.page-btn { padding: 4px 12px; border: 1px solid #ddd; background: white; border-radius: 4px;cursor:pointer; }
.page-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display:flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-container {
  background-color: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  display:flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.modal-close {
  background:none;
  border:none;
  font-size: 20px;
  cursor:pointer;
  color: #999;
  padding: 0;
  line-height: 1;
}

.modal-close:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.detail-item {
  margin-bottom: 16px;
  display:flex;
  align-items: flex-start;
}

.detail-label {
  width: 120px;
  flex-shrink: 0;
  color: #666;
  font-size: 14px;
}

.detail-value {
  flex-grow: 1;
  font-size: 14px;
  color: #333;
}

.modal-footer {
  padding: 12px 20px;
  border-top: 1px solid #eee;
  display:flex;
  justify-content: flex-end;
}

.btn-close {
  padding: 6px 16px;
  background-color: #1890ff;
  color: white;
  border:none;
  border-radius: 4px;
  cursor:pointer;
  font-size: 14px;
}

.btn-close:hover {
  background-color: #096dd9;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-bar { flex-direction: column;align-items: flex-start; }
  .filter-item { width: 100%; }
  .search-input, .filter-select, .filter-input { width: 100%; }

  .detail-item {
    flex-direction:column;
  }

  .detail-label {
    width: auto;
    margin-bottom: 4px;
    font-weight: 500;
  }
}
</style>
