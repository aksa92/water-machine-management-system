<!-- ScanPage.vue - 修复样式版本 -->
<template>
  <div class="scan-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-title">扫码取水</div>
      <button class="back-btn" @click="goBack">‹</button>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 扫描区域 -->
      <div class="scan-section" v-if="!deviceInfo">
        <div class="scan-area">
          <!-- 扫描框 -->
          <div class="scan-frame">
            <div class="scan-lines">
              <div class="scan-line"></div>
            </div>
            <div class="scan-corners">
              <div class="corner top-left"></div>
              <div class="corner top-right"></div>
              <div class="corner bottom-left"></div>
              <div class="corner bottom-right"></div>
            </div>
          </div>

          <div class="scan-instruction">
            请扫描设备二维码
          </div>

          <div class="scan-hint">
            将二维码放入框内，即可自动扫描
          </div>

          <!-- 开发调试按钮（更简洁的样式） -->
          <div class="dev-options" v-if="isDevelopment">
            <div class="dev-buttons">
              <button class="dev-btn" @click="simulateScan('TERM001')">TERM001</button>
              <button class="dev-btn" @click="simulateScan('TERM002')">TERM002</button>
              <button class="dev-btn" @click="simulateScan('TERM003')">TERM003</button>
            </div>
          </div>
        </div>

        <!-- 手动输入选项 -->
        <div class="manual-input" @click="showManualInput">
          <span class="manual-icon">🔢</span>
          <span class="manual-text">手动输入设备ID</span>
        </div>
      </div>

      <!-- 设备信息区域（扫描后显示） -->
      <div class="device-section" v-else>
        <div class="device-card">
          <div class="device-header">
            <div class="device-icon">🚰</div>
            <div class="device-info">
              <div class="device-name">{{ deviceInfo.name }}</div>
              <div class="device-details">
                <span class="device-id">ID: {{ deviceInfo.id }}</span>
                <span class="device-location">{{ deviceInfo.location }}</span>
              </div>
            </div>
            <div class="device-status" :class="deviceInfo.status">
              {{ deviceInfo.statusText }}
            </div>
          </div>

          <!-- 用户信息（简洁样式） -->
          <div class="user-info" v-if="userInfo">
            <div class="user-label">当前用户:</div>
            <div class="user-name">{{ userInfo.username }}</div>
            <div class="user-id">{{ userInfo.studentId }}</div>
          </div>

          <div class="divider"></div>

          <!-- 取水量选择 -->
          <div class="water-amount-section">
            <div class="section-title">选择取水量</div>
            <div class="amount-options">
              <div
                  v-for="amount in waterAmounts"
                  :key="amount.value"
                  class="amount-option"
                  :class="{
                    selected: selectedAmount === amount.value,
                    disabled: deviceInfo.status !== 'online'
                  }"
                  @click="deviceInfo.status === 'online' && selectAmount(amount.value)"
              >
                <div class="amount-value">{{ amount.value }}ml</div>
                <div class="amount-price">{{ amount.price }}</div>
              </div>
            </div>

            <!-- 自定义输入（精简样式） -->
            <div class="custom-amount">
              <input
                  type="number"
                  v-model="customAmount"
                  placeholder="自定义取水量 (50-1000ml)"
                  min="50"
                  max="1000"
                  :disabled="deviceInfo.status !== 'online'"
                  @keyup.enter="useCustomAmount"
                  class="custom-input"
              />
            </div>
          </div>

          <!-- 水质信息预览 -->
          <div class="water-quality-preview" v-if="deviceInfo.status === 'online'">
            <div class="quality-header">
              <span class="quality-title">水质预览</span>
              <button class="quality-detail-btn" @click="viewWaterQuality">
                查看详情
              </button>
            </div>
            <div class="quality-items">
              <div class="quality-item">
                <span class="quality-label">自来水:</span>
                <span class="quality-value">{{ deviceInfo.waterQuality.tapWater }} mg/L</span>
              </div>
              <div class="quality-item">
                <span class="quality-label">纯净水:</span>
                <span class="quality-value">{{ deviceInfo.waterQuality.pureWater }} mg/L</span>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <button
                class="action-btn primary"
                @click="confirmWater"
                :disabled="!selectedAmount || deviceInfo.status !== 'online' || isProcessing"
            >
              <span v-if="isProcessing">取水中...</span>
              <span v-else>确认取水</span>
            </button>
            <button
                class="action-btn secondary"
                @click="viewRealtimeData"
                :disabled="deviceInfo.status !== 'online'"
            >
              查看实时数据
            </button>
          </div>

          <!-- 重新扫描按钮 -->
          <div class="rescan-section">
            <button class="rescan-btn" @click="resetScan">
              重新扫描
            </button>
          </div>
        </div>

        <!-- 取水过程显示 -->
        <div class="water-process" v-if="isProcessing">
          <div class="process-content">
            <div class="process-icon">🚰</div>
            <div class="process-info">
              <div class="process-title">正在取水...</div>
              <div class="process-details">
                <span class="process-item">取水量: {{ selectedAmount }}ml</span>
                <span class="process-item">剩余: {{ remainingTime }}秒</span>
              </div>
            </div>
            <div class="process-progress">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: progress + '%' }"></div>
              </div>
              <div class="progress-text">{{ progress }}%</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-button" @click="goToPage('home')">
        <div class="nav-icon">🗺️</div>
        <div class="nav-text">地图</div>
      </div>

      <div class="nav-button active" @click="goToPage('scan')">
        <div class="nav-icon">📷</div>
        <div class="nav-text">扫码</div>
      </div>

      <div class="nav-button" @click="goToPage('profile')">
        <div class="nav-icon">👤</div>
        <div class="nav-text">我的</div>
      </div>
    </div>

    <!-- 手动输入弹窗（保持原有样式） -->
    <div v-if="showManualDialog" class="dialog-overlay">
      <div class="dialog-content">
        <div class="dialog-header">
          <h3>手动输入设备ID</h3>
          <button class="close-btn" @click="closeManualDialog">✕</button>
        </div>
        <div class="dialog-body">
          <input
              type="text"
              v-model="manualDeviceId"
              placeholder="请输入设备ID（如：TERM001）"
              class="device-input"
              @keyup.enter="submitManualInput"
          />
          <div class="input-hint">可在设备上找到二维码下方的ID</div>
        </div>
        <div class="dialog-footer">
          <button class="dialog-btn secondary" @click="closeManualDialog">
            取消
          </button>
          <button class="dialog-btn primary" @click="submitManualInput" :disabled="!manualDeviceId">
            确定
          </button>
        </div>
      </div>
    </div>

    <!-- 取水结果弹窗 -->
    <div v-if="showResultDialog" class="dialog-overlay">
      <div class="dialog-content result-dialog">
        <div class="result-header">
          <div class="result-icon" :class="resultStatus">
            {{ resultStatus === 'success' ? '✓' : '✗' }}
          </div>
          <h3>{{ resultTitle }}</h3>
        </div>
        <div class="result-body">
          <div class="result-message">{{ resultMessage }}</div>
        </div>
        <div class="result-footer">
          <button class="dialog-btn primary" @click="closeResultDialog">
            确定
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<!-- ScanPage.vue - 将所有设备设置为在线 -->
<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { deviceService } from '@/services/deviceService'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 开发模式标志
const isDevelopment = ref(true)

