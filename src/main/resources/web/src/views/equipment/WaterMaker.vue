<!-- src/views/equipment/WaterMaker.vue -->
<template>
  <div class="water-maker-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>制水机管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 设备监控 / 制水机</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <button class="btn-add" @click="showAddModal = true">添加制水机</button>

      <div class="filters">
        <!-- 搜索框 -->
        <div class="search-box">
          <input
            type="text"
            placeholder="搜索设备ID或位置..."
            v-model="searchKeyword"
            @input="handleSearch"
          >
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>

        <!-- 片区筛选 -->
        <select
          v-model="selectedArea"
          class="filter-select"
          @change="handleSearch"
        >
          <option value="">全部片区</option>
          <option value="市区">市区</option>
          <option value="校区">校区</option>
        </select>

        <!-- 状态筛选 -->
        <select
          v-model="selectedStatus"
          class="filter-select"
          @change="handleSearch"
        >
          <option value="">全部状态</option>
          <option value="online">在线</option>
          <option value="offline">离线</option>
          <option value="warning">警告</option>
          <option value="fault">故障</option>
        </select>
      </div>
    </div>

    <!-- 设备表格 - 新增设备机型列 -->
    <div class="card">
      <table class="equipment-table">
        <thead>
          <tr>
            <th>设备ID</th>
            <th>设备机型</th> <!-- 新增机型列 -->
            <th>所属片区</th>
            <th>详细位置</th>
            <th>状态</th>
            <th>最后上传时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="device in paginatedDevices" :key="device.deviceId">
            <td>{{ device.deviceId }}</td>
            <td>{{ device.deviceType === 'WATER_MAKER' ? '制水机' : device.deviceType }}</td>
            <td>{{ device.areaId }}</td>
            <td>{{ device.installLocation }}</td>
            <td>
              <span :class="`status-tag ${device.status}`">
                {{ formatStatus(device.status) }}
              </span>
            </td>
            <td>{{ formatDate(device.lastHeartbeatTime) }}</td>
            <td class="operation-buttons">
              <button class="btn-view" @click="viewDevice(device.deviceId)">查看详情</button>
              <button
                class="btn-online"
                @click="updateDeviceStatus(device.deviceId, 'online')"
                :disabled="device.status === 'online'"
              >
                设为在线
              </button>
              <button
                class="btn-offline"
                @click="showOfflineModal(device.deviceId)"
                :disabled="device.status === 'offline'"
              >
                设为离线
              </button>
              <button
                class="btn-fault"
                @click="showFaultModalFunc(device.deviceId)"
                :disabled="device.status === 'fault'"
              >
              设为故障
              </button>

            </td>
          </tr>
          <tr v-if="paginatedDevices.length === 0">
            <td colspan="7" class="no-data">暂无设备数据</td> <!-- colspan从6改为7 -->
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
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页 (共 {{ filteredDevices.length }} 条记录)
      </span>
      <button
        class="page-btn"
        :disabled="currentPage === totalPages"
        @click="currentPage++"
      >
        下一页
      </button>
    </div>

    <!-- 添加设备模态框 -->
    <div v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <div class="modal-content" @click.stop>
        <h3>添加制水机</h3>
        <form @submit.prevent="addDevice">
          <div class="form-group">
            <label>设备ID:</label>
            <input v-model="newDevice.deviceId" type="text" required>
          </div>
          <div class="form-group">
            <label>设备名称:</label>
            <input v-model="newDevice.deviceName" type="text" required>
          </div>
          <div class="form-group">
            <label>所属片区:</label>
            <select v-model="newDevice.areaId" required>
              <option value="市区">市区</option>
              <option value="校区">校区</option>
            </select>
          </div>
          <div class="form-group">
            <label>安装位置:</label>
            <input v-model="newDevice.installLocation" type="text" required>
          </div>
          <div class="form-actions">
            <button type="button" @click="showAddModal = false">取消</button>
            <button type="submit">添加</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 离线原因模态框 -->
    <div v-if="showOfflineReasonModal" class="modal-overlay" @click="showOfflineReasonModal = false">
      <div class="modal-content" @click.stop>
        <h3>设置离线原因</h3>
        <form @submit.prevent="confirmOffline">
          <div class="form-group">
            <label>离线原因:</label>
            <textarea v-model="offlineReason" placeholder="请输入离线原因"></textarea>
          </div>
          <div class="form-actions">
            <button type="button" @click="showOfflineReasonModal = false">取消</button>
            <button type="submit">确认</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 故障信息模态框 -->
    <div v-if="showFaultModal" class="modal-overlay" @click="showFaultModal = false">
      <div class="modal-content" @click.stop>
        <h3>设置故障信息</h3>
        <form @submit.prevent="confirmFault">
          <div class="form-group">
            <label>故障类型:</label>
            <input v-model="faultInfo.faultType" type="text" placeholder="请输入故障类型" required>
          </div>
          <div class="form-group">
            <label>故障描述:</label>
            <textarea v-model="faultInfo.description" placeholder="请输入故障描述" required></textarea>
          </div>
          <div class="form-actions">
            <button type="button" @click="showFaultModal = false">取消</button>
            <button type="submit">确认</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { DeviceStatusApi } from '@/api/deviceStatus'

