<template>
  <div class="area-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>片区管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 区域管理 / 片区管理</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <!-- 新增片区按钮 -->
      <button class="btn-add" @click="handleAddArea">新增片区</button>

      <!-- 片区下拉筛选 -->
      <div class="filter-box">
        <label>选择片区：</label>
        <select v-model="selectedArea" @change="handleAreaChange" class="area-select">
          <option value="">全部片区</option>
          <option v-for="area in areaList" :key="area.areaId" :value="area.areaId">
            {{ area.areaName }}
          </option>
        </select>
      </div>
    </div>

    <!-- 片区表格 -->
    <div class="card">
      <table class="area-table">
        <thead>
          <tr>
            <th>片区</th>
            <th>设备数量</th>
            <th>范围</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="area in filteredAreas" :key="area.areaId">
            <td>{{ area.areaName }}</td>
            <td>{{ area.deviceCount || 0 }}</td>
            <td>{{ area.address || '未设置' }}</td>
            <td class="operation-buttons">
              <button
                class="btn-edit"
                @click="handleEdit(area)"
              >
                编辑
              </button>
              <button
                class="btn-delete"
                @click="handleDelete(area.areaId, area.areaName)"
              >
                删除
              </button>
            </td>
          </tr>
          <tr v-if="loading">
            <td colspan="4" class="no-data">正在加载数据...</td>
          </tr>
          <tr v-else-if="filteredAreas.length === 0">
            <td colspan="4" class="no-data">暂无片区数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页控件 -->
    <div class="pagination">
      <button
        class="page-btn"
        :disabled="currentPage === 1 || loading"
        @click="currentPage--"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页
      </span>
      <button
        class="page-btn"
        :disabled="currentPage === totalPages || loading"
        @click="currentPage++"
      >
        下一页
      </button>
    </div>

    <!-- 新增/编辑片区弹窗 -->
    <div class="modal-mask" v-if="showModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑片区' : '新增片区' }}</h3>
          <button class="close-btn" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSave">
            <div class="form-item">
              <label>片区名称：</label>
              <input
                type="text"
                v-model="formData.areaName"
                placeholder="请输入片区名称"
                required
              >
            </div>
            <div class="form-item">
              <label>片区范围：</label>
              <textarea
                v-model="formData.address"
                placeholder="请输入片区范围描述"
                rows="3"
              ></textarea>
            </div>
            <div class="form-item">
              <label>负责人：</label>
              <input
                type="text"
                v-model="formData.manager"
                placeholder="请输入负责人姓名"
              >
            </div>
            <div class="form-item">
              <label>联系电话：</label>
              <input
                type="text"
                v-model="formData.managerPhone"
                placeholder="请输入负责人联系电话"
              >
            </div>
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="showModal = false">取消</button>
              <button type="submit" class="btn-submit" :disabled="saving">
                {{ saving ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="modal-mask" v-if="showDeleteConfirm">
      <div class="modal-container confirm-modal">
        <div class="modal-header">
          <h3>确认删除</h3>
          <button class="close-btn" @click="showDeleteConfirm = false">×</button>
        </div>
        <div class="modal-body">
          <p>确定要删除 "{{ deleteAreaName }}" 片区吗？此操作不可撤销。</p>
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="showDeleteConfirm = false">取消</button>
            <button type="button" class="btn-delete-confirm" @click="confirmDelete" :disabled="deleting">
              {{ deleting ? '删除中...' : '确认删除' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/api/request'  // 导入项目封装的请求工具
import { useAuthStore } from '@/stores/auth'  // 导入 authStore

// 路由和状态管理
const router = useRouter()
const authStore = useAuthStore()

// 片区数据接口，与后端Area实体对应
interface Area {
  areaId: string
  areaName: string
  areaType: 'campus' | 'building' | 'zone'
  parentAreaId: string | null
  address: string
  manager: string
  managerPhone: string
  deviceCount?: number
  createdTime?: Date
  updatedTime?: Date
}

// 响应式数据
const areaList = ref<Area[]>([])
const selectedArea = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showModal = ref(false)
const isEdit = ref(false)
const showDeleteConfirm = ref(false)
const deleteAreaId = ref('')
const deleteAreaName = ref('')
const loading = ref(false)  // 添加加载状态
const saving = ref(false)   // 添加保存状态
const deleting = ref(false) // 添加删除状态

// 表单数据
const formData = ref<Partial<Area>>({
  areaId: '',
  areaName: '',
  areaType: 'zone',
  parentAreaId: null,
  address: '',
  manager: '',
  managerPhone: '',
  deviceCount: 0
})

// 筛选后的片区列表
const filteredAreas = computed(() => {
  let filtered = [...areaList.value]

  // 根据下拉选择筛选片区
  if (selectedArea.value) {
    filtered = filtered.filter(area => area.areaId === selectedArea.value)
  }

  // 分页处理
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return filtered.slice(startIndex, endIndex)
})

// 总页数计算
const totalPages = computed(() => {
  const filteredCount = selectedArea.value
    ? areaList.value.filter(area => area.areaId === selectedArea.value).length
    : areaList.value.length
  return Math.ceil(filteredCount / pageSize.value)
})

// 获取片区列表
// 修改获取片区列表的方法
const fetchAreaList = async () => {
  loading.value = true
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 将参数拼接到URL中
    const url = `/api/web/area/list?areaType=zone`
    const response = await request<{
      code: number
      msg: string
      data: Area[]
    }>(url, {
      method: 'GET',
    })

    if (response.code === 200) {
      areaList.value = response.data
    } else {
      const errorMsg = response.msg || `获取失败（错误码：${response.code}）`
      console.error('获取片区列表失败:', errorMsg)
      alert(`获取片区列表失败：${errorMsg}`)
    }
  } catch (error: any) {
    console.error('请求异常:', error)
    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'
    alert(`获取片区列表失败：${errorMsg}`)
  } finally {
    loading.value = false
  }
}


// 片区选择变更处理
const handleAreaChange = () => {
  currentPage.value = 1 // 重置到第一页
}

// 新增片区
const handleAddArea = () => {
  isEdit.value = false
  // 重置表单
  formData.value = {
    areaId: '',
    areaName: '',
    areaType: 'zone',
    parentAreaId: null,
    address: '',
    manager: '',
    managerPhone: '',
    deviceCount: 0
  }
  showModal.value = true
}

// 编辑片区
const handleEdit = (area: Area) => {
  isEdit.value = true
  formData.value = { ...area }
  showModal.value = true
}

// 删除片区（先显示确认弹窗）
const handleDelete = (id: string, name: string) => {
  const area = areaList.value.find(item => item.areaId === id)
  if (area) {
    deleteAreaId.value = id
    deleteAreaName.value = name
    showDeleteConfirm.value = true
  }
}

// 确认删除
const confirmDelete = async () => {
  deleting.value = true
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    const response = await request<{
      code: number
      msg: string
    }>(`/api/web/area/delete/${deleteAreaId.value}`, {
      method: 'DELETE',
    })

    if (response.code === 200) {
      fetchAreaList() // 重新获取列表
      showDeleteConfirm.value = false
    } else {
      const errorMsg = response.msg || `删除失败（错误码：${response.code}）`
      console.error('删除片区失败:', errorMsg)
      alert(`删除片区失败：${errorMsg}`)
    }
  } catch (error: any) {
    console.error('删除请求异常:', error)
    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '删除失败，请稍后重试'
    alert(`删除片区失败：${errorMsg}`)
  } finally {
    deleting.value = false
  }
}

// 保存片区信息
// 在 handleSave 方法中修改新增逻辑
const handleSave = async () => {
  saving.value = true
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    let response

    if (isEdit.value) {
      // 编辑模式 - 保持原有逻辑
      response = await request<{
        code: number
        msg: string
        data: Area
      }>('/api/web/area/update', {
        method: 'PUT',
        body: JSON.stringify(formData.value)
      })

      if (response.code === 200) {
        fetchAreaList() // 重新获取列表
        showModal.value = false
      } else {
        const errorMsg = response.msg || `更新失败（错误码：${response.code}）`
        console.error('更新片区失败:', errorMsg)
        alert(`更新片区失败：${errorMsg}`)
      }
    } else {
      // 新增模式 - 修改为不设置 parentAreaId
      const newArea = {
        areaName: formData.value.areaName,
        areaType: 'zone' as const,
        address: formData.value.address,
        manager: formData.value.manager,
        managerPhone: formData.value.managerPhone
      }

      response = await request<{
        code: number
        msg: string
        data: Area
      }>('/api/web/area/add', {
        method: 'POST',
        body: JSON.stringify(newArea)
      })

      if (response.code === 200) {
        fetchAreaList() // 重新获取列表
        showModal.value = false
      } else {
        const errorMsg = response.msg || `新增失败（错误码：${response.code}）`
        console.error('新增片区失败:', errorMsg)
        alert(`新增片区失败：${errorMsg}`)
      }
    }
  } catch (error: any) {
    console.error('保存请求异常:', error)
    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '保存失败，请稍后重试'
    alert(`保存片区失败：${errorMsg}`)
  } finally {
    saving.value = false
  }
}


// 初始化加载数据
onMounted(() => {
  console.log('Token:', authStore.token)
  fetchAreaList()
})
</script>

<!-- 在 Urban.vue 文件中确保有正确的样式定义 -->
<style scoped>
/* 确保样式规则完整且正确 */
.area-page {
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

.filter-box {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.area-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 200px;
  font-size: 14px;
}

/* 表格样式 */
.area-table {
  width: 100%;
  border-collapse: collapse;
}

.area-table th,
.area-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.area-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.area-table tbody tr:hover {
  background-color: #f8f9fa;
}

.operation-buttons {
  display: flex;
  gap: 8px;
}

.btn-edit {
  background-color: #e6f4ff;
  color: #1890ff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: opacity 0.3s;
}

.btn-edit:hover {
  opacity: 0.9;
}

.btn-delete {
  background-color: #ffebe6;
  color: #cf1322;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: opacity 0.3s;
}

.btn-delete:hover {
  opacity: 0.9;
}

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
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-container {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.confirm-modal {
  width: 400px;
}

.modal-header {
  padding: 16px;
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

.close-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #666;
}

.modal-body {
  padding: 16px;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.form-item input,
.form-item textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-delete-confirm {
  background: #cf1322;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-box {
    width: 100%;
  }

  .area-select {
    width: 100%;
  }
}
</style>
