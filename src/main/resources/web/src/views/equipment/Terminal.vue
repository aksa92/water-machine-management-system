<template>
  <div class="terminal-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>终端机管理</h2>
      <div class="breadcrumb">校园矿化水平台 / 设备监控 / 终端机</div>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <button class="btn-add" @click="showAddModal = true">添加终端机</button>

      <div class="filters">
        <!-- 搜索框 -->
        <div class="search-box">
          <input
              type="text"
              placeholder="搜索终端ID或名称..."
              v-model="searchKeyword"
              @input="handleSearch"
          >
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>

        <!-- 状态筛选 -->
        <select
            v-model="selectedStatus"
            class="filter-select"
            @change="handleSearch"
        >
          <option value="">全部状态</option>
          <option value="active">在线</option>
          <option value="inactive">离线</option>
          <option value="warning">警告</option>
          <option value="fault">故障</option>
        </select>
      </div>
    </div>

    <!-- 终端表格 -->
    <div class="card">
      <table class="equipment-table">
        <thead>
        <tr>
          <th>终端ID</th>
          <th>终端名称</th>
          <th>经度</th>
          <th>纬度</th>
          <th>状态</th>
          <th>安装日期</th>
          <th>关联设备ID</th>
          <th>所属片区</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="terminal in paginatedTerminals" :key="terminal.terminalId">
          <td>{{ terminal.terminalId }}</td>
          <td>{{ terminal.terminalName }}</td>
          <td>{{ terminal.longitude }}</td>
          <td>{{ terminal.latitude }}</td>
          <td>
              <span :class="`status-tag ${terminal.terminalStatus}`">
                {{ formatStatus(terminal.terminalStatus) }}
              </span>
          </td>
          <td>{{ formatDate(terminal.installDate) }}</td>
          <td>{{ terminal.deviceId || '-' }}</td>
          <td>{{ terminal.areaId || '-' }}</td>
          <td class="operation-buttons">
            <button class="btn-view" @click="viewTerminal(terminal.terminalId)">查看详情</button>
            <button
                class="btn-edit"
                @click="editTerminal(terminal)"
            >
              编辑
            </button>
            <button
                class="btn-delete"
                @click="deleteTerminal(terminal.terminalId)"
            >
              删除
            </button>
          </td>
        </tr>
        <tr v-if="paginatedTerminals.length === 0">
          <td colspan="9" class="no-data">暂无终端数据</td>
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
        第 {{ currentPage }} 页 / 共 {{ totalPages }} 页 (共 {{ filteredTerminals.length }} 条记录)
      </span>
      <button
          class="page-btn"
          :disabled="currentPage === totalPages"
          @click="currentPage++"
      >
        下一页
      </button>
    </div>

    <!-- 添加/编辑终端模态框 -->
    <div v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <div class="modal-content" @click.stop>
        <h3>{{ isEditing ? '编辑终端' : '添加终端机' }}</h3>
        <form @submit.prevent="saveTerminal">
          <div class="form-group">
            <label>终端ID:</label>
            <input v-model="currentTerminal.terminalId" type="text" :disabled="isEditing" required>
          </div>
          <div class="form-group">
            <label>终端名称:</label>
            <input v-model="currentTerminal.terminalName" type="text" required>
          </div>
          <div class="form-group">
            <label>经度:</label>
            <input v-model="currentTerminal.longitude" type="number" step="any" required>
          </div>
          <div class="form-group">
            <label>纬度:</label>
            <input v-model="currentTerminal.latitude" type="number" step="any" required>
          </div>
          <div class="form-group">
            <label>状态:</label>
            <select v-model="currentTerminal.terminalStatus">
              <option value="active">在线</option>
              <option value="inactive">离线</option>
              <option value="warning">警告</option>
              <option value="fault">故障</option>
            </select>
          </div>
          <div class="form-group">
            <label>安装日期:</label>
            <input v-model="currentTerminal.installDate" type="date">
          </div>

          <!-- 市区选择 -->
          <div class="form-group">
            <label>选择市区:</label>
            <select
              v-model="selectedCityId"
              @change="onCityChange"
              class="select-input"
              :disabled="isEditing"
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
              :disabled="!campusList.length || isEditing"
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
            <label>关联设备ID:</label>
            <select
              v-model="currentTerminal.deviceId"
              class="select-input"
              :disabled="!filteredSupplyDevices.length"
            >
              <option value="">无关联设备</option>
              <option
                  v-for="device in filteredSupplyDevices"
                  :key="device.deviceId"
                  :value="device.deviceId"
              >
                {{ device.deviceId }} - {{ device.installLocation }} ({{ device.deviceName }})
              </option>
            </select>
            <p v-if="selectedCampusId && !filteredSupplyDevices.length" class="no-data-message">
              该校区暂无可用的供水机
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