// 状态
const deviceInfo = ref(null)
const showManualDialog = ref(false)
const manualDeviceId = ref('')
const selectedAmount = ref(null)
const customAmount = ref('')
const isProcessing = ref(false)
const progress = ref(0)
const remainingTime = ref(0)
const showResultDialog = ref(false)
const resultStatus = ref('')
const resultTitle = ref('')
const resultMessage = ref('')

// 用户信息
const userInfo = computed(() => {
  return {
    username: userStore.username || '用户',
    studentId: userStore.studentId || '未登录'
  }
})

// 取水量选项
const waterAmounts = [
  { value: 250, price: '免费' },
  { value: 500, price: '免费' },
  { value: 750, price: '免费' }
]

// 修改：将所有设备设置为在线状态
const mockDevices = {
  'TERM001': {
    id: 'TERM001',
    name: '教学楼饮水机',
    deviceId: 'WM001',
    status: 'online',  // 改为online
    statusText: '在线',
    location: '教学楼1F大厅',
    waterQuality: {
      tapWater: '285',
      pureWater: '13'
    }
  },
  'TERM002': {
    id: 'TERM002',
    name: '学生公寓饮水机',
    deviceId: 'WM002',
    status: 'online',  // 改为online
    statusText: '在线',
    location: '天马学生公寓1F',
    waterQuality: {
      tapWater: '290',
      pureWater: '15'
    }
  },
  'TERM003': {
    id: 'TERM003',
    name: '图书馆饮水机',
    deviceId: 'WM003',
    status: 'online',  // 改为online
    statusText: '在线',
    location: '图书馆2F',
    waterQuality: {
      tapWater: '275',
      pureWater: '18'
    }
  }
}

