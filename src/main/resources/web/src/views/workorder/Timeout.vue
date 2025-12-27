<!-- src/views/order/OrderTimeout.vue -->
<template>
  <div class="order-timeout-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>超时未抢工单</h2>
      <div class="breadcrumb">校园矿化水平台 / 工单管理 / 超时未抢</div>
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
              <button class="btn-assign" @click="openAssignDialog(order)">人工派单</button>
            </td>
          </tr>
          <tr v-if="paginatedOrders.length === 0">
            <td colspan="7" class="no-data">
              {{ loading ? '正在加载数据...' : '暂无超时未抢工单' }}
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

    <!-- 人工派单弹窗 -->
    <div class="dialog-mask" v-if="assignDialogVisible">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>人工派单</h3>
          <button class="dialog-close" @click="closeAssignDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label class="form-label">工单号：</label>
            <div class="form-value">{{ currentOrder?.orderNo || '' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">建单时间：</label>
            <div class="form-value">{{ currentOrder?.createTime || '' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">设备机型与ID：</label>
            <div class="form-value">
              {{ currentOrder?.deviceType || '' }}({{ currentOrder?.deviceId || '' }})
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">警告内容：</label>
            <div class="form-value">{{ currentOrder?.problemDesc || '' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">设备最后上传时间：</label>
            <div class="form-value">{{ currentOrder?.lastUploadTime || '未知' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">设备位置：</label>
            <div class="form-value">{{ currentOrder?.location || '未知' }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">当前状态：</label>
            <div class="form-value">
              <span :class="`status-tag ${currentOrder?.status}`">
                {{ currentOrder ? formatStatus(currentOrder.status) : '' }}
              </span>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label required">选择维修人员：</label>
            <select
              v-model="selectedStaffId"
              class="form-select"
              @change="handleStaffChange"
            >
              <option value="">请选择维修人员</option>
              <option
                v-for="staff in filteredStaff"
                :key="staff.repairmanId"
                :value="staff.repairmanId"
              >
                {{ staff.repairmanName }} ({{ staff.phone }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">派单备注：</label>
            <textarea
              v-model="assignRemark"
              class="form-textarea"
              placeholder="请输入派单备注信息"
              rows="3"
            ></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeAssignDialog">取消</button>
          <button
            class="btn-confirm"
            @click="confirmAssign"
            :disabled="!selectedStaffId"
          >
            确认派单
          </button>
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

// 工单数据接口
interface TimeoutOrder {
  id: string
  orderNo: string
  deviceType: string // 设备机型（制水机/供水机）
  deviceId: string // 设备ID
  area: string // 所属片区
  problemDesc: string // 问题描述
  status: OrderStatus // 工单状态
  createTime: string // 创建时间
  lastUploadTime?: string // 设备最后上传时间
  location?: string // 设备位置
}

// 维修人员接口
interface Repairman {
  repairmanId: string
  repairmanName: string
  phone?: string
  areaId: string
  status?: 'idle' | 'busy' | 'vacation'
}

// 响应式数据
const orders = ref<TimeoutOrder[]>([])
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
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

// 派单弹窗相关
const assignDialogVisible = ref(false)
const currentOrder = ref<TimeoutOrder | null>(null)
const selectedStaffId = ref('')
const assignRemark = ref('')
const allStaff = ref<Repairman[]>([])

// 加载超时工单数据 - 修改此函数以支持时间筛选
const loadTimeoutOrders = async () => {
  loading.value = true
  try {
    // 检查 Token 是否存在
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    console.log('当前 Token:', token.substring(0, 20) + '...')

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
      params.append('status', 'timeout') // 添加状态筛选

      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const areaId = filterForm.value.area || userInfo.areaId || ''
      if (areaId) {
        params.append('areaId', areaId)
      }
    } else {
      // 默认使用 by-status 接口
      url = '/api/work-orders/by-status'
      params.append('status', 'timeout')

      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const areaId = filterForm.value.area || userInfo.areaId || ''
      if (areaId) {
        params.append('areaId', areaId)
      }
    }

    const queryString = params.toString()
    if (queryString) {
      url += `?${queryString}`
    }

    // 使用项目封装的 request 工具
    const response = await request<{
      code: number
      msg: string
      data: any[]
    }>(url, {
      method: 'GET',
    })

    // 处理响应
    if (response.code === 200) {
      orders.value = (response.data || []).map((order: any) => ({
        id: order.orderId || '',
        orderNo: order.orderId || '',
        deviceType: order.deviceType || '未知设备',
        deviceId: order.deviceId || '',
        area: order.areaId || '',
        problemDesc: order.description || '暂无描述',
        status: order.status || 'timeout',
        createTime: order.createdTime ? new Date(order.createdTime).toLocaleString('zh-CN') : '未知时间',
        lastUploadTime: order.updatedTime ? new Date(order.updatedTime).toLocaleString('zh-CN') : '未知',
        location: order.areaId || '未知位置'
      }))
    } else {
      const errorMsg = response.msg || `获取失败（错误码：${response.code}）`
      console.error('获取超时工单失败:', errorMsg)
      alert(`获取超时工单失败：${errorMsg}`)
    }
  } catch (error: any) {
    console.error('请求异常:', error)
    console.error('错误详情:', {
      message: error.message,
      status: error.status,
      response: error.response
    })

    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'
    alert(`获取超时工单失败：${errorMsg}`)

    if (error.message.includes('401') || error.message.includes('403')) {
      authStore.logout()
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 加载维修人员数据
const loadMaintenanceStaff = async (areaId: string) => {
  try {
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    // 调用按片区查询维修人员的接口
    const response = await request<{
      code: number
      msg: string
      data: Repairman[]
    }>('/api/web/repairman/by-area/' + areaId, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    console.log('维修人员响应数据:', response)  // 添加这行来查看
    console.log('响应数据结构:', typeof response.data, response.data)

    if (response.code === 200) {
      allStaff.value = (response.data || []).map(staff => ({
        repairmanId: staff.repairmanId,
        repairmanName: staff.repairmanName,
        phone: staff.phone || '未知',
        areaId: staff.areaId || '',
        status: staff.status || 'idle'
      }))
    } else {
      console.error('获取维修人员失败:', response.msg)  // 失败时输出错误信息
      alert('获取维修人员失败：' + response.msg)
    }
  } catch (error: any) {
    console.error('请求异常:', error)  // 异常时输出错误详情
    alert('获取维修人员失败：' + (error.message || '网络错误'))
  }

}

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

    // 移除日期筛选逻辑，因为已在后端处理
    // const dateMatch = filterForm.value.createDate === '' ||
    //   order.createTime.split(' ')[0] === filterForm.value.createDate

    // 返回时不包含 dateMatch
    return keywordMatch && areaMatch
  })
})

// 分页后的工单列表
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredOrders.value.slice(start, end)
})

// 筛选出负责当前工单区域的维修人员
const filteredStaff = computed(() => {
  console.log('当前工单:', currentOrder.value)
  console.log('所有维修人员:', allStaff.value)

  if (!currentOrder.value) return []
  return allStaff.value.filter(staff =>
      staff.areaId === currentOrder.value!.area
  )
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
  loadTimeoutOrders() // 重新加载数据
}

// 重置筛选条件
const resetFilter = () => {
  searchKeyword.value = '' // 清空搜索关键词
  filterForm.value = {
    area: '',
    createDate: ''
  }
  currentPage.value = 1
  loadTimeoutOrders()
}

// 打开派单弹窗
const openAssignDialog = async (order: TimeoutOrder) => {
  currentOrder.value = order
  selectedStaffId.value = ''
  assignRemark.value = ''
  assignDialogVisible.value = true

  // 等待维修人员数据加载完成
  await loadMaintenanceStaff(order.area)
}


// 关闭派单弹窗
const closeAssignDialog = () => {
  assignDialogVisible.value = false
  currentOrder.value = null
  selectedStaffId.value = ''
  assignRemark.value = ''
}

// 处理维修人员选择变化
const handleStaffChange = () => {
  // 可以添加额外的处理逻辑
}

// 确认派单
// 确认派单 - 修改为JSON格式
const confirmAssign = async () => {
  if (!currentOrder.value || !selectedStaffId.value) return

  try {
    console.log('🚀 发送派单请求（使用JSON格式）...')

    // 检查Token是否存在
    const token = authStore.token
    if (!token) {
      alert('认证失败：未找到用户Token，请重新登录')
      return
    }

    // 构建 JSON 数据
    const requestData = {
      orderId: currentOrder.value.id,
      repairmanId: selectedStaffId.value
    }

    console.log('📤 JSON请求数据:', requestData)

    const response = await request<{
      code: number
      msg: string
      data: boolean
    }>('/api/work-orders/assign', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json', // 修改为JSON格式
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(requestData) // 发送JSON字符串
    })

    console.log('✅ 派单响应:', response)

    if (response.code === 200 && response.data) {
      alert('派单成功')
      closeAssignDialog()
      loadTimeoutOrders()
    } else {
      const errorMsg = response.msg || '派单失败'
      alert(`派单失败：${errorMsg} (错误码: ${response.code})`)
    }
  } catch (error: any) {
    console.error('❌ 派单请求异常:', error)

    // 显示详细的错误信息
    if (error.message.includes('401') || error.message.includes('403')) {
      alert('权限不足或认证失败：请重新登录')
      authStore.logout()
    } else if (error.message.includes('Unexpected token')) {
      alert('派单失败：后端期望JSON格式，但可能接口配置不正确。请联系后端开发人员确认接口参数接收方式。')
    } else {
      alert('派单失败：' + (error.message || '未知错误'))
    }
  }
}





// 页面加载时获取数据
onMounted(() => {
  console.log('Token:', authStore.token)
  loadTimeoutOrders()
})
</script>

<style scoped>
/* 基础页面样式 */
.order-timeout-page {
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

/* 状态标签样式 */
.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.timeout {
  background-color: #ffebe6;
  color: #cf1322;
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

/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  gap: 8px;
}

.btn-assign {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: opacity 0.3s;
  background-color: #e6f7ff;
  color: #1890ff;
}

.btn-assign:hover {
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
.dialog-mask {
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

.dialog-container {
  width: 600px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.dialog-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.dialog-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  transition: color 0.2s;
}

.dialog-close:hover {
  color: #333;
}

.dialog-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
}

.form-label {
  width: 120px;
  flex-shrink: 0;
  font-size: 14px;
  color: #4e5969;
  padding-top: 6px;
}

.form-label.required::after {
  content: '*';
  color: #cf1322;
  margin-left: 4px;
}

.form-value {
  flex-grow: 1;
  font-size: 14px;
  color: #333;
  padding-top: 4px;
}

.form-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
  box-sizing: border-box;
}

.dialog-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.btn-cancel:hover {
  background-color: #f0f0f0;
}

.btn-confirm {
  padding: 8px 16px;
  border: none;
  background-color: #1890ff;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-confirm:hover {
  background-color: #096dd9;
}

.btn-confirm:disabled {
  background-color: #8cc5ff;
  cursor: not-allowed;
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

  .dialog-container {
    width: 90%;
    max-width: 500px;
  }

  .form-group {
    flex-direction: column;
  }

  .form-label {
    width: 100%;
    margin-bottom: 8px;
    padding-top: 0;
  }
}
</style>