// 设备状态类型定义
type DeviceStatus = 'online' | 'offline' | 'fault'

// 设备类型定义
interface WaterMakerDevice {
  deviceId: string
  deviceName: string
  deviceType: string
  areaId: string
  installLocation: string
  status: DeviceStatus
  lastHeartbeatTime?: string
  createTime?: string
}

// 响应式数据
const devices = ref<WaterMakerDevice[]>([])
const searchKeyword = ref('')
const selectedArea = ref('') // 片区筛选值
const selectedStatus = ref('') // 状态筛选值
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const router = useRouter()

// 模态框相关
const showAddModal = ref(false)
const showOfflineReasonModal = ref(false)
const showFaultModal = ref(false)

// 当前操作的设备ID
const currentDeviceId = ref('')

// 表单数据
const newDevice = ref({
  deviceId: '',
  deviceName: '',
  areaId: '市区',
  installLocation: '',
  deviceType: 'WATER_MAKER'
})

const offlineReason = ref('')
const faultInfo = ref({
  faultType: '',
  description: ''
})

// 加载设备数据
// 加载设备数据 - 修改 loadDevices 函数
// 在 loadDevices 函数中确保 deviceType 参数正确传递
const loadDevices = async (): Promise<WaterMakerDevice[]> => {
  try {
    const statuses = ['online', 'offline', 'fault']
    const allDevices: WaterMakerDevice[] = []

    for (const status of statuses) {
      // 明确指定 deviceType 参数
      const result = await DeviceStatusApi.getDevicesByStatus(status, undefined, 'water_maker')

      if (result.code === 200 && result.data && Array.isArray(result.data)) {
        allDevices.push(...result.data.map((item: any) => ({
          deviceId: item.deviceId,
          deviceName: item.deviceName,
          deviceType: item.deviceType,
          areaId: item.areaId,
          installLocation: item.installLocation,
          status: item.status,
          lastHeartbeatTime: item.lastHeartbeatTime
        })))
      }
    }

    devices.value = allDevices
    return allDevices
  } catch (error) {
    console.error('加载设备数据失败:', error)
    return []
  }
}




// 多条件过滤设备数据
const filteredDevices = computed(() => {
  return devices.value.filter(device => {
    const keywordMatch = searchKeyword.value.trim() === '' ||
      device.deviceId.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      device.installLocation.toLowerCase().includes(searchKeyword.value.toLowerCase())

    const areaMatch = selectedArea.value === '' || device.areaId === selectedArea.value
    const statusMatch = selectedStatus.value === '' || device.status === selectedStatus.value

    return keywordMatch && areaMatch && statusMatch
  })
})

// 分页计算
const paginatedDevices = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredDevices.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredDevices.value.length / pageSize)
})

// 状态格式化
// 状态格式化 - 位置在 script setup 部分中
const formatStatus = (status: DeviceStatus): string => {
  const statusMap: Record<string, string> = {
    online: '在线',
    offline: '离线',
    fault: '故障'
  }
  return statusMap[status] || status
}


// 时间格式化
const formatDate = (dateString?: string): string => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1 // 重置到第一页
}

// 查看详情
const viewDevice = (id: string) => {
  router.push(`/home/equipment/water-maker/${id}`)
}

// 显示离线模态框
const showOfflineModal = (deviceId: string) => {
  currentDeviceId.value = deviceId
  showOfflineReasonModal.value = true
}

// 显示故障模态框
const showFaultModalFunc = (deviceId: string) => {
  currentDeviceId.value = deviceId
  showFaultModal.value = true
}

