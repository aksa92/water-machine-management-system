<template>
  <div class="terminal-detail-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>终端详情</h2>
      <div class="breadcrumb">校园矿化水平台 / 设备监控 / 终端机 / 详情</div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error">{{ error }}</div>

    <!-- 终端详情内容 -->
    <div v-else-if="terminal" class="terminal-detail-content">
      <!-- 终端基本信息卡片 -->
      <div class="card">
        <h3>终端基本信息</h3>
        <div class="detail-grid">
          <div class="detail-item">
            <label>终端ID:</label>
            <span>{{ terminal.terminalId }}</span>
          </div>
          <div class="detail-item">
            <label>终端名称:</label>
            <span>{{ terminal.terminalName }}</span>
          </div>
          <div class="detail-item">
            <label>经度:</label>
            <span>{{ terminal.longitude }}</span>
          </div>
          <div class="detail-item">
            <label>纬度:</label>
            <span>{{ terminal.latitude }}</span>
          </div>
          <div class="detail-item">
            <label>状态:</label>
            <span :class="`status-tag ${terminal.terminalStatus}`">
              {{ formatStatus(terminal.terminalStatus) }}
            </span>
          </div>
          <div class="detail-item">
            <label>安装日期:</label>
            <span>{{ formatDate(terminal.installDate) }}</span>
          </div>
          <div class="detail-item">
            <label>关联设备ID:</label>
            <span>{{ terminal.deviceId || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button class="btn-back" @click="goBack">返回列表</button>
      </div>
    </div>

    <!-- 无数据状态 -->
    <div v-else class="no-data">未找到终端信息</div>

    <!-- 编辑模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
      <div class="modal-content" @click.stop>
        <h3>编辑终端</h3>
        <form @submit.prevent="saveTerminal">
          <div class="form-group">
            <label>终端ID:</label>
            <input v-model="editingTerminal.terminalId" type="text" disabled>
          </div>
          <div class="form-group">
            <label>终端名称:</label>
            <input v-model="editingTerminal.terminalName" type="text" required>
          </div>
          <div class="form-group">
            <label>经度:</label>
            <input v-model="editingTerminal.longitude" type="number" step="any" required>
          </div>
          <div class="form-group">
            <label>纬度:</label>
            <input v-model="editingTerminal.latitude" type="number" step="any" required>
          </div>
          <div class="form-group">
            <label>状态:</label>
            <select v-model="editingTerminal.terminalStatus">
              <option value="active">在线</option>
              <option value="inactive">离线</option>
              <option value="warning">警告</option>
              <option value="fault">故障</option>
            </select>
          </div>
          <div class="form-group">
            <label>安装日期:</label>
            <input v-model="editingTerminal.installDate" type="date">
          </div>
          <div class="form-group">
            <label>关联设备ID:</label>
            <input v-model="editingTerminal.deviceId" type="text">
          </div>
          <div class="form-actions">
            <button type="button" @click="showEditModal = false">取消</button>
            <button type="submit">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { request } from '@/api/request'
import type { ResultVO } from '@/api/types/auth'

// 终端状态类型定义
type TerminalStatus = 'active' | 'inactive' | 'fault' | 'warning'

// 终端类型定义 - 适配后端TerminalManageVO
interface TerminalManageVO {
  terminalId: string
  terminalName: string
  longitude: number
  latitude: number
  terminalStatus: TerminalStatus
  installDate?: string
  deviceId?: string
}

// 检查是否为终端对象的类型守卫
function isTerminalManageVO(obj: any): obj is TerminalManageVO {
  return obj &&
         typeof obj === 'object' &&
         typeof obj.terminalId === 'string' &&
         typeof obj.terminalName === 'string' &&
         typeof obj.longitude === 'number' &&
         typeof obj.latitude === 'number' &&
         ['active', 'inactive', 'warning', 'fault'].includes(obj.terminalStatus)
}

// 响应式数据
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')
const terminal = ref<TerminalManageVO | null>(null)
const showEditModal = ref(false)
const editingTerminal = ref<TerminalManageVO>({
  terminalId: '',
  terminalName: '',
  longitude: 0,
  latitude: 0,
  terminalStatus: 'active'
})

// 获取终端ID
const terminalId = route.params.id as string

// 格式化状态
const formatStatus = (status: TerminalStatus): string => {
  const statusMap: Record<string, string> = {
    active: '在线',
    inactive: '离线',
    warning: '警告',
    fault: '故障'
  }
  return statusMap[status] || status
}

// 时间格式化
const formatDate = (dateString?: string): string => {
  if (!dateString) return '-'
  // 如果是日期字符串，直接返回，否则格式化
  return dateString
}

// 加载终端详情
const loadTerminal = async () => {
  if (!terminalId) {
    error.value = '缺少终端ID'
    return
  }

  try {
    loading.value = true
    error.value = ''

    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    const result = await request<ResultVO<TerminalManageVO> | TerminalManageVO>(`/api/web/terminal/${terminalId}`, {
      method: 'GET'
    })

    console.log('终端详情请求结果:', result)

    // 检查响应是否为ResultVO格式
    if (result && typeof result === 'object' && 'code' in result) {
      // 如果是ResultVO格式
      if (result.code === 200 && result.data && isTerminalManageVO(result.data)) {
        // 确保数据格式正确
        terminal.value = {
          terminalId: result.data.terminalId,
          terminalName: result.data.terminalName,
          longitude: result.data.longitude,
          latitude: result.data.latitude,
          terminalStatus: result.data.terminalStatus,
          installDate: result.data.installDate,
          deviceId: result.data.deviceId
        }
        // 初始化编辑终端数据
        editingTerminal.value = { ...terminal.value }
      } else {
        error.value = result.message || '获取终端信息失败'
        console.warn('API响应非成功状态或数据格式错误:', result)
      }
    } else if (isTerminalManageVO(result)) {
      // 如果直接返回终端对象（可能是后端直接返回对象而非ResultVO包装）
      terminal.value = {
        terminalId: result.terminalId,
        terminalName: result.terminalName,
        longitude: result.longitude,
        latitude: result.latitude,
        terminalStatus: result.terminalStatus,
        installDate: result.installDate,
        deviceId: result.deviceId
      }
      // 初始化编辑终端数据
      editingTerminal.value = { ...terminal.value }
    } else {
      error.value = '获取终端信息失败'
      console.warn('API响应数据格式错误:', result)
    }
  } catch (err) {
    console.error('加载终端详情失败:', err)
    error.value = '加载终端详情失败'
    if ((err as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 编辑终端
const editTerminal = () => {
  if (terminal.value) {
    editingTerminal.value = { ...terminal.value }
    showEditModal.value = true
  }
}

// 保存终端修改
// 保存终端修改
const saveTerminal = async () => {
  try {
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    const result = await request<ResultVO<TerminalManageVO> | TerminalManageVO>(`/api/web/terminal/update`, {
      method: 'PUT',
      body: JSON.stringify(editingTerminal.value)
    })

    // 检查是否为ResultVO格式的响应
    if (result && typeof result === 'object' && 'code' in result) {
      // 如果是ResultVO格式
      if (result.code === 200 && result.data && isTerminalManageVO(result.data)) {
        // 更新本地数据
        if (terminal.value) {
          Object.assign(terminal.value, result.data)
        }
        showEditModal.value = false
        alert('终端信息更新成功')
      } else if (result.code === 200) {
        // 如果code是200但没有data字段，可能是更新成功但返回格式不同
        if (terminal.value) {
          Object.assign(terminal.value, editingTerminal.value)
        }
        showEditModal.value = false
        alert('终端信息更新成功')
      } else {
        // 处理错误情况
        const errorMessage = result.message || '更新终端信息失败'
        alert(`更新终端信息失败: ${errorMessage}`)
      }
    }
    // 处理直接返回终端对象的情况（这是当前遇到的情况）
    else if (isTerminalManageVO(result)) {
      // 如果直接返回终端对象，说明操作成功
      if (terminal.value) {
        Object.assign(terminal.value, result)
      }
      showEditModal.value = false
      alert('终端信息更新成功')
    }
    else {
      // 如果不是ResultVO格式，也可能是直接返回成功
      if (terminal.value) {
        Object.assign(terminal.value, editingTerminal.value)
      }
      showEditModal.value = false
      alert('终端信息更新成功')
    }
  } catch (err) {
    console.error('更新终端信息失败:', err)
    alert('更新终端信息失败')
    if ((err as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}


// 删除终端
const deleteTerminal = async () => {
  if (!confirm(`确定要删除终端 ${terminal.value?.terminalId} 吗？`)) {
    return
  }

  try {
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    const result = await request<ResultVO<boolean>>(`/api/web/terminal/delete/${terminal.value?.terminalId}`, {
      method: 'DELETE'
    })

    if (result.code === 200 || result.code === 201 || result.code === 204) {
      alert('终端删除成功')
      router.push('/home/equipment/terminal')
    } else {
      alert(`删除终端失败: ${result.message}`)
    }
  } catch (err) {
    console.error('删除终端失败:', err)
    alert('删除终端失败')
    if ((err as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}


// 返回列表
const goBack = () => {
  router.push('/home/equipment/terminal')
}

// 组件挂载时加载数据
onMounted(() => {
  loadTerminal()
})
</script>


<style scoped>
.terminal-detail-page {
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

.loading, .error, .no-data {
  text-align: center;
  padding: 40px 0;
  color: #666;
}

.error {
  color: #f5222d;
}

.card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.card h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-item label {
  font-weight: 500;
  color: #666;
  margin-bottom: 4px;
  font-size: 14px;
}

.detail-item span {
  color: #333;
  font-size: 15px;
}

.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  align-self: flex-start;
  max-width: fit-content;
}

.status-tag.active {
  background-color: #e6f7ee;
  color: #00875a;
}

.status-tag.inactive {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

.status-tag.warning {
  background-color: #fff7e6;
  color: #d48806;
}

.status-tag.fault {
  background-color: #ffebe6;
  color: #cf1322;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.action-buttons button {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #ddd;
  font-size: 14px;
}

.btn-edit {
  background: #42b983;
  color: white;
  border: none;
}

.btn-delete {
  background: #ff4d4f;
  color: white;
  border: none;
}

.btn-back {
  background: #f5f5f5;
  color: #666;
}

/* 模态框样式 */
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
  padding: 24px;
  border-radius: 8px;
  min-width: 400px;
  max-width: 500px;
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.form-actions button {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #ddd;
}

.form-actions button[type="button"] {
  background: #f5f5f5;
}

.form-actions button[type="submit"] {
  background: #42b983;
  color: white;
  border: none;
}
</style>
