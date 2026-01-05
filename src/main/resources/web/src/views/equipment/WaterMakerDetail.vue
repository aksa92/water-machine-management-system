<template>
  <div class="water-maker-detail-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>制水机详情</h2>
      <div class="breadcrumb">校园矿化水平台 / 设备监控 / 制水机 / 详情</div>
    </div>

    <!-- 返回按钮 -->
    <div class="back-button-container">
      <button class="btn-back" @click="goBack">← 返回制水机列表</button>
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

      </div>
    </div>

    <!-- 实时数据卡片 -->
    <div class="card detail-card" v-if="deviceDetail && deviceDetail.realtimeData">
      <h3>实时数据</h3>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="label">原水TDS:</span>
          <span class="value">{{ deviceDetail.realtimeData?.tdsValue1 || '-' }} ppm</span>
        </div>
        <div class="detail-item">
          <span class="label">纯水TDS:</span>
          <span class="value">{{ deviceDetail.realtimeData?.tdsValue2 || '-' }} ppm</span>
        </div>
        <div class="detail-item">
          <span class="label">矿化水TDS:</span>
          <span class="value">{{ deviceDetail.realtimeData?.tdsValue3 || '-' }} ppm</span>
        </div>
        <div class="detail-item">
          <span class="label">流量1:</span>
          <span class="value">{{ deviceDetail.realtimeData?.waterFlow1 || '-' }} m³/h</span>
        </div>
        <div class="detail-item">
          <span class="label">流量2:</span>
          <span class="value">{{ deviceDetail.realtimeData?.waterFlow2 || '-' }} m³/h</span>
        </div>
        <div class="detail-item">
          <span class="label">水压:</span>
          <span class="value">{{ deviceDetail.realtimeData?.waterPress || '-' }} MPa</span>
        </div>
        <div class="detail-item">
          <span class="label">滤芯寿命:</span>
          <span class="value">{{ deviceDetail.realtimeData?.filterLife || '-' }}%</span>
        </div>
        <div class="detail-item">
          <span class="label">漏水检测:</span>
          <span class="value">
            <span :class="`status-tag ${deviceDetail.realtimeData?.leakage ? 'warning' : 'normal'}`">
              {{ deviceDetail.realtimeData?.leakage ? '检测到漏水' : '正常' }}
            </span>
          </span>
        </div>
        <div class="detail-item">
          <span class="label">水质:</span>
          <span class="value">{{ deviceDetail.realtimeData?.waterQuality || '-' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">运行状态:</span>
          <span class="value">
            <span :class="`status-tag ${deviceDetail.realtimeData?.status || 'normal'}`">
              {{ formatRealtimeStatus(deviceDetail.realtimeData?.status) }}
            </span>
          </span>
        </div>
        <div class="detail-item">
          <span class="label">数据记录时间:</span>
          <span class="value">{{ formatDate(deviceDetail.realtimeData?.recordTime) }}</span>
        </div>
      </div>

      <!-- TDS值柱状图 -->
      <div class="chart-container">
        <h4>TDS值对比</h4>
        <div id="tds-chart" class="chart"></div>
      </div>

      <!-- 滤芯寿命百分比条码 -->
      <div class="chart-container">
        <h4>滤芯寿命</h4>
        <div class="progress-bar-container">
          <div class="progress-bar">
            <div
              class="progress-bar-fill"
              :style="{ width: `${deviceDetail.realtimeData?.filterLife || 0}%` }"
            ></div>
          </div>
          <div class="progress-text">{{ deviceDetail.realtimeData?.filterLife || 0 }}%</div>
        </div>
      </div>
    </div>

    <!-- 关联的供水机列表卡片 -->
    <div class="card detail-card">
      <h3>
        关联的供水机
        <span v-if="loadingSuppliers" class="loading-indicator">加载中...</span>
      </h3>
      <div v-if="supplierDevices.length > 0" class="suppliers-list">
        <div
          v-for="(supplier, index) in supplierDevices"
          :key="index"
          class="supplier-item"
        >
          <div class="supplier-header">
            <h4>{{ supplier.deviceName || supplier.deviceId }}</h4>
            <span :class="`status-tag ${supplier.status}`">
              {{ formatStatus(supplier.status) }}
            </span>
          </div>
          <div class="supplier-details">
            <div class="detail-item">
              <span class="label">设备ID:</span>
              <span class="value">{{ supplier.deviceId }}</span>
            </div>
            <div class="detail-item">
              <span class="label">设备类型:</span>
              <span class="value">{{ formatDeviceType(supplier.deviceType) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">安装位置:</span>
              <span class="value">{{ supplier.installLocation || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">所属片区:</span>
              <span class="value">{{ supplier.areaId || '-' }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else-if="!loadingSuppliers" class="no-data">
        该制水机暂未关联任何供水机
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
import { ref, onMounted, watch, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { request } from '@/api/request'
import type { ResultVO } from '@/api/types/auth'
import * as echarts from 'echarts'

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
}

// 根据 WaterMakerRealtimeData 实体类定义
interface WaterMakerRealtimeData {
  tdsValue1?: number  // 原水TDS
  tdsValue2?: number  // 纯水TDS
  tdsValue3?: number  // 矿化水TDS
  waterFlow1?: number // 流量1
  waterFlow2?: number // 流量2
  waterPress?: number // 水压
  filterLife?: number // 滤芯寿命
  leakage?: boolean   // 漏水检测
  waterQuality?: string // 水质
  status?: string     // 设备状态
  recordTime?: string // 记录时间
  createdTime?: string // 创建时间
}

// 供水机设备信息
interface SupplierDevice {
  deviceId: string
  deviceName: string
  deviceType: string
  areaId: string
  installLocation: string
  status: string
  createTime?: string
  lastHeartbeatTime?: string
}

interface DeviceDetail {
  deviceInfo: DeviceInfo
  realtimeData?: WaterMakerRealtimeData
}

// 响应式数据
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const deviceDetail = ref<DeviceDetail | null>(null)
const supplierDevices = ref<SupplierDevice[]>([]) // 新增：关联的供水机列表
const loading = ref(true)
const loadingSuppliers = ref(false) // 新增：供水机加载状态
const error = ref('')

// 图表实例
const tdsChart = ref<echarts.ECharts | null>(null)

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

// 格式化实时数据状态
const formatRealtimeStatus = (status: string | undefined): string => {
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

// 获取关联的供水机信息
const loadSupplierDevices = async (makerId: string) => {
  try {
    loadingSuppliers.value = true

    // 检查token
    const token = authStore.token
    if (!token) {
      await router.push('/login')
      return
    }

    // 调用现有的接口获取供水机列表
    const result = await request<ResultVO<SupplierDevice[]>>(
      `/api/web/device/maker/${makerId}/suppliers`,
      { method: 'GET' }
    )

    if (result.code === 200 && result.data && Array.isArray(result.data)) {
      supplierDevices.value = result.data
      console.log('关联的供水机数据:', result.data)
    } else {
      console.warn('获取关联供水机失败:', result.message)
      supplierDevices.value = []
    }
  } catch (err) {
    console.error('加载关联供水机失败:', err)
    supplierDevices.value = []
    if ((err as Error).message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  } finally {
    loadingSuppliers.value = false
  }
}

// 初始化TDS柱状图
const initTDSChart = () => {
  const chartDom = document.getElementById('tds-chart')
  if (!chartDom) return

  tdsChart.value = echarts.init(chartDom)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['原水TDS', '纯水TDS', '矿化水TDS']
    },
    yAxis: {
      type: 'value',
      name: 'ppm'
    },
    series: [{
      data: [
        deviceDetail.value?.realtimeData?.tdsValue1 || 0,
        deviceDetail.value?.realtimeData?.tdsValue2 || 0,
        deviceDetail.value?.realtimeData?.tdsValue3 || 0
      ],
      type: 'bar',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2378f7' },
            { offset: 0.7, color: '#2378f7' },
            { offset: 1, color: '#83bff6' }
          ])
        }
      },
      // 添加标签配置
      label: {
        show: true,
        position: 'top',
        formatter: '{c} ppm', // 显示数值和单位
        color: '#333',
        fontSize: 12
      }
    }]
  }

  tdsChart.value.setOption(option)
}


// 加载设备详情
const loadDeviceDetail = async () => {
  try {
    loading.value = true
    error.value = ''

    // 检查token
    const token = authStore.token
    if (!token) {
      await router.push('/login')
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

      // 如果是制水机，加载关联的供水机信息
      if (result.data.deviceInfo?.deviceType === 'water_maker') {
        await loadSupplierDevices(deviceId)
      }

      // 初始化图表
      await nextTick(() => {
        initTDSChart()
      })
    } else {
      error.value = result.message || '获取设备详情失败'
    }
  } catch (err) {
    console.error('加载设备详情失败:', err)
    error.value = '加载设备详情失败'
    if ((err as Error).message.includes('401')) {
      authStore.logout()
      await router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 监听数据变化并更新图表
watch(() => deviceDetail.value?.realtimeData, (newData) => {
  if (newData && tdsChart.value) {
    tdsChart.value.setOption({
      series: [{
        data: [
          newData.tdsValue1 || 0,
          newData.tdsValue2 || 0,
          newData.tdsValue3 || 0
        ]
      }]
    })
  }
}, { deep: true })

// 组件挂载时加载数据
onMounted(() => {
  if (deviceId) {
    loadDeviceDetail()
  } else {
    error.value = '无效的设备ID'
    loading.value = false
  }
})

// 页面销毁时销毁图表
onUnmounted(() => {
  if (tdsChart.value) {
    tdsChart.value.dispose()
  }
})
</script>


<style scoped>
.water-maker-detail-page {
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

  .water-maker-detail-page {
    padding: 16px;
  }
}

/* 新增样式 */
.suppliers-list {
  margin-top: 16px;
}

.supplier-item {
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 16px;
  padding: 16px;
  background-color: #fafafa;
}

.supplier-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.supplier-header h4 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.supplier-details {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.no-data {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 20px 0;
}

.loading-indicator {
  font-size: 14px;
  color: #666;
  font-weight: normal;
  margin-left: 8px;
}

/* 图表相关样式 */
.chart-container {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.chart-container h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.chart {
  height: 300px;
  width: 100%;
}

.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 16px;
}

.progress-bar {
  flex: 1;
  height: 24px;
  background-color: #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #42b983, #359e75);
  transition: width 0.3s ease;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.progress-text {
  min-width: 40px;
  text-align: center;
  font-weight: bold;
  color: #333;
}
</style>