// 确认设置为离线
const confirmOffline = async () => {
  try {
    const result = await DeviceStatusApi.markDeviceOffline(
      currentDeviceId.value,
      offlineReason.value
    )

    if (result.code === 200) {
      const device = devices.value.find(d => d.deviceId === currentDeviceId.value)
      if (device) {
        device.status = 'offline'
      }
      showOfflineReasonModal.value = false
      offlineReason.value = ''
    }
  } catch (error) {
    console.error('设置设备离线失败:', error)
  }
}

// 确认设置为故障
const confirmFault = async () => {
  try {
    const result = await DeviceStatusApi.markDeviceFault(
      currentDeviceId.value,
      faultInfo.value.faultType,
      faultInfo.value.description
    )

    if (result.code === 200) {
      const device = devices.value.find(d => d.deviceId === currentDeviceId.value)
      if (device) {
        device.status = 'fault'
      }
      showFaultModal.value = false
      faultInfo.value = { faultType: '', description: '' }
    }
  } catch (error) {
    console.error('设置设备故障失败:', error)
  }
}

// 更新设备状态
// 更新设备状态 - 修改 updateDeviceStatus 函数
const updateDeviceStatus = async (deviceId: string, status: string, remark: string = '') => {
  try {
    let result: any;

    switch (status) {
      case 'online':
        result = await DeviceStatusApi.markDeviceOnline(deviceId)
        break
      case 'offline':
        result = await DeviceStatusApi.markDeviceOffline(deviceId, remark)
        break
      case 'fault':
        // 注意：对于故障状态，需要提供故障类型和描述
        result = await DeviceStatusApi.markDeviceFault(deviceId, 'MANUAL_FAULT', remark || '手动设置故障')
        break
      default:
        throw new Error('不支持的状态类型')
    }

    if (result.code === 200) {
      // 更新本地数据
      const device = devices.value.find(d => d.deviceId === deviceId)
      if (device) {
        device.status = status as DeviceStatus
      }
      return true
    } else {
      throw new Error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('更新设备状态失败:', error)
    throw error
  }
}


// 添加设备
const addDevice = async () => {
  try {
    // 构造设备对象
    const deviceToAdd = {
      deviceId: newDevice.value.deviceId,
      deviceName: newDevice.value.deviceName,
      areaId: newDevice.value.areaId,
      installLocation: newDevice.value.installLocation,
      deviceType: newDevice.value.deviceType
    };

    // 调用后端API添加设备
    const result = await fetch('/api/web/device/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(deviceToAdd)
    }).then(response => response.json());

    if (result.code === 200) {
      // 添加成功后重新加载设备列表
      await loadDevices();

      // 重置表单并关闭模态框
      showAddModal.value = false;
      newDevice.value = {
        deviceId: '',
        deviceName: '',
        areaId: '市区',
        installLocation: '',
        deviceType: 'WATER_MAKER'
      };

      console.log('设备添加成功');
    } else {
      console.error('设备添加失败:', result.message);
    }
  } catch (error) {
    console.error('添加设备失败:', error);
  }
};


// 组件挂载时加载数据
// 在 onMounted 中添加更多调试信息
onMounted(async () => {
  console.log('🚀 开始加载设备数据...')

  try {
    const result = await loadDevices()
    console.log('🌐 API返回 data:', result)

    if (result.length === 0) {
      console.warn('⚠️ 数据库中无设备数据')
    } else {
      console.log('✅ 成功加载设备数据:', result)
    }
  } catch (error) {
    console.error('❌ 加载设备数据失败:', error)
  }
})



</script>

<style scoped>
/* 样式与供水机页面保持一致 */
.water-maker-page {
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

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
}

.equipment-table {
  width: 100%;
  border-collapse: collapse;
}

.equipment-table th,
.equipment-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.equipment-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.equipment-table tbody tr:hover {
  background-color: #f8f9fa;
}

.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.online {
  background-color: #e6f7ee;
  color: #00875a;
}

.status-tag.offline {
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

.operation-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.operation-buttons button {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: opacity 0.3s;
}

.operation-buttons button:hover:not(:disabled) {
  opacity: 0.9;
}

.operation-buttons button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-view {
  background-color: #e6f7ff;
  color: #1890ff;
}

.btn-online {
  background-color: #e6f7ee;
  color: #00875a;
}

.btn-offline {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

.btn-fault {
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
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
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

/* 响应式调整 */
@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    width: 100%;
  }

  .search-box, .filter-select {
    width: 100%;
  }

  .modal-content {
    width: 90%;
    min-width: auto;
  }
}
</style>
