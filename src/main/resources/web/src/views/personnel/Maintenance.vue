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
      <button class="btn-add" @click="handleAddMaintenance">新增维修人员</button>

      <div class="search-box">
        <input
          type="text"
          placeholder="搜索姓名..."
          v-model="searchKeyword"
          @input="handleSearch"
        >
        <button class="search-btn" @click="handleSearch">搜索</button>
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
                @click="handleEdit(staff.repairmanId)"
              >
                编辑
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import type { RepairmanStatus } from '@/api/types/repairman'

// 维修人员数据接口
interface MaintenanceStaff {
  repairmanId: string
  repairmanName: string
  phone: string
  areaId: string
  status: RepairmanStatus
}

const authStore = useAuthStore()
const router = useRouter()

// 响应式数据
const staffList = ref<MaintenanceStaff[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const loading = ref(false)

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
    if (searchKeyword.value.trim()) {
      params.append('name', searchKeyword.value.trim())
    }

    // 使用封装的request工具发送请求
    const response = await request<{
      code: number
      msg: string
      data: MaintenanceStaff[]
    }>(`/api/web/repairman/list?${params.toString()}`, {
      method: 'GET'
    })

    // 处理响应
    if (response.code === 200) {
      staffList.value = response.data || []
    } else {
      const errorMsg = response.msg || `获取失败（错误码：${response.code}）`
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
  return staffList.value.filter(person => {
    const keywordMatch = searchKeyword.value.trim() === '' ||
      person.repairmanName.toLowerCase().includes(searchKeyword.value.toLowerCase())
    return keywordMatch
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

// 获取状态文本
const getStatusText = (status: RepairmanStatus) => {
  const statusMap: Record<RepairmanStatus, string> = {
    'idle': '空闲',
    'busy': '忙碌',
    'vacation': '休假'
  }
  return statusMap[status] || status
}

// 编辑处理
const handleEdit = (id: string) => {
  router.push(`/home/personnel/maintenance/edit/${id}`)
}

// 查看维修记录
const handleViewRecords = (id: string) => {
  router.push(`/home/personnel/maintenance/records/${id}`)
}

// 新增维修人员
const handleAddMaintenance = () => {
  router.push('/home/personnel/maintenance/add')
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

.search-box {
  display: flex;
  gap: 8px;
}

.search-box input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 240px;
}

.search-btn {
  background: #667eea;
  color: white;
  border: none;
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

  .search-box {
    width: 100%;
  }

  .search-box input {
    width: 100%;
  }
}
</style>