onMounted(() => {
  console.log('扫码页面加载')
})

// 模拟扫码
const simulateScan = async (deviceId) => {
  console.log('模拟扫码:', deviceId)
  // 先重置扫描
  resetScan()
  // 延迟加载设备信息，确保状态重置
  setTimeout(async () => {
    await loadDeviceInfo(deviceId)
  }, 100)
}

// 修改：加载设备信息时强制设置为在线
const loadDeviceInfo = async (terminalId) => {
  try {
    const result = await deviceService.getTerminalInfo(terminalId)

    if (result.code === 200 && result.data) {
      const data = result.data

      deviceInfo.value = {
        id: terminalId,
        name: data.terminalName || mockDevices[terminalId]?.name || '饮水机',
        deviceId: data.deviceId || mockDevices[terminalId]?.deviceId,
        status: 'online',  // 强制设置为在线
        statusText: '在线',
        location: data.location || mockDevices[terminalId]?.location || '',
        waterQuality: {
          tapWater: data.rawWaterTds || mockDevices[terminalId]?.waterQuality.tapWater || '--',
          pureWater: data.pureWaterTds || mockDevices[terminalId]?.waterQuality.pureWater || '--'
        }
      }
    } else {
      // 使用模拟数据，确保在线
      deviceInfo.value = {
        ...mockDevices[terminalId],
        status: 'online',
        statusText: '在线'
      }
    }
  } catch (error) {
    console.error('获取设备信息失败，使用模拟数据:', error)
    // 使用模拟数据，确保在线
    deviceInfo.value = mockDevices[terminalId] || {
      id: terminalId,
      name: `${terminalId}饮水机`,
      status: 'online',
      statusText: '在线',
      location: '未知位置',
      waterQuality: {
        tapWater: '285',
        pureWater: '15'
      }
    }
  }
}

// 选择取水量
const selectAmount = (amount) => {
  selectedAmount.value = amount
  customAmount.value = ''
}

// 使用自定义水量
const useCustomAmount = () => {
  if (!customAmount.value) return

  const amount = parseInt(customAmount.value)
  if (amount >= 50 && amount <= 1000) {
    selectedAmount.value = amount
  } else {
    alert('请输入50-1000ml之间的水量')
  }
}

// 修改：简化确认取水逻辑
const confirmWater = async () => {
  if (!selectedAmount.value) {
    alert('请选择取水量')
    return
  }

  if (!userStore.studentId) {
    alert('请先登录')
    router.push('/')
    return
  }

  // 开始取水流程
  startWaterProcess()
}

// 开始取水流程
const startWaterProcess = async () => {
  isProcessing.value = true
  progress.value = 0
  remainingTime.value = Math.ceil(selectedAmount.value / 50)

  // 先调用一次API，并保存结果
  let apiResult = null
  try {
    apiResult = await callWaterUsageAPI()
  } catch (error) {
    console.error('取水API调用失败:', error)
    // 立即停止并显示错误
    isProcessing.value = false
    showResult(
        'error',
        '取水失败',
        'API调用失败，请重试'
    )
    return
  }

  // 如果API调用失败，立即停止
  if (!apiResult || apiResult.code !== 200) {
    isProcessing.value = false
    showResult(
        'error',
        '取水失败',
        apiResult?.message || '取水操作失败'
    )
    return
  }

  // API调用成功后，开始模拟进度
  const interval = setInterval(() => {
    progress.value += 5
    if (remainingTime.value > 0) {
      remainingTime.value -= 0.25
    }

    if (progress.value >= 100) {
      clearInterval(interval)
      completeWaterProcess()
    }
  }, 200)
}

