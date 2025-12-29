<!-- src/views/area/Campus.vue -->
<template>
  <div class="campus-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>校区管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 区域管理 / 校区管理</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <!-- 新增校区按钮 -->
      <button class="btn-add" @click="handleAddCampus">新增校区</button>

    </div>

    <!-- 校区表格 -->
    <div class="card">
      <table class="campus-table">
        <thead>
          <tr>
            <th>校区名称</th>
            <th>地址</th>
            <th>负责人</th>
            <th>联系电话</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="campus in filteredCampus" :key="campus.areaId">
            <td>{{ campus.areaName }}</td>
            <td>{{ campus.address }}</td>
            <td>{{ campus.manager }}</td>
            <td>{{ campus.managerPhone }}</td>
            <td>{{ formatDate(campus.createdTime) }}</td>
            <td class="operation-buttons">
              <button
                class="btn-edit"
                @click="handleEdit(campus)"
              >
                编辑
              </button>
              <button
                class="btn-delete"
                @click="handleDelete(campus.areaId, campus.areaName)"
              >
                删除
              </button>
            </td>
          </tr>
          <tr v-if="filteredCampus.length === 0">
            <td colspan="6" class="no-data">
              {{ loading ? '正在加载数据...' : '暂无校区数据' }}
            </td>
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

    <!-- 新增/编辑校区弹窗 -->
    <div class="modal-mask" v-if="showModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑校区' : '新增校区' }}</h3>
          <button class="close-btn" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSave">
            <div class="form-item" v-if="!isEdit">
              <label>所属市区：</label>
              <select
                v-model="formData.parentAreaId"
                required
              >
                <option value="">请选择所属市区</option>
                <option v-for="city in cityList" :key="city.areaId" :value="city.areaId">
                  {{ city.areaName }}
                </option>
              </select>
            </div>
            <div class="form-item">
              <label>校区名称：</label>
              <input
                type="text"
                v-model="formData.areaName"
                placeholder="请输入校区名称"
                required
              >
            </div>
            <div class="form-item">
              <label>地址：</label>
              <input
                type="text"
                v-model="formData.address"
                placeholder="请输入校区地址"
                required
              >
            </div>
            <div class="form-item">
              <label>负责人：</label>
              <select
                v-model="selectedManager"
                @change="onManagerChange"
                required
              >
                <option value="">请选择负责人</option>
                <option v-for="admin in adminList" :key="admin.adminId" :value="admin">
                  {{ admin.adminName }}
                </option>
              </select>
            </div>
            <div class="form-item">
              <label>联系电话：</label>
              <input
                type="text"
                v-model="formData.managerPhone"
                placeholder="联系电话会自动填充"
                readonly
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
          <p>确定要删除 "{{ deleteCampusName }}" 校区吗？此操作不可撤销。</p>
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

// 管理员数据接口
interface Admin {
  adminId: string
  adminName: string
  password: string
  phone: string
  role: string
  areaId: string | null
  createdTime?: Date
  updatedTime?: Date
}

// 校区数据接口，与后端Area实体对应
interface Area {
  areaId: string
  areaName: string
  areaType: 'campus' | 'building' | 'zone'
  parentAreaId: string | null
  address: string
  manager: string
  managerPhone: string
  createdTime?: Date
  updatedTime?: Date
}

// 响应式数据
const campusList = ref<Area[]>([])
const cityList = ref<Area[]>([])
const adminList = ref<Admin[]>([])
const selectedManager = ref<Admin | null>(null)
const selectedCampus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showModal = ref(false)
const isEdit = ref(false)
const showDeleteConfirm = ref(false)
const deleteCampusId = ref('')
const deleteCampusName = ref('')
const loading = ref(false)  // 添加加载状态
const saving = ref(false)   // 添加保存状态
const deleting = ref(false) // 添加删除状态

// 表单数据
const formData = ref<Area>({
  areaId: '',
  areaName: '',
  areaType: 'campus',
  parentAreaId: null,
  address: '',
  manager: '',
  managerPhone: '',
  createdTime: undefined,
  updatedTime: undefined
})

// 格式化日期
const formatDate = (date: Date | undefined) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN')
}

// 筛选后的校区列表
const filteredCampus = computed(() => {
  let filtered = [...campusList.value]

  // 根据下拉选择筛选校区
  if (selectedCampus.value) {
    filtered = filtered.filter(campus => campus.areaId === selectedCampus.value)
  }

  // 分页处理
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return filtered.slice(startIndex, endIndex)
})

