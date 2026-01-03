<template>
  <div class="supply-machine-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>供水机管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 设备监控 / 供水机</div>
    </div>

    <div class="action-bar">
      <button class="btn-add" @click="showAddModal = true">添加供水机</button>
      <div class="filters">
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索设备ID或位置..."
            @input="handleSearch"
          >
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>

        <!-- 两层筛选：市区选择影响校区列表 -->
        <div class="area-filter">
          <select
            v-model="selectedCity"
            class="filter-select"
            @change="onCityChange"
          >
            <option value="">选择市区</option>
            <option
              v-for="city in cityList"
              :key="city.areaId"
              :value="city.areaId"
            >
              {{ city.areaName }}
            </option>
          </select>

          <select
            v-model="selectedCampus"
            class="filter-select"
            :disabled="!selectedCity"
            @change="onCampusChange"
          >
            <option value="">选择校区</option>
            <option
              v-for="campus in campusList"
              :key="campus.areaId"
              :value="campus.areaId"
            >
              {{ campus.areaName }}
            </option>
          </select>
        </div>

        <select
          v-model="selectedStatus"
          class="filter-select"
          @change="currentPage = 1"
        >
          <option value="">全部状态</option>
          <option value="online">在线</option>
          <option value="offline">离线</option>
          <option value="warning">警告</option>
          <option value="fault">故障</option>
        </select>
      </div>
    </div>

    <div class="card">
      <table class="equipment-table">
        <thead>
          <tr>
            <th>设备ID</th>
            <th>设备名称</th>
            <th>设备类型</th>
            <th>所属片区</th>
            <th>安装位置</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="device in paginatedDevices" :key="device.deviceId">
            <td>{{ device.deviceId }}</td>
            <td>{{ device.deviceName }}</td>
            <td>{{ device.deviceType }}</td>
            <td>{{ device.areaId }}</td>
            <td>{{ device.installLocation }}</td>
            <td>
               <span :class="`status-tag ${device.status}`">
                  {{ formatStatus(device.status) }}
               </span>
            </td>
            <td class="operation-buttons">
              <button class="btn-view" @click="viewDevice(device.deviceId)">查看详情</button>
              <button class="btn-edit" @click="openEditModal(device)">编辑</button>
              <button
                class="btn-delete"
                @click="deleteDevice(device.deviceId)"
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

    <!-- 添加/编辑设备模态框 -->
    <div v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <div class="modal-content" @click.stop>
        <h3>{{ isEditing ? '编辑设备' : '添加供水机' }}</h3>
        <form @submit.prevent="saveDevice">
          <div class="form-group">
            <label>设备ID:</label>
            <input v-model="currentDevice.deviceId" type="text" :disabled="isEditing" required>
          </div>
          <div class="form-group">
            <label>设备名称:</label>
            <input v-model="currentDevice.deviceName" type="text" required>
          </div>
          <div class="form-group">
            <label>安装位置:</label>
            <input v-model="currentDevice.installLocation" type="text" required>
          </div>
          <div class="form-group">
            <label>设备类型:</label>
            <select v-model="currentDevice.deviceType" required>
              <option value="water_supply">供水机</option>
              <option value="other">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>状态:</label>
            <select v-model="currentDevice.status">
              <option value="online">在线</option>
              <option value="offline">离线</option>
              <option value="warning">警告</option>
              <option value="fault">故障</option>
            </select>
          </div>

          <!-- 市区选择 -->
          <div class="form-group" v-if="!isEditing">
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
          <div class="form-group" v-if="!isEditing && selectedCityId">
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

          <div class="form-actions">
            <button type="button" @click="showAddModal = false">取消</button>
            <button type="submit">{{ isEditing ? '更新' : '添加' }}</button>
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

// 检查是否为设备对象的类型守卫
const isDeviceManageVO = (obj: any): obj is DeviceManageVO => {
  return obj &&
         typeof obj === 'object' &&
         typeof obj.deviceId === 'string' &&
         typeof obj.deviceName === 'string' &&
         typeof obj.installLocation === 'string' &&
         typeof obj.deviceType === 'string' &&
         ['online', 'offline', 'warning', 'fault'].includes(obj.status)
}

// 设备状态类型定义
type DeviceStatus = 'online' | 'offline' | 'warning' | 'fault'

