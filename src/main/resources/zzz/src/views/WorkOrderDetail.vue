<!-- src/views/WorkOrderDetail.vue -->
<template>
  <div class="work-order-detail">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">{{ pageTitle }}</div>
      <div class="header-right"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="detail-container" v-if="currentOrder">
        <!-- 工单信息 -->
        <div class="detail-section">
          <div class="section-title">工单信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <div class="item-label">工单ID</div>
              <div class="item-value">{{ currentOrder.orderId }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">设备ID</div>
              <div class="item-value">{{ currentOrder.deviceId }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">告警项目</div>
              <div class="item-value">{{ getOrderTypeName(currentOrder.orderType) }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">工单类型</div>
              <div class="item-value">{{ getOrderPriorityName(currentOrder.priority) }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">上报时间</div>
              <div class="item-value">{{ formatDate(currentOrder.createdTime) }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">安装位置</div>
              <div class="item-value">{{ getOrderLocation(currentOrder.deviceId) }}</div>
            </div>
          </div>
        </div>

        <!-- 告警信息 -->
        <div class="detail-section">
          <div class="section-title">告警信息</div>
          <div class="alert-content">
            <div class="alert-item">
              <div class="alert-label">告警内容</div>
              <div class="alert-value">{{ currentOrder.description }}</div>
            </div>
            <div class="alert-item">
              <div class="alert-label">处理建议</div>
              <div class="alert-value">请根据实际情况进行处理</div>
            </div>
          </div>
        </div>

        <!-- 维修项目 -->
        <div class="detail-section">
          <div class="section-title">维修项目</div>
          <div class="repair-content">
            <div class="repair-item">
              <div class="repair-label">报修项目</div>
              <div class="repair-value">{{ currentOrder.description }}</div>
            </div>

            <!-- 标准维护项目选择 -->
            <div class="form-group" v-if="showRepairForm">
              <div class="form-label">标准维护项目</div>
              <select v-model="selectedStandardItem" class="form-select">
                <option value="">点击以选择</option>
                <option value="更换传感器">更换传感器</option>
                <option value="更换滤芯1">更换滤芯1</option>
                <option value="更换滤芯2">更换滤芯2</option>
                <option value="更换滤芯3">更换滤芯3</option>
                <option value="更换滤芯4">更换滤芯4</option>
                <option value="更换D8滤芯1">更换D8滤芯1</option>
              </select>
            </div>

            <!-- 实际维修项目选择 -->
            <div class="form-group" v-if="showRepairForm">
              <div class="form-label">实际维修项目</div>
              <select v-model="selectedActualItem" class="form-select">
                <option value="">点击以选择</option>
                <option value="无">无</option>
                <option value="更换前置滤芯1">1.更换前置滤芯1 -材料费50￥+检修费60￥= 110￥</option>
                <option value="更换前置滤芯2">2.更换前置滤芯2 -材料费50￥+检修费60￥= 110￥</option>
                <option value="更换前置滤芯3">3.更换前置滤芯3 -材料费50￥+检修费60￥= 110￥</option>
                <option value="更换前置滤芯4">4.更换前置滤芯4 -材料费50￥+检修费60￥= 110￥</option>
                <option value="更换D8滤芯1">5.更换D8滤芯1 -材料费50￥+检修费60￥= 110￥</option>
              </select>
            </div>

            <!-- 现场照片 -->
            <div class="form-group" v-if="showRepairForm">
              <div class="form-label">现场照片</div>
              <div class="photo-upload" @click="uploadPhoto">
                <div class="upload-icon">📷</div>
                <div class="upload-text">点击上传照片</div>
              </div>
              <input
                type="file"
                ref="fileInput"
                @change="handleFileUpload"
                accept="image/*"
                style="display: none"
              />

              <!-- 照片预览 -->
              <div v-if="uploadedPhotos.length > 0" class="photo-preview">
                <div v-for="(photo, index) in uploadedPhotos" :key="index" class="preview-item">
                  <img :src="photo.url" :alt="`照片${index + 1}`" />
                  <button class="preview-delete" @click="removePhoto(index)">×</button>
                </div>
              </div>
            </div>

            <!-- 处理备注 -->
            <div class="form-group" v-if="showRepairForm">
              <div class="form-label">处理备注</div>
              <textarea
                v-model="repairNotes"
                class="form-textarea"
                placeholder="请描述处理过程和结果..."
                rows="3"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- 根据工单状态显示不同操作区域 -->
        <div v-if="currentOrder.status === 'pending' && !showRepairForm" class="action-buttons">
          <button class="action-btn grab" @click="grabOrder">
            抢单
          </button>
          <button class="action-btn reject" @click="showRejectModal = true">
            拒单
          </button>
        </div>

        <div v-if="currentOrder.status === 'processing' && !showRepairForm" class="action-buttons">
          <button class="action-btn process" @click="startRepair">
            开始维修
          </button>
          <button class="action-btn complete" @click="showCompleteConfirm = true">
            直接完成
          </button>
        </div>

        <div v-if="showRepairForm" class="action-buttons">
          <button class="action-btn submit" @click="submitRepair" :disabled="submitting">
            {{ submitting ? '提交中...' : '提交维修结果' }}
          </button>
          <button class="action-btn cancel" @click="cancelRepair">
            取消
          </button>
        </div>
      </div>

      <!-- 拒单弹窗 -->
      <div v-if="showRejectModal" class="confirm-modal">
        <div class="modal-content">
          <div class="modal-header">
            <div class="modal-title">拒单</div>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label">拒单原因</label>
              <textarea
                v-model="rejectReason"
                class="form-textarea"
                placeholder="请输入拒单原因..."
                rows="3"
              ></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel" @click="showRejectModal = false">取消</button>
            <button class="modal-btn confirm" @click="rejectOrder">确定</button>
          </div>
        </div>
      </div>

      <!-- 维修完成确认弹窗 -->
      <div v-if="showCompleteConfirm" class="confirm-modal">
        <div class="modal-content">
          <div class="modal-header">
            <div class="modal-title">确认完成</div>
          </div>
          <div class="modal-body">
            <div class="modal-message">确定直接完成此工单吗？此操作将跳过维修记录填写。</div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel" @click="showCompleteConfirm = false">取消</button>
            <button class="modal-btn confirm" @click="confirmComplete">确定</button>
          </div>
        </div>
      </div>

      <!-- 提交成功弹窗 -->
      <div v-if="showSuccessModal" class="success-modal">
        <div class="modal-content">
          <div class="modal-icon">✅</div>
          <div class="modal-title">提交成功</div>
          <div class="modal-message">维修结果已提交</div>
          <button class="modal-btn" @click="closeSuccessModal">
            确定
          </button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        加载中...
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goToHome">首页</div>
      <div class="nav-item" @click="goToInspection">巡检</div>
      <div class="nav-item active" @click="goToWorkOrders">工单</div>
      <div class="nav-item" @click="goToProfile">我的</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useDeviceStore } from '@/stores/device'
import { workOrderService } from '@/services/workOrderService'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const deviceStore = useDeviceStore()

// 状态
const loading = ref(false)
const currentOrder = ref(null)
const showRepairForm = ref(false)
const submitting = ref(false)
const showSuccessModal = ref(false)
const showCompleteConfirm = ref(false)
const showRejectModal = ref(false)

// 表单数据
const selectedStandardItem = ref('')
const selectedActualItem = ref('')
const repairNotes = ref('')
const rejectReason = ref('')
const uploadedPhotos = ref([])
const fileInput = ref(null)

// 返回上一页
const goBack = () => {
  router.back()
}

// 导航函数
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

// 抢单
const grabOrder = async () => {
  try {
    const repairmanId = authStore.getRepairmanId
    await workOrderService.grabOrder(currentOrder.value.orderId, repairmanId)

    // 更新本地状态
    currentOrder.value.status = 'processing'
    showRepairForm.value = true

    alert('抢单成功')
  } catch (error) {
    console.error('抢单失败:', error)
    alert('抢单失败: ' + (error.message || '未知错误'))
  }
}

// 拒单
const rejectOrder = async () => {
  if (!rejectReason.value.trim()) {
    alert('请输入拒单原因')
    return
  }

  try {
    const repairmanId = authStore.getRepairmanId
    await workOrderService.rejectOrder(
      currentOrder.value.orderId,
      repairmanId,
      rejectReason.value
    )

    showRejectModal.value = false
    alert('拒单成功')
    router.push('/work-orders')
  } catch (error) {
    console.error('拒单失败:', error)
    alert('拒单失败: ' + (error.message || '未知错误'))
  }
}

// 开始维修
const startRepair = () => {
  showRepairForm.value = true
}

// 直接完成工单
const confirmComplete = async () => {
  try {
    const repairmanId = authStore.getRepairmanId
    await workOrderService.submitRepairResult(
      currentOrder.value.orderId,
      repairmanId,
      '直接完成工单'
    )

    showCompleteConfirm.value = false
    alert('工单已完成')
    router.push('/work-orders')
  } catch (error) {
    console.error('完成失败:', error)
    alert('操作失败: ' + (error.message || '未知错误'))
  }
}

// 取消维修
const cancelRepair = () => {
  if (confirm('确定要取消维修吗？未保存的内容将丢失')) {
    showRepairForm.value = false
    resetForm()
  }
}

// 提交维修结果
const submitRepair = async () => {
  if (!selectedActualItem.value) {
    alert('请选择实际维修项目')
    return
  }

  if (!repairNotes.value.trim()) {
    alert('请填写处理备注')
    return
  }

  submitting.value = true

  try {
    const repairmanId = authStore.getRepairmanId
    // 注意：这里简化了图片上传逻辑，实际应用中需要上传图片并获取URL
    await workOrderService.submitRepairResult(
      currentOrder.value.orderId,
      repairmanId,
      repairNotes.value,
      null // 图片URL，实际应用中需要处理
    )

    // 显示成功弹窗
    showSuccessModal.value = true
  } catch (error) {
    console.error('提交失败:', error)
    alert('提交失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 关闭成功弹窗
const closeSuccessModal = () => {
  showSuccessModal.value = false
  router.push('/work-orders')
}

// 照片上传
const uploadPhoto = () => {
  fileInput.value.click()
}

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedPhotos.value.push({
        file: file,
        url: e.target.result,
        name: file.name
      })
    }
    reader.readAsDataURL(file)

    // 重置文件输入
    event.target.value = ''
  }
}

const removePhoto = (index) => {
  uploadedPhotos.value.splice(index, 1)
}

// 重置表单
const resetForm = () => {
  selectedStandardItem.value = ''
  selectedActualItem.value = ''
  repairNotes.value = ''
  uploadedPhotos.value = []
}

// 根据工单状态显示标题
const pageTitle = computed(() => {
  if (!currentOrder.value) return '工单详情'
  return currentOrder.value.status === 'processing' ? '处理工单' : '工单详情'
})

// 获取订单类型名称
const getOrderTypeName = (type) => {
  const types = {
    repair: '维修',
    maintenance: '保养',
    inspection: '巡检'
  }
  return types[type] || type
}

// 获取优先级名称
const getOrderPriorityName = (priority) => {
  const priorities = {
    low: '低',
    medium: '中',
    high: '高',
    urgent: '紧急'
  }
  return priorities[priority] || priority
}

// 获取订单位置信息
const getOrderLocation = (deviceId) => {
  const device = deviceStore.getWaterSupplierById(deviceId)
  return device ? device.location : '未知位置'
}

// 格式化日期显示
const formatDate = (dateStr) => {
  if (!dateStr) return '未知时间'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 加载工单详情
const loadOrderDetail = async (orderId) => {
  loading.value = true
  try {
    // 在实际应用中，这里应该调用获取单个工单详情的API
    // 目前我们模拟从工单列表中找到对应工单
    const myOrdersRes = await workOrderService.getMyOrders(authStore.getRepairmanId)
    const order = myOrdersRes.data.find(o => o.orderId === orderId)

    if (order) {
      currentOrder.value = order
    } else {
      // 如果在我的工单中找不到，则尝试从可抢工单中查找
      const availableRes = await workOrderService.getAvailableOrders('A') // 这里应该使用实际区域
      const availableOrder = availableRes.data.find(o => o.orderId === orderId)
      if (availableOrder) {
        currentOrder.value = availableOrder
      } else {
        throw new Error('工单不存在')
      }
    }
  } catch (error) {
    console.error('加载工单详情失败:', error)
    alert('加载工单详情失败: ' + (error.message || '未知错误'))
    router.push('/work-orders')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const orderId = route.params.id
  if (orderId) {
    loadOrderDetail(orderId)
  } else {
    router.push('/work-orders')
  }
})
</script>


<style scoped>
.work-order-detail {
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
  position: relative;
}

.detail-container {
  margin-bottom: 20px;
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

/* 工单信息网格 */
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
  min-width: 80px;
}

.item-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  text-align: right;
  flex: 1;
}

/* 告警信息 */
.alert-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.alert-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.alert-label {
  font-size: 14px;
  color: #666;
}

.alert-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
}

/* 维修项目 */
.repair-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.repair-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.repair-label {
  font-size: 14px;
  color: #666;
  min-width: 80px;
}

.repair-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  text-align: right;
  flex: 1;
}

/* 表单组 */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-select {
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.form-select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  min-height: 80px;
  line-height: 1.5;
}

.form-textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.form-textarea::placeholder {
  color: #999;
}

/* 照片上传 */
.photo-upload {
  padding: 40px 20px;
  border: 2px dashed #e0e0e0;
  border-radius: 4px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.photo-upload:hover {
  border-color: #1890ff;
  background: #f8fbff;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
  opacity: 0.6;
}

.upload-text {
  font-size: 14px;
  color: #666;
}

.photo-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.preview-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-delete {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.preview-delete:hover {
  background: rgba(0, 0, 0, 0.9);
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.action-btn {
  flex: 1;
  padding: 14px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.grab {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
}

.action-btn.grab:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.action-btn.reject {
  background: #ff4d4f;
  color: white;
}

.action-btn.reject:hover {
  background: #ff7875;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
}

.action-btn.process {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: white;
}

.action-btn.process:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.action-btn.complete {
  background: linear-gradient(135deg, #13c2c2 0%, #08979c 100%);
  color: white;
}

.action-btn.complete:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(19, 194, 194, 0.3);
}

.action-btn.submit {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: white;
}

.action-btn.submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.action-btn.submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.action-btn.cancel {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #e0e0e0;
}

.action-btn.cancel:hover {
  background: #e8e8e8;
  transform: translateY(-2px);
}

/* 确认弹窗 */
.confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.confirm-modal .modal-content {
  background: white;
  border-radius: 12px;
  width: 300px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.confirm-modal .modal-header {
  padding: 20px 24px 12px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
}

.confirm-modal .modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.confirm-modal .modal-body {
  padding: 20px 24px;
}

.confirm-modal .modal-message {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.confirm-modal .modal-footer {
  display: flex;
  border-top: 1px solid #f0f0f0;
}

.confirm-modal .modal-btn {
  flex: 1;
  padding: 14px;
  border: none;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.confirm-modal .modal-btn.cancel {
  background: #f5f5f5;
  color: #666;
  border-right: 1px solid #f0f0f0;
}

.confirm-modal .modal-btn.cancel:hover {
  background: #e8e8e8;
}

.confirm-modal .modal-btn.confirm {
  background: #1890ff;
  color: white;
}

.confirm-modal .modal-btn.confirm:hover {
  background: #096dd9;
}

/* 成功弹窗 */
.success-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.success-modal .modal-content {
  background: white;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  width: 280px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.success-modal .modal-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.success-modal .modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.success-modal .modal-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
}

.success-modal .modal-btn {
  width: 100%;
  padding: 12px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.success-modal .modal-btn:hover {
  background: #096dd9;
  transform: translateY(-1px);
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

/* 添加加载状态样式 */
.loading {
  text-align: center;
  padding: 20px;
  color: #1890ff;
}
</style>
