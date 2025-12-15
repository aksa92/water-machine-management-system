<template>
  <div class="inspection-form">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">巡检表单</div>
      <div class="header-right"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 设备信息卡片 -->
      <div v-if="currentDevice" class="device-info-card">
        <div class="device-header">
          <div class="device-icon">💧</div>
          <div class="device-details">
            <div class="device-name">{{ currentDevice.name }}</div>
            <div class="device-id">{{ currentDevice.id }}</div>
          </div>
          <div class="device-status" :class="getStatusClass(currentDevice.status)">
            {{ formatStatus(currentDevice.status) }}
          </div>
        </div>
        <div class="device-info">
          <div class="info-row">
            <span class="info-label">位置:</span>
            <span class="info-value">{{ currentDevice.location }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">类型:</span>
            <span class="info-value">{{ deviceTypeText }}</span>
          </div>
          <div v-if="currentDevice.waterLevel !== undefined" class="info-row">
            <span class="info-label">当前水位:</span>
            <span class="info-value">{{ currentDevice.waterLevel }}%</span>
          </div>
        </div>
      </div>

      <!-- 巡检表单容器 -->
      <div class="form-container">
        <!-- 巡检状态 -->
        <div class="form-section">
          <div class="section-title">
            巡检结果
            <span class="required-star">*</span>
          </div>
          <div class="status-selector">
            <div
              class="status-option"
              :class="{ 'active': inspectionData.status === 'normal' }"
              @click="selectStatus('normal')"
            >
              <div class="option-icon">✅</div>
              <div class="option-text">正常</div>
            </div>
            <div
              class="status-option"
              :class="{ 'active': inspectionData.status === 'warning' }"
              @click="selectStatus('warning')"
            >
              <div class="option-icon">⚠️</div>
              <div class="option-text">异常</div>
            </div>
            <div
              class="status-option"
              :class="{ 'active': inspectionData.status === 'fault' }"
              @click="selectStatus('fault')"
            >
              <div class="option-icon">❌</div>
              <div class="option-text">故障</div>
            </div>
          </div>
        </div>

        <!-- 异常/故障描述 -->
        <div v-if="inspectionData.status !== 'normal'" class="form-section">
          <div class="section-title">
            {{ inspectionData.status === 'warning' ? '异常描述' : '故障描述' }}
            <span class="required-star">*</span>
          </div>
          <textarea
            v-model="inspectionData.description"
            class="description-textarea"
            :placeholder="getDescriptionPlaceholder()"
            rows="4"
            maxlength="500"
            @input="updateCharacterCount"
          ></textarea>
          <div class="character-count">
            {{ descriptionLength }}/500
          </div>
        </div>

        <!-- 检查项目 -->
        <div class="form-section">
          <div class="section-title">检查项目</div>
          <div class="checklist">
            <!-- 通用检查项目 -->
            <div class="check-item" v-if="deviceType === 'water_maker'">
              <div class="check-info">
                <div class="check-label">水质检测</div>
                <div class="check-desc">检查TDS值是否正常</div>
              </div>
              <div class="check-action">
                <button
                  class="check-btn"
                  :class="{ 'checked': inspectionData.checks.waterQuality }"
                  @click="toggleCheck('waterQuality')"
                >
                  {{ inspectionData.checks.waterQuality ? '已检查' : '未检查' }}
                </button>
              </div>
            </div>

            <div class="check-item">
              <div class="check-info">
                <div class="check-label">设备清洁</div>
                <div class="check-desc">检查设备表面是否清洁</div>
              </div>
              <div class="check-action">
                <button
                  class="check-btn"
                  :class="{ 'checked': inspectionData.checks.cleaning }"
                  @click="toggleCheck('cleaning')"
                >
                  {{ inspectionData.checks.cleaning ? '已检查' : '未检查' }}
                </button>
              </div>
            </div>

            <!-- 制水机专用检查项 -->
            <div class="check-item" v-if="deviceType === 'water_maker'">
              <div class="check-info">
                <div class="check-label">滤芯状态</div>
                <div class="check-desc">检查滤芯寿命及状态</div>
              </div>
              <div class="check-action">
                <button
                  class="check-btn"
                  :class="{ 'checked': inspectionData.checks.filterStatus }"
                  @click="toggleCheck('filterStatus')"
                >
                  {{ inspectionData.checks.filterStatus ? '已检查' : '未检查' }}
                </button>
              </div>
            </div>

            <!-- 供水机专用检查项 -->
            <div class="check-item" v-if="deviceType === 'water_supply'">
              <div class="check-info">
                <div class="check-label">水位检查</div>
                <div class="check-desc">检查当前水位是否正常</div>
              </div>
              <div class="check-action">
                <button
                  class="check-btn"
                  :class="{ 'checked': inspectionData.checks.waterLevel }"
                  @click="toggleCheck('waterLevel')"
                >
                  {{ inspectionData.checks.waterLevel ? '已检查' : '未检查' }}
                </button>
              </div>
            </div>

            <div class="check-item" v-if="deviceType === 'water_supply'">
              <div class="check-info">
                <div class="check-label">浮球阀检查</div>
                <div class="check-desc">检查高低水位浮球阀</div>
              </div>
              <div class="check-action">
                <button
                  class="check-btn"
                  :class="{ 'checked': inspectionData.checks.floatValve }"
                  @click="toggleCheck('floatValve')"
                >
                  {{ inspectionData.checks.floatValve ? '已检查' : '未检查' }}
                </button>
              </div>
            </div>

            <!-- 漏水检查 -->
            <div class="check-item">
              <div class="check-info">
                <div class="check-label">漏水检查</div>
                <div class="check-desc">检查设备周边是否漏水</div>
              </div>
              <div class="check-action">
                <button
                  class="check-btn"
                  :class="{ 'checked': inspectionData.checks.leakCheck }"
                  @click="toggleCheck('leakCheck')"
                >
                  {{ inspectionData.checks.leakCheck ? '已检查' : '未检查' }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 现场照片 -->
        <div class="form-section">
          <div class="section-title">
            现场照片
            <span class="optional-text">（可选）</span>
          </div>
          <div class="photo-upload">
            <div class="upload-instruction">
              上传现场照片，最多5张。建议拍摄设备状态、异常部位等。
            </div>

            <div class="upload-actions">
              <div class="upload-area" @click="uploadPhoto">
                <div class="upload-icon">📷</div>
                <div class="upload-text">点击上传照片</div>
                <div class="upload-hint">支持 JPG、PNG 格式</div>
              </div>
              <input
                type="file"
                ref="fileInput"
                @change="handleFileUpload"
                accept="image/*"
                multiple
                style="display: none"
              />
            </div>

            <!-- 照片预览 -->
            <div v-if="uploadedPhotos.length > 0" class="photo-preview">
              <div v-for="(photo, index) in uploadedPhotos" :key="index" class="preview-item">
                <div class="preview-image">
                  <img :src="photo.url" :alt="`巡检照片${index + 1}`" />
                </div>
                <div class="preview-info">
                  <div class="preview-name">{{ photo.name }}</div>
                  <div class="preview-size">{{ formatFileSize(photo.size) }}</div>
                </div>
                <div class="preview-actions">
                  <button class="preview-btn delete" @click="removePhoto(index)">
                    删除
                  </button>
                </div>
              </div>
            </div>

            <div class="upload-tips">
              <div class="tip-item">📸 拍摄清晰照片，便于后续分析</div>
              <div class="tip-item">⚡ 建议每张照片不超过5MB</div>
            </div>
          </div>
        </div>

        <!-- 维修建议（异常/故障时显示） -->
        <div v-if="inspectionData.status !== 'normal'" class="form-section">
          <div class="section-title">维修建议</div>
          <div class="suggestions">
            <div class="suggestion-item">
              <input
                type="checkbox"
                id="suggestion-repair"
                v-model="inspectionData.suggestions.needRepair"
                class="suggestion-checkbox"
              />
              <label for="suggestion-repair" class="suggestion-label">需要维修</label>
            </div>
            <div class="suggestion-item">
              <input
                type="checkbox"
                id="suggestion-replace"
                v-model="inspectionData.suggestions.needReplace"
                class="suggestion-checkbox"
              />
              <label for="suggestion-replace" class="suggestion-label">需要更换部件</label>
            </div>
            <div class="suggestion-item">
              <input
                type="checkbox"
                id="suggestion-followup"
                v-model="inspectionData.suggestions.needFollowup"
                class="suggestion-checkbox"
              />
              <label for="suggestion-followup" class="suggestion-label">需要跟进</label>
            </div>
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-section">
        <button
          class="submit-btn"
          @click="submitInspection"
          :disabled="submitting || !canSubmit"
          :class="{ 'disabled': !canSubmit }"
        >
          <span v-if="submitting">
            <span class="submit-spinner"></span>
            提交中...
          </span>
          <span v-else>
            {{ getSubmitButtonText() }}
          </span>
        </button>

        <div v-if="!canSubmit && inspectionData.status !== 'normal'" class="submit-hint">
          ⚠️ 请填写{{ inspectionData.status === 'warning' ? '异常' : '故障' }}描述
        </div>
      </div>
    </div>

    <!-- 提交成功弹窗 -->
    <div v-if="showSuccessModal" class="success-modal">
      <div class="modal-overlay" @click="closeSuccessModal"></div>
      <div class="modal-content">
        <div class="modal-icon">✅</div>
        <div class="modal-title">巡检提交成功</div>
        <div class="modal-message">
          您的巡检记录已成功提交，系统将自动更新设备状态。
        </div>
        <div class="modal-actions">
          <button class="modal-btn primary" @click="goToInspection">
            返回巡检
          </button>
          <button class="modal-btn secondary" @click="goToHome">
            返回首页
          </button>
        </div>
      </div>
    </div>

    <!-- 确认弹窗 -->
    <div v-if="showConfirmModal" class="confirm-modal">
      <div class="modal-overlay" @click="closeConfirmModal"></div>
      <div class="modal-content">
        <div class="modal-icon">⚠️</div>
        <div class="modal-title">确认提交</div>
        <div class="modal-message">
          您标记设备状态为<span class="status-highlight">{{ formatStatus(inspectionData.status) }}</span>，
          请确认描述信息已填写完整。
        </div>
        <div class="modal-actions">
          <button class="modal-btn secondary" @click="closeConfirmModal">
            返回修改
          </button>
          <button class="modal-btn primary" @click="confirmSubmit">
            确认提交
          </button>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goToHome">首页</div>
      <div class="nav-item" @click="goToInspection">巡检</div>
      <div class="nav-item" @click="goToWorkOrders">工单</div>
      <div class="nav-item" @click="goToProfile">我的</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { deviceService } from '@/services/deviceService'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 设备信息
const currentDevice = ref(null)
const deviceId = computed(() => route.query.deviceId)
const deviceType = computed(() => route.query.deviceType || 'water_supply')

// 巡检数据
const inspectionData = reactive({
  deviceId: '',
  deviceType: '',
  status: 'normal',
  description: '',
  checks: {
    waterQuality: false,
    cleaning: false,
    filterStatus: false,
    waterLevel: false,
    floatValve: false,
    leakCheck: false
  },
  suggestions: {
    needRepair: false,
    needReplace: false,
    needFollowup: false
  }
})

// 照片上传
const uploadedPhotos = ref([])
const fileInput = ref(null)

// 状态控制
const submitting = ref(false)
const showSuccessModal = ref(false)
const showConfirmModal = ref(false)

// 计算属性
const deviceTypeText = computed(() => {
  return deviceType.value === 'water_maker' ? '制水机' : '供水机'
})

const descriptionLength = computed(() => {
  return inspectionData.description.length
})

const canSubmit = computed(() => {
  if (inspectionData.status === 'normal') {
    return true
  }
  return inspectionData.description.trim().length > 0
})

// 获取设备信息
const fetchDeviceInfo = async () => {
  if (!deviceId.value) {
    console.warn('未指定设备ID')
    return
  }

  try {
    const response = await deviceService.getDeviceDetail(deviceId.value)
    if (response.code === 200) {
      const data = response.data
      currentDevice.value = {
        id: data.deviceId,
        name: data.deviceName || `${deviceTypeText.value}#${data.deviceId}`,
        location: data.installLocation || '未指定位置',
        status: data.status,
        areaId: data.areaId,
        waterLevel: data.waterLevel,
        deviceType: data.deviceType
      }

      // 设置巡检数据
      inspectionData.deviceId = data.deviceId
      inspectionData.deviceType = data.deviceType || deviceType.value
    }
  } catch (error) {
    console.error('获取设备信息失败:', error)
    // 如果无法获取设备信息，使用基础信息
    currentDevice.value = {
      id: deviceId.value,
      name: `${deviceTypeText.value}#${deviceId.value}`,
      location: '未指定位置',
      status: 'online',
      deviceType: deviceType.value
    }
    inspectionData.deviceId = deviceId.value
    inspectionData.deviceType = deviceType.value
  }
}

// 格式化状态显示
const formatStatus = (status) => {
  const statusMap = {
    'online': '在线',
    'offline': '离线',
    'fault': '故障',
    'normal': '正常',
    'warning': '异常',
    'maintenance': '维护中'
  }
  return statusMap[status] || status
}

// 状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 'online':
    case 'normal':
      return 'status-online'
    case 'offline':
    case 'warning':
      return 'status-warning'
    case 'fault':
      return 'status-fault'
    case 'maintenance':
      return 'status-maintenance'
    default:
      return 'status-unknown'
  }
}

