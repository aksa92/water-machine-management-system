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
        <div class="order-item" @click="viewOrderDetail('1001')">
          <div class="order-content">
            <div class="order-title">制水机#A105 - TDS超标</div>
            <div class="order-info">
              <span class="order-location">A区教学楼</span>
              <span class="order-time">10分钟前</span>
            </div>
          </div>
          <button class="order-btn grab" @click.stop="grabOrder('1001')">
            抢单
          </button>
        </div>

        <div class="order-item" @click="viewOrderDetail('1002')">
          <div class="order-content">
            <div class="order-title">供水机#B105 - 水位异常</div>
            <div class="order-info">
              <span class="order-location">B区图书馆</span>
              <span class="order-time">25分钟前</span>
            </div>
          </div>
          <button class="order-btn grab" @click.stop="grabOrder('1002')">
            抢单
          </button>
        </div>
      </div>

      <!-- 处理中工单列表 -->
      <div v-if="activeTab === 'processing'" class="order-list">
        <div class="order-item" @click="viewOrderDetail('1002')">
          <div class="order-content">
            <div class="order-title">供水机#B105 - 水位异常</div>
            <div class="order-info">
              <span class="order-location">B区图书馆</span>
              <span class="order-time">抢单时间: 25分钟前</span>
            </div>
            <div class="order-status">
              <span class="status-badge processing">处理中</span>
            </div>
          </div>
          <button class="order-btn process" @click.stop="startProcessOrder('1002')">
            继续处理
          </button>
        </div>

        <div class="order-item" @click="viewOrderDetail('1003')">
          <div class="order-content">
            <div class="order-title">制水机#A102 - TDS超标</div>
            <div class="order-info">
              <span class="order-location">A区教学楼</span>
              <span class="order-time">抢单时间: 15分钟前</span>
            </div>
            <div class="order-status">
              <span class="status-badge pending">待处理</span>
            </div>
          </div>
          <button class="order-btn process" @click.stop="startProcessOrder('1003')">
            开始处理
          </button>
        </div>
      </div>

      <!-- 历史工单列表 -->
      <div v-if="activeTab === 'history'" class="order-list">
        <div class="order-item">
          <div class="order-content">
            <div class="order-title">供水机#B105 - 滤芯损坏</div>
            <div class="order-info">
              <span class="order-location">B区图书馆</span>
              <span class="order-time">2023-10-04</span>
            </div>
          </div>
          <button class="order-btn completed">
            已结款
          </button>
        </div>
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

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeTab = ref('pending')
const showGrabSuccess = ref(false)

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
    activeTab.value = 'pending'
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
const grabOrder = (orderId) => {
  console.log(`抢单 ${orderId}`)
  // 模拟抢单成功
  showGrabSuccess.value = true
  // 3秒后自动关闭弹窗
  setTimeout(() => {
    showGrabSuccess.value = false
    // 抢单成功后，该工单应该从待抢单列表移除，添加到处理中列表
    switchTab('processing')
  }, 3000)
}

// 开始处理工单
const startProcessOrder = (orderId) => {
  console.log(`开始处理工单 ${orderId}`)
  // 跳转到处理页面，传递工单ID和状态
  router.push({
    path: `/work-orders/${orderId}`,
    query: { status: 'processing' }
  })
}

// 关闭抢单成功弹窗
const closeGrabSuccess = () => {
  showGrabSuccess.value = false
}
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
