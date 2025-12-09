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
          placeholder="搜索姓名或账号..." 
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
            <th>账号</th>
            <th>联系电话</th>
            <th>维修片区</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="staff in filteredStaff" :key="staff.id">
            <td>{{ staff.name }}</td>
            <td>{{ staff.account }}</td>
            <td>{{ staff.phone }}</td>
            <td>{{ staff.area }}</td>
            <td>
              <span :class="`status-tag ${staff.status}`">
                {{ staff.status === 'active' ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="operation-buttons">
              <button 
                class="btn-view" 
                @click="handleViewRecords(staff.id)"
              >
                查看维修记录
              </button>
              <button 
                class="btn-edit" 
                @click="handleEdit(staff.id)"
              >
                编辑
              </button>
              <button 
                class="btn-status" 
                :class="staff.status === 'active' ? 'btn-disable' : 'btn-enable'"
                @click="handleStatusChange(staff.id, staff.status)"
              >
                {{ staff.status === 'active' ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
          <tr v-if="filteredStaff.length === 0">
            <td colspan="6" class="no-data">暂无维修人员数据</td>
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

// 维修人员状态类型
type StaffStatus = 'active' | 'disabled'

// 维修人员数据接口
interface MaintenanceStaff {
  id: string
  name: string
  account: string
  phone: string
  area: string
  status: StaffStatus
}

// 模拟维修人员数据
const staffList: MaintenanceStaff[] = [
  {
    id: '1',
    name: '赵六',
    account: 'repair01',
    phone: '13500135000',
    area: '市区',
    status: 'active'
  },
  {
    id: '2',
    name: '孙七',
    account: 'repair02',
    phone: '13600136000',
    area: '校区',
    status: 'active'
  },
  {
    id: '3',
    name: '周八',
    account: 'repair03',
    phone: '13400134000',
    area: '市区',
    status: 'disabled'
  }
]

// 响应式数据
const staff = ref<MaintenanceStaff[]>(staffList)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const router = useRouter()

// 筛选后的维修人员列表
const filteredStaff = computed(() => {
  return staff.value.filter(person => {
    const keywordMatch = searchKeyword.value.trim() === '' ||
      person.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      person.account.toLowerCase().includes(searchKeyword.value.toLowerCase())
    return keywordMatch
  })
})

// 分页计算
const totalPages = computed(() => {
  return Math.ceil(filteredStaff.value.length / pageSize)
})

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1 // 重置到第一页
}

// 状态变更处理
const handleStatusChange = (id: string, currentStatus: StaffStatus) => {
  const newStatus: StaffStatus = currentStatus === 'active' ? 'disabled' : 'active'
  staff.value = staff.value.map(person => 
    person.id === id ? { ...person, status: newStatus } : person
  )
  // 实际项目中这里应该调用API更新状态
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

.status-tag.active {
  background-color: #e6f7ee;
  color: #00875a;
}

.status-tag.disabled {
  background-color: #f5f5f5;
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

.btn-enable {
  background-color: #e6f7ee;
  color: #00875a;
}

.btn-disable {
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
  
  .search-box {
    width: 100%;
  }
  
  .search-box input {
    width: 100%;
  }
}
</style>