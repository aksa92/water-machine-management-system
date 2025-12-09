<!-- src/views/personnel/Admin.vue -->
<template>
  <div class="admin-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>管理员管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 人员管理 / 管理员</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <button class="btn-add" @click="handleAddAdmin">新增管理员</button>
      
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

    <!-- 管理员表格 -->
    <div class="card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>账号</th>
            <th>联系电话</th>
            <th>身份</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="admin in filteredAdmins" :key="admin.id">
            <td>{{ admin.name }}</td>
            <td>{{ admin.account }}</td>
            <td>{{ admin.phone }}</td>
            <td>{{ admin.role }}</td>
            <td>
              <span :class="`status-tag ${admin.status}`">
                {{ admin.status === 'active' ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="operation-buttons">
              <button 
                class="btn-edit" 
                @click="handleEdit(admin.id)"
              >
                编辑
              </button>
              <button 
                class="btn-status" 
                :class="admin.status === 'active' ? 'btn-disable' : 'btn-enable'"
                @click="handleStatusChange(admin.id, admin.status)"
              >
                {{ admin.status === 'active' ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
          <tr v-if="filteredAdmins.length === 0">
            <td colspan="6" class="no-data">暂无管理员数据</td>
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

// 管理员状态类型
type AdminStatus = 'active' | 'disabled'

// 管理员数据接口
interface Admin {
  id: string
  name: string
  account: string
  phone: string
  role: string
  status: AdminStatus
}

// 模拟管理员数据
const adminList: Admin[] = [
  {
    id: '1',
    name: '张三',
    account: 'admin01',
    phone: '13800138000',
    role: '超级管理员',
    status: 'active'
  },
  {
    id: '2',
    name: '李四',
    account: 'admin02',
    phone: '13900139000',
    role: '设备管理员',
    status: 'active'
  },
  {
    id: '3',
    name: '王五',
    account: 'admin03',
    phone: '13700137000',
    role: '系统管理员',
    status: 'disabled'
  }
]

// 响应式数据
const admins = ref<Admin[]>(adminList)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const router = useRouter()

// 筛选后的管理员列表
const filteredAdmins = computed(() => {
  return admins.value.filter(admin => {
    const keywordMatch = searchKeyword.value.trim() === '' ||
      admin.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      admin.account.toLowerCase().includes(searchKeyword.value.toLowerCase())
    return keywordMatch
  })
})

// 分页计算
const totalPages = computed(() => {
  return Math.ceil(filteredAdmins.value.length / pageSize)
})

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1 // 重置到第一页
}

// 状态变更处理
const handleStatusChange = (id: string, currentStatus: AdminStatus) => {
  const newStatus: AdminStatus = currentStatus === 'active' ? 'disabled' : 'active'
  admins.value = admins.value.map(admin => 
    admin.id === id ? { ...admin, status: newStatus } : admin
  )
  // 实际项目中这里应该调用API更新状态
}

// 编辑处理
const handleEdit = (id: string) => {
  router.push(`/home/personnel/admin/edit/${id}`)
}

// 新增管理员
const handleAddAdmin = () => {
  router.push('/home/personnel/admin/add')
}
</script>

<style scoped>
.admin-page {
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

.admin-table {
  width: 100%;
  border-collapse: collapse;
}

.admin-table th,
.admin-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.admin-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.admin-table tbody tr:hover {
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