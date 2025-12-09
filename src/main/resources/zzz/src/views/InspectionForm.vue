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
      <div class="form-container">
        <!-- 设备状态 -->
        <div class="form-section">
          <div class="section-title">设备状态</div>
          <div class="status-selector">
            <div
              class="status-option"
              :class="{ 'active': selectedStatus === 'normal' }"
              @click="selectStatus('normal')"
            >
              正常
            </div>
            <div
              class="status-option"
              :class="{ 'active': selectedStatus === 'warning' }"
              @click="selectStatus('warning')"
            >
              异常
            </div>
          </div>
        </div>

        <!-- 异常描述 -->
        <div v-if="selectedStatus === 'warning'" class="form-section">
          <div class="section-title">异常描述</div>
          <textarea
            v-model="abnormalDescription"
            class="abnormal-textarea"
            placeholder="请描述处理异常..."
            rows="4"
          ></textarea>
        </div>

        <!-- 现场照片 -->
        <div class="form-section">
          <div class="section-title">现场照片</div>
          <div class="photo-upload">
            <div class="upload-area" @click="uploadPhoto">
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
                <div class="preview-image">
                  <img :src="photo.url" :alt="`照片${index + 1}`" />
                </div>
                <div class="preview-actions">
                  <button class="preview-btn delete" @click="removePhoto(index)">
                    删除
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <button class="submit-btn" @click="submitInspection" :disabled="submitting">
        {{ submitting ? '提交中...' : '提交巡检' }}
      </button>
    </div>

    <!-- 提交成功弹窗 -->
    <div v-if="showSuccessModal" class="success-modal">
      <div class="modal-content">
        <div class="modal-icon">✅</div>
        <div class="modal-title">提交成功</div>
        <button class="modal-btn" @click="closeSuccessModal">
          返回
        </button>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 设备状态
const selectedStatus = ref('normal')
const abnormalDescription = ref('')

// 照片上传
const uploadedPhotos = ref([])
const fileInput = ref(null)

// 提交状态
const submitting = ref(false)
const showSuccessModal = ref(false)

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

// 选择设备状态
const selectStatus = (status) => {
  selectedStatus.value = status
  if (status === 'normal') {
    abnormalDescription.value = ''
  }
}

// 上传照片
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

// 提交巡检
const submitInspection = async () => {
  if (selectedStatus.value === 'warning' && !abnormalDescription.value.trim()) {
    alert('请填写异常描述')
    return
  }

  submitting.value = true

  // 模拟API调用
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))

    console.log('提交巡检数据:', {
      status: selectedStatus.value,
      description: abnormalDescription.value,
      photos: uploadedPhotos.value.length
    })

    // 显示成功弹窗
    showSuccessModal.value = true

  } catch (error) {
    console.error('提交失败:', error)
    alert('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 关闭成功弹窗
const closeSuccessModal = () => {
  showSuccessModal.value = false
  // 返回巡检页面
  router.push('/inspection')
}

onMounted(() => {
  // 初始化时可以选择默认设备
  console.log('巡检表单页面加载')
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
}

/* 状态选择器 */
.status-selector {
  display: flex;
  gap: 12px;
}

.status-option {
  flex: 1;
  padding: 12px;
  text-align: center;
  border: 2px solid #f0f0f0;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.status-option:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.status-option.active {
  border-color: #1890ff;
  background: #f0f7ff;
  color: #1890ff;
}

/* 异常描述文本框 */
.abnormal-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  min-height: 80px;
}

.abnormal-textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.abnormal-textarea::placeholder {
  color: #999;
}

/* 照片上传区域 */
.photo-upload {
  width: 100%;
}

.upload-area {
  width: 100%;
  padding: 40px 20px;
  border: 2px dashed #e0e0e0;
  border-radius: 6px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 16px;
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
}

/* 照片预览 */
.photo-preview {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.preview-item {
  background: #f8f9fa;
  border-radius: 6px;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 120px;
  overflow: hidden;
}

.preview-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-actions {
  padding: 8px;
  text-align: center;
}

.preview-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.preview-btn.delete {
  background: #ff4d4f;
  color: white;
}

.preview-btn.delete:hover {
  background: #ff7875;
}

/* 提交按钮 */
.submit-btn {
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

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.3);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
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

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  width: 280px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.modal-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 24px;
}

.modal-btn {
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

.modal-btn:hover {
  background: #096dd9;
  transform: translateY(-1px);
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
