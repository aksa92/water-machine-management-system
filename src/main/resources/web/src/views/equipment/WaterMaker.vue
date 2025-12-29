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
      <!-- 在操作按钮列中添加删除按钮 -->


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
          <option value="A">A</option>
          <option value="B">B</option>
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

              删除
            </button>
          </td>
        </tr>
        <tr v-if="paginatedDevices.length === 0">
          <td colspan="7" class="no-data">暂无设备数据</td>
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

          <!-- 市区选择 -->
          <div class="form-group">
            <label>选择市区:</label>
            <select
              v-model="selectedCityId"
              @change="onCityChange"
              class="select-input"
            >
              <option value="">请选择市区</option>
              <option
                v-for="city in cityList"
                :key="city.areaId"
                :value="city.areaId"
              >
                {{ city.areaName }}
              </option>
            </select>
          </div>

          <!-- 校区选择 -->
          <div class="form-group" v-if="selectedCityId">
            <label>选择校区:</label>
            <select
              v-model="selectedCampusId"
              @change="onCampusChange"
              class="select-input"
              :disabled="!campusList.length"
            >
              <option value="">请选择校区</option>
              <option
                v-for="campus in campusList"
                :key="campus.areaId"
                :value="campus.areaId"
              >
                {{ campus.areaName }}
              </option>
            </select>
            <p v-if="selectedCityId && !campusList.length" class="no-data-message">
              该市区暂无校区
            </p>
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
import { useAuthStore } from '@/stores/auth'
import { request } from '@/api/request'
import type { ResultVO } from '@/api/types/auth'

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

// 区域类型定义
interface Area {
  areaId: string
  areaName: string
  areaType: string
  parentAreaId: string | null
  address: string
  manager: string
  managerPhone: string
}

// 响应式数据
const devices = ref<WaterMakerDevice[]>([])
const searchKeyword = ref('')
const selectedArea = ref('') // 片区筛选值
const selectedStatus = ref('') // 状态筛选值
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const router = useRouter()
const authStore = useAuthStore() // 初始化auth store

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
  areaId: '',
  installLocation: '',
  deviceType: 'water_maker'
})

// 片区相关数据
const cityList = ref<Area[]>([]) // 市区列表
const campusList = ref<Area[]>([]) // 校区列表
const selectedCityId = ref('') // 选择的市区ID
const selectedCampusId = ref('') // 选择的校区ID

const offlineReason = ref('')
const faultInfo = ref({
  faultType: '',
  description: ''
})

