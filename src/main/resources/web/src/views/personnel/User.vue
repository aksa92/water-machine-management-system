<!-- src/views/personnel/User.vue -->
<template>
  <div class="user-page">
    <!-- 页面标题和面包屑（恢复，与管理员/维修人员页面一致） -->
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 人员管理 / 用户</div>
    </div>

    <!-- 操作按钮区（移除新增按钮，保留搜索功能） -->
    <div class="action-bar">
      <!-- 移除新增用户按钮 -->
      
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

    <!-- 用户表格（保持与管理员/维修人员页面一致的列结构） -->
    <div class="card">
      <table class="user-table">
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
          <tr v-for="user in filteredUsers" :key="user.id">
            <td>{{ user.name }}</td>
            <td>{{ user.account }}</td>
            <td>{{ user.phone }}</td>
            <td>
              <span :class="`role-tag ${user.role}`">
                {{ formatRole(user.role) }}
              </span>
            </td>
            <td>
              <span :class="`status-tag ${user.status}`">
                {{ user.status === 'active' ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="operation-buttons">
              <button 
                class="btn-view" 
                @click="handleView(user.id)"
              >
                查看
              </button>
              <button 
                class="btn-edit" 
                @click="handleEdit(user.id)"
              >
                编辑
              </button>
              <button 
                class="btn-status" 
                :class="user.status === 'active' ? 'btn-disable' : 'btn-enable'"
                @click="handleStatusChange(user.id, user.status)"
              >
                {{ user.status === 'active' ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
          <tr v-if="filteredUsers.length === 0">
            <td colspan="6" class="no-data">暂无用户数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页控件（与管理员/维修人员页面样式一致） -->
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

// 限定用户身份类型（仅学生、老师、游客）
type UserRole = 'student' | 'teacher' | 'visitor'
type UserStatus = 'active' | 'disabled'

// 用户数据接口
interface User {
  id: string
  name: string
  account: string
  phone: string
  role: UserRole
  status: UserStatus
}

// 模拟用户数据（仅包含学生、老师、游客三种身份）
const userList: User[] = [
  {
    id: '1',
    name: '吴九',
    account: 'user01',
    phone: '13100131000',
    role: 'student', // 学生
    status: 'active'
  },
  {
    id: '2',
    name: '郑十',
    account: 'user02',
    phone: '13200132000',
    role: 'teacher', // 老师
    status: 'active'
  },
  {
    id: '3',
    name: '王十一',
    account: 'user03',
    phone: '13300133000',
    role: 'visitor', // 游客
    status: 'disabled'
  }
]

// 响应式数据（与管理员/维修人员页面逻辑一致）
const users = ref<User[]>(userList)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 10
const router = useRouter()

// 格式化身份显示（转换为中文）
const formatRole = (role: UserRole) => {
  switch(role) {
    case 'student': return '学生';
    case 'teacher': return '老师';
    case 'visitor': return '游客';
    default: return '未知身份';
  }
}

// 筛选后的用户列表（恢复搜索逻辑）
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const keywordMatch = searchKeyword.value.trim() === '' ||
      user.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      user.account.toLowerCase().includes(searchKeyword.value.toLowerCase())
    return keywordMatch
  })
})

// 分页计算（与管理员/维修人员页面一致）
const totalPages = computed(() => {
  return Math.ceil(filteredUsers.value.length / pageSize)
})

// 搜索处理（恢复）
const handleSearch = () => {
  currentPage.value = 1 // 重置到第一页
}

// 状态变更处理
const handleStatusChange = (id: string, currentStatus: UserStatus) => {
  const newStatus: UserStatus = currentStatus === 'active' ? 'disabled' : 'active'
  users.value = users.value.map(user => 
    user.id === id ? { ...user, status: newStatus } : user
  )
}

// 查看用户详情
const handleView = (id: string) => {
  router.push(`/home/personnel/user/view/${id}`)
}

// 编辑用户
const handleEdit = (id: string) => {
  router.push(`/home/personnel/user/edit/${id}`)
}
</script>

<style scoped>
/* 完全复用管理员/维修人员页面的样式，仅调整角色标签颜色 */
.user-page {
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

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.user-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.user-table tbody tr:hover {
  background-color: #f8f9fa;
}

/* 状态标签样式（与管理员/维修人员页面一致） */
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

/* 角色标签样式（新增，区分学生/老师/游客） */
.role-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.role-tag.student {
  background-color: #e6f7ff;
  color: #1890ff;
}

.role-tag.teacher {
  background-color: #f6f7ff;
  color: #667eea;
}

.role-tag.visitor {
  background-color: #fff7e6;
  color: #d48806;
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

/* 响应式调整（与管理员/维修人员页面一致） */
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