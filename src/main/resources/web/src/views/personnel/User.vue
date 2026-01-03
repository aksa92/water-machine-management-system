<template>
  <div class="user-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 人员管理 / 用户</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
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

    <!-- 用户表格 -->
    <div class="card">
      <table class="user-table">
        <thead>
          <tr>
            <th>姓名</th>
            <th>学号</th>
            <th>联系电话</th>
            <th>身份</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in paginatedUsers" :key="user.studentId">
            <td>{{ user.studentName }}</td>
            <td>{{ user.studentId }}</td>
            <td>{{ user.phone }}</td>
            <td>
              <span class="role-tag student">
                学生
              </span>
            </td>
            <td>
              <span :class="`status-tag ${user.status}`">
                {{ user.status === 'active' ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="operation-buttons">
              <button
                class="btn-delete"
                @click="handleDelete(user.studentId, user.studentName)"
              >
                删除
              </button>
            </td>
          </tr>
          <tr v-if="paginatedUsers.length === 0">
            <td colspan="6" class="no-data">暂无用户数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="modal" v-if="showDeleteModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>确认删除</h3>
          <button class="close-btn" @click="closeDeleteModal">&times;</button>
        </div>
        <div class="modal-body">
          <p>确定要删除用户 <strong>{{ deleteUserName }}</strong> 吗？</p>
          <p>此操作不可撤销，删除后用户信息将永久丢失。</p>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeDeleteModal">取消</button>
          <button class="btn-confirm-delete" @click="confirmDelete" :disabled="deleteLoading">
            {{ deleteLoading ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
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
import { request } from '@/api/request'
import { useAuthStore } from '@/stores/auth'

// 用户状态类型
type UserStatus = 'active' | 'inactive'

// 用户数据接口
interface User {
  studentId: string
  studentName: string
  phone: string
  status: UserStatus
}

const authStore = useAuthStore()

// 响应式数据
const users = ref<User[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 10
const loading = ref(false)

// 删除相关状态
const showDeleteModal = ref(false)
const deleteUserId = ref('')
const deleteUserName = ref('')
const deleteLoading = ref(false)

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    // 检查是否有token
    if (!authStore.token) {
      console.warn('未获取到 Token，跳转到登录页')
      window.location.href = '/login' // 简单跳转到登录页
      return
    }

    // 构建查询参数
    const params = new URLSearchParams()
    if (searchKeyword.value.trim()) {
      params.append('studentName', searchKeyword.value.trim())
    }

    // 使用封装的request工具发送请求
    const response = await request<{
      code: number
      msg: string
      data: User[]
    }>(`/api/web/user/list?${params.toString()}`, {
      method: 'GET'
    })

    // 处理响应
    if (response.code === 200) {
      users.value = response.data || []
    } else {
      const errorMsg = response.msg || `获取失败（错误码：${response.code}）`
      console.error('获取用户列表失败:', errorMsg)
      alert(`获取用户列表失败：${errorMsg}`)
    }
  } catch (error: any) {
    console.error('请求异常:', error)
    const errorMsg = error.message.includes('401')
        ? '登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'
    alert(`获取用户列表失败：${errorMsg}`)

    // Token 无效时跳转登录页
    if (error.message.includes('401')) {
      authStore.logout()
      window.location.href = '/login'
    }
  } finally {
    loading.value = false
  }
}

// 筛选后的用户列表
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const keywordMatch = searchKeyword.value.trim() === '' ||
      user.studentName.toLowerCase().includes(searchKeyword.value.toLowerCase())
    return keywordMatch
  })
})

// 分页计算
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredUsers.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredUsers.value.length / pageSize)
})

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchUserList()
})

// 删除用户 - 显示确认弹窗
const handleDelete = (id: string, name: string) => {
  deleteUserId.value = id
  deleteUserName.value = name
  showDeleteModal.value = true
}

// 确认删除用户
const confirmDelete = async () => {
  deleteLoading.value = true
  try {
    // 发送删除请求
    const response = await request<{
      code: number
      msg: string
      data: null
    }>(`/api/web/user/${deleteUserId.value}`, {
      method: 'DELETE'
    })

    if (response.code === 200) {
      // 删除成功，刷新列表
      showDeleteModal.value = false
      await fetchUserList()
      alert('用户删除成功')
    } else {
      const errorMsg = response.msg || `删除失败（错误码：${response.code}）`
      console.error('删除用户失败:', errorMsg)
      alert(`删除用户失败：${errorMsg}`)
    }
  } catch (error: any) {
    console.error('删除请求异常:', error)
    const errorMsg = error.message.includes('401')
        ? '登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '删除失败，请稍后重试'
    alert(`删除用户失败：${errorMsg}`)

    // Token 无效时跳转登录页
    if (error.message.includes('401')) {
      authStore.logout()
      window.location.href = '/login'
    }
  } finally {
    deleteLoading.value = false
  }
}

// 关闭删除确认弹窗
const closeDeleteModal = () => {
  showDeleteModal.value = false
  deleteUserId.value = ''
  deleteUserName.value = ''
}
</script>

<style scoped>
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

/* 状态标签样式 */
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

.status-tag.inactive {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

/* 角色标签样式 */
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

.btn-delete {
  background-color: #ffe6e6;
  color: #ff4d4f;
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

/* 删除确认弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 450px;
  max-width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-body p {
  margin: 0 0 10px 0;
  color: #666;
  line-height: 1.5;
}

.modal-body p:last-child {
  margin-bottom: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
}

.btn-cancel:hover {
  background: #f5f5f5;
}

.btn-confirm-delete {
  padding: 8px 16px;
  border: none;
  background: #ff4d4f;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}

.btn-confirm-delete:hover {
  background: #ff7875;
}

.btn-confirm-delete:disabled {
  background: #ccc;
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

  .operation-buttons {
    flex-wrap: wrap;
  }

  .modal-content {
    width: 90%;
  }
}
</style>