// 选择巡检状态
const selectStatus = (status) => {
  inspectionData.status = status
  if (status === 'normal') {
    inspectionData.description = ''
  }
}

// 获取描述占位符
const getDescriptionPlaceholder = () => {
  if (inspectionData.status === 'warning') {
    return '请详细描述发现的异常情况，包括具体位置、表现等...'
  } else if (inspectionData.status === 'fault') {
    return '请详细描述故障情况，包括故障现象、可能原因等...'
  }
  return ''
}

// 更新字符计数
const updateCharacterCount = () => {
  if (inspectionData.description.length > 500) {
    inspectionData.description = inspectionData.description.substring(0, 500)
  }
}

// 切换检查项
const toggleCheck = (checkName) => {
  inspectionData.checks[checkName] = !inspectionData.checks[checkName]
}

// 上传照片
const uploadPhoto = () => {
  if (uploadedPhotos.value.length >= 5) {
    alert('最多只能上传5张照片')
    return
  }
  fileInput.value.click()
}

const handleFileUpload = (event) => {
  const files = Array.from(event.target.files)

  // 检查总数
  if (uploadedPhotos.value.length + files.length > 5) {
    alert(`最多只能上传5张照片，已上传${uploadedPhotos.value.length}张`)
    return
  }

  files.forEach(file => {
    if (file.size > 5 * 1024 * 1024) { // 5MB限制
      alert(`文件 ${file.name} 超过5MB限制，请重新选择`)
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedPhotos.value.push({
        file: file,
        url: e.target.result,
        name: file.name,
        size: file.size,
        type: file.type
      })
    }
    reader.readAsDataURL(file)
  })

  // 重置文件输入
  event.target.value = ''
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
}

