<template>
  <div class="admin-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>管理员管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 人员管理 / 管理员</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <button class="btn-add" @click="showAddModal = true">新增管理员</button>

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
          <th>关联区域</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="admin in paginatedAdmins" :key="admin.adminId">
          <td>{{ admin.name }}</td>
          <td>{{ admin.account }}</td>
          <td>{{ admin.phone }}</td>
          <td>{{ formatRole(admin.role) }}</td>
          <td>
            <span v-if="admin.role === 'ROLE_AREA_ADMIN'" class="area-list">
              {{ admin.areaName || (admin.areaId ? getAreaNameById(admin.areaId) : '') || '未关联区域' }}
            </span>
            <span v-else>-</span>
          </td>
          <td class="operation-buttons">
            <button
                class="btn-edit"
                @click="handleEdit(admin.adminId)"
            >
              编辑
            </button>
            <button
                class="btn-delete"
                @click="handleDelete(admin.adminId, admin.name)"
            >
              删除
            </button>
          </td>
        </tr>
        <tr v-if="paginatedAdmins.length === 0">
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
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页 (共 {{ filteredAdmins.length }} 条记录)
      </span>
      <button
          class="page-btn"
          :disabled="currentPage === totalPages"
          @click="currentPage++"
      >
        下一页
      </button>
    </div>

    <!-- 添加管理员弹窗 -->
    <div class="modal-overlay" v-if="showAddModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>新增管理员</h3>
          <button class="modal-close" @click="showAddModal = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label for="name" class="form-label required">姓名：</label>
              <input type="text" id="name" v-model="formData.name" required>
            </div>
            <div class="form-group">
              <label for="account" class="form-label required">账号：</label>
              <input type="text" id="account" v-model="formData.account" required>
            </div>
            <div class="form-group">
              <label for="phone" class="form-label required">联系电话：</label>
              <input type="tel" id="phone" v-model="formData.phone" required>
            </div>
            <div class="form-group">
              <label for="role" class="form-label required">身份：</label>
              <select id="role" v-model="formData.role" @change="handleRoleChange" required>
                <option value="ROLE_SUPER_ADMIN">超级管理员</option>
                <option value="ROLE_AREA_ADMIN">区域管理员</option>
                <option value="ROLE_VIEWER">查看者</option>
              </select>
            </div>

            <!-- 区域选择 - 仅当选择区域管理员时显示，改为单选下拉菜单，选填 -->
            <div class="form-group" v-if="formData.role === 'ROLE_AREA_ADMIN'">
              <label class="form-label">关联片区（选填）：</label>
              <select v-model="selectedAreaId">
                <option value="">请选择片区（可选）</option>
                <option v-for="area in unassignedAreas" :key="area.areaId" :value="area.areaId">
                  {{ area.areaName }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label for="password" class="form-label">初始密码：</label>
              <input
                  type="password"
                  id="password"
                  v-model="formData.password"
                  placeholder="不填默认为123456"
              >
            </div>
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="showAddModal = false">取消</button>
              <button type="submit" class="btn-submit">创建</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 编辑管理员弹窗 -->
    <div class="modal-overlay" v-if="showEditModal">
      <div class="modal-container">
        <div class="modal-header">
          <h3>编辑管理员</h3>
          <button class="modal-close" @click="showEditModal = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="handleEditSubmit">
            <div class="form-group">
              <label for="edit-name" class="form-label required">姓名：</label>
              <input type="text" id="edit-name" v-model="editFormData.name" required>
            </div>
            <div class="form-group">
              <label for="edit-account" class="form-label required">账号：</label>
              <input type="text" id="edit-account" v-model="editFormData.account" required disabled>
            </div>
            <div class="form-group">
              <label for="edit-phone" class="form-label required">联系电话：</label>
              <input type="tel" id="edit-phone" v-model="editFormData.phone" required>
            </div>
            <div class="form-group">
              <label for="edit-role" class="form-label required">身份：</label>
              <select id="edit-role" v-model="editFormData.role" @change="handleEditRoleChange" required>
                <option value="ROLE_SUPER_ADMIN">超级管理员</option>
                <option value="ROLE_AREA_ADMIN">区域管理员</option>
                <option value="ROLE_VIEWER">查看者</option>
              </select>
            </div>

            <!-- 编辑表单中显示关联区域（只读） -->
            <div class="form-group" v-if="editFormData.role === 'ROLE_AREA_ADMIN' && originalAdminData?.areaId">
              <label class="form-label">关联区域：</label>
              <div class="readonly-area">
                {{ getAreaNameById(originalAdminData.areaId) }}
              </div>
            </div>

            <div class="form-group">
              <label for="edit-password" class="form-label">重置密码：</label>
              <input
                  type="password"
                  id="edit-password"
                  v-model="editFormData.password"
                  placeholder="不修改密码请留空"
              >
            </div>
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="showEditModal = false">取消</button>
              <button type="submit" class="btn-submit">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {request} from '@/api/request'
import {useAuthStore} from '@/stores/auth'
import type {ResultVO} from '@/api/types/auth'

// 区域信息接口
interface Area {
  areaId: string
  areaName: string
  areaType: string
  parentAreaId: string | null
  address: string
  manager: string
  managerPhone: string
}

// 管理员数据接口
interface Admin {
  adminId: string
  name: string
  account: string
  phone: string
  role: string
  areaId?: string
  areaName?: string
}

// 表单数据接口
interface FormData {
  name: string
  account: string
  phone: string
  role: string
  password?: string
  areaId?: string
}

// 编辑表单数据接口
interface EditFormData {
  adminId: string
  name: string
  account: string
  phone: string
  role: string
  password?: string
  areaId?: string
}

const authStore = useAuthStore()
const router = useRouter()

// 响应式数据
const admins = ref<Admin[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const loading = ref(false)
const showAddModal = ref(false)
const showEditModal = ref(false)

// 未分配的区域列表（未设置负责人的片区）
const unassignedAreas = ref<Area[]>([])

// 选中的区域ID（单选，新增）
const selectedAreaId = ref<string>('')

// 存储原始管理员数据（用于编辑时保留原有区域关联）
const originalAdminData = ref<Admin | null>(null)

// 弹窗表单数据
const formData = ref<FormData>({
  name: '',
  account: '',
  phone: '',
  role: 'ROLE_SUPER_ADMIN',
  password: '',
  areaId: undefined
})

// 编辑表单数据
const editFormData = ref<EditFormData>({
  adminId: '',
  name: '',
  account: '',
  phone: '',
  role: 'ROLE_SUPER_ADMIN',
  password: '',
  areaId: undefined
})

// 加载未设置负责人的区域列表
const loadUnassignedAreas = async () => {
  try {
    const token = authStore.token

    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    const response = await request<ResultVO<Area[]>>('/api/web/area/without-manager', {
      method: 'GET',
    })

    if (response.code === 200) {
      unassignedAreas.value = response.data || []
    } else {
      console.error('获取未设置负责人片区列表失败:', response.message)
    }
  } catch (error: any) {
    console.error('请求未设置负责人片区列表异常:', error)
    if (error.message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  }
}

// 获取管理员列表
const fetchAdminList = async () => {
  loading.value = true
  try {
    const token = authStore.token

    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    const params = new URLSearchParams()
    if (searchKeyword.value.trim()) {
      params.append('name', searchKeyword.value.trim())
    }

    const response = await request<ResultVO<any[]>>(`/api/web/admin/list?${params.toString()}`, {
      method: 'GET',
    })

    if (response.code === 200) {
      admins.value = (response.data || []).map((admin: any) => ({
        adminId: admin.adminId || '',
        name: admin.adminName || '未知姓名', // 正确映射
        account: admin.adminId || '',
        phone: admin.phone || '未知电话',
        role: admin.role || '未知角色',
        areaId: admin.areaId,
        areaName: admin.areaName || undefined // 添加区域名称
      }))
    } else {
      const errorMsg = response.message || `获取失败（错误码：${response.code}）`
      console.error('获取管理员列表失败:', errorMsg)
      alert(`获取管理员列表失败：${ errorMsg}`)
    }
  } catch (error: any) {
    console.error('请求异常:', error)
    const errorMsg = error.message.includes('401')
        ? '登录已过期，请重新登录'
        : error.message.includes('Net')
            ? '网络连接失败，请检查网络'
            : error.message || '获取数据失败，请稍后重试'
    alert(`获取管理员列表失败：${ errorMsg}`)

    if (error.message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 格式化角色显示
const formatRole = (role: string): string => {
  const roleMap: Record<string, string> = {
    'ROLE_SUPER_ADMIN': '超级管理员',
    'ROLE_AREA_ADMIN': '区域管理员',
    'ROLE_VIEWER': '查看者'
  }
  return roleMap[role] || role
}

// 根据区域ID获取区域名称
// 在script部分添加类型安全的函数
const getAreaNameById = (areaId: string | undefined): string => {
  if (!areaId) return '未知区域'
  const area = unassignedAreas.value.find(a => a.areaId === areaId)
  return area ? area.areaName : '未知区域'
}


// 筛选后的管理员列表
const filteredAdmins = computed(() => {
  return admins.value.filter(admin => {
    return searchKeyword.value.trim() === '' ||
        admin.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        admin.account.toLowerCase().includes(searchKeyword.value.toLowerCase())
  })
})

// 分页计算 - 计算当前页显示的数据
const paginatedAdmins = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredAdmins.value.slice(start, end)
})

// 总页数计算
const totalPages = computed(() => {
  return Math.ceil(filteredAdmins.value.length / pageSize)
})

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchAdminList()
}

// 角色变化处理 - 清空区域选择
const handleRoleChange = () => {
  if (formData.value.role !== 'ROLE_AREA_ADMIN') {
    selectedAreaId.value = ''  // 清空单选区域
  }
}

// 编辑角色变化处理 - 清空区域选择
const handleEditRoleChange = () => {
  if (editFormData.value.role !== 'ROLE_AREA_ADMIN') {
    // 不再清空区域，因为不再允许编辑
  }
}

// 页面加载时获取数据
onMounted(async () => {
  await loadUnassignedAreas()
  await fetchAdminList()
})

// 编辑处理
const handleEdit = async (id: string) => {
  try {
    // 查找要编辑的管理员信息
    const adminToEdit = admins.value.find(admin => admin.adminId === id);
    if (adminToEdit) {
      // 保存原始管理员数据
      originalAdminData.value = { ...adminToEdit };

      // 填充编辑表单数据
      editFormData.value = {
        adminId: adminToEdit.adminId,
        name: adminToEdit.name,
        account: adminToEdit.account,
        phone: adminToEdit.phone,
        role: adminToEdit.role,
        password: '',
        areaId: adminToEdit.areaId
      };

      showEditModal.value = true;
    }
  } catch (error: any) {
    console.error('获取管理员信息失败:', error);
    alert(`获取管理员信息失败：${error.message}`);
  }
}

// 提交编辑表单
const handleEditSubmit = async () => {
  try {
    // 不再处理区域选择逻辑，直接提交
    const submitData = {
      adminId: editFormData.value.adminId,
      adminName: editFormData.value.name,
      account: editFormData.value.account,
      phone: editFormData.value.phone,
      role: editFormData.value.role,
      password: editFormData.value.password || undefined,
      // areaId: undefined // 不传递区域信息，因为不允许编辑关联区域
    };

    const response = await request<ResultVO>(`/api/web/admin/save`, {
      method: 'POST',
      body: JSON.stringify(submitData)
    });

    if (response.code === 200) {
      alert('管理员信息更新成功');
      showEditModal.value = false;
      // 重置编辑表单
      editFormData.value = {
        adminId: '',
        name: '',
        account: '',
        phone: '',
        role: 'ROLE_SUPER_ADMIN',
        password: '',
        areaId: undefined
      };
      originalAdminData.value = null;
      // 重新获取列表
      await fetchAdminList();
    } else {
      alert(`更新失败：${response.message}`);
    }
  } catch (error: any) {
    console.error('更新管理员失败:', error);
    alert(`更新管理员失败：${error.message}`);
  }
};

// 删除管理员处理
const handleDelete = (id: string, name: string) => {
  if (confirm(`确定要删除管理员 "${name}" 吗？此操作不可恢复！`)) {
    performDelete(id);
  }
}

// 执行删除操作
const performDelete = async (id: string) => {
  try {
    const token = authStore.token;

    if (!token) {
      console.warn('未获取到 Token，跳转到登录页');
      await router.push('/login');
      return;
    }

    const response = await request<ResultVO>(`/api/web/admin/${id}`, {
      method: 'DELETE',
    });

    if (response.code === 200) {
      alert('管理员删除成功');
      // 重新获取列表
      await fetchAdminList();
    } else {
      const errorMsg = response.message || `删除失败（错误码：${response.code}）`;
      console.error('删除管理员失败:', errorMsg);
      alert(`删除管理员失败：${ errorMsg}`);
    }
  } catch (error: any) {
    console.error('请求异常:', error);
    const errorMsg = error.message.includes('401')
        ? '登录已过期，请重新登录'
        : error.message.includes('Network')
            ? '网络连接失败，请检查网络'
            : error.message || '删除失败，请稍后重试';
    alert(`删除管理员失败：${ errorMsg}`);

    if (error.message.includes('401')) {
      authStore.logout();
      await router.push('/login');
    }
  }
}

// 添加区域过滤方法
computed(() => {
  return unassignedAreas.value.filter(area => area.areaType === '校园')
});
// 提交新增管理员表单
const handleSubmit = async () => {
  try {
    // 补充默认密码和区域ID
    const submitData = {
      adminName: formData.value.name,  // 修改这里：从 name 改为 adminName
      account: formData.value.account,
      phone: formData.value.phone,
      role: formData.value.role,
      password: formData.value.password || '123456',
      // 只有在选择了区域时才传递areaId
      areaId: selectedAreaId.value ? selectedAreaId.value : undefined
    }

    // 验证账号唯一性等其他验证
    if (!formData.value.name || !formData.value.account || !formData.value.phone) {
      alert('请填写必填项：姓名、账号、联系电话')
      return
    }

    const response = await request<ResultVO>(`/api/web/admin/save`, {
      method: 'POST',
      body: JSON.stringify(submitData)
    })

    if (response.code === 200) {
      alert('管理员添加成功')
      showAddModal.value = false
      // 重置表单
      formData.value = {
        name: '',
        account: '',
        phone: '',
        role: 'ROLE_SUPER_ADMIN',
        password: '',
        areaId: undefined
      }
      selectedAreaId.value = '' // 重置区域选择
      // 重新获取列表
      await fetchAdminList()
    } else {
      alert(`添加失败：${response.message}`)
    }
  } catch (error: any) {
    console.error('添加管理员失败:', error)
    alert(`添加管理员失败：${error.message}`)
  }
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

.area-list {
  color: #666;
  font-size: 12px;
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

.btn-delete {
  background-color: #ffebe6;
  color: #cf1322;
}

.btn-delete:hover {
  background-color: #ffccc7;
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

.modal-container {
  background-color: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
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

.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
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

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-group select[multiple] {
  height: 120px;
}

.readonly-area {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f5f5f5;
  font-size: 14px;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  margin-top: 24px;
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

.btn-submit:hover {
  background: #359e75;
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

.btn-cancel:hover {
  background: #e0e0e0;
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

  .admin-table th,
  .admin-table td {
    padding: 8px 10px;
    font-size: 12px;
  }
}
</style>
