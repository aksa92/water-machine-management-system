<template>
  <div class="inspection-scan">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">设备巡检</div>
      <div class="header-right"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="scan-container">
        <!-- 扫码框 -->
        <div class="scan-box">
          <div class="scan-icon">📷</div>
          <div class="scan-border">
            <div class="scan-line"></div>
          </div>
        </div>

        <div class="scan-text">请扫描设备上二维码以开始巡检</div>

        <button class="scan-complete-btn" @click="simulateScanComplete">
          扫描完成
        </button>

        <div class="scan-tips">
          <p>💡 提示：将二维码放入框内自动扫描</p>
          <p>或点击"扫描完成"模拟测试</p>
        </div>
      </div>
    </div>

    <!-- 底部导航栏 -->
    <div class="bottom-nav">
      <div class="nav-item" @click="goToHome">首页</div>
      <div class="nav-item active" @click="goToInspection">巡检</div>
      <div class="nav-item" @click="goToWorkOrders">工单</div>
      <div class="nav-item" @click="goToProfile">我的</div>
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

const goToInspection = () => {
  router.push('/inspection')
}

const goToWorkOrders = () => {
  router.push('/work-orders')
}

const goToProfile = () => {
  router.push('/profile')
}

// 模拟扫码完成
const simulateScanComplete = () => {
  console.log('模拟扫码完成，跳转到制水机列表')
  router.push('/inspection/water-maker')
}
</script>

<style scoped>
.inspection-scan {
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
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 扫码容器 */
.scan-container {
  width: 100%;
  max-width: 320px;
  background: white;
  border-radius: 12px;
  padding: 30px 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  text-align: center;
}

/* 扫码框 */
.scan-box {
  position: relative;
  width: 200px;
  height: 200px;
  margin: 0 auto 30px;
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
}

.scan-icon {
  font-size: 60px;
  margin: 40px 0 20px;
  opacity: 0.7;
}

.scan-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2px dashed #1890ff;
  border-radius: 8px;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 10px;
  right: 10px;
  height: 2px;
  background: #1890ff;
  animation: scanAnimation 2s linear infinite;
}

@keyframes scanAnimation {
  0% {
    top: 0;
  }
  100% {
    top: 100%;
  }
}

/* 扫码文本 */
.scan-text {
  font-size: 16px;
  color: #333;
  margin-bottom: 30px;
  font-weight: 500;
}

/* 扫码完成按钮 */
.scan-complete-btn {
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
  margin-bottom: 20px;
}

.scan-complete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.3);
}

/* 扫码提示 */
.scan-tips {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.scan-tips p {
  margin: 5px 0;
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
