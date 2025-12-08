<!-- src/views/equipment/WaterMakerDetail.vue -->
<template>
  <div class="water-machine-detail">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <h2>制水机设备详情</h2>
      <div class="breadcrumb">校园矿化水平台 / 设备管理 / 制水机 / 设备详情</div>
    </div>

    <!-- 设备基本信息卡片 -->
    <div class="card device-info-card">
      <div class="info-header">
        <h3 class="card-title">设备基本信息</h3>
        <button class="btn-refresh" @click="refreshData">
          <i class="refresh-icon">↻</i> 刷新数据
        </button>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <span class="info-label">设备ID：</span>
          <span class="info-value">{{ machineInfo.deviceId }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">设备型号：</span>
          <span class="info-value">{{ machineInfo.model }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">所属片区：</span>
          <span class="info-value">{{ machineInfo.area }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">安装位置：</span>
          <span class="info-value">{{ machineInfo.location }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">安装日期：</span>
          <span class="info-value">{{ formatDate(machineInfo.installDate) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">运行状态：</span>
          <span class="info-value status-tag" :class="machineInfo.status">{{ formatStatus(machineInfo.status) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">最后在线时间：</span>
          <span class="info-value">{{ formatDate(machineInfo.lastOnlineTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 设备状态和数据 -->
    <div class="left-column">
      <!-- 实时监测数据 -->
      <div class="card">
        <h3 class="card-title">实时监测数据</h3>
        <div class="realtime-data">
          <!-- 自来水TDS -->
          <div class="data-item">
            <div class="data-label">自来水TDS</div>
            <div class="data-value">{{ realtimeData.tapWaterTds }} ppm</div>
            <div class="data-status" :class="getTdsStatus(realtimeData.tapWaterTds, 'tap')"></div>
          </div>
          <!-- 纯净水TDS -->
          <div class="data-item">
            <div class="data-label">纯净水TDS</div>
            <div class="data-value">{{ realtimeData.pureWaterTds }} ppm</div>
            <div class="data-status" :class="getTdsStatus(realtimeData.pureWaterTds, 'pure')"></div>
          </div>
          <div class="data-item">
            <div class="data-label">水温</div>
            <div class="data-value">{{ realtimeData.temperature }} °C</div>
          </div>
          <div class="data-item">
            <div class="data-label">出水压力</div>
            <div class="data-value">{{ realtimeData.pressure }} MPa</div>
          </div>
          <div class="data-item">
            <div class="data-label">流量计1</div>
            <div class="data-value">{{ realtimeData.flow1 }} L/min</div>
          </div>
          <div class="data-item">
            <div class="data-label">流量计2</div>
            <div class="data-value">{{ realtimeData.flow2 }} L/min</div>
          </div>
        </div>
      </div>

      <!-- 滤芯状态 -->
      <div class="card">
        <h3 class="card-title">滤芯状态</h3>
        <div class="filter-status">
          <div class="filter-item" v-for="filter in filterStatus" :key="filter.id">
            <div class="filter-name">{{ filter.name }}</div>
            <div class="filter-progress">
              <div class="progress-bar" :style="{ width: filter.usage + '%' }" :class="getFilterStatusClass(filter.usage)"></div>
            </div>
            <div class="filter-info">
              <span class="usage">{{ filter.usage }}%</span>
              <span class="remaining">剩余{{ filter.remainingDays }}天</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史数据记录 -->
    <div class="card">
      <div class="table-header">
        <h3 class="card-title">历史数据记录</h3>
        <div class="table-filter">
          <label>日期筛选：</label>
          <input type="date" v-model="historyDate" @change="fetchHistoryData">
        </div>
      </div>

      <table class="history-table">
        <thead>
          <tr>
            <th>日期</th>
            <th>自来水TDS平均值 (ppm)</th>
            <th>纯净水TDS平均值 (ppm)</th>
            <th>矿化水TDS平均值 (ppm)</th>
          </tr>
        </thead>
        <tbody>
            <tr v-if="historyData.length > 0">
              <td>{{ historyData[0]?.date }}</td>
              <td>{{ historyData[0]?.tapWaterTdsAvg }}</td>
              <td>{{ historyData[0]?.pureWaterTdsAvg }}</td>
              <td>{{ historyData[0]?.mineralWaterTdsAvg }}</td>
            </tr>
          <tr v-if="historyData.length === 0">
            <td colspan="4" class="no-data">暂无历史数据记录</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 维护记录 -->
    <div class="card">
      <h3 class="card-title">维护记录</h3>
      <table class="maintenance-table">
        <thead>
          <tr>
            <th>工单编号</th>
            <th>维护类型</th>
            <th>维护人员</th>
            <th>维护时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in maintenanceRecords" :key="record.orderNo">
            <td>{{ record.orderNo }}</td>
            <td>{{ record.maintenanceType }}</td>
            <td>{{ record.maintainer }}</td>
            <td>{{ formatDate(record.maintenanceTime) }}</td>
            <td><span class="status-tag small" :class="record.status">{{ formatStatus(record.status) }}</span></td>
            <td>
              <button class="btn-view" @click="viewMaintenanceDetail(record.orderNo)">查看详情</button>
            </td>
          </tr>
          <tr v-if="maintenanceRecords.length === 0">
            <td colspan="6" class="no-data">暂无维护记录</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { WaterMakerApi } from '@/api/waterMaker'

// 设备状态类型
type DeviceStatus = 'online' | 'offline' | 'warning' | 'error'

// 制水机基本信息接口
interface MachineInfo {
  deviceId: string
  model: string
  area: string
  location: string
  installDate: string
  status: DeviceStatus
  lastOnlineTime: string
}

// 实时数据接口
interface RealtimeData {
  tapWaterTds: number       // 自来水TDS
  pureWaterTds: number      // 纯净水TDS
  temperature: number
  pressure: number
  flow1: number
  flow2: number
  updateTime: string
}

// 滤芯状态接口
interface FilterStatus {
  id: string
  name: string
  usage: number
  remainingDays: number
}

// 历史数据记录接口
interface HistoryRecord {
  date: string                          // 日期
  tapWaterTdsAvg: number                // 自来水TDS平均值
  pureWaterTdsAvg: number               // 纯净水TDS平均值
  mineralWaterTdsAvg: number            // 矿化水TDS平均值
}

// 维护记录接口
interface MaintenanceRecord {
  orderNo: string
  maintenanceType: string
  maintainer: string
  maintenanceTime: string
  status: 'completed' | 'processing' | 'pending'
}

// 路由和响应式数据
const route = useRoute()
const router = useRouter()
const historyDate = ref('')

// 严格类型处理：确保deviceId是string类型
const deviceId: string = (() => {
  const paramId = route.params.id as string | undefined
  return paramId ?? 'WM-2023-001'
})()

// 设备信息数据
const machineInfo = ref<MachineInfo>({
  deviceId: deviceId,
  model: 'WM-RO-500G',
  area: '校区',
  location: '第一教学楼一楼大厅',
  installDate: '2023-06-15',
  status: 'online',
  lastOnlineTime: new Date().toISOString()
})

// 实时数据
const realtimeData = ref<RealtimeData>({
  tapWaterTds: 186,         // 自来水TDS
  pureWaterTds: 12,         // 纯净水TDS
  temperature: 25.6,
  pressure: 0.45,
  flow1: 1.2,
  flow2: 8.1,
  updateTime: new Date().toISOString()
})

// 滤芯状态
const filterStatus = ref<FilterStatus[]>([
  { id: 'f1', name: '滤芯1', usage: 65, remainingDays: 45 },
  { id: 'f2', name: '滤芯2', usage: 42, remainingDays: 90 },
  { id: 'f3', name: '滤芯3', usage: 30, remainingDays: 120 }
])

// 历史数据
const historyData = ref<HistoryRecord[]>([])

// 维护记录
const maintenanceRecords = ref<MaintenanceRecord[]>([
  {
    orderNo: 'ORD-20231015-002',
    maintenanceType: '滤芯更换',
    maintainer: '张三',
    maintenanceTime: '2023-10-15 14:30:00',
    status: 'completed'
  },
  {
    orderNo: 'ORD-20230920-015',
    maintenanceType: '常规检修',
    maintainer: '李四',
    maintenanceTime: '2023-09-20 09:15:00',
    status: 'completed'
  }
])

// 初始化页面数据
onMounted(async () => {
  // 设置默认日期为今天
  const today = new Date().toISOString().substring(0, 10)
  historyDate.value = today

  // 并行加载所有数据
  await Promise.all([
    loadDeviceBasicInfo(),
    loadRealtimeData(),
    loadFilterStatus(),
    fetchHistoryData(),
    loadMaintenanceRecords()
  ])
})

// 加载设备基本信息
const loadDeviceBasicInfo = async () => {
  try {
    const result = await WaterMakerApi.getDeviceById(deviceId)
    if (result.code === 200) {
      const data = result.data
      machineInfo.value = {
        deviceId: data.deviceId,
        model: data.model || '未知型号',
        area: data.areaId,
        location: data.installLocation,
        installDate: data.installDate,
        status: data.status,
        lastOnlineTime: data.lastHeartbeatTime
      }
    }
  } catch (error) {
    console.error('加载设备基本信息失败:', error)
  }
}

// 加载实时数据
const loadRealtimeData = async () => {
  try {
    const result = await WaterMakerApi.getRealtimeData(deviceId)
    if (result.code === 200) {
      const data = result.data
      realtimeData.value = {
        tapWaterTds: data.rawWaterTds || 0,
        pureWaterTds: data.pureWaterTds || 0,
        temperature: data.temperature || 0,
        pressure: data.waterPressure || 0,
        flow1: data.flowRate1 || 0,
        flow2: data.flowRate2 || 0,
        updateTime: new Date().toISOString()
      }
    }
  } catch (error) {
    console.error('加载实时数据失败:', error)
  }
}

// 加载滤芯状态
const loadFilterStatus = async () => {
  try {
    const result = await WaterMakerApi.getFilterStatus(deviceId)
    if (result.code === 200) {
      filterStatus.value = result.data.map((item: any) => ({
        id: item.filterId,
        name: item.filterName,
        usage: item.usagePercentage,
        remainingDays: item.remainingDays
      }))
    }
  } catch (error) {
    console.error('加载滤芯状态失败:', error)
  }
}

// 获取历史数据
const fetchHistoryData = async () => {
  try {
    const result = await WaterMakerApi.getHistoryData(deviceId, historyDate.value)
    if (result.code === 200) {
      historyData.value = result.data.map((item: any) => ({
        date: item.statDate,
        tapWaterTdsAvg: item.rawWaterTdsAvg,
        pureWaterTdsAvg: item.pureWaterTdsAvg,
        mineralWaterTdsAvg: item.mineralWaterTdsAvg
      }))
    }
  } catch (error) {
    console.error('获取历史数据失败:', error)
  }
}

// 加载维护记录
const loadMaintenanceRecords = async () => {
  try {
    const result = await WaterMakerApi.getMaintenanceRecords(deviceId)
    if (result.code === 200) {
      maintenanceRecords.value = result.data.map((item: any) => ({
        orderNo: item.orderNo,
        maintenanceType: item.type,
        maintainer: item.maintainer,
        maintenanceTime: item.maintenanceTime,
        status: item.status
      }))
    }
  } catch (error) {
    console.error('加载维护记录失败:', error)
  }
}

// 格式化状态显示
const formatStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    online: '在线',
    offline: '离线',
    warning: '警告',
    error: '故障',
    completed: '已完成',
    processing: '处理中',
    pending: '待处理'
  }
  return statusMap[status] || status
}

// 格式化日期
const formatDate = (dateString?: string): string => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return isNaN(date.getTime()) ? '-' : date.toLocaleString('zh-CN')
  } catch {
    return '-'
  }
}

// 获取TDS状态
const getTdsStatus = (tds: number, type: 'tap' | 'pure'): string => {
  if (type === 'pure') {
    // 纯净水TDS判断
    if (tds < 10) return 'excellent'
    if (tds < 20) return 'good'
    return 'warning'
  } else {
    // 自来水TDS判断
    if (tds < 200) return 'good'
    if (tds < 300) return 'warning'
    return 'error'
  }
}

// 获取滤芯状态类名
const getFilterStatusClass = (usage: number) => {
  if (usage < 70) return 'normal'
  if (usage < 90) return 'warning'
  return 'error'
}

// 刷新数据
const refreshData = async () => {
  try {
    const result = await WaterMakerApi.refreshDeviceData(deviceId)
    if (result.code === 200) {
      // 刷新所有数据
      await Promise.all([
        loadRealtimeData(),
        loadFilterStatus()
      ])
    }
  } catch (error) {
    console.error('刷新数据失败:', error)
  }
}

// 查看维护详情
const viewMaintenanceDetail = (orderNo: string) => {
  router.push(`/home/order/detail/${orderNo}`)
}
</script>

<style scoped>
.water-machine-detail {
  padding: 20px;
}

/* 页面头部样式 */
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

/* 信息卡片样式 */
.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.btn-refresh {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-refresh:hover {
  background-color: #e9ecef;
}

.refresh-icon {
  font-size: 16px;
}

/* 信息网格样式 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.info-value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

/* 状态标签样式 */
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

.status-tag.completed {
  background-color: #e6f7ee;
  color: #00875a;
}

.status-tag.processing {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-tag.pending {
  background-color: #fff7e6;
  color: #d48806;
}

.status-tag.small {
  padding: 2px 6px;
  font-size: 11px;
}

/* 左侧列样式 */
.left-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

/* 实时数据样式 */
.realtime-data {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.data-item {
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  text-align: center;
}

.data-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.data-value {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.data-status {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin: 0 auto;
}

.data-status.excellent {
  background-color: #52c41a;
}

.data-status.good {
  background-color: #1890ff;
}

.data-status.warning {
  background-color: #faad14;
}

.data-status.error {
  background-color: #ff4d4f;
}

/* 滤芯状态样式 */
.filter-status {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-name {
  font-size: 14px;
  color: #333;
}

.filter-progress {
  height: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  transition: width 0.3s;
}

.progress-bar.normal {
  background-color: #52c41a;
}

.progress-bar.warning {
  background-color: #faad14;
}

.progress-bar.error {
  background-color: #ff4d4f;
}

.filter-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

/* 表格样式 */
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-filter input {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.history-table, .maintenance-table {
  width: 100%;
  border-collapse: collapse;
}

.history-table th, .history-table td,
.maintenance-table th, .maintenance-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.history-table th, .maintenance-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #4e5969;
  font-size: 14px;
}

.history-table tbody tr:hover,
.maintenance-table tbody tr:hover {
  background-color: #f8f9fa;
}

.no-data {
  text-align: center;
  padding: 40px 0;
  color: #8c8c8c;
}

/* 按钮样式 */
.btn-view {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: opacity 0.3s;
  background-color: #e6f7ff;
  color: #1890ff;
}

.btn-view:hover {
  opacity: 0.9;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr 1fr;
  }

  .realtime-data {
    grid-template-columns: 1fr;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .history-table, .maintenance-table {
    font-size: 12px;
  }

  .history-table th, .history-table td,
  .maintenance-table th, .maintenance-table td {
    padding: 8px 6px;
  }
}
</style>