// 设备类型定义 - 适配后端DeviceManageVO
interface DeviceManageVO {
  deviceId: string
  deviceName: string
  installLocation: string
  deviceType: string
  status: DeviceStatus
  areaId?: string
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
const devices = ref<DeviceManageVO[]>([])
const searchKeyword = ref('')
const selectedCity = ref('') // 市区筛选值
const selectedCampus = ref('') // 校区筛选值
const selectedStatus = ref('') // 状态筛选值
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const router = useRouter()
const authStore = useAuthStore() // 初始化auth store

// 模态框相关
const showAddModal = ref(false)
const isEditing = ref(false)

// 当前操作的设备数据
const currentDevice = ref<DeviceManageVO>({
  deviceId: '',
  deviceName: '',
  installLocation: '',
  deviceType: 'water_supply',
  status: 'online',
  areaId: undefined
})

// 片区相关数据
const cityList = ref<Area[]>([]) // 市区列表
const campusList = ref<Area[]>([]) // 校区列表
const selectedCityId = ref('') // 选择的市区ID
const selectedCampusId = ref('') // 选择的校区ID

// 加载设备数据
const loadDevices = async (): Promise<void> => {
  try {
    // 显式检查token而不是仅仅依赖isLoggedIn
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    console.log('开始加载供水机设备数据...')

    // 构建请求参数
    const params = new URLSearchParams()
    if (selectedStatus.value && selectedStatus.value !== '') {
      params.append('status', selectedStatus.value)
    }
    // 如果选择了校区，则按校区筛选；否则不筛选
    if (selectedCampus.value && selectedCampus.value !== '') {
      params.append('areaId', selectedCampus.value)
    }
    params.append('deviceType', 'water_supply')

    const queryString = params.toString()
    const url = `/api/web/device-status/by-type${queryString ? `?${queryString}` : ''}`

    // 直接按设备类型查询所有供水机
    const result = await request<ResultVO<DeviceManageVO[]>>(
      url,
      { method: 'GET' }
    )

    console.log('供水机设备请求结果:', result)

    if (result.code === 200 && result.data && Array.isArray(result.data)) {
      console.log(`获取到${result.data.length}个供水机设备`)
      devices.value = result.data.map((item: any) => ({
        deviceId: item.deviceId,
        deviceName: item.deviceName,
        deviceType: item.deviceType,
        areaId: item.areaId,
        installLocation: item.installLocation,
        status: item.status
      }))
    }

    if (devices.value.length === 0) {
      console.log('提示：未找到任何供水机设备，请确认是否已添加设备')
    }
  } catch (error) {
    console.error('加载设备数据失败:', error)
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  }
}

// 加载市区列表
const loadCityList = async (): Promise<void> => {
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
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
      await router.push('/login')
    }
  }
}

// 根据市区ID加载校区列表
const loadCampusListByCity = async (cityId: string): Promise<void> => {
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
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
      await router.push('/login')
    }
  }
}

// 市区选择变化时的处理
const onCityChange = async () => {
  selectedCampus.value = ''
  campusList.value = []

  if (selectedCityId.value) {  // ✅ 应该监听 selectedCityId
    await loadCampusListByCity(selectedCityId.value)
  }
}

// 校区选择变化时的处理
const onCampusChange = () => {
  currentPage.value = 1 // 选择校区后重置到第一页

  // 根据 selectedCampusId 获取对应的校区对象，然后提取 areaName
  if (selectedCampusId.value) {
    const selectedCampus = campusList.value.find(campus => campus.areaId === selectedCampusId.value)
    if (selectedCampus) {
      currentDevice.value.areaId = selectedCampus.areaName // 将 areaName 赋值给 areaId
    } else {
      currentDevice.value.areaId = undefined
    }
  } else {
    currentDevice.value.areaId = undefined
  }
}



