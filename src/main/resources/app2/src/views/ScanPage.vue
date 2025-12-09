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
              <div class="device-id">ID: {{ deviceInfo.id }}</div>
            </div>
            <div class="device-status" :class="deviceInfo.status">
              {{ deviceInfo.statusText }}
            </div>
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
                  :class="{ selected: selectedAmount === amount.value }"
                  @click="selectAmount(amount.value)"
              >
                <div class="amount-value">{{ amount.value }}ml</div>
                <div class="amount-price">{{ amount.price }}</div>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <button class="action-btn primary" @click="confirmWater" :disabled="!selectedAmount">
              确认取水
            </button>
            <button class="action-btn secondary" @click="viewWaterQuality">
              查看水质
            </button>
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

    <!-- 手动输入弹窗 -->
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
              placeholder="请输入设备ID（如：A201）"
              class="device-input"
              @keyup.enter="submitManualInput"
          />
          <div class="input-hint">可在设备上找到二维码下方的ID</div>
        </div>
        <div class="dialog-footer">
          <button class="dialog-btn secondary" @click="closeManualDialog">
            取消
          </button>
          <button class="dialog-btn primary" @click="submitManualInput">
            确定
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 状态
const deviceInfo = ref(null)
const showManualDialog = ref(false)
const manualDeviceId = ref('')
const selectedAmount = ref(null)

// 取水量选项
const waterAmounts = [
  { value: 150, price: '免费' },
  { value: 200, price: '免费' },
  { value: 250, price: '免费' }
]

// 模拟的设备数据
const mockDevices = {
  'A201': {
    id: 'A201',
    name: '湖南大学教学楼1F饮水机',
    status: 'online',
    statusText: '在线'
  },
  'B201': {
    id: 'B201',
    name: '天马学生公寓1F饮水机',
    status: 'online',
    statusText: '在线'
  },
  'C101': {
    id: 'C101',
    name: '图书馆2F饮水机',
    status: 'offline',
    statusText: '离线'
  }
}

// 模拟扫码（实际应使用摄像头API）
const simulateScan = () => {
  // 模拟扫描到设备A201
  setTimeout(() => {
    deviceInfo.value = mockDevices['A201']
  }, 1000)
}

onMounted(() => {
  // 可以在这里初始化摄像头扫描
  // 暂时模拟扫码
  simulateScan()
})

// 选择取水量
const selectAmount = (amount) => {
  selectedAmount.value = amount
}

// 确认取水
const confirmWater = () => {
  if (!selectedAmount.value) {
    alert('请选择取水量')
    return
  }

  alert(`开始取水：${selectedAmount.value}ml`)
  // 这里应该调用API开始取水
}

// 查看水质
const viewWaterQuality = () => {
  if (deviceInfo.value) {
    router.push({
      path: '/water-quality',
      query: { deviceId: deviceInfo.value.id }
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
const submitManualInput = () => {
  if (!manualDeviceId.value.trim()) {
    alert('请输入设备ID')
    return
  }

  const device = mockDevices[manualDeviceId.value.trim()]
  if (device) {
    deviceInfo.value = device
    closeManualDialog()
  } else {
    alert('设备ID不存在，请检查后重新输入')
  }
}

// 返回上一页
const goBack = () => {
  if (deviceInfo.value) {
    // 如果在设备信息页面，返回扫描页面
    deviceInfo.value = null
    selectedAmount.value = null
  } else {
    // 如果在扫描页面，返回首页
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
      // 已经在扫码页面
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
  0% {
    top: 20%;
  }
  50% {
    top: 80%;
  }
  100% {
    top: 20%;
  }
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
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.device-icon {
  font-size: 32px;
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

.device-id {
  font-size: 14px;
  color: #1890ff;
  font-weight: 500;
}

.device-status {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
}

.device-status.online {
  background: rgba(4, 217, 25, 0.1);
  color: #04d919;
}

.device-status.offline {
  background: rgba(170, 170, 170, 0.1);
  color: #aaaaaa;
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

.amount-option:hover {
  transform: translateY(-2px);
  border-color: #1890ff;
}

.amount-option.selected {
  background: #f0f5ff;
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
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

/* 操作按钮 */
.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
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