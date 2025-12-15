<template>
  <div class="water-supplier-detail">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">供水机详情</div>
      <div class="header-right"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <div>加载中...</div>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <div class="error-icon">❌</div>
        <div class="error-message">{{ error }}</div>
        <button class="retry-btn" @click="fetchDeviceDetail">重试</button>
      </div>

      <!-- 正常状态 -->
      <div v-else class="detail-container">
        <!-- 设备信息 -->
        <div class="detail-section">
          <div class="section-title">设备信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <div class="item-label">设备ID</div>
              <div class="item-value">{{ deviceInfo.id }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">设备名称</div>
              <div class="item-value">{{ deviceInfo.name }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">安装位置</div>
              <div class="item-value">{{ deviceInfo.location }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">设备状态</div>
              <div class="item-value" :class="getStatusClass(deviceInfo.status)">
                {{ formatStatus(deviceInfo.status) }}
              </div>
            </div>
            <div class="detail-item">
              <div class="item-label">所属区域</div>
              <div class="item-value">{{ deviceInfo.areaId || '未分配' }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">关联制水机</div>
              <div class="item-value">{{ deviceInfo.parentMakerId || '未关联' }}</div>
            </div>
          </div>
        </div>

        <!-- 水位信息 -->
        <div class="detail-section" v-if="showWaterLevelInfo">
          <div class="section-title">水位信息</div>
          <div class="water-level-content">
            <div class="water-level-chart">
              <div class="level-box">
                <div class="level-percentage">{{ deviceInfo.waterLevel || 66 }}%</div>
                <div class="level-label">当前水位</div>
              </div>

              <div class="storage-box">
                <div class="storage-value">{{ deviceInfo.storageCapacity || 360 }}L</div>
                <div class="storage-label">储水量</div>
              </div>
            </div>

            <!-- 水位进度条 -->
            <div class="water-level-progress">
              <div class="progress-bar">
                <div
                  class="progress-fill"
                  :style="{ width: deviceInfo.waterLevel + '%' }"
                  :class="getWaterLevelClass(deviceInfo.waterLevel)"
                ></div>
              </div>
              <div class="progress-labels">
                <span class="label-low">低</span>
                <span class="label-normal">正常</span>
                <span class="label-high">高</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 浮球阀状态 -->
        <div class="detail-section" v-if="showFloatValveInfo">
          <div class="section-title">浮球阀状态</div>
          <div class="float-valve-content">
            <!-- 高水位浮球阀 -->
            <div class="float-valve-item">
              <div class="valve-info">
                <div class="valve-label">高水位浮球阀</div>
                <div class="valve-value" :class="getValveClass(deviceInfo.highValve)">
                  {{ formatValveStatus(deviceInfo.highValve) }}
                </div>
              </div>
              <div class="valve-description">水位达到90%时自动关闭进水</div>
            </div>

            <!-- 低水位浮球阀 -->
            <div class="float-valve-item">
              <div class="valve-info">
                <div class="valve-label">低水位浮球阀</div>
                <div class="valve-value" :class="getValveClass(deviceInfo.lowValve)">
                  {{ formatValveStatus(deviceInfo.lowValve) }}
                </div>
              </div>
              <div class="valve-description">水位低于10%时自动开启进水</div>
            </div>
          </div>
        </div>

        <!-- 漏水检测 -->
        <div class="detail-section" v-if="showLeakDetection">
          <div class="section-title">漏水检测</div>
          <div class="leak-detection-content">
            <div class="detection-info">
              <div class="sensor-info">
                <div class="sensor-label">浮球阀传感器</div>
                <div class="sensor-time" v-if="deviceInfo.lastCheckTime">
                  最后检测: {{ formatTime(deviceInfo.lastCheckTime) }}
                </div>
              </div>
              <div class="detection-result">
                <div class="result-info">
                  <div class="result-label">检测结果</div>
                  <div class="result-value" :class="getLeakClass(deviceInfo.leakStatus)">
                    {{ formatLeakStatus(deviceInfo.leakStatus) }}
                  </div>
                </div>
                <div class="result-description">{{ deviceInfo.leakDescription || '设备周边未检测到漏水情况' }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 备注信息 -->
        <div class="detail-section" v-if="deviceInfo.remark">
          <div class="section-title">备注信息</div>
          <div class="remark-content">
            {{ deviceInfo.remark }}
          </div>
        </div>
      </div>

      <!-- 开始巡检按钮 -->
      <button
        v-if="!loading && !error"
        class="start-inspection-btn"
        @click="startInspection"
        :disabled="deviceInfo.status === 'fault'"
      >
        {{ deviceInfo.status === 'fault' ? '设备故障，无法巡检' : '开始巡检' }}
      </button>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goToHome">
        <span>首页</span>
      </div>
      <div class="nav-item" @click="goToInspection">
        <span>巡检</span>
      </div>
      <div class="nav-item" @click="goToWorkOrders">
        <span>工单</span>
      </div>
      <div class="nav-item" @click="goToProfile">
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { deviceService } from '@/services/deviceService'

const router = useRouter()
const route = useRoute()

// 设备数据
const deviceInfo = ref({})
const loading = ref(true)
const error = ref(null)

// 获取设备ID
const deviceId = computed(() => route.params.id)

// 计算属性：判断是否显示各个模块
const showWaterLevelInfo = computed(() => {
  return deviceInfo.value.waterLevel !== undefined ||
    deviceInfo.value.storageCapacity !== undefined ||
    deviceInfo.value.deviceType === 'water_supply'
})

const showFloatValveInfo = computed(() => {
  return deviceInfo.value.highValve !== undefined ||
    deviceInfo.value.lowValve !== undefined ||
    deviceInfo.value.deviceType === 'water_supply'
})

const showLeakDetection = computed(() => {
  return deviceInfo.value.leakStatus !== undefined ||
    deviceInfo.value.deviceType === 'water_supply'
})

// 获取设备详情
const fetchDeviceDetail = async () => {
  try {
    loading.value = true
    error.value = null

    if (!deviceId.value) {
      error.value = '设备ID不存在'
      return
    }

    const response = await deviceService.getDeviceDetail(deviceId.value)

    if (response.code === 200) {
      const data = response.data

      // 转换后端数据到前端格式
      deviceInfo.value = {
        id: data.deviceId,
        name: data.deviceName || `供水机#${data.deviceId}`,
        location: data.installLocation || '未指定位置',
        status: data.status || 'offline',
        areaId: data.areaId,
        parentMakerId: data.parentMakerId,
        installDate: data.installDate,
        remark: data.remark,
        waterLevel: data.waterLevel,
        storageCapacity: data.storageCapacity,
        highValve: data.highValve,
        lowValve: data.lowValve,
        leakStatus: data.leakStatus,
        leakDescription: data.leakDescription,
        lastCheckTime: data.lastCheckTime,
        createTime: data.createTime,
        deviceType: data.deviceType
      }

      console.log('设备详情数据:', deviceInfo.value)
    } else {
      error.value = response.message || '获取设备详情失败'
    }
  } catch (err) {
    error.value = err.message || '获取设备详情失败'
    console.error('获取设备详情失败:', err)
  } finally {
    loading.value = false
  }
}

// 格式化状态显示
const formatStatus = (status) => {
  const statusMap = {
    'online': '在线',
    'offline': '离线',
    'fault': '故障',
    'maintenance': '维护中'
  }
  return statusMap[status] || status
}

// 状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 'online':
      return 'status-online'
    case 'offline':
      return 'status-offline'
    case 'fault':
      return 'status-fault'
    case 'maintenance':
      return 'status-maintenance'
    default:
      return 'status-unknown'
  }
}

// 水位级别样式
const getWaterLevelClass = (level) => {
  if (level >= 90) return 'level-high'
  if (level <= 20) return 'level-low'
  return 'level-normal'
}

// 格式化浮球阀状态
const formatValveStatus = (valveStatus) => {
  const valveMap = {
    'open': '开启',
    'closed': '关闭',
    'fault': '故障'
  }
  return valveMap[valveStatus] || valveStatus
}

// 获取阀门状态样式类
const getValveClass = (valveStatus) => {
  switch (valveStatus) {
    case 'open':
      return 'valve-open'
    case 'closed':
      return 'valve-closed'
    case 'fault':
      return 'valve-fault'
    default:
      return ''
  }
}

// 格式化漏水状态
const formatLeakStatus = (leakStatus) => {
  const leakMap = {
    'normal': '无漏水',
    'leaking': '漏水检测',
    'fault': '传感器故障'
  }
  return leakMap[leakStatus] || leakStatus
}

// 获取漏水状态样式类
const getLeakClass = (leakStatus) => {
  switch (leakStatus) {
    case 'normal':
      return 'leak-none'
    case 'leaking':
      return 'leak-detected'
    case 'fault':
      return 'leak-fault'
    default:
      return 'leak-unknown'
  }
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  try {
    const date = new Date(timeString)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return timeString
  }
}

// 导航函数
const goBack = () => {
  router.back()
}

const goToHome = () => {
  router.push('/home')
}

const goToInspection = () => {
  router.push('/inspection')
}

const goToWorkOrders = () => {
  router.push('/work-orders')
}

const goToProfile = () => {
  router.push('/profile')
}

// 开始巡检
const startInspection = () => {
  if (deviceInfo.value.status === 'fault') {
    alert('设备故障状态，无法进行巡检')
    return
  }

  console.log('开始巡检供水机', deviceInfo.value.id)
  router.push({
    path: '/inspection/form',
    query: {
      deviceId: deviceInfo.value.id,
      deviceType: 'water_supply',
      deviceName: deviceInfo.value.name
    }
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchDeviceDetail()
})
</script>


<style scoped>
.water-supplier-detail {
  width: 100%;
  height: 100%;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
}

/* 顶部标题栏 */
.header {
  background: white;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.header-left {
  width: 80px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
  flex: 1;
}

.header-right {
  width: 80px;
}

.back-btn {
  font-size: 14px;
  color: #1890ff;
  cursor: pointer;
  transition: color 0.3s;
}

.back-btn:hover {
  color: #096dd9;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  flex: 1;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  flex: 1;
  text-align: center;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #ff4d4f;
}

.error-message {
  color: #666;
  margin-bottom: 16px;
  max-width: 80%;
  word-break: break-word;
}

.retry-btn {
  padding: 8px 24px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.retry-btn:hover {
  background: #096dd9;
  transform: translateY(-1px);
}

.detail-container {
  flex: 1;
}

/* 详情区块 */
.detail-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

/* 设备信息网格 */
.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.detail-item:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}

.item-label {
  font-size: 14px;
  color: #666;
}

.item-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* 状态样式 */
.status-online {
  color: #52c41a;
}

.status-offline {
  color: #fa541c;
}

.status-fault {
  color: #ff4d4f;
}

.status-maintenance {
  color: #faad14;
}

.status-unknown {
  color: #999;
}

/* 水位信息 */
.water-level-content {
  padding: 8px 0;
}

.water-level-chart {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 20px;
}

.level-box {
  flex: 1;
  text-align: center;
  padding: 16px;
  background: #f0f7ff;
  border-radius: 8px;
  border: 1px solid #d6e4ff;
}

.level-percentage {
  font-size: 32px;
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 4px;
}

.level-label {
  font-size: 14px;
  color: #666;
}

.storage-box {
  flex: 1;
  text-align: center;
  padding: 16px;
  background: #f6ffed;
  border-radius: 8px;
  border: 1px solid #b7eb8f;
}

.storage-value {
  font-size: 28px;
  font-weight: 700;
  color: #52c41a;
  margin-bottom: 4px;
}

.storage-label {
  font-size: 14px;
  color: #666;
}

/* 水位进度条 */
.water-level-progress {
  margin-top: 20px;
}

.progress-bar {
  height: 20px;
  background: #f5f5f5;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  transition: width 0.5s ease;
}

.level-low {
  background: linear-gradient(90deg, #ff7a45, #ff9c6e);
}

.level-normal {
  background: linear-gradient(90deg, #73d13d, #95de64);
}

.level-high {
  background: linear-gradient(90deg, #ff4d4f, #ff7875);
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

/* 浮球阀状态 */
.float-valve-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.float-valve-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.valve-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.valve-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.valve-value {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 4px;
}

.valve-open {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.valve-closed {
  background: #fff2e8;
  color: #fa541c;
  border: 1px solid #ffbb96;
}

.valve-fault {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.valve-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

/* 漏水检测 */
.leak-detection-content {
  padding: 8px 0;
}

.detection-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sensor-info {
  padding: 8px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sensor-label {
  font-size: 14px;
  color: #666;
}

.sensor-time {
  font-size: 12px;
  color: #999;
}

.detection-result {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-label {
  font-size: 14px;
  color: #666;
}

.result-value {
  font-size: 14px;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 4px;
}

.leak-none {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.leak-detected {
  background: #fff2e8;
  color: #fa541c;
  border: 1px solid #ffbb96;
}

.leak-fault {
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.leak-unknown {
  background: #f5f5f5;
  color: #999;
  border: 1px solid #d9d9d9;
}

.result-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

/* 备注信息 */
.remark-content {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

/* 开始巡检按钮 */
.start-inspection-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 20px;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.start-inspection-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.3);
}

.start-inspection-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  background: linear-gradient(135deg, #cccccc 0%, #999999 100%);
}

/* 底部导航栏 */
.bottom-nav {
  display: flex;
  background: white;
  border-top: 1px solid #e8e8e8;
  padding: 8px 0;
}

.nav-item {
  flex: 1;
  text-align: center;
  padding: 8px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: color 0.3s;
}

.nav-item.active {
  color: #1890ff;
}

.nav-item:hover {
  color: #1890ff;
}
</style>
