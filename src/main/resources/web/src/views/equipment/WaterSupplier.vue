<template>
  <div class="water-supplier-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>供水机管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 设备监控 / 供水机</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <button class="btn-add" @click="showAddModal = true">添加供水机</button>

      <div class="filters">
        <!-- 搜索框 -->
        <div class="search-box">
          <input
            type="text"
            placeholder="搜索设备ID或位置..."
            v-model="searchKeyword"
            @input="handleSearch"
          >
          <button class="search-btn">搜索</button>
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
          <option value="error">故障</option>
        </select>
      </div>
    </div>

    <!-- 表格 -->
    <div class="card">
      <table class="equipment-table">
        <thead>
          <tr>
            <th>设备ID</th>
            <th>设备机型</th>
            <th>所属片区</th>
            <th>详细位置</th>
            <th>状态</th>
            <th>最后上传时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="device in paginatedDevices" :key="device.id">
            <td>{{ device.id }}</td>
            <td>供水机</td>
            <td>{{ device.area }}</td>
            <td>{{ device.location }}</td>
            <td>
              <span :class="`status-tag ${device.status}`">
                {{ formatStatus(device.status) }}
              </span>
            </td>
            <td>{{ device.lastUploadTime }}</td>
            <td class="operation-buttons">
              <button class="btn-view" @click="viewDevice(device.id)">查看详情</button>
              <button class="btn-edit" @click="openEditModal(device)">编辑</button>
              <button class="btn-delete" @click="currentDeviceId = device.id; showDeleteModal = true">删除</button>
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
        <h3>添加供水机</h3>
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
            <select v-model="newDevice.areaId" @change="loadAvailableMakers" required>
              <option value="">请选择片区</option>
              <option
                v-for="area in cityList"
                :key="area.areaId"
                :value="area.areaId"
              >
                {{ area.areaName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>关联制水机:</label>
            <select v-model="newDevice.parentMakerId" :disabled="!newDevice.areaId">
              <option value="">请选择关联制水机</option>
              <option v-for="maker in availableMakers" :key="maker.id" :value="maker.id">
                {{ maker.name }}
              </option>
            </select>
            <p v-if="!newDevice.areaId" class="help-text">请先选择片区以加载可关联的制水机</p>
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

    <!-- 编辑设备模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click="showEditModal = false">
      <div class="modal-content" @click.stop>
        <h3>编辑供水机</h3>
        <form @submit.prevent="updateDevice">
          <div class="form-group">
            <label>设备ID:</label>
            <input v-model="editingDevice.deviceId" type="text" disabled>
          </div>
          <div class="form-group">
            <label>设备名称:</label>
            <input v-model="editingDevice.deviceName" type="text" required>
          </div>
          <div class="form-group">
            <label>所属片区:</label>
            <select v-model="editingDevice.areaId" @change="loadAvailableMakersForEdit" required>
              <option value="">请选择片区</option>
              <option
                v-for="area in cityList"
                :key="area.areaId"
                :value="area.areaId"
              >
                {{ area.areaName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>关联制水机:</label>
            <select v-model="editingDevice.parentMakerId" :disabled="!editingDevice.areaId">
              <option value="">请选择关联制水机</option>
              <option v-for="maker in availableMakersForEdit" :key="maker.id" :value="maker.id">
                {{ maker.name }}
              </option>
            </select>
            <p v-if="!editingDevice.areaId" class="help-text">请先选择片区以加载可关联的制水机</p>
          </div>
          <div class="form-group">
            <label>安装位置:</label>
            <input v-model="editingDevice.installLocation" type="text" required>
          </div>
          <div class="form-actions">
            <button type="button" @click="showEditModal = false">取消</button>
            <button type="submit">更新</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 删除确认模态框 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal-content" @click.stop>
        <h3>确认删除设备</h3>
        <p>确定要删除设备 ID: {{ currentDeviceId }} 吗？此操作不可恢复。</p>
        <div class="form-actions">
          <button type="button" @click="showDeleteModal = false">取消</button>
          <button type="button" @click="deleteDevice()">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/api/request'
import type { ResultVO } from '@/api/types/auth'
import { useAuthStore } from '@/stores/auth'

// 设备状态类型定义
type DeviceStatus = 'online' | 'offline' | 'warning' | 'error'

// 设备数据接口
interface WaterSupplierDevice {
  id: string
  area: string
  location: string
  status: DeviceStatus
  lastUploadTime: string
}

// 设备详情接口
interface DeviceDetail {
  deviceId: string
  deviceName: string
  areaId: string
  installLocation: string
  deviceType: string
  parentMakerId?: string
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
const devices = ref<WaterSupplierDevice[]>([])
const searchKeyword = ref('')
const selectedCity = ref('') // 市区筛选值
const selectedCampus = ref('') // 校区筛选值
const selectedStatus = ref('') // 状态筛选值
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const router = useRouter()
const authStore = useAuthStore()

// 片区相关数据
const cityList = ref<Area[]>([]) // 市区列表
const campusList = ref<Area[]>([]) // 校区列表

// 新增：添加设备相关状态
const showAddModal = ref(false)
const newDevice = ref({
  deviceId: '',
  deviceName: '',
  areaId: '',
  parentMakerId: '', // 关联的制水机ID
  installLocation: '',
  deviceType: 'water_supply'
})

// 编辑：编辑设备相关状态
const showEditModal = ref(false)
const editingDevice = ref<DeviceDetail>({
  deviceId: '',
  deviceName: '',
  areaId: '',
  installLocation: '',
  deviceType: 'water_supply',
  parentMakerId: ''
})

// 新增：可关联的制水机列表
const availableMakers = ref<{id: string, name: string}[]>([])
const availableMakersForEdit = ref<{id: string, name: string}[]>([])

// 新增：删除设备相关状态
const showDeleteModal = ref(false)
const currentDeviceId = ref('')

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
  // 清空校区选择
  selectedCampus.value = ''
  campusList.value = []

  if (selectedCity.value) {
    await loadCampusListByCity(selectedCity.value)
  } else {
    // 如果清空市区选择，也清空校区列表
    campusList.value = []
  }
}

// 校区选择变化时的处理
const onCampusChange = () => {
  // 选择校区后直接触发设备加载
  currentPage.value = 1
  loadWaterSuppliers()
}

// 加载供水机列表
const loadWaterSuppliers = async () => {
  try {
    // 检查token
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    // 构建请求参数
    const params = new URLSearchParams();
    if (selectedStatus.value && selectedStatus.value !== '') {
      params.append('status', selectedStatus.value);
    }
    // 只有在选择了校区时才按校区筛选，市区选择不作为筛选条件
    if (selectedCampus.value && selectedCampus.value !== '') {
      params.append('areaId', selectedCampus.value)
    }
    params.append('deviceType', 'water_supply'); // 供水机类型

    const queryString = params.toString();
    const url = `/api/web/device-status/by-type${queryString ? `?${queryString}` : ''}`;

    // 发起请求
    const response = await request<ResultVO<any[]>>(url, { method: 'GET' });

    if (response.code === 200 && response.data && Array.isArray(response.data)) {
      devices.value = response.data.map((device: any) => ({
        id: device.deviceId,
        area: device.areaId,
        location: device.installLocation,
        status: device.status,
        lastUploadTime: device.lastHeartbeatTime || device.lastUpdateTime || '-'
      }));
    } else {
      console.error('获取供水机列表失败:', response.message);
      alert('获取供水机列表失败: ' + response.message);
    }
  } catch (error) {
    console.error('加载供水机列表失败:', error);
    alert('获取供水机列表失败，请检查网络连接');
    if (error instanceof Error && error.message.includes('401')) {
      authStore.logout();
      await router.push('/login');
    }
  }
}

// 加载指定片区内可用的制水机设备
const loadAvailableMakers = async () => {
  if (!newDevice.value.areaId) {
    availableMakers.value = []
    return
  }

  try {
    const token = authStore.token
    if (!token) {
      await router.push('/login')
      return
    }

    // 请求该片区内的所有制水机
    const params = new URLSearchParams();
    params.append('areaId', newDevice.value.areaId);
    params.append('deviceType', 'water_maker');

    const queryString = params.toString();
    const url = `/api/web/device-status/by-type${queryString ? `?${queryString}` : ''}`;

    const response = await request<ResultVO<any[]>>(url, { method: 'GET' });

    if (response.code === 200 && response.data && Array.isArray(response.data)) {
      // 转换为选项格式，包含ID和位置信息
      availableMakers.value = response.data.map((maker: any) => ({
        id: maker.deviceId,
        name: `${maker.deviceId} - ${maker.installLocation}`
      }))
    } else {
      console.error('获取制水机列表失败:', response.message);
      availableMakers.value = []
    }
  } catch (error) {
    console.error('加载制水机列表失败:', error);
    availableMakers.value = []
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  }
}

// 编辑模式下加载指定片区内可用的制水机设备
const loadAvailableMakersForEdit = async () => {
  if (!editingDevice.value.areaId) {
    availableMakersForEdit.value = []
    return
  }

  try {
    const token = authStore.token
    if (!token) {
      await router.push('/login')
      return
    }

    // 请求该片区内的所有制水机
    const params = new URLSearchParams();
    params.append('areaId', editingDevice.value.areaId);
    params.append('deviceType', 'water_maker');

    const queryString = params.toString();
    const url = `/api/web/device-status/by-type${queryString ? `?${queryString}` : ''}`;

    const response = await request<ResultVO<any[]>>(url, { method: 'GET' });

    if (response.code === 200 && response.data && Array.isArray(response.data)) {
      // 转换为选项格式，包含ID和位置信息
      availableMakersForEdit.value = response.data.map((maker: any) => ({
        id: maker.deviceId,
        name: `${maker.deviceId} - ${maker.installLocation}`
      }))
    } else {
      console.error('获取制水机列表失败:', response.message);
      availableMakersForEdit.value = []
    }
  } catch (error) {
    console.error('加载制水机列表失败:', error);
    availableMakersForEdit.value = []
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 多条件过滤设备数据
// 修改 filteredDevices 的计算属性，只根据校区进行筛选
const filteredDevices = computed(() => {
  return devices.value.filter(device => {
    const keywordMatch = searchKeyword.value.trim() === '' ||
      device.id.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      device.location.toLowerCase().includes(searchKeyword.value.toLowerCase())

    // 只根据校区进行筛选，如果校区未选择则不过滤片区
    let areaMatch = true
    if (selectedCampus.value && selectedCampus.value !== '') {
      // 只有在选择了校区时才进行片区匹配
      areaMatch = device.area === selectedCampus.value
    }
    // 如果没有选择校区，则不进行片区过滤

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
  const statusMap = {
    online: '在线',
    offline: '离线',
    warning: '警告',
    error: '故障'
  }
  return statusMap[status]
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1 // 搜索后重置到第一页
  loadWaterSuppliers()
}

// 查看详情
const viewDevice = (id: string) => {
  router.push(`/home/equipment/water-supplier/${id}`)
}

// 添加设备
const addDevice = async () => {
  try {
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    const deviceToAdd = {
      deviceId: newDevice.value.deviceId,
      deviceName: newDevice.value.deviceName,
      areaId: newDevice.value.areaId,
      installLocation: newDevice.value.installLocation,
      deviceType: newDevice.value.deviceType,
      parentMakerId: newDevice.value.parentMakerId || null // 关联的制水机ID
    }

    const result = await request<ResultVO<any>>('/api/web/device/add', {
      method: 'POST',
      body: JSON.stringify(deviceToAdd)
    })

    if (result.code === 200) {
      await loadWaterSuppliers()
      showAddModal.value = false
      resetAddForm()
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

// 重置添加表单
const resetAddForm = () => {
  newDevice.value = {
    deviceId: '',
    deviceName: '',
    areaId: '',
    parentMakerId: '',
    installLocation: '',
    deviceType: 'water_supply'
  }
  availableMakers.value = []
}

// 打开编辑模态框
const openEditModal = async (device: WaterSupplierDevice) => {
  try {
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    const response = await request<ResultVO<{ deviceInfo: DeviceDetail }>>(`/api/web/device/${device.id}`, { method: 'GET' })

    if (response.code === 200 && response.data?.deviceInfo) {
      const deviceDetail = response.data.deviceInfo // 👈 关键修改：从 deviceInfo 提取
      editingDevice.value = {
        deviceId: deviceDetail.deviceId,
        deviceName: deviceDetail.deviceName,
        areaId: deviceDetail.areaId,
        installLocation: deviceDetail.installLocation,
        deviceType: deviceDetail.deviceType,
        parentMakerId: deviceDetail.parentMakerId || ''
      }

      // 加载可关联的制水机
      await loadAvailableMakersForEdit()

      showEditModal.value = true
    } else {
      alert(`获取设备详情失败: ${response.message}`)
    }
  } catch (error) {
    console.error('获取设备详情失败:', error)
    alert('获取设备详情失败')
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}


// 更新设备
const updateDevice = async () => {
  try {
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    const deviceToUpdate = {
      deviceId: editingDevice.value.deviceId,
      deviceName: editingDevice.value.deviceName,
      areaId: editingDevice.value.areaId,
      installLocation: editingDevice.value.installLocation,
      deviceType: editingDevice.value.deviceType,
      parentMakerId: editingDevice.value.parentMakerId || null
    }

    const result = await request<ResultVO<any>>('/api/web/device/edit', {
      method: 'PUT',
      body: JSON.stringify(deviceToUpdate)
    })

    if (result.code === 200) {
      await loadWaterSuppliers()
      showEditModal.value = false
      resetEditForm()
      alert('设备更新成功')
    } else {
      alert(`设备更新失败: ${result.message}`)
    }
  } catch (error) {
    console.error('更新设备失败:', error)
    alert('更新设备失败')
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 重置编辑表单
const resetEditForm = () => {
  editingDevice.value = {
    deviceId: '',
    deviceName: '',
    areaId: '',
    installLocation: '',
    deviceType: 'water_supply',
    parentMakerId: ''
  }
  availableMakersForEdit.value = []
}

// 删除设备
const deleteDevice = async () => {
  try {
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    const result = await request<ResultVO<boolean>>(`/api/web/device/delete/${currentDeviceId.value}`, {
      method: 'DELETE'
    })

    if (result.code === 200) {
      await loadWaterSuppliers()
      showDeleteModal.value = false
      alert('设备删除成功')
    } else {
      alert(`设备删除失败: ${result.message}`)
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

// 页面加载时获取数据
onMounted(async () => {
  await loadCityList() // 加载市区列表
  loadWaterSuppliers()
})
</script>

<style scoped>
/* 样式与终端机页面保持一致 */
.water-supplier-page {
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
  flex-wrap: wrap;
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

.area-filter {
  display: flex;
  gap: 8px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  min-width: 120px;
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

.status-tag.error {
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

  .search-box, .area-filter, .filter-select {
    width: 100%;
  }

  .area-filter {
    flex-direction: column;
  }

  .modal-content {
    width: 90%;
    min-width: auto;
  }
}
</style>