const removePhoto = (index) => {
  uploadedPhotos.value.splice(index, 1)
}

// 获取提交按钮文本
const getSubmitButtonText = () => {
  if (!canSubmit.value) return '请完善信息'
  return `提交${deviceTypeText.value}巡检`
}

// 提交巡检
const submitInspection = async () => {
  // 验证表单
  if (!canSubmit.value) {
    if (inspectionData.status !== 'normal' && !inspectionData.description.trim()) {
      alert('请填写描述信息')
    }
    return
  }

  // 如果状态不是正常，显示确认弹窗
  if (inspectionData.status !== 'normal') {
    showConfirmModal.value = true
    return
  }

  // 直接提交正常状态
  await performSubmit()
}

// 确认提交
const confirmSubmit = async () => {
  showConfirmModal.value = false
  await performSubmit()
}

// 执行提交
const performSubmit = async () => {
  submitting.value = true

  try {
    // 获取维修人员信息
    const repairmanId = authStore.getRepairmanId
    const areaId = authStore.getAreaId

    if (!repairmanId) {
      throw new Error('未获取到维修人员信息')
    }

    // 构建提交数据
    const submitData = {
      deviceId: inspectionData.deviceId,
      deviceType: inspectionData.deviceType,
      status: inspectionData.status,
      description: inspectionData.description,
      checks: inspectionData.checks,
      suggestions: inspectionData.suggestions,
      photos: uploadedPhotos.value.map(photo => ({
        name: photo.name,
        size: photo.size,
        type: photo.type,
        // 在实际项目中，这里应该上传到服务器并返回URL
        url: photo.url
      })),
      repairmanId: repairmanId,
      areaId: areaId,
      timestamp: new Date().toISOString()
    }

    console.log('提交巡检数据:', submitData)

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))

    // 在实际项目中，这里应该调用后端的巡检提交接口
    // const response = await inspectionService.submitInspection(submitData)

    // 如果后端需要更新设备状态，可以调用设备状态服务
    if (inspectionData.status === 'fault') {
      // 标记设备故障
      // await deviceService.markDeviceFault(inspectionData.deviceId, '巡检发现故障', inspectionData.description)
    }

    // 显示成功弹窗
    showSuccessModal.value = true

  } catch (error) {
    console.error('提交失败:', error)
    alert(`提交失败: ${error.message}`)
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
const closeSuccessModal = () => {
  showSuccessModal.value = false
}

const closeConfirmModal = () => {
  showConfirmModal.value = false
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
  closeSuccessModal()
}

const goToWorkOrders = () => {
  router.push('/work-orders')
}

const goToProfile = () => {
  router.push('/profile')
}

// 组件挂载时获取数据
onMounted(() => {
  console.log('巡检表单页面加载，设备ID:', deviceId.value, '设备类型:', deviceType.value)

  if (deviceId.value) {
    fetchDeviceInfo()
  } else {
    console.warn('未传递设备信息，将使用默认数据')
    currentDevice.value = {
      id: '未知设备',
      name: `${deviceTypeText.value}#未知`,
      location: '未指定位置',
      status: 'online',
      deviceType: deviceType.value
    }
  }
})
</script>

<style scoped>
.inspection-form {
  width: 100%;
  height: 100%;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  position: relative;
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
  z-index: 10;
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

/* 设备信息卡片 */
.device-info-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.device-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.device-icon {
  font-size: 32px;
  margin-right: 12px;
}

.device-details {
  flex: 1;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.device-id {
  font-size: 13px;
  color: #666;
}

.device-status {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.status-online {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-warning {
  background: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
}

.status-fault {
  background: #fff2e8;
  color: #fa541c;
  border: 1px solid #ffbb96;
}

.device-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  font-size: 14px;
}

.info-label {
  color: #666;
  width: 60px;
  flex-shrink: 0;
}

.info-value {
  color: #333;
  font-weight: 500;
  flex: 1;
}

/* 表单容器 */
.form-container {
  flex: 1;
}

/* 表单区块 */
.form-section {
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
  display: flex;
  align-items: center;
}

.required-star {
  color: #ff4d4f;
  margin-left: 4px;
}

.optional-text {
  font-size: 13px;
  color: #999;
  font-weight: normal;
  margin-left: 8px;
}

/* 状态选择器 */
.status-selector {
  display: flex;
  gap: 12px;
}

.status-option {
  flex: 1;
  padding: 16px 12px;
  text-align: center;
  border: 2px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.status-option:hover {
  border-color: #1890ff;
  transform: translateY(-2px);
}

.status-option.active {
  border-color: #1890ff;
  background: #f0f7ff;
}

.option-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.option-text {
  font-size: 14px;
  font-weight: 500;
}

.status-option.active .option-text {
  color: #1890ff;
}

/* 描述文本框 */
.description-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  min-height: 80px;
  transition: border-color 0.3s;
}

.description-textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.description-textarea::placeholder {
  color: #999;
}

.character-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 检查项目列表 */
.checklist {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.check-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.check-info {
  flex: 1;
}

.check-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.check-desc {
  font-size: 12px;
  color: #666;
}

.check-btn {
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background: white;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 80px;
}

.check-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.check-btn.checked {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

/* 照片上传 */
.photo-upload {
  width: 100%;
}

.upload-instruction {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
}

.upload-actions {
  margin-bottom: 16px;
}

.upload-area {
  width: 100%;
  padding: 40px 20px;
  border: 2px dashed #e0e0e0;
  border-radius: 6px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #1890ff;
  background: #f8fbff;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.6;
}

.upload-text {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.upload-hint {
  font-size: 12px;
  color: #999;
}

/* 照片预览 */
.photo-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.preview-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.preview-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  margin-right: 12px;
  flex-shrink: 0;
}

.preview-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-info {
  flex: 1;
}

.preview-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  word-break: break-all;
}

.preview-size {
  font-size: 12px;
  color: #999;
}

.preview-actions {
  flex-shrink: 0;
}

.preview-btn.delete {
  padding: 6px 12px;
  background: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.preview-btn.delete:hover {
  background: #ffccc7;
}

.upload-tips {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f6ffed;
  border-radius: 6px;
  border: 1px solid #b7eb8f;
}

.tip-item {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 维修建议 */
.suggestions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.suggestion-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.suggestion-label {
  font-size: 14px;
  color: #333;
  cursor: pointer;
}

/* 提交区域 */
.submit-section {
  margin-top: 20px;
}

.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-btn:hover:not(:disabled):not(.disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.3);
}

.submit-btn:disabled,
.submit-btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  background: linear-gradient(135deg, #cccccc 0%, #999999 100%);
}

.submit-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.submit-hint {
  text-align: center;
  font-size: 13px;
  color: #faad14;
  margin-top: 8px;
}

/* 弹窗样式 */
.success-modal,
.confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: relative;
  background: white;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  width: 300px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  z-index: 1001;
}

.modal-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.modal-message {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 24px;
}

.status-highlight {
  color: #ff4d4f;
  font-weight: 500;
  margin: 0 4px;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.modal-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.modal-btn.primary {
  background: #1890ff;
  color: white;
}

.modal-btn.primary:hover {
  background: #096dd9;
}

.modal-btn.secondary {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #d9d9d9;
}

.modal-btn.secondary:hover {
  background: #e8e8e8;
}

/* 底部导航栏 */
.bottom-nav {
  display: flex;
  background: white;
  border-top: 1px solid #e8e8e8;
  padding: 8px 0;
  z-index: 100;
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

.nav-item:hover {
  color: #1890ff;
}
</style>