// 加载设备数据
const loadDevices = async (): Promise<void> => {
  try {
    // 显式检查token而不是仅仅依赖isLoggedIn
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    console.log('开始加载制水机设备数据...')

    // 直接按设备类型查询所有制水机
    const result = await request<ResultVO<WaterMakerDevice[]>>(
      `/api/web/device-status/by-type?deviceType=water_maker`,
      { method: 'GET' }
    )

    console.log('制水机设备请求结果:', result)

    if (result.code === 200 && result.data && Array.isArray(result.data)) {
      console.log(`获取到${result.data.length}个制水机设备`)
      devices.value = result.data.map((item: any) => ({
        deviceId: item.deviceId,
        deviceName: item.deviceName,
        deviceType: item.deviceType,
        areaId: item.areaId,
        installLocation: item.installLocation,
        status: item.status,
        lastHeartbeatTime: item.lastHeartbeatTime
      }))
    }

    if (devices.value.length === 0) {
      console.log('提示：未找到任何制水机设备，请确认是否已添加设备')
    }
  } catch (error) {
    console.error('加载设备数据失败:', error)
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 加载市区列表
const loadCityList = async (): Promise<void> => {
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    console.log('开始加载市区列表...')

    const result = await request<any>('/api/web/area/cities', { method: 'GET' })

    if (result && typeof result === 'object' && 'code' in result) {
      if (result.code === 200 && result.data && Array.isArray(result.data)) {
        cityList.value = result.data
        console.log(`获取到${cityList.value.length}个市区`)
      } else {
        console.warn('API响应非成功状态或数据格式错误:', result)
        cityList.value = []
      }
    } else if (Array.isArray(result)) {
      cityList.value = result
    } else {
      console.warn('API响应数据格式错误:', result)
      cityList.value = []
    }
  } catch (error) {
    console.error('加载市区列表失败:', error)
    cityList.value = []
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 根据市区ID加载校区列表
const loadCampusListByCity = async (cityId: string): Promise<void> => {
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    console.log(`开始加载市区 ${cityId} 的校区列表...`)

    const result = await request<any>(`/api/web/area/campuses/${cityId}`, { method: 'GET' })

    if (result && typeof result === 'object' && 'code' in result) {
      if (result.code === 200 && result.data && Array.isArray(result.data)) {
        campusList.value = result.data
        console.log(`获取到${campusList.value.length}个校区`)
      } else {
        console.warn('API响应非成功状态或数据格式错误:', result)
        campusList.value = []
      }
    } else if (Array.isArray(result)) {
      campusList.value = result
    } else {
      console.warn('API响应数据格式错误:', result)
      campusList.value = []
    }
  } catch (error) {
    console.error('加载校区列表失败:', error)
    campusList.value = []
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 市区选择变化时的处理
const onCityChange = async () => {
  // 清空校区选择和设备ID
  selectedCampusId.value = ''
  campusList.value = []

  if (selectedCityId.value) {
    await loadCampusListByCity(selectedCityId.value)
  }
}

// 校区选择变化时的处理
const onCampusChange = () => {
  // 设置areaId为选中校区的areaName（不是areaId）
  const selectedCampus = campusList.value.find(campus => campus.areaId === selectedCampusId.value)
  if (selectedCampus) {
    newDevice.value.areaId = selectedCampus.areaName // 使用areaName作为areaId
  } else {
    newDevice.value.areaId = ''
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
    // 显式检查token
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    const result = await request<ResultVO<boolean>>(`/api/web/device-status/${currentDeviceId.value}/offline`, {
      method: 'POST',
      body: JSON.stringify({ reason: offlineReason.value })
    })

    if (result.code === 200) {
      const device = devices.value.find(d => d.deviceId === currentDeviceId.value)
      if (device) {
        device.status = 'offline'
      }
      showOfflineReasonModal.value = false
      offlineReason.value = ''
      alert('设备已标记为离线')
    } else {
      alert(`设置设备离线失败: ${result.message}`)
    }
  } catch (error) {
    console.error('设置设备离线失败:', error)
    alert('设置设备离线失败')
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 确认设置为故障
const confirmFault = async () => {
  try {
    // 显式检查token
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    const result = await request<ResultVO<boolean>>(`/api/web/device-status/${currentDeviceId.value}/fault`, {
      method: 'POST',
      body: JSON.stringify(faultInfo.value)
    })

    if (result.code === 200) {
      const device = devices.value.find(d => d.deviceId === currentDeviceId.value)
      if (device) {
        device.status = 'fault'
      }
      showFaultModal.value = false
      faultInfo.value = { faultType: '', description: '' }
      alert('设备已标记为故障')
    } else {
      alert(`设置设备故障失败: ${result.message}`)
    }
  } catch (error) {
    console.error('设置设备故障失败:', error)
    alert('设置设备故障失败')
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 更新设备状态为在线
const updateDeviceStatus = async (deviceId: string, status: string) => {
  try {
    // 显式检查token
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    let url = ''
    let body = {}

    switch (status) {
      case 'online':
        url = `/api/web/device-status/${deviceId}/online`
        break
      case 'offline':
        url = `/api/web/device-status/${deviceId}/offline`
        body = { reason: '手动设置为离线' }
        break
      case 'fault':
        url = `/api/web/device-status/${deviceId}/fault`
        body = { faultType: 'MANUAL', description: '手动设置为故障' }
        break
      default:
        throw new Error('不支持的状态类型')
    }

    const result = await request<ResultVO<boolean>>(url, {
      method: 'POST',
      body: JSON.stringify(body)
    })

    if (result.code === 200) {
      // 更新本地数据
      const device = devices.value.find(d => d.deviceId === deviceId)
      if (device) {
        device.status = status as DeviceStatus
      }
      alert(`设备已标记为${formatStatus(status as DeviceStatus)}`)
    } else {
      alert(`更新设备状态失败: ${result.message}`)
    }
  } catch (error) {
    console.error('更新设备状态失败:', error)
    alert('更新设备状态失败')
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 删除设备
const deleteDevice = async (deviceId: string) => {
  if (!confirm(`确定要删除设备 ${deviceId} 吗？此操作不可恢复。`)) {
    return
  }

  try {
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    const result = await request<ResultVO<boolean>>(`/api/web/device/delete/${deviceId}`, {
      method: 'DELETE'
    })

    if (result.code === 200) {
      // 从本地列表中移除设备
      devices.value = devices.value.filter(d => d.deviceId !== deviceId)
      alert('设备删除成功')
    } else {
      alert(`删除设备失败: ${result.message}`)
    }
  } catch (error) {
    console.error('删除设备失败:', error)
    alert('删除设备失败')
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}


// 添加设备
const addDevice = async () => {
  try {
    // 显式检查token
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      router.push('/login')
      return
    }

    // 验证是否选择了校区
    if (!selectedCampusId.value) {
      alert('请选择校区')
      return
    }

    // 验证areaId是否已设置
    if (!newDevice.value.areaId) {
      alert('请先选择校区以确定所属片区')
      return
    }

    // 构造设备对象
    const deviceToAdd = {
      deviceId: newDevice.value.deviceId,
      deviceName: newDevice.value.deviceName,
      areaId: newDevice.value.areaId,
      installLocation: newDevice.value.installLocation,
      deviceType: newDevice.value.deviceType
    }

    // 使用统一请求工具，自动携带token
    const result = await request<ResultVO<any>>('/api/web/device/add', {
      method: 'POST',
      body: JSON.stringify(deviceToAdd)
    })

    if (result.code === 200) {
      // 添加成功后重新加载设备列表
      await loadDevices()

      // 重置表单并关闭模态框
      showAddModal.value = false
      newDevice.value = {
        deviceId: '',
        deviceName: '',
        areaId: '',
        installLocation: '',
        deviceType: 'water_maker'
      }
      selectedCityId.value = ''
      selectedCampusId.value = ''
      campusList.value = []

      alert('设备添加成功')
    } else {
      alert(`设备添加失败: ${result.message}`)
    }
  } catch (error) {
    console.error('添加设备失败:', error)
    alert('添加设备失败')
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  console.log('🚀 开始加载设备数据...')
  await loadDevices()
  await loadCityList()
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

/* 新增：下拉框样式 */
.select-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
  background: white;
  cursor: pointer;
}

.select-input:focus {
  outline: none;
  border-color: #42b983;
}

/* 新增：无数据提示样式 */
.no-data-message {
  margin-top: 8px;
  color: #8c8c8c;
  font-size: 12px;
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

  .btn-delete {
    background-color: #ff4d4f;
    color: white;
  }
}
</style>
