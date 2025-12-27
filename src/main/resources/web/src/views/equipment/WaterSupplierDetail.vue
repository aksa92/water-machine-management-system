<template>
  <div class="water-supplier-detail-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>供水机详情</h2>
      <div class="breadcrumb">校园矿化水平台 / 设备监控 / 供水机 / 详情</div>
    </div>

    <!-- 返回按钮 -->
    <div class="back-button-container">
      <button class="btn-back" @click="goBack">← 返回供水机列表</button>
    </div>

    <!-- 设备基本信息卡片 -->
    <div class="card detail-card" v-if="deviceDetail">
      <h3>设备基本信息</h3>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="label">设备ID:</span>
          <span class="value">{{ deviceDetail.deviceInfo?.deviceId }}</span>
        </div>
        <div class="detail-item">
          <span class="label">设备名称:</span>
          <span class="value">{{ deviceDetail.deviceInfo?.deviceName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">设备类型:</span>
          <span class="value">{{ formatDeviceType(deviceDetail.deviceInfo?.deviceType) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">所属片区:</span>
          <span class="value">{{ deviceDetail.deviceInfo?.areaId }}</span>
        </div>
        <div class="detail-item">
          <span class="label">安装位置:</span>
          <span class="value">{{ deviceDetail.deviceInfo?.installLocation }}</span>
        </div>
        <div class="detail-item">
          <span class="label">设备状态:</span>
          <span class="value">
            <span :class="`status-tag ${deviceDetail.deviceInfo?.status}`">
              {{ formatStatus(deviceDetail.deviceInfo?.status) }}
            </span>
          </span>
        </div>
        <div class="detail-item">
          <span class="label">创建时间:</span>
          <span class="value">{{ formatDate(deviceDetail.deviceInfo?.createTime) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">最后心跳时间:</span>
          <span class="value">{{ formatDate(deviceDetail.deviceInfo?.lastHeartbeatTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 关联的制水机信息卡片 -->
    <div class="card detail-card" v-if="associatedMaker">
      <h3>关联的制水机</h3>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="label">制水机ID:</span>
          <span class="value">{{ associatedMaker.deviceId }}</span>
        </div>
        <div class="detail-item">
          <span class="label">制水机名称:</span>
          <span class="value">{{ associatedMaker.deviceName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">制水机状态:</span>
          <span class="value">
            <span :class="`status-tag ${associatedMaker.status}`">
              {{ formatStatus(associatedMaker.status) }}
            </span>
          </span>
        </div>
        <div class="detail-item">
          <span class="label">安装位置:</span>
          <span class="value">{{ associatedMaker.installLocation }}</span>
        </div>
        <div class="detail-item">
          <span class="label">所属片区:</span>
          <span class="value">{{ associatedMaker.areaId }}</span>
        </div>
      </div>
    </div>

    <!-- 实时数据卡片 -->
    <div class="card detail-card" v-if="deviceDetail && deviceDetail.realtimeData">
      <h3>实时数据</h3>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="label">水压:</span>
          <span class="value">{{ deviceDetail.realtimeData?.waterPress || '-' }} MPa</span>
        </div>
        <div class="detail-item">
          <span class="label">流量:</span>
          <span class="value">{{ deviceDetail.realtimeData?.waterFlow || '-' }} m³/h</span>
        </div>
        <div class="detail-item">
          <span class="label">温度:</span>
          <span class="value">{{ deviceDetail.realtimeData?.temperature || '-' }} °C</span>
        </div>

        <div class="detail-item">
          <span class="label">运行状态:</span>
          <span class="value">
            <span :class="`status-tag ${deviceDetail.realtimeData?.status || 'normal'}`">
              {{ formatRunningStatus(deviceDetail.realtimeData?.status) }}
            </span>
          </span>
        </div>
        <div class="detail-item">
          <span class="label">数据记录时间:</span>
          <span class="value">{{ formatDate(deviceDetail.realtimeData?.timestamp) }}</span>
        </div>
      </div>
    </div>

    <!-- 加载中提示 -->
    <div v-if="loading" class="loading">
      正在加载设备详情...
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { request } from '@/api/request'
import type { ResultVO } from '@/api/types/auth'

// 类型定义 - 根据后端实体类调整
interface DeviceInfo {
  deviceId: string
  deviceName: string
  deviceType: string
  areaId: string
  installLocation: string
  status: string
  createTime?: string
  lastHeartbeatTime?: string
  parentMakerId?: string // 关联的制水机ID
}

// 根据 WaterSupplyRealtimeData 实体类定义
interface WaterSupplyRealtimeData {
  waterFlow?: number // 流量
  waterPress?: number // 水压
  waterLevel?: number // 水位
  temperature?: number // 温度
  status?: string     // 设备状态
  timestamp?: string // 记录时间
  createdTime?: string // 创建时间
}

interface DeviceDetail {
  deviceInfo: DeviceInfo
  realtimeData?: WaterSupplyRealtimeData
}

// 响应式数据
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const deviceDetail = ref<DeviceDetail | null>(null)
const associatedMaker = ref<DeviceInfo | null>(null) // 关联的制水机信息
const loading = ref(true)
const error = ref('')

// 获取设备ID
const deviceId = route.params.id as string

// 格式化设备类型
const formatDeviceType = (type: string | undefined): string => {
  if (!type) return '-'
  const typeMap: Record<string, string> = {
    'water_maker': '制水机',
    'water_supply': '供水机'
  }
  return typeMap[type] || type
}

// 格式化设备状态
const formatStatus = (status: string | undefined): string => {
  if (!status) return '-'
  const statusMap: Record<string, string> = {
    'online': '在线',
    'offline': '离线',
    'fault': '故障',
    'warning': '警告'
  }
  return statusMap[status] || status
}

// 格式化运行状态
const formatRunningStatus = (status: string | undefined): string => {
  if (!status) return '正常'
  const statusMap: Record<string, string> = {
    'normal': '正常',
    'warning': '警告',
    'error': '故障'
  }
  return statusMap[status] || status
}

// 格式化日期
const formatDate = (dateString: string | undefined): string => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN')
  } catch {
    return '-'
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 加载设备详情
const loadDeviceDetail = async () => {
  try {
    loading.value = true
    error.value = ''

    // 检查token
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    // 请求设备详情接口
    const result = await request<ResultVO<DeviceDetail>>(
        `/api/web/device/${deviceId}`,
        { method: 'GET' }
    )

    if (result.code === 200 && result.data) {
      deviceDetail.value = result.data
      console.log('设备详情数据:', deviceDetail.value)

      // 如果供水机有关联的制水机，加载制水机信息
      if (result.data.deviceInfo.parentMakerId) {
        await loadAssociatedMaker(result.data.deviceInfo.parentMakerId)
      }
    } else {
      error.value = result.message || '获取设备详情失败'
    }
  } catch (err) {
    console.error('加载设备详情失败:', err)
    error.value = '加载设备详情失败'
    if ((err as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  } finally {
    loading.value = false
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
      router.push('/login')
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
      router.push('/login')
    }
  }
}


// 加载关联的制水机信息
const loadAssociatedMaker = async (makerId: string) => {
  try {
    const token = authStore.token
    if (!token) {
      router.push('/login')
      return
    }

    const result = await request<ResultVO<DeviceDetail>>(
      `/api/web/device/${makerId}`,
      { method: 'GET' }
    )

    if (result.code === 200 && result.data) {
      associatedMaker.value = result.data.deviceInfo
      console.log('关联的制水机数据:', associatedMaker.value)
    } else {
      console.error('获取关联制水机失败:', result.message)
    }
  } catch (err) {
    console.error('加载关联制水机失败:', err)
    if ((err as Error).message.includes('401')) {
      authStore.logout()
      router.push('/login')
    }
  }
}

// 组件挂载时加载数据
onMounted(() => {
  if (deviceId) {
    loadDeviceDetail()
  } else {
    error.value = '无效的设备ID'
    loading.value = false
  }
})
</script>

<style scoped>
.water-supplier-detail-page {
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

.back-button-container {
  margin-bottom: 20px;
}

.btn-back {
  background: #f0f0f0;
  color: #333;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.btn-back:hover {
  background: #e0e0e0;
}

.detail-card {
  margin-bottom: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
}

.detail-card h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  min-height: 60px;
  padding: 8px 0;
}

.label {
  font-weight: 500;
  color: #666;
  margin-bottom: 4px;
  font-size: 14px;
}

.value {
  font-size: 16px;
  color: #333;
  min-height: 24px;
}

.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  min-width: 60px;
  text-align: center;
}

.status-tag.online,
.status-tag.normal {
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

.status-tag.fault,
.status-tag.error {
  background-color: #ffebe6;
  color: #cf1322;
}

.loading, .error-message {
  text-align: center;
  padding: 40px 0;
  font-size: 16px;
}

.error-message {
  color: #ff4d4f;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .water-supplier-detail-page {
    padding: 16px;
  }
}
</style>
