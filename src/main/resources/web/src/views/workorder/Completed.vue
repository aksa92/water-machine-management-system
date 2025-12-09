<!-- src/views/order/OrderCompleted.vue -->
<template>
  <div class="order-completed-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>已结单工单</h2>
      <div class="breadcrumb">校园矿化水平台 / 工单管理 / 已结单</div>
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
          <option value="市区">市区</option>
          <option value="校区">校区</option>
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
            <th>片区</th>
            <th>问题描述</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in filteredOrders" :key="order.id">
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
            <td colspan="7" class="no-data">暂无已结单工单</td>
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
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

// 工单状态类型
type OrderStatus = 'timeout' | 'pending' | 'processing' | 'reviewing' | 'completed'

// 工单数据接口
interface CompletedOrder {
  id: string
  orderNo: string
  deviceType: string // 设备机型（制水机/供水机）
  deviceId: string // 设备ID
  area: string // 所属片区
  problemDesc: string // 问题描述
  status: OrderStatus // 工单状态
  createTime: string // 创建时间
}

// 模拟已结单工单数据
const orderList: CompletedOrder[] = [
  {
    id: '11',
    orderNo: 'ORD-20231023-001',
    deviceType: '制水机',
    deviceId: 'WM-2023-001',
    area: '市区',
    problemDesc: '更换密封垫后漏水问题已解决，设备运行正常',
    status: 'completed',
    createTime: '2023-10-23 08:10:05'
  },
  {
    id: '12',
    orderNo: 'ORD-20231023-002',
    deviceType: '供水机',
    deviceId: 'WS-2023-001',
    area: '校区',
    problemDesc: '水质检测合格，出水口感异常问题已解决',
    status: 'completed',
    createTime: '2023-10-23 14:20:33'
  },
  {
    id: '13',
    orderNo: 'ORD-20231024-003',
    deviceType: '制水机',
    deviceId: 'WM-2023-002',
    area: '校区',
    problemDesc: '水泵检修完成，出水速度恢复正常，已审核通过',
    status: 'completed',
    createTime: '2023-10-24 11:30:15'
  }
]

// 响应式数据
const orders = ref<CompletedOrder[]>(orderList)
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const router = useRouter()

// 搜索关键词（工单号/设备ID）
const searchKeyword = ref('')

// 筛选表单数据
const filterForm = ref({
  area: '', // 片区筛选
  createDate: '' // 日期筛选
})

// 格式化状态显示
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
    const dateMatch = filterForm.value.createDate === '' || 
      order.createTime.split(' ')[0] === filterForm.value.createDate
    
    return keywordMatch && areaMatch && dateMatch
  })
})

// 分页计算
const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / pageSize)
})

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1 // 搜索后重置到第一页
}

// 处理筛选（片区/日期）
const handleFilter = () => {
  currentPage.value = 1 // 筛选后重置到第一页
}

// 重置筛选条件
const resetFilter = () => {
  searchKeyword.value = '' // 清空搜索关键词
  filterForm.value = {
    area: '',
    createDate: ''
  }
  currentPage.value = 1
}

// 查看工单详情
const viewOrderDetail = (id: string) => {
   router.push(`/home/work-order/completed/${id}`)
}
</script>

<style scoped>
/* 样式与前几个页面完全一致，仅修改页面容器类名 */
.order-completed-page {
  padding: 20px;
}

/* 复用相同样式 */
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; font-weight: 600; color: #333; margin-bottom: 8px; }
.breadcrumb { color: #666; font-size: 14px; }
.filter-bar { display: flex; align-items: center; gap: 20px; padding: 16px; background-color: #f8f9fa; border-radius: 4px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-item { display: flex; align-items: center; gap: 8px; }
.filter-item label { font-size: 14px; color: #4e5969; font-weight: 500; }
.search-input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; min-width: 240px; }
.filter-select, .filter-input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; min-width: 160px; }
.btn-reset { padding: 8px 16px; border: 1px solid #ddd; background-color: white; border-radius: 4px; cursor: pointer; font-size: 14px; color: #666; transition: all 0.3s; }
.btn-reset:hover { background-color: #f0f0f0; }
.order-table { width: 100%; border-collapse: collapse; }
.order-table th, .order-table td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #f0f0f0; }
.order-table th { background-color: #f8f9fa; font-weight: 600; color: #4e5969; font-size: 14px; }
.order-table tbody tr:hover { background-color: #f8f9fa; }
.device-info { display: flex; flex-direction: column; gap: 4px; }
.device-type { font-weight: 500; color: #333; }
.device-id { font-size: 12px; color: #666; }
.desc-cell { max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: pointer; }
.desc-cell:hover { white-space: normal; overflow: visible; background-color: white; z-index: 10; position: relative; }
.status-tag { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; }
.status-tag.timeout { background-color: #ffebe6; color: #cf1322; }
.status-tag.pending { background-color: #fff7e6; color: #d48806; }
.status-tag.processing { background-color: #e6f7ff; color: #1890ff; }
.status-tag.reviewing { background-color: #f6f7ff; color: #667eea; }
.status-tag.completed { background-color: #e6f7ee; color: #00875a; }
.operation-buttons { display: flex; gap: 8px; }
.btn-view { padding: 4px 8px; border-radius: 4px; font-size: 12px; cursor: pointer; border: none; transition: opacity 0.3s; background-color: #e6f7ff; color: #1890ff; }
.btn-view:hover { opacity: 0.9; }
.no-data { text-align: center; padding: 40px 0; color: #8c8c8c; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 24px; color: #666; font-size: 14px; }
.page-btn { padding: 4px 12px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; }
.page-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-bar { flex-direction: column; align-items: flex-start; }
  .filter-item { width: 100%; }
  .search-input, .filter-select, .filter-input { width: 100%; }
}
</style>