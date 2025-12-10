<template>
  <div class="work-order-list">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">工单管理</div>
      <div class="header-right"></div>
    </div>

    <!-- 标签切换 -->
    <div class="tab-container">
      <div class="tabs">
        <div
          class="tab"
          :class="{ 'active': activeTab === 'pending' }"
          @click="switchTab('pending')"
        >
          待抢单
        </div>
        <div
          class="tab"
          :class="{ 'active': activeTab === 'processing' }"
          @click="switchTab('processing')"
        >
          处理中
        </div>
        <div
          class="tab"
          :class="{ 'active': activeTab === 'history' }"
          @click="switchTab('history')"
        >
          历史
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 待抢单工单列表 -->
      <div v-if="activeTab === 'pending'" class="order-list">
        <div
          class="order-item"
          v-for="order in availableOrders"
          :key="order.orderId"
          @click="viewOrderDetail(order.orderId)"
        >
          <div class="order-content">
            <div class="order-title">{{ order.description }}</div>
            <div class="order-info">
              <span class="order-location">{{ getOrderLocation(order.deviceId) }}</span>
              <span class="order-time">{{ formatTime(order.createdTime) }}</span>
            </div>
          </div>
          <button class="order-btn grab" @click.stop="grabOrder(order.orderId)">
            抢单
          </button>
        </div>

        <div v-if="availableOrders.length === 0" class="empty-state">
          暂无待抢单工单
        </div>
      </div>

      <!-- 处理中工单列表 -->
      <div v-if="activeTab === 'processing'" class="order-list">
        <div
          class="order-item"
          v-for="order in processingOrders"
          :key="order.orderId"
          @click="viewOrderDetail(order.orderId)"
        >
          <div class="order-content">
            <div class="order-title">{{ order.description }}</div>
            <div class="order-info">
              <span class="order-location">{{ getOrderLocation(order.deviceId) }}</span>
              <span class="order-time">抢单时间: {{ formatTime(order.grabbedTime) }}</span>
            </div>
            <div class="order-status">
              <span class="status-badge processing">处理中</span>
            </div>
          </div>
          <button class="order-btn process" @click.stop="startProcessOrder(order.orderId)">
            继续处理
          </button>
        </div>

        <div v-if="processingOrders.length === 0" class="empty-state">
          暂无处理中的工单
        </div>
      </div>

      <!-- 历史工单列表 -->
      <div v-if="activeTab === 'history'" class="order-list">
        <div
          class="order-item"
          v-for="order in completedOrders"
          :key="order.orderId"
        >
          <div class="order-content">
            <div class="order-title">{{ order.description }}</div>
            <div class="order-info">
              <span class="order-location">{{ getOrderLocation(order.deviceId) }}</span>
              <span class="order-time">{{ formatDate(order.completedTime) }}</span>
            </div>
          </div>
          <button class="order-btn completed">
            已完成
          </button>
        </div>

        <div v-if="completedOrders.length === 0" class="empty-state">
          暂无历史工单
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        加载中...
      </div>

      <!-- 抢单成功弹窗 -->
      <div v-if="showGrabSuccess" class="success-modal">
        <div class="modal-content">
          <div class="modal-header">
            <div class="modal-close" @click="closeGrabSuccess">×</div>
          </div>
          <div class="modal-body">
            <div class="modal-icon">✅</div>
            <div class="modal-title">抢单成功！</div>
          </div>
        </div>
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

<script setup>import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { workOrderService } from '@/services/workOrderService'
import { useDeviceStore } from '@/stores/device'

const router = useRouter()
const authStore = useAuthStore()
const deviceStore = useDeviceStore()

const activeTab = ref('pending')
const showGrabSuccess = ref(false)
const loading = ref(false)

// 工单数据
const allOrders = ref([])
const availableOrders = ref([])
const processingOrders = ref([])
const completedOrders = ref([])

// 切换标签页
const switchTab = (tab) => {
  activeTab.value = tab
}

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
  // 如果已经在工单页面，则刷新
  if (router.currentRoute.value.path === '/work-orders') {
    loadOrders()
  } else {
    router.push('/work-orders')
  }
}