// 调用后端取水API
const callWaterUsageAPI = async () => {
  // 添加调用计数，用于调试
  console.log('调用取水API，水量:', selectedAmount.value)

  try {
    const result = await deviceService.scanToDrink(
        deviceInfo.value.id,
        userStore.studentId,
        selectedAmount.value
    )

    if (result.code === 200) {
      // 只在API成功时记录历史
      console.log('API调用成功，记录历史')
      recordWaterHistory(result.data)
      return result
    } else {
      console.error('取水API失败:', result.message)
      return result
    }
  } catch (error) {
    console.error('取水API异常:', error)
    return { code: 500, message: '取水操作失败' }
  }
}

// 完成取水流程
const completeWaterProcess = async () => {
  isProcessing.value = false


  showResult(
      'success',
      '取水成功',
      `您已成功取水 ${selectedAmount.value}ml`
  )


  setTimeout(() => {
    resetScan()
    showResultDialog.value = false
  }, 2000)
}

// 显示结果
const showResult = (status, title, message) => {
  resultStatus.value = status
  resultTitle.value = title
  resultMessage.value = message
  showResultDialog.value = true
}

// 关闭结果弹窗
const closeResultDialog = () => {
  showResultDialog.value = false
  resetScan()
}

/// 记录取水历史
// 记录取水历史
const recordWaterHistory = (data) => {
  // 只保存当前扫描的设备记录
  if (!deviceInfo.value || !deviceInfo.value.id) {
    console.log('设备信息不完整，不保存历史记录')
    return
  }

  const history = {
    id: Date.now(),
    date: '今日',
    deviceName: deviceInfo.value.name || '饮水机',
    deviceId: deviceInfo.value.id,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    amount: `${selectedAmount.value}ml`,
    timestamp: new Date().toISOString(),
    location: deviceInfo.value.location || ''
  }

  // 添加去重机制
  const existingHistory = JSON.parse(localStorage.getItem('waterHistory') || '[]')

  // 检查5秒内是否有相同设备的记录
  const now = new Date()
  const fiveSecondsAgo = new Date(now.getTime() - 5000) // 5秒前

  const duplicateIndex = existingHistory.findIndex(record => {
    const recordTime = new Date(record.timestamp)
    return (
        record.deviceId === deviceInfo.value.id &&
        recordTime >= fiveSecondsAgo &&
        recordTime <= now
    )
  })

  if (duplicateIndex !== -1) {
    // 如果5秒内有重复记录，替换它而不是添加新记录
    console.log('发现重复记录，替换而不是新增')
    existingHistory[duplicateIndex] = history
  } else {
    // 没有重复记录，正常添加
    console.log('保存新记录')
    existingHistory.unshift(history)
  }

  // 过滤掉重复的terminalID记录（只保留最新的）
  const deviceRecords = {}
  const finalHistory = existingHistory.filter(record => {
    if (!deviceRecords[record.deviceId]) {
      deviceRecords[record.deviceId] = true
      return true
    }
    return false
  })

  // 限制总记录数
  const limitedHistory = finalHistory.slice(0, 20)

  localStorage.setItem('waterHistory', JSON.stringify(limitedHistory))
  console.log('更新后的历史记录:', limitedHistory)
}

// 查看水质
const viewRealtimeData = () => {
  if (deviceInfo.value) {
    router.push({
      path: '/realtime-data',
      query: {
        terminalId: deviceInfo.value.id,
        deviceId: deviceInfo.value.deviceId
      }
    })
  }
}

// 显示手动输入
const showManualInput = () => {
  showManualDialog.value = true
}

