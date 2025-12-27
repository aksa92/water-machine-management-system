<!-- src/views/NotificationsPage.vue -->
<template>
  <div class="notifications-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">消息通知</div>
      <div class="header-right">
        <span class="mark-all-btn" @click="markAllAsRead">全部已读</span>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <div>加载中...</div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="notifications.length === 0" class="empty-state">
        <div class="empty-icon">🔔</div>
        <div class="empty-text">暂无通知</div>
      </div>

      <!-- 通知列表 -->
      <div v-else class="notification-list">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          :class="['notification-item', { 'unread': !getNotificationReadStatus(notification) }]"
          @click="viewNotification(notification)"
        >
          <div class="notification-content">
            <div class="notification-header">
              <div class="notification-title">{{ getNotificationTitle(notification) }}</div>
              <div class="notification-time">{{ formatTime(notification.createdTime) }}</div>
            </div>
            <div class="notification-body">
              <div class="notification-text">{{ notification.content }}</div>
              <div class="notification-status">
                <span v-if="!getNotificationReadStatus(notification)" class="unread-dot"></span>
                <span class="status-text">{{ getNotificationReadStatus(notification) ? '已读' : '未读' }}</span>
              </div>
            </div>
          </div>
          <div class="notification-actions" v-if="!getNotificationReadStatus(notification)">
            <button class="mark-read-btn" @click.stop="markAsRead(notification.id)">
              标记已读
            </button>
          </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { notificationService } from '@/services/notificationService'

const router = useRouter()
const authStore = useAuthStore()

// 通知数据
const notifications = ref([])
const loading = ref(true)

// 获取通知列表
// 修改 loadNotifications 函数，正确处理字段映射
const loadNotifications = async () => {
  try {
    const repairmanId = authStore.getRepairmanId
    if (!repairmanId) {
      console.error('未获取到维修人员ID')
      return
    }

    const response = await notificationService.getAllNotifications(repairmanId)
    if (response.code === 200) {
      notifications.value = response.data.map(notification => ({
        ...notification,
        // 重点：后端返回的字段可能是 'read' 而不是 'isRead'
        isRead: notification.read !== undefined ? notification.read : notification.isRead || false
      }))
    } else {
      console.error('获取通知失败:', response.message)
    }
  } catch (error) {
    console.error('获取通知失败:', error)
    alert('获取通知失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}


// 修正 markAsRead 函数，确保本地状态与后端同步
const markAsRead = async (notificationId) => {
  try {
    const response = await notificationService.markNotificationAsRead(notificationId);
    if (response.code === 200) {
      console.log('标记已读成功');

      // 重新加载通知列表以确保状态同步
      await loadNotifications();
    }
  } catch (error) {
    console.error('标记已读失败:', error);
  }
};

// 创建一个辅助函数来获取通知的已读状态
const getNotificationReadStatus = (notification) => {
  return notification.read !== undefined ? notification.read : notification.isRead || false;
};



// 修复 markAsReadAndRefresh 函数
const markAsReadAndRefresh = async (notificationId) => {
  try {
    await notificationService.markNotificationAsRead(notificationId);  // 修正函数名
    // 立即刷新通知列表以获取最新状态
    await loadNotifications();
  } catch (error) {
    console.error('操作失败:', error);
  }
};

// 确保 markAllAsRead 函数也使用正确的函数名
const markAllAsRead = async () => {
  try {
    const unreadNotifications = notifications.value.filter(n => !n.isRead)  // 使用正确的字段名
    if (unreadNotifications.length === 0) {
      alert('没有未读通知')
      return
    }

    for (const notification of unreadNotifications) {
      await notificationService.markNotificationAsRead(notification.id)
      notification.isRead = true  // 使用正确的字段名
    }

    // 重新加载通知列表以获取最新状态
    await loadNotifications();

    alert('已标记所有通知为已读')
  } catch (error) {
    console.error('标记所有已读失败:', error)
    alert('标记失败: ' + (error.message || '未知错误'))
  }
}



// 获取通知标题
const getNotificationTitle = (notification) => {
  switch (notification.type) {
    case 'ORDER_ASSIGNED':
      return '派单通知'
    case 'SYSTEM':
      return '系统通知'
    case 'MAINTENANCE':
      return '维护通知'
    default:
      return '通知'
  }
}

// 格式化时间
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

// 查看通知详情
const viewNotification = (notification) => {
  const isRead = getNotificationReadStatus(notification);

  // 如果是派单通知，跳转到工单详情
  if (notification.type === 'ORDER_ASSIGNED' && notification.orderId) {
    router.push(`/work-orders/${notification.orderId}`)
    // 同时标记为已读
    if (!isRead) {
      markAsRead(notification.id)
    }
  } else {
    // 标记为已读
    if (!isRead) {
      markAsRead(notification.id)
    }
    // 显示通知内容
    alert(notification.content)
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

// 页面加载时获取通知
onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notifications-page {
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
  text-align: right;
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

.mark-all-btn {
  font-size: 14px;
  color: #1890ff;
  cursor: pointer;
  transition: color 0.3s;
}

.mark-all-btn:hover {
  color: #096dd9;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
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

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #ccc;
}

.empty-text {
  font-size: 16px;
  color: #999;
}

/* 通知列表 */
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid #e8e8e8;
}

.notification-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.notification-item.unread {
  border-left-color: #1890ff;
  background: #f0f7ff;
}

.notification-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.notification-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  margin-left: 8px;
}

.notification-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.notification-text {
  flex: 1;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.notification-status {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
}

.status-text {
  font-size: 12px;
  color: #999;
}

.notification-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.mark-read-btn {
  padding: 6px 12px;
  background: #f0f7ff;
  color: #1890ff;
  border: 1px solid #1890ff;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.mark-read-btn:hover {
  background: #1890ff;
  color: white;
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

.nav-item:hover {
  color: #1890ff;
}
</style>