// 多条件过滤设备数据
const filteredDevices = computed(() => {
  return devices.value.filter(device => {
    const keywordMatch = searchKeyword.value.trim() === '' ||
        device.deviceId.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        device.installLocation.toLowerCase().includes(searchKeyword.value.toLowerCase())

    // 如果选择了校区，则匹配校区；否则不过滤片区
    let areaMatch = true
    if (selectedCampus.value && selectedCampus.value !== '') {
      areaMatch = device.areaId === selectedCampus.value ||
                 device.areaId === campusList.value.find(c => c.areaId === selectedCampus.value)?.areaName
    }

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
    warning: '警告',
    fault: '故障'
  }
  return statusMap[status] || status
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1 // 重置到第一页
  loadDevices() // 重新加载数据
}

// 查看详情
const viewDevice = (id: string) => {
  router.push(`/home/equipment/water-supplier/${id}`)
}

// 编辑设备
const openEditModal = (device: DeviceManageVO) => {
  currentDevice.value = { ...device }
  isEditing.value = true
  showAddModal.value = true

  // 确保市区列表已加载
  if (cityList.value.length === 0) {
    loadCityList()
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
      await router.push('/login')
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
      await router.push('/login')
    }
  }
}

// 保存设备（新增或更新）
const saveDevice = async () => {
  try {
    // 显式检查token
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    // 验证是否选择了校区（新增设备时）
    if (!isEditing.value && !selectedCampusId.value) {
      alert('请选择校区')
      return
    }

    // 验证areaId是否已设置
    if (!currentDevice.value.areaId) {
      alert('请先选择校区以确定所属片区')
      return
    }

    let result: ResultVO<DeviceManageVO> | DeviceManageVO
    if (isEditing.value) {
      // 更新设备
      result = await request<ResultVO<DeviceManageVO> | DeviceManageVO>('/api/web/device/edit', {
        method: 'PUT',
        body: JSON.stringify(currentDevice.value)
      })
    } else {
      // 添加设备
      // 验证是否选择了校区（新增设备时）
      if (!selectedCampusId.value) {
        alert('请选择校区')
        return
      }

      // 验证是否设置了片区
      if (!currentDevice.value.areaId) {
        alert('请选择校区以确定所属片区')
        return
      }

      result = await request<ResultVO<DeviceManageVO> | DeviceManageVO>('/api/web/device/add', {
        method: 'POST',
        body: JSON.stringify(currentDevice.value)
      })
    }

    // 检查是否为ResultVO格式的响应
    if (result && typeof result === 'object' && 'code' in result) {
      // 如果是ResultVO格式
      if (result.code === 200 && result.data && isDeviceManageVO(result.data)) {
        // 添加成功后重新加载设备列表
        await loadDevices()

        // 重置表单并关闭模态框
        showAddModal.value = false
        isEditing.value = false
        currentDevice.value = {
          deviceId: '',
          deviceName: '',
          installLocation: '',
          deviceType: 'water_supply',
          status: 'online',
          areaId: undefined
        }
        selectedCityId.value = ''
        selectedCampusId.value = ''
        campusList.value = []

        alert(isEditing.value ? '设备更新成功' : '设备添加成功')
      } else if (result.code === 200) {
        // 如果code是200但没有data字段
        // 添加成功后重新加载设备列表
        await loadDevices()

        // 重置表单并关闭模态框
        showAddModal.value = false
        isEditing.value = false
        currentDevice.value = {
          deviceId: '',
          deviceName: '',
          installLocation: '',
          deviceType: 'water_supply',
          status: 'online',
          areaId: undefined
        }
        selectedCityId.value = ''
        selectedCampusId.value = ''
        campusList.value = []

        alert(isEditing.value ? '设备更新成功' : '设备添加成功')
      } else {
        alert(`${isEditing.value ? '更新' : '添加'}设备失败: ${result.message}`)
      }
    }
    // 处理直接返回设备对象的情况
    else if (isDeviceManageVO(result)) {
      // 添加成功后重新加载设备列表
      await loadDevices()

      // 重置表单并关闭模态框
      showAddModal.value = false
      isEditing.value = false
      currentDevice.value = {
        deviceId: '',
        deviceName: '',
        installLocation: '',
        deviceType: 'water_supply',
        status: 'online',
        areaId: undefined
      }
      selectedCityId.value = ''
      selectedCampusId.value = ''
      campusList.value = []

      alert(isEditing.value ? '设备更新成功' : '设备添加成功')
    }
    else {
      // 其他成功情况
      // 添加成功后重新加载设备列表
      await loadDevices()

      // 重置表单并关闭模态框
      showAddModal.value = false
      isEditing.value = false
      currentDevice.value = {
        deviceId: '',
        deviceName: '',
        installLocation: '',
        deviceType: 'water_supply',
        status: 'online',
        areaId: undefined
      }
      selectedCityId.value = ''
      selectedCampusId.value = ''
      campusList.value = []

      alert(isEditing.value ? '设备更新成功' : '设备添加成功')
    }
  } catch (error) {
    console.error(`${isEditing.value ? '更新' : '添加'}设备失败:`, error)
    alert(`${isEditing.value ? '更新' : '添加'}设备失败`)
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  console.log('🚀 开始加载设备数据...')
  await loadCityList()
  await loadDevices() // 加载设备数据
})
</script>



<style scoped>
/* 样式与终端机页面保持一致 */
.supply-machine-page {
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

.status-tag.maintenance {
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

.btn-edit {
  background-color: #faad14;
  color: white;
}

.btn-delete {
  background-color: #ff4d4f;
  color: white;
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
}
</style>