// 关闭手动输入
const closeManualDialog = () => {
  showManualDialog.value = false
  manualDeviceId.value = ''
}

// 提交手动输入
const submitManualInput = async () => {
  if (!manualDeviceId.value.trim()) {
    alert('请输入设备ID')
    return
  }

  const deviceId = manualDeviceId.value.trim().toUpperCase()
  await loadDeviceInfo(deviceId)
  closeManualDialog()
}

// 重置扫描
const resetScan = () => {
  deviceInfo.value = null
  selectedAmount.value = null
  customAmount.value = ''
  isProcessing.value = false
  progress.value = 0
}

// 返回上一页
const goBack = () => {
  if (deviceInfo.value) {
    resetScan()
  } else {
    router.push('/home')
  }
}

// 页面跳转
const goToPage = (page) => {
  switch(page) {
    case 'home':
      router.push('/home')
      break
    case 'scan':
      break
    case 'profile':
      router.push('/profile')
      break
  }
}
</script>

<style scoped>
.scan-page {
  width: 375px;
  height: 667px;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

/* 顶部标题栏 */
.header {
  height: 40px;
  background: linear-gradient(135deg, #1156b1 0%, #81d3f8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: white;
  letter-spacing: 1px;
}

.back-btn {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 扫描区域 */
.scan-section {
  text-align: center;
}

.scan-area {
  background: white;
  border-radius: 12px;
  padding: 30px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 扫描框 */
.scan-frame {
  width: 250px;
  height: 250px;
  margin: 0 auto 20px;
  position: relative;
  border: 2px solid rgba(24, 144, 255, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  overflow: hidden;
}

/* 扫描线动画 */
.scan-lines {
  position: absolute;
  width: 100%;
  height: 100%;
}

.scan-line {
  position: absolute;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #1890ff, transparent);
  top: 20%;
  animation: scanLine 2s infinite linear;
}

@keyframes scanLine {
  0% { top: 20%; }
  50% { top: 80%; }
  100% { top: 20%; }
}

/* 扫描框四角 */
.scan-corners {
  position: absolute;
  width: 100%;
  height: 100%;
}

.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #1890ff;
}

.corner.top-left {
  top: -3px;
  left: -3px;
  border-right: none;
  border-bottom: none;
  border-radius: 6px 0 0 0;
}

.corner.top-right {
  top: -3px;
  right: -3px;
  border-left: none;
  border-bottom: none;
  border-radius: 0 6px 0 0;
}

.corner.bottom-left {
  bottom: -3px;
  left: -3px;
  border-right: none;
  border-top: none;
  border-radius: 0 0 0 6px;
}

.corner.bottom-right {
  bottom: -3px;
  right: -3px;
  border-left: none;
  border-top: none;
  border-radius: 0 0 6px 0;
}

.scan-instruction {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.scan-hint {
  font-size: 14px;
  color: #666;
}

/* 开发调试按钮 */
.dev-options {
  margin-top: 16px;
}

.dev-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.dev-btn {
  padding: 6px 12px;
  background: #f0f5ff;
  border: 1px solid #d6e4ff;
  border-radius: 4px;
  color: #1890ff;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.dev-btn:hover {
  background: #1890ff;
  color: white;
}

/* 手动输入 */
.manual-input {
  background: white;
  border: 1px dashed #1890ff;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.manual-input:hover {
  background: #f0f5ff;
  transform: translateY(-1px);
}

.manual-icon {
  font-size: 18px;
}

.manual-text {
  font-size: 14px;
  color: #1890ff;
  font-weight: 500;
}

/* 设备信息卡片 */
.device-section {
  padding: 10px 0;
}

.device-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.device-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.device-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.device-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-id {
  font-size: 14px;
  color: #1890ff;
  font-weight: 500;
}

.device-location {
  font-size: 12px;
  color: #666;
}

.device-status {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
  white-space: nowrap;
}

.device-status.online {
  background: rgba(4, 217, 25, 0.1);
  color: #04d919;
}

.device-status.offline {
  background: rgba(170, 170, 170, 0.1);
  color: #aaaaaa;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 16px;
  font-size: 12px;
}

.user-label {
  color: #666;
}

.user-name {
  color: #333;
  font-weight: 500;
}

.user-id {
  color: #1890ff;
  background: white;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: auto;
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e8e8e8, transparent);
  margin: 20px 0;
}

/* 取水量选择 */
.water-amount-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-left: 4px;
  border-left: 4px solid #1890ff;
}

.amount-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.amount-option {
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.amount-option:hover:not(.disabled) {
  transform: translateY(-2px);
  border-color: #1890ff;
}

.amount-option.selected {
  background: #f0f5ff;
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.amount-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.amount-option.disabled:hover {
  transform: none;
  border-color: #e8e8e8;
}

.amount-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.amount-price {
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

/* 自定义输入 */
.custom-amount {
  width: 100%;
}

.custom-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  font-size: 14px;
}

.custom-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.custom-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

/* 水质预览 */
.water-quality-preview {
  background: #f0f5ff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.quality-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.quality-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.quality-detail-btn {
  background: none;
  border: 1px solid #1890ff;
  border-radius: 6px;
  padding: 4px 12px;
  color: #1890ff;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.quality-detail-btn:hover {
  background: #1890ff;
  color: white;
}

.quality-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quality-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.quality-label {
  color: #666;
}

.quality-value {
  color: #333;
  font-weight: 500;
}

/* 操作按钮 */
.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.action-btn {
  padding: 14px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.primary {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
}

.action-btn.secondary {
  background: #f0f5ff;
  color: #1890ff;
  border: 1px solid #d6e4ff;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

/* 重新扫描按钮 */
.rescan-section {
  text-align: center;
}

.rescan-btn {
  background: none;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 10px 20px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.rescan-btn:hover {
  background: #f8f9fa;
  border-color: #1890ff;
  color: #1890ff;
}

/* 取水过程显示 */
.water-process {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-top: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.process-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.process-icon {
  font-size: 48px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.process-info {
  text-align: center;
}

.process-title {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 8px;
}

.process-details {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #666;
}

.process-progress {
  width: 100%;
}

.progress-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #1890ff, #40a9ff);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  font-size: 12px;
  color: #666;
}

/* 底部导航栏 */
.bottom-nav {
  height: 60px;
  background: white;
  border-top: 1px solid #e8e8e8;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.nav-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  background: none;
  border: none;
  padding: 0;
}

.nav-button:hover {
  background: #f8f9fa;
}

.nav-button.active {
  color: #1890ff;
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.nav-text {
  font-size: 12px;
  font-weight: 500;
}

/* 手动输入弹窗 */
.dialog-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.dialog-content {
  background: white;
  border-radius: 16px;
  width: 320px;
  max-width: 90%;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 16px;
  border-bottom: 1px solid #e8e8e8;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.dialog-body {
  padding: 20px;
}

.device-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 8px;
}

.device-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.input-hint {
  font-size: 12px;
  color: #666;
  text-align: left;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px 20px;
}

.dialog-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.dialog-btn.primary {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
}

.dialog-btn.secondary {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #e8e8e8;
}

.dialog-btn:hover {
  transform: translateY(-1px);
}

.dialog-btn.primary:hover {
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

/* 结果弹窗 */
.result-dialog {
  text-align: center;
}

.result-header {
  padding: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.result-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 12px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: bold;
}

.result-icon.success {
  background: #f6ffed;
  color: #52c41a;
  border: 3px solid #52c41a;
}

.result-icon.error {
  background: #fff2f0;
  color: #ff4d4f;
  border: 3px solid #ff4d4f;
}

.result-body {
  padding: 20px;
}

.result-message {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
}

.result-footer {
  padding: 16px 20px 20px;
}

/* 响应式调整 */
@media (max-width: 420px) {
  .scan-page {
    width: 100%;
    height: 100vh;
  }

  .main-content {
    padding: 16px;
  }

  .scan-frame {
    width: 200px;
    height: 200px;
  }

  .amount-options {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    grid-template-columns: 1fr;
  }
}
</style>