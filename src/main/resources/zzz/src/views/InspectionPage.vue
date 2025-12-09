<template>
  <div class="inspection-page">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">设备巡检</div>
      <div class="header-right">
        <span class="my-area-btn">我的片区</span>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 片区列表 -->
      <div class="area-list">
        <!-- 岳麓片区 -->
        <div class="area-card">
          <div class="area-info">
            <div class="area-name">岳麓片区</div>
            <div class="area-details">5所学校 · 12台制水机</div>
          </div>
          <div class="area-divider"></div>
          <button class="view-btn" @click="viewYueluArea">查看</button>
        </div>

        <!-- 雨花片区 -->
        <div class="area-card">
          <div class="area-info">
            <div class="area-name">雨花片区</div>
            <div class="area-details">3所学校 · 8台制水机</div>
          </div>
          <div class="area-divider"></div>
          <button class="view-btn" @click="viewYuhuaArea">查看</button>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goToHome">
        <span>首页</span>
      </div>
      <div class="nav-item active">
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
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}

const goToHome = () => {
  router.push('/home')
}

const goToWorkOrders = () => {
  console.log('跳转到工单页面')
  router.push('/work-orders')
}

const goToProfile = () => {
  console.log('跳转到我的页面')
  router.push('/profile')
}

const viewYueluArea = () => {
  console.log('查看岳麓片区详情')
  // 跳转到扫码页面
  router.push('/inspection/scan')
}

const viewYuhuaArea = () => {
  console.log('查看雨花片区详情')
  // 跳转到扫码页面
  router.push('/inspection/scan')
}
</script>

<style scoped>
.inspection-page {
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

.header-left,
.header-right {
  width: 80px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
  flex: 1;
}

.back-btn,
.my-area-btn {
  font-size: 14px;
  color: #1890ff;
  cursor: pointer;
  transition: color 0.3s;
}

.back-btn:hover,
.my-area-btn:hover {
  color: #096dd9;
}

.back-btn {
  text-align: left;
}

.my-area-btn {
  text-align: right;
  display: block;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  padding: 20px 16px;
  overflow-y: auto;
}

/* 片区列表 */
.area-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.area-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  padding: 16px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.area-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.area-info {
  flex: 1;
}

.area-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.area-details {
  font-size: 13px;
  color: #666;
}

.area-divider {
  width: 1px;
  height: 40px;
  background: #e8e8e8;
  margin: 0 16px;
}

.view-btn {
  padding: 8px 24px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.view-btn:hover {
  background: #096dd9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
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
