<!-- src/views/workorder/CompletedDetail.vue -->
<template>
  <div class="completed-detail-page">
    <!-- 页面标题和面包屑 -->
    <div class="page-header">
      <div>
        <h2>结单信息</h2>
        <div class="breadcrumb">矿化水平台 / 工单管理 / 已结单 / 工单信息</div>
      </div>
      <button class="btn-back" @click="handleBack">返回</button>
    </div>

    <!-- 工单基本信息卡片 -->
    <div class="card main-card">
      <div class="card-header">
        <h3>工单基本信息</h3>
      </div>
      <div class="card-body">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">工单号：</span>
            <span class="info-value">{{ orderDetail.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">工单状态：</span>
            <span class="info-value status-tag completed">{{ formatStatus(orderDetail.status) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">维修人员：</span>
            <span class="info-value">{{ orderDetail.repairman }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">联系电话：</span>
            <span class="info-value">{{ orderDetail.phone }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">接单时间：</span>
            <span class="info-value">{{ orderDetail.acceptTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">设备ID：</span>
            <span class="info-value">{{ orderDetail.deviceId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">警告项目：</span>
            <span class="info-value">{{ orderDetail.warningItem }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">设备位置：</span>
            <span class="info-value">{{ orderDetail.location }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 处理详情 -->
    <div class="card">
      <div class="card-header">
        <h3>处理详情</h3>
      </div>
      <div class="card-body">
        <div class="detail-item">
          <span class="detail-label">处理备注：</span>
          <div class="detail-content">{{ orderDetail.processRemark }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">实际处理：</span>
          <div class="detail-content">{{ orderDetail.actualProcess }}</div>
        </div>
      </div>
    </div>

    <!-- 现场照片 -->
    <div class="card">
      <div class="card-header">
        <h3>现场照片</h3>
      </div>
      <div class="card-body">
        <div class="photos-container">
          <div class="photo-item" v-for="(photo, index) in orderDetail.photos" :key="index">
            <img :src="photo" :alt="`现场照片 ${index + 1}`" class="现场照片" @click="previewPhoto(photo)">
          </div>
          <div v-if="orderDetail.photos.length === 0" class="no-photos">暂无现场照片</div>
        </div>
      </div>
    </div>

    <!-- 结单信息 -->
    <div class="card">
      <div class="card-header">
        <h3>结单信息</h3>
      </div>
      <div class="card-body">
        <div class="detail-item total-cost">
          <span class="detail-label">结单总费用：</span>
          <div class="detail-content cost-value">{{ orderDetail.totalCost }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">审核意见：</span>
          <div class="detail-content">{{ orderDetail.reviewOpinion || '无审核意见' }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">结单时间：</span>
          <div class="detail-content">{{ orderDetail.completeTime }}</div>
        </div>
      </div>
    </div>

    <!-- 照片预览弹窗 -->
    <div v-if="previewImage" class="photo-preview-overlay" @click="previewImage = ''">
      <div class="photo-preview-container" @click.stop>
        <img :src="previewImage" alt="照片预览" class="preview-img">
        <button class="preview-close" @click="previewImage = ''">×</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

// 工单状态类型定义
type OrderStatus = 'timeout' | 'pending' | 'processing' | 'reviewing' | 'completed'

// 结单工单详情接口定义
interface OrderDetail {
  id: string
  orderNo: string
  status: OrderStatus
  repairman: string
  phone: string
  acceptTime: string
  completeTime: string
  deviceId: string
  warningItem: string
  location: string
  processRemark: string
  actualProcess: string
  totalCost: string
  reviewOpinion?: string
  photos: string[]
}

// 响应式数据
const orderDetail = ref<OrderDetail>({
  id: '',
  orderNo: '',
  status: 'completed',
  repairman: '',
  phone: '',
  acceptTime: '',
  completeTime: '',
  deviceId: '',
  warningItem: '',
  location: '',
  processRemark: '',
  actualProcess: '',
  totalCost: '',
  photos: []
})
const previewImage = ref('')

// 路由相关
const route = useRoute()
const router = useRouter()

// 格式化状态显示文本
const formatStatus = (status: OrderStatus): string => {
  const statusMap: Record<OrderStatus, string> = {
    timeout: '超时未抢',
    pending: '待抢单',
    processing: '处理中',
    reviewing: '待审核',
    completed: '已结单'
  }
  return statusMap[status]
}

// 获取工单详情数据
const fetchOrderDetail = async (id: string) => {
  try {
    // 实际项目中这里会调用真实API获取数据
    // 示例：const response = await getCompletedOrderDetailApi(id)
    // orderDetail.value = response.data

    // 模拟API数据
    const mockData: Record<string, OrderDetail> = {
      '11': {
        id: '11',
        orderNo: '1O05',
        status: 'completed',
        repairman: '王五',
        phone: '123456789',
        acceptTime: '2025/10/20-16:21',
        completeTime: '2025/10/20-18:45',
        deviceId: '供水机#A11',
        warningItem: '更换滤芯',
        location: 'A区图书馆',
        processRemark: '原滤芯老旧已损，更换了新滤芯',
        actualProcess: '更换滤芯',
        totalCost: '110元',
        reviewOpinion: '审核通过，已结单',
        photos: [
          'https://picsum.photos/seed/filter1/400/300',
          'https://picsum.photos/seed/filter2/400/300'
        ]
      },
      '12': {
        id: '12',
        orderNo: '1O06',
        status: 'completed',
        repairman: '赵六',
        phone: '987654321',
        acceptTime: '2025/10/21-09:15',
        completeTime: '2025/10/21-11:30',
        deviceId: '制水机#B07',
        warningItem: '水泵故障',
        location: 'B区教学楼',
        processRemark: '水泵已修复，设备运行正常',
        actualProcess: '维修水泵，更换损坏零件',
        totalCost: '260元',
        reviewOpinion: '维修合格，同意结单',
        photos: [
          'https://picsum.photos/seed/pump1/400/300',
          'https://picsum.photos/seed/pump2/400/300'
        ]
      }
    }
    
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 300))
    
    if (mockData[id]) {
      orderDetail.value = mockData[id]
    } else {
      throw new Error('未找到工单数据')
    }
  } catch (error) {
    console.error('获取工单详情失败:', error)
    alert('获取工单详情详情失败，请重试')
    router.push('/home/work-order/completed')
  }
}

// 预览照片
const previewPhoto = (photo: string) => {
  previewImage.value = photo
}

// 返回上一页
const handleBack = () => {
  router.go(-1)
}

// 页面加载时获取工单详情
onMounted(() => {
  const id = route.params.id as string
  if (id) {
    fetchOrderDetail(id)
  } else {
    router.push('/home/work-order/completed')
  }
})
</script>

<style scoped>
.completed-detail-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  box-sizing: border-box;
}

.page-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.breadcrumb {
  color: #666;
  font-size: 14px;
}

.btn-back {
  padding: 8px 16px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-back:hover {
  background-color: #f5f5f5;
}

.card {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.card-body {
  padding: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
}

.info-label, .detail-label {
  color: #666;
  width: 100px;
  flex-shrink: 0;
  font-weight: 500;
}

.info-value, .detail-content {
  flex-grow: 1;
  color: #333;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
}

.status-tag.completed {
  background-color: #52c41a;
}

.detail-item {
  margin-bottom: 16px;
  display: flex;
}

.detail-content {
  line-height: 1.5;
}

.photos-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 10px;
}

.photo-item {
  width: 160px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.photo-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.photo-item img:hover {
  transform: scale(1.05);
}

.no-photos {
  color: #999;
  padding: 20px 0;
}

.total-cost .cost-value {
  color: #f5222d;
  font-weight: 600;
  font-size: 16px;
}

/* 照片预览样式 */
.photo-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.photo-preview-container {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.preview-img {
  max-width: 100%;
  max-height: 80vh;
  border: 4px solid white;
  border-radius: 4px;
}

.preview-close {
  position: absolute;
  top: -30px;
  right: -30px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>