// 检查是否为终端对象的类型守卫
const isTerminalManageVO = (obj: any): obj is TerminalManageVO => {
  return obj &&
         typeof obj === 'object' &&
         typeof obj.terminalId === 'string' &&
         typeof obj.terminalName === 'string' &&
         typeof obj.longitude === 'number' &&
         typeof obj.latitude === 'number' &&
         ['active', 'inactive', 'warning', 'fault'].includes(obj.terminalStatus)
}

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
  areaId?: string
}

// 设备类型定义
interface Device {
  deviceId: string
  deviceName: string
  installLocation: string
  deviceType: string
  status: string
  areaId: string // 设备所属片区ID
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
const terminals = ref<TerminalManageVO[]>([])
const searchKeyword = ref('')
const selectedStatus = ref('') // 状态筛选值
const currentPage = ref(1)
const pageSize = 10 // 每页显示数量
const router = useRouter()
const authStore = useAuthStore() // 初始化auth store

// 模态框相关
const showAddModal = ref(false)
const isEditing = ref(false)

// 当前操作的终端数据
const currentTerminal = ref<TerminalManageVO>({
  terminalId: '',
  terminalName: '',
  longitude: 0,
  latitude: 0,
  terminalStatus: 'active',
  deviceId: undefined
})

// 可用供水机设备列表
const availableSupplyDevices = ref<Device[]>([])

// 片区相关数据
const cityList = ref<Area[]>([]) // 市区列表
const campusList = ref<Area[]>([]) // 校区列表
const selectedCityId = ref('') // 选择的市区ID
const selectedCampusId = ref('') // 选择的校区ID

// 加载终端数据
const loadTerminals = async (): Promise<void> => {
  try {
    // 显式检查token而不是仅仅依赖isLoggedIn
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    console.log('开始加载终端机数据...')

    // 获取所有终端
    const result = await request<ResultVO<TerminalManageVO[]>>(`/api/web/terminal/list`, { method: 'GET' })

    console.log('终端机请求结果:', result)

    // 检查响应是否为ResultVO格式
    if (result && typeof result === 'object' && 'code' in result) {
      // 如果是ResultVO格式
      if (result.code === 200 && result.data && Array.isArray(result.data)) {
        console.log(`获取到${result.data.length}个终端`)
        // 确保数据格式正确
        terminals.value = result.data.map(item => ({
          terminalId: item.terminalId,
          terminalName: item.terminalName,
          longitude: item.longitude,
          latitude: item.latitude,
          terminalStatus: item.terminalStatus,
          installDate: item.installDate,
          deviceId: item.deviceId,
          areaId: item.areaId
        }))
      } else {
        console.warn('API响应非成功状态或数据格式错误:', result)
        terminals.value = []
      }
    } else {
      // 如果直接返回数组（可能是后端直接返回数组而非ResultVO包装）
      if (Array.isArray(result)) {
        console.log(`获取到${(result as TerminalManageVO[]).length}个终端`)
        terminals.value = (result as TerminalManageVO[]).map(item => ({
          terminalId: item.terminalId,
          terminalName: item.terminalName,
          longitude: item.longitude,
          latitude: item.latitude,
          terminalStatus: item.terminalStatus,
          installDate: item.installDate,
          deviceId: item.deviceId,
          areaId: item.areaId
        }))
      } else {
        console.warn('API响应数据格式错误:', result)
        terminals.value = []
      }
    }

    if (terminals.value.length === 0) {
      console.log('提示：未找到任何终端，请确认是否已添加终端')
    }
  } catch (error) {
    console.error('加载终端数据失败:', error)
    terminals.value = []
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

// 加载可用供水机设备列表
const loadAvailableSupplyDevices = async (): Promise<void> => {
  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    console.log('开始加载设备列表...')

    // 使用与供水机页面相同的接口获取供水机
    const params = new URLSearchParams();
    params.append('deviceType', 'water_supply'); // 只获取供水机
    params.append('status', 'online'); // 只获取在线设备

    const queryString = params.toString();
    const url = `/api/web/device-status/by-type${queryString ? `?${queryString}` : ''}`;

    const result = await request<any>(url, { method: 'GET' })

    if (result && typeof result === 'object' && 'code' in result) {
      if (result.code === 200 && result.data && Array.isArray(result.data)) {
        // 直接使用返回的供水机设备数据
        // 在 loadAvailableSupplyDevices 方法中添加数据转换
        availableSupplyDevices.value = result.data.map((device: any) => ({
          deviceId: device.deviceId,
          deviceName: device.deviceName,
          installLocation: device.installLocation,
          deviceType: device.deviceType,
          status: device.status,
          areaId: device.areaId ? device.areaId.split('-')[0] : '' // 提取简单的片区标识符
        }))

        console.log(`获取到${availableSupplyDevices.value.length}个可用供水机`)
      } else {
        console.warn('API响应非成功状态或数据格式错误:', result)
        availableSupplyDevices.value = []
      }
    } else if (Array.isArray(result)) {
      // 直接返回数组的情况
      availableSupplyDevices.value = (result as any[]).map((device: any) => ({
        deviceId: device.deviceId,
        deviceName: device.deviceName,
        installLocation: device.installLocation,
        deviceType: device.deviceType,
        status: device.status,
        areaId: device.areaId // 设备所属片区ID
      }))
    } else {
      console.warn('API响应数据格式错误:', result)
      availableSupplyDevices.value = []
    }
  } catch (error) {
    console.error('加载可用供水机列表失败:', error)
    availableSupplyDevices.value = []
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  }
}

// 根据选择的校区过滤供水机
const filteredSupplyDevices = computed(() => {
  if (!selectedCampusId.value) {
    return availableSupplyDevices.value
  }

  // 获取选中校区的 area_name
  const selectedCampus = campusList.value.find(campus => campus.areaId === selectedCampusId.value)
  if (!selectedCampus) {
    return availableSupplyDevices.value
  }

  // 使用校区的 area_name 来匹配设备的 areaId
  return availableSupplyDevices.value.filter(device => {
    // 假设设备的 areaId 字段实际上存储的是区域的 area_name
    return device.areaId === selectedCampus.areaName
  })
})


// 市区选择变化时的处理
const onCityChange = async () => {
  // 清空校区选择和设备ID
  selectedCampusId.value = ''
  currentTerminal.value.deviceId = undefined
  campusList.value = []

  if (selectedCityId.value) {
    await loadCampusListByCity(selectedCityId.value)
  }
}

// 校区选择变化时的处理
const onCampusChange = () => {
  // 当选择校区时，清空当前选择的设备ID
  currentTerminal.value.deviceId = undefined

  // 设置areaId为选中校区的areaName（不是areaId）
  const selectedCampus = campusList.value.find(campus => campus.areaId === selectedCampusId.value)
  if (selectedCampus) {
    currentTerminal.value.areaId = selectedCampus.areaName // 使用areaName作为areaId
  } else {
    currentTerminal.value.areaId = undefined
  }
}


// 多条件过滤终端数据
const filteredTerminals = computed(() => {
  return terminals.value.filter(terminal => {
    const keywordMatch = searchKeyword.value.trim() === '' ||
        terminal.terminalId.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        terminal.terminalName.toLowerCase().includes(searchKeyword.value.toLowerCase())

    const statusMatch = selectedStatus.value === '' || terminal.terminalStatus === selectedStatus.value

    return keywordMatch && statusMatch
  })
})

// 分页计算
const paginatedTerminals = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredTerminals.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredTerminals.value.length / pageSize)
})