// 总页数计算
const totalPages = computed(() => {
  const filteredCount = selectedCampus.value
    ? campusList.value.filter(campus => campus.areaId === selectedCampus.value).length
    : campusList.value.length
  return Math.ceil(filteredCount / pageSize.value)
})

// 获取校区列表
const fetchCampusList = async () => {
  loading.value = true
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 需要先获取所有市区，然后获取所有校区
    // 方案1: 获取所有市区，然后获取每个市区下的校区
    const citiesResponse = await request<{
      code: number
      msg: string
      data: Area[]
    }>('/api/web/area/cities', {
      method: 'GET',
    })

    if (citiesResponse?.code !== 200 || !citiesResponse?.data) {
      console.error('获取市区列表失败:', citiesResponse?.msg || '未知错误')
      alert(`获取市区列表失败：${citiesResponse?.msg || '未知错误'}`)
      return
    }

    const cities = citiesResponse.data
    const allCampuses: Area[] = []

    // 获取每个市区下的校区
    for (const city of cities) {
      try {
        const campusResponse = await request<{
          code: number
          msg: string
          data: Area[]
        }>(`/api/web/area/campuses/${city.areaId}`, {
          method: 'GET',
        })

        if (campusResponse?.code === 200 && campusResponse?.data) {
          allCampuses.push(...campusResponse.data)
        } else {
          console.warn(`获取市区 ${city.areaName} 的校区失败:`, campusResponse?.msg || '未知错误')
        }
      } catch (error) {
        console.warn(`获取市区 ${city.areaName} 的校区时出错:`, error)
      }
    }

    campusList.value = allCampuses
  } catch (error: any) {
    console.error('请求异常:', error)
    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'
    alert(`获取校区列表失败：${errorMsg}`)
  } finally {
    loading.value = false
  }
}

// 获取市区列表
const fetchCityList = async () => {
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
      data: Area[]
    }>('/api/web/area/cities', {
      method: 'GET',
    })

    if (response?.code === 200 && response?.data) {
      cityList.value = response.data
    } else {
      console.error('获取市区列表失败:', response?.msg || '未知错误')
      alert(`获取市区列表失败：${response?.msg || '未知错误'}`)
    }
  } catch (error: any) {
    console.error('获取市区列表异常:', error)
    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取市区列表失败，请稍后重试'
    alert(`获取市区列表失败：${errorMsg}`)
  }
}

// 获取区域管理员列表
// 修改获取管理员列表的函数
const fetchAdminList = async () => {
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 先获取所有管理员，然后在前端过滤
    const response = await request<{
      code: number
      msg: string
      data: Admin[]
    }>('/api/web/admin/list', {
      method: 'GET',
    })

    if (response?.code === 200 && response?.data) {
      // 过滤出区域管理员
      const areaAdmins = response.data.filter(admin =>
        admin.role === 'AREA_ADMIN' || admin.role === 'ROLE_AREA_ADMIN'
      )
      adminList.value = areaAdmins
    } else {
      console.error('获取管理员列表失败:', response?.msg || '未知错误')
      alert(`获取管理员列表失败：${response?.msg || '未知错误'}`)
    }
  } catch (error: any) {
    console.error('获取管理员列表异常:', error)
    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取管理员列表失败，请稍后重试'
    alert(`获取管理员列表失败：${errorMsg}`)
  }
}


// 处理负责人选择变化
const onManagerChange = () => {
  if (selectedManager.value) {
    formData.value.manager = selectedManager.value.adminName
    formData.value.managerPhone = selectedManager.value.phone
  } else {
    formData.value.manager = ''
    formData.value.managerPhone = ''
  }
}

// 新增校区
const handleAddCampus = () => {
  isEdit.value = false
  // 重置表单
  formData.value = {
    areaId: '',
    areaName: '',
    areaType: 'campus',
    parentAreaId: null, // 重置为null，让用户选择
    address: '',
    manager: '',
    managerPhone: '',
    createdTime: undefined,
    updatedTime: undefined
  }
  selectedManager.value = null
  showModal.value = true
}

// 编辑校区
const handleEdit = (campus: Area) => {
  isEdit.value = true
  formData.value = { ...campus }

  // 尝试匹配现有的负责人
  const matchedAdmin = adminList.value.find(admin => admin.adminName === campus.manager)
  selectedManager.value = matchedAdmin || null

  showModal.value = true
}