const goToProfile = () => {
  router.push('/profile')
}

// 查看工单详情
const viewOrderDetail = (orderId) => {
  router.push(`/work-orders/${orderId}`)
}

// 抢单
const grabOrder = async (orderId) => {
  try {
    const repairmanId = authStore.getRepairmanId
    await workOrderService.grabOrder(orderId, repairmanId)

    // 显示成功提示
    showGrabSuccess.value = true

    // 重新加载数据
    setTimeout(async () => {
      showGrabSuccess.value = false
      await loadOrders()
      switchTab('processing')
    }, 2000)
  } catch (error) {
    console.error('抢单失败:', error)
    alert('抢单失败: ' + (error.message || '未知错误'))
  }
}

// 开始处理工单
const startProcessOrder = (orderId) => {
  router.push({
    path: `/work-orders/${orderId}`,
    query: { status: 'processing' }
  })
}

// 关闭抢单成功弹窗
const closeGrabSuccess = () => {
  showGrabSuccess.value = false
}

// 获取订单位置信息
const getOrderLocation = (deviceId) => {
  const device = deviceStore.getWaterSupplierById(deviceId)
  return device ? device.location : '未知位置'
}

// 格式化时间显示
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'

  const date = new Date(timeStr)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMins / 60)
  const diffDays = Math.floor(diffHours / 24)

  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  return `${diffDays}天前`
}

// 格式化日期显示
const formatDate = (dateStr) => {
  if (!dateStr) return '未知日期'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

// 加载工单数据
const loadOrders = async () => {
  loading.value = true
  try {
    // 获取我的工单
    const myOrders = await workOrderService.getMyOrders(authStore.getRepairmanId)

    // 分类工单
    allOrders.value = myOrders.data || []
    processingOrders.value = allOrders.value.filter(order =>
      order.status === 'processing'
    )
    completedOrders.value = allOrders.value.filter(order =>
      order.status === 'completed'
    )

    // 获取可抢工单
    // 注意：这里需要根据维修人员所在区域获取，暂时使用默认值
    const available = await workOrderService.getAvailableOrders('A')
    availableOrders.value = available.data || []
  } catch (error) {
    console.error('加载工单失败:', error)
    alert('加载工单失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 初始化时加载数据
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.work-order-list {
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

/* 添加空状态样式 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

/* 添加加载状态样式 */
.loading {
  text-align: center;
  padding: 20px;
  color: #1890ff;
}

/* 标签切换 */
.tab-container {
  background: white;
  padding: 0 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.tabs {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab:hover {
  color: #1890ff;
}

.tab.active {
  color: #1890ff;
  font-weight: 600;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #1890ff;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  position: relative;
}

/* 工单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.order-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.order-content {
  flex: 1;
}

.order-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.order-info {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #666;
}

.order-location {
  position: relative;
  padding-right: 12px;
}

.order-location::after {
  content: '•';
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.order-time {
  color: #999;
}

/* 工单状态 */
.order-status {
  margin-top: 8px;
}

.status-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 500;
  border-radius: 4px;
}

.status-badge.processing {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.status-badge.pending {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

/* 工单按钮 */
.order-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-left: 8px;
  min-width: 60px;
}

.order-btn.grab {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: white;
}

.order-btn.grab:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.order-btn.process {
  background: #52c41a;
  color: white;
}

.order-btn.process:hover {
  background: #73d13d;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

.order-btn.completed {
  background: #f0f0f0;
  color: #666;
  cursor: default;
}

/* 抢单成功弹窗 */
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
  width: 280px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.modal-header {
  padding: 8px;
  display: flex;
  justify-content: flex-end;
}

.modal-close {
  font-size: 20px;
  color: #999;
  cursor: pointer;
  padding: 4px 12px;
  transition: color 0.3s;
}

.modal-close:hover {
  color: #333;
}

.modal-body {
  padding: 32px 24px;
  text-align: center;
}

.modal-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
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