// 状态格式化
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
  // 如果是日期字符串，直接返回
  return dateString
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1 // 重置到第一页
}

// 查看详情
const viewTerminal = (id: string) => {
  router.push(`/home/equipment/terminal/${id}`)
}

// 编辑终端
const editTerminal = async (terminal: TerminalManageVO) => {
  // 复制终端数据
  currentTerminal.value = { ...terminal }
  isEditing.value = true
  showAddModal.value = true

  // 确保设备列表已加载
  if (availableSupplyDevices.value.length === 0) {
    await loadAvailableSupplyDevices()
  }

  // 确保市区列表已加载
  if (cityList.value.length === 0) {
    await loadCityList()
  }

  // 如果终端有areaId，尝试自动匹配对应的校区信息
  if (terminal.areaId) {
    // 加载所有城市
    if (cityList.value.length > 0) {
      for (const city of cityList.value) {
        // 加载该城市的校区列表
        await loadCampusListByCity(city.areaId)

        // 在校区列表中查找匹配的areaId（实际存储的是areaName）
        const matchedCampus = campusList.value.find(campus => campus.areaName === terminal.areaId)
        if (matchedCampus) {
          // 找到匹配的校区，设置城市和校区选择
          selectedCityId.value = city.areaId
          selectedCampusId.value = matchedCampus.areaId
          break
        }
      }
    }
  }
}

// 删除终端
const deleteTerminal = async (terminalId: string) => {
  if (!confirm(`确定要删除终端 ${terminalId} 吗？`)) {
    return
  }

  try {
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    const result = await request<any>(`/api/web/terminal/delete/${terminalId}`, {
      method: 'DELETE'
    })

    // 检查是否为ResultVO格式的响应
    if (result && typeof result === 'object' && 'code' in result) {
      // ResultVO格式
      if (result.code === 200) {
        // 从本地数据中移除
        terminals.value = terminals.value.filter(t => t.terminalId !== terminalId)
        alert('终端删除成功')
      } else {
        alert(`删除终端失败: ${result.message || '未知错误'}`)
      }
    } else {
      // 直接成功响应
      terminals.value = terminals.value.filter(t => t.terminalId !== terminalId)
      alert('终端删除成功')
    }
  } catch (error) {
    console.error('删除终端失败:', error)
    alert('删除终端失败')
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  }
}