// 删除校区（先显示确认弹窗）
const handleDelete = (id: string, name: string) => {
  deleteCampusId.value = id
  deleteCampusName.value = name
  showDeleteConfirm.value = true
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
    }>(`/api/web/area/delete/${deleteCampusId.value}`, {
      method: 'DELETE',
    })

    if (response?.code === 200) {
      fetchCampusList() // 重新获取列表
      showDeleteConfirm.value = false
    } else {
      const errorMsg = response?.msg || `删除失败（错误码：${response?.code || '未知'}）`
      console.error('删除校区失败:', errorMsg)
      alert(`删除校区失败：${errorMsg}`)
    }
  } catch (error: any) {
    console.error('删除请求异常:', error)
    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '删除失败，请稍后重试'
    alert(`删除校区失败：${errorMsg}`)
  } finally {
    deleting.value = false
  }
}

// 保存校区信息
// 保存校区信息
const handleSave = async () => {
  saving.value = true
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 必须选择负责人
    if (!selectedManager.value) {
      alert('请选择负责人')
      return
    }

    let response

    if (isEdit.value) {
      // 编辑模式
      response = await request<{
        code: number
        msg: string
        data: Area
      }>('/api/web/area/update', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authStore.token}`
        },
        body: JSON.stringify({
          areaId: formData.value.areaId,
          areaName: formData.value.areaName,
          areaType: 'campus',
          parentAreaId: formData.value.parentAreaId,
          address: formData.value.address,
          manager: formData.value.manager,
          managerPhone: formData.value.managerPhone
        })
      })

      if (response?.code === 200 && response?.data) {
        fetchCampusList() // 重新获取列表
        showModal.value = false
      } else {
        const errorMsg = response?.msg || `更新失败（错误码：${response?.code || '未知'}）`
        console.error('更新校区失败:', errorMsg)
        alert(`更新校区失败：${ errorMsg}`)
      }
    } else {
      // 新增模式 - 添加更严格的验证
      if (!formData.value.parentAreaId || formData.value.parentAreaId.trim() === '') {
        alert('新增校区时必须选择所属市区')
        return
      }

      // 验证选择的市区是否真实存在且类型为市区
      const selectedCity = cityList.value.find(city =>
          city.areaId === formData.value.parentAreaId && city.areaType === 'zone'
      )

      if (!selectedCity) {
        alert('选择的市区不存在或类型错误，请重新选择')
        return
      }

      // 新增模式
      const newCampus = {
        areaName: formData.value.areaName,
        areaType: 'campus',
        parentAreaId: formData.value.parentAreaId,
        address: formData.value.address,
        manager: formData.value.manager,
        managerPhone: formData.value.managerPhone
      }

      console.log('发送的校区数据:', newCampus) // 调试日志

      response = await request<{
        code: number
        msg: string
        data: Area
      }>('/api/web/area/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authStore.token}`
        },
        body: JSON.stringify(newCampus)
      })

      if (response?.code === 200 && response?.data) {
        fetchCampusList() // 重新获取列表
        showModal.value = false
      } else {
        const errorMsg = response?.msg || `新增失败（错误码：${response?.code || '未知'}）`
        console.error('新增校区失败:', errorMsg)
        alert(`新增校区失败：${ errorMsg}`)
      }
    }
  } catch (error: any) {
    console.error('保存请求异常:', error)
    const errorMsg = error.message.includes('401') || error.message.includes('403')
        ? '权限不足或登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '保存失败，请稍后重试'
    alert(`保存校区失败：${ errorMsg}`)
  } finally {
    saving.value = false
  }
}

// 初始化加载数据
onMounted(async () => {
  console.log('Token:', authStore.token)
  await fetchCityList() // 先加载市区列表
  await fetchAdminList() // 加载管理员列表
  fetchCampusList()
})
</script>

<style scoped>
/* 基础样式 */
.campus-page {
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

/* 新增导航按钮样式 */
.btn-nav {
  background: #1890ff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-nav:hover {
  background: #096dd9;
}

/* 表格样式 */
.campus-table {
  width: 100%;
  border-collapse: collapse;
}

.campus-table th,
.campus-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.campus-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.campus-table tbody tr:hover {
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
.form-item textarea,
.form-item select {
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

.btn-submit:disabled {
  background: #a0d911;
  cursor: not-allowed;
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

.btn-delete-confirm:disabled {
  background: #ff7875;
  cursor: not-allowed;
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

  .campus-select, .area-select {
    width: 100%;
  }
}
</style>
