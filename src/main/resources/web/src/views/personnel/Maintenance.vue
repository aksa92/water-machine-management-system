<!-- src/views/personnel/Maintenance.vue -->
<template>
  <div class="maintenance-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>维修人员管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 人员管理 / 维修人员</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <button class="btn-add" @click="openAddForm">新增维修人员</button>

      <div class="filters">
        <!-- 姓名搜索 -->
        <div class="filter-item">
          <input
              type="text"
              placeholder="搜索姓名..."
              v-model="searchFilters.name"
              @input="handleSearch"
          >
        </div>

        <!-- 区域筛选 -->
        <div class="filter-item">
          <select v-model="searchFilters.areaId" @change="handleSearch">
            <option value="">全部区域</option>
            <option value="A">A区</option>
            <option value="B">B区</option>
            <option value="C">C区</option>
            <option value="D">D区</option>
          </select>
        </div>

        <!-- 状态筛选 -->
        <div class="filter-item">
          <select v-model="searchFilters.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="idle">空闲</option>
            <option value="busy">忙碌</option>
            <option value="vacation">休假</option>
          </select>
        </div>

        <button class="search-btn" @click="handleSearch">搜索</button>
        <button class="reset-btn" @click="resetFilters">重置</button>
      </div>
    </div>

    <!-- 维修人员表格 -->
    <div class="card">
      <table class="maintenance-table">
        <thead>
        <tr>
          <th>姓名</th>
          <th>联系电话</th>
          <th>维修片区</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="staff in paginatedStaff" :key="staff.repairmanId">
          <td>{{ staff.repairmanName }}</td>
          <td>{{ staff.phone }}</td>
          <td>{{ staff.areaId }}</td>
          <td>
              <span :class="`status-tag ${staff.status}`">
                {{ getStatusText(staff.status) }}
              </span>
          </td>
          <td class="operation-buttons">
            <button
                class="btn-view"
                @click="handleViewRecords(staff.repairmanId)"
            >
              查看维修记录
            </button>
            <button
                class="btn-edit"
                @click="openEditForm(staff)"
            >
              编辑
            </button>
            <button
                class="btn-delete"
                @click="confirmDelete(staff.repairmanId, staff.repairmanName)"
            >
              删除
            </button>
          </td>
        </tr>
        <tr v-if="paginatedStaff.length === 0">
          <td colspan="5" class="no-data">暂无维修人员数据</td>
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
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页 (共 {{ filteredStaff.length }} 条记录)
      </span>
      <button
          class="page-btn"
          :disabled="currentPage === totalPages"
          @click="currentPage++"
      >
        下一页
      </button>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="isModalOpen" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑维修人员' : '新增维修人员' }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveRepairman">
            <!-- ID字段（编辑时显示） -->
            <div v-if="isEditing" class="form-group">
              <label>ID：</label>
              <input
                  type="text"
                  v-model="form.repairmanId"
                  disabled
                  class="disabled-field"
              />
            </div>
            <div class="form-group">
              <label class="form-label required">姓名：</label>
              <input
                  type="text"
                  v-model="form.repairmanName"
                  required
                  placeholder="请输入姓名"
              />
            </div>
            <div class="form-group">
              <label class="form-label required">联系电话：</label>
              <input
                  type="tel"
                  v-model="form.phone"
                  required
                  placeholder="请输入联系电话"
              />
            </div>
            <div class="form-group">
              <label class="form-label required">维修片区：</label>
              <select v-model="form.areaId" required>
                <option value="">请选择片区</option>
                <option value="A">A</option>
                <option value="B">B</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label required">状态：</label>
              <select v-model="form.status" required>
                <option value="idle">空闲</option>
                <option value="busy">忙碌</option>
                <option value="vacation">休假</option>
              </select>
            </div>
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeModal">取消</button>
              <button type="submit" class="btn-submit">{{ isEditing ? '保存' : '创建' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="isDeleteConfirmOpen" class="modal-overlay" @click="closeDeleteConfirm">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>确认删除</h3>
          <button class="close-btn" @click="closeDeleteConfirm">×</button>
        </div>
        <div class="modal-body">
          <p>确定要删除维修人员 "{{ deleteName }}" 吗？此操作不可恢复。</p>
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="closeDeleteConfirm">取消</button>
            <button type="button" class="btn-submit" @click="deleteRepairman">确认删除</button>
          </div>
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
import type { ResultVO } from '@/api/types/auth'

// 维修人员状态类型
type RepairmanStatus = 'idle' | 'busy' | 'vacation'

// 维修人员数据接口（与后端实体保持一致）
interface MaintenanceStaff {
  repairmanId: string
  repairmanName: string
  phone: string
  areaId: string
  status: RepairmanStatus
}

// 搜索筛选条件接口
interface SearchFilters {
  name: string
  areaId: string
  status: string
}

// 表单数据接口
interface FormData {
  repairmanId: string
  repairmanName: string
  phone: string
  areaId: string
  status: RepairmanStatus
}

const authStore = useAuthStore()
const router = useRouter()

// ========== 已移除权限检查函数 ==========

// 响应式数据
const staffList = ref<MaintenanceStaff[]>([])
const searchFilters = ref<SearchFilters>({
  name: '',
  areaId: '',
  status: ''
})
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const loading = ref(false)

// 弹窗相关数据
const isModalOpen = ref(false)
const isEditing = ref(false)
const isDeleteConfirmOpen = ref(false)
const deleteId = ref<string>('')
const deleteName = ref<string>('')

// 表单数据（初始化）
const form = ref<FormData>({
  repairmanId: '',
  repairmanName: '',
  phone: '',
  areaId: '',
  status: 'idle'
})

// 获取维修人员列表
const fetchMaintenanceStaff = async () => {
  loading.value = true
  try {
    // 检查是否有token
    if (!authStore.token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 构建查询参数
    const params = new URLSearchParams()
    if (searchFilters.value.name.trim()) {
      params.append('name', searchFilters.value.name.trim())
    }
    if (searchFilters.value.areaId) {
      params.append('areaId', searchFilters.value.areaId)
    }
    if (searchFilters.value.status) {
      params.append('status', searchFilters.value.status)
    }

    // 使用封装的request工具发送请求
    const response = await request<ResultVO<MaintenanceStaff[]>>(
        `/api/web/repairman/list?${params.toString()}`,
        {
          method: 'GET'
        }
    )

    // 处理响应
    if (response.code === 200) {
      staffList.value = response.data || []
    } else {
      const errorMsg = response.message || `获取失败（错误码：${response.code}）`
      console.error('获取维修人员列表失败:', errorMsg)
      alert(`获取维修人员列表失败：${errorMsg}`)
    }
  } catch (error: any) {
    console.error('请求异常:', error)
    const errorMsg = error.message.includes('401')
        ? '登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'
    alert(`获取维修人员列表失败：${errorMsg}`)

    // Token 无效时跳转登录页
    if (error.message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 筛选后的维修人员列表
const filteredStaff = computed(() => {
  // 前端额外筛选（作为后备保障）
  return staffList.value.filter(person => {
    const nameMatch = searchFilters.value.name.trim() === '' ||
        person.repairmanName.toLowerCase().includes(searchFilters.value.name.toLowerCase())

    const areaMatch = searchFilters.value.areaId === '' ||
        person.areaId === searchFilters.value.areaId

    const statusMatch = searchFilters.value.status === '' ||
        person.status === searchFilters.value.status

    return nameMatch && areaMatch && statusMatch
  })
})

// 分页计算
const paginatedStaff = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredStaff.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredStaff.value.length / pageSize)
})

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchMaintenanceStaff()
}

// 重置筛选条件
const resetFilters = () => {
  searchFilters.value = {
    name: '',
    areaId: '',
    status: ''
  }
  currentPage.value = 1
  fetchMaintenanceStaff()
}

// 获取状态文本
const getStatusText = (status: RepairmanStatus) => {
  const statusMap: Record<RepairmanStatus, string> = {
    'idle': '空闲',
    'busy': '忙碌',
    'vacation': '休假'
  }
  return statusMap[status] || status
}

// ========== 编辑处理（已移除权限检查） ==========
const openEditForm = (staff: MaintenanceStaff) => {
  // 深拷贝数据，避免直接修改原数据
  form.value = JSON.parse(JSON.stringify(staff))
  isEditing.value = true
  isModalOpen.value = true
}

// ========== 新增处理（已移除权限检查） ==========
const openAddForm = () => {
  // 重置表单数据
  form.value = {
    repairmanId: '',
    repairmanName: '',
    phone: '',
    areaId: '',
    status: 'idle'
  }
  isEditing.value = false
  isModalOpen.value = true
}

// 关闭弹窗
const closeModal = () => {
  isModalOpen.value = false
  isEditing.value = false
}

// 保存维修人员
const saveRepairman = async () => {
  try {
    // 验证表单数据
    if (!form.value.repairmanName.trim()) {
      alert('请输入姓名')
      return
    }
    if (!form.value.phone.trim()) {
      alert('请输入联系电话')
      return
    }
    if (!form.value.areaId) {
      alert('请选择维修片区')
      return
    }

    // 发送请求
    const response = await request<ResultVO<MaintenanceStaff>>(
        `/api/web/repairman/save`,
        {
          method: 'POST',
          body: JSON.stringify(form.value)
        }
    )

    if (response.code === 200) {
      alert(isEditing.value ? '维修人员更新成功' : '维修人员新增成功')
      closeModal()
      fetchMaintenanceStaff() // 刷新列表
    } else {
      // 如果后端返回权限错误，这里会显示
      alert(`保存失败：${response.message}`)
    }
  } catch (error: any) {
    console.error('保存失败:', error)
    if (error.message.includes('403')) {
      alert('权限不足：您没有权限执行此操作，请联系管理员')
    } else {
      alert('保存失败，请稍后重试')
    }
  }
}

// 删除确认
const confirmDelete = (id: string, name: string) => {
  deleteId.value = id
  deleteName.value = name
  isDeleteConfirmOpen.value = true
}

// 关闭删除确认
const closeDeleteConfirm = () => {
  isDeleteConfirmOpen.value = false
  deleteId.value = ''
  deleteName.value = ''
}

// 删除维修人员
const deleteRepairman = async () => {
  if (!deleteId.value) return

  try {
    const response = await request<ResultVO>(
        `/api/web/repairman/${deleteId.value}`,
        {
          method: 'DELETE'
        }
    )

    if (response.code === 200) {
      alert('删除成功')
      closeDeleteConfirm()
      fetchMaintenanceStaff() // 刷新列表
    } else {
      alert(`删除失败：${response.message}`)
    }
  } catch (error: any) {
    console.error('删除失败:', error)
    alert('删除失败，请稍后重试')
  }
}

// 查看维修记录
const handleViewRecords = (id: string) => {
  // 确保 id 参数正确传递
  console.log('跳转到维修记录:', id)
  router.push(`/home/personnel/maintenance/records/${id}`)
}

// 页面加载时获取数据
onMounted(() => {
  fetchMaintenanceStaff()
})
</script>

<style scoped>
.maintenance-page {
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

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.btn-add {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-add:hover {
  background: #359e75;
}

.filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-item input,
.filter-item select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.filter-item input {
  width: 180px;
}

.filter-item select {
  min-width: 120px;
}

.search-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.reset-btn {
  background: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.maintenance-table {
  width: 100%;
  border-collapse: collapse;
}

.maintenance-table th,
.maintenance-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.maintenance-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.maintenance-table tbody tr:hover {
  background-color: #f8f9fa;
}

.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
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

.operation-buttons button {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: opacity 0.3s;
}

.operation-buttons button:hover {
  opacity: 0.9;
}

.btn-view {
  background-color: #f6f7ff;
  color: #667eea;
}

.btn-edit {
  background-color: #e6f7ff;
  color: #1890ff;
}

.btn-delete {
  background-color: #ffebe6;
  color: #cf1322;
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
  .action-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .filters {
    width: 100%;
  }

  .filter-item {
    width: 100%;
  }

  .filter-item input,
  .filter-item select {
    width: 100%;
  }
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
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 4px;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-label.required::after {
  content: '*';
  color: #cf1322;
  margin-left: 4px;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.disabled-field {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.btn-submit {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-cancel {
  background: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit:hover {
  background: #359e75;
}

.btn-cancel:hover {
  background: #e0e0e0;
}
</style>