// 保存终端（新增或更新）
const saveTerminal = async () => {
  try {
    // 显式检查token
    const token = authStore.token
    if (!token) {
      console.warn('未获取到 Token，跳转到登录页')
      await router.push('/login')
      return
    }

    // 验证是否选择了校区（新增终端时）
    if (!isEditing.value && !selectedCampusId.value) {
      alert('请选择校区')
      return
    }

    // 验证areaId是否已设置
    if (!currentTerminal.value.areaId) {
      alert('请先选择校区以确定所属片区')
      return
    }

    // 修改：关联设备不再是必选项
    // 原来的验证逻辑已移除
    // if (!currentTerminal.value.deviceId) {
    //   alert('请选择关联的供水机')
    //   return
    // }

    let result: ResultVO<TerminalManageVO> | TerminalManageVO
    if (isEditing.value) {
      // 更新终端
      result = await request<ResultVO<TerminalManageVO> | TerminalManageVO>('/api/web/terminal/update', {
        method: 'PUT',
        body: JSON.stringify(currentTerminal.value)
      })
    } else {
      // 添加终端
      // 验证是否选择了校区（新增终端时）
      if (!selectedCampusId.value) {
        alert('请选择校区')
        return
      }

      result = await request<ResultVO<TerminalManageVO> | TerminalManageVO>('/api/web/terminal/add', {
        method: 'POST',
        body: JSON.stringify(currentTerminal.value)
      })
    }

    // 检查是否为ResultVO格式的响应
    if (result && typeof result === 'object' && 'code' in result) {
      // 如果是ResultVO格式
      if (result.code === 200 && result.data && isTerminalManageVO(result.data)) {
        // 添加成功后重新加载终端列表
        await loadTerminals()

        // 重置表单并关闭模态框
        showAddModal.value = false
        isEditing.value = false
        currentTerminal.value = {
          terminalId: '',
          terminalName: '',
          longitude: 0,
          latitude: 0,
          terminalStatus: 'active',
          deviceId: undefined
        }
        selectedCityId.value = ''
        selectedCampusId.value = ''
        campusList.value = []

        alert(isEditing.value ? '终端更新成功' : '终端添加成功')
      } else if (result.code === 200) {
        // 如果code是200但没有data字段
        // 添加成功后重新加载终端列表
        await loadTerminals()

        // 重置表单并关闭模态框
        showAddModal.value = false
        isEditing.value = false
        currentTerminal.value = {
          terminalId: '',
          terminalName: '',
          longitude: 0,
          latitude: 0,
          terminalStatus: 'active',
          deviceId: undefined
        }
        selectedCityId.value = ''
        selectedCampusId.value = ''
        campusList.value = []

        alert(isEditing.value ? '终端更新成功' : '终端添加成功')
      } else {
        alert(`${isEditing.value ? '更新' : '添加'}终端失败: ${result.message}`)
      }
    }
    // 处理直接返回终端对象的情况
    else if (isTerminalManageVO(result)) {
      // 添加成功后重新加载终端列表
      await loadTerminals()

      // 重置表单并关闭模态框
      showAddModal.value = false
      isEditing.value = false
      currentTerminal.value = {
        terminalId: '',
        terminalName: '',
        longitude: 0,
        latitude: 0,
        terminalStatus: 'active',
        deviceId: undefined
      }
      selectedCityId.value = ''
      selectedCampusId.value = ''
      campusList.value = []

      alert(isEditing.value ? '终端更新成功' : '终端添加成功')
    }
    else {
      // 其他成功情况
      // 添加成功后重新加载终端列表
      await loadTerminals()

      // 重置表单并关闭模态框
      showAddModal.value = false
      isEditing.value = false
      currentTerminal.value = {
        terminalId: '',
        terminalName: '',
        longitude: 0,
        latitude: 0,
        terminalStatus: 'active',
        deviceId: undefined
      }
      selectedCityId.value = ''
      selectedCampusId.value = ''
      campusList.value = []

      alert(isEditing.value ? '终端更新成功' : '终端添加成功')
    }
  } catch (error) {
    console.error(`${isEditing.value ? '更新' : '添加'}终端失败:`, error)
    alert(`${isEditing.value ? '更新' : '添加'}终端失败`)
    if ((error as Error).message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  console.log('🚀 开始加载终端数据...')
  await loadTerminals()
  await loadAvailableSupplyDevices()
  await loadCityList()
})
</script>

<style scoped>
/* 样式与制水机页面保持一致 */
.terminal-page {
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
