<template>
  <div class="water-maker-detail">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">制水机详情</div>
      <div class="header-right"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="device-detail-container">
        <!-- 设备信息 -->
        <div class="info-section">
          <div class="section-title">设备信息</div>
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">设备ID</div>
              <div class="info-value">制水机 #A102</div>
            </div>
            <div class="info-item">
              <div class="info-label">安装位置</div>
              <div class="info-value">A区教学楼 2楼水房</div>
            </div>
            <div class="info-item">
              <div class="info-label">设备状态</div>
              <div class="info-value status-normal">正常运行</div>
            </div>
            <div class="info-item">
              <div class="info-label">运行时间</div>
              <div class="info-value">1,246 小时</div>
            </div>
          </div>
        </div>

        <!-- 水质监测 -->
        <div class="info-section">
          <div class="section-title">水质监测</div>
          <div class="monitoring-grid">
            <div class="monitoring-item">
              <div class="monitoring-label">自来水TDS</div>
              <div class="monitoring-value">285</div>
              <div class="monitoring-unit">mg/L</div>
            </div>
            <div class="monitoring-item">
              <div class="monitoring-label">纯净水TDS</div>
              <div class="monitoring-value">13</div>
              <div class="monitoring-unit">mg/L</div>
            </div>
            <div class="monitoring-item">
              <div class="monitoring-label">矿化水TDS</div>
              <div class="monitoring-value">87</div>
              <div class="monitoring-unit">mg/L</div>
            </div>
          </div>
        </div>

        <!-- 滤芯状态 -->
        <div class="info-section">
          <div class="section-title">滤芯状态</div>
          <div class="filter-grid">
            <div class="filter-item">
              <div class="filter-name">滤芯1</div>
              <div class="filter-status status-normal">正常</div>
              <div class="filter-lifespan">剩余寿命：65%</div>
            </div>
            <div class="filter-item">
              <div class="filter-name">滤芯2</div>
              <div class="filter-status status-warning">寿命低</div>
              <div class="filter-lifespan">剩余寿命：35%</div>
            </div>
            <div class="filter-item">
              <div class="filter-name">滤芯3</div>
              <div class="filter-status status-normal">正常</div>
              <div class="filter-lifespan">剩余寿命：72%</div>
            </div>
          </div>
        </div>

        <!-- 传感器监控 -->
        <div class="info-section">
          <div class="section-title">传感器监控</div>
          <div class="sensor-grid">
            <div class="sensor-item">
              <div class="sensor-name">流量计1</div>
              <div class="sensor-value">12.9</div>
              <div class="sensor-unit">L/min</div>
              <div class="sensor-extra">今日流量 1,245L</div>
            </div>
            <div class="sensor-item">
              <div class="sensor-name">流量计2</div>
              <div class="sensor-value">8.1</div>
              <div class="sensor-unit">L/min</div>
              <div class="sensor-extra">今日流量 865L</div>
            </div>
            <div class="sensor-item">
              <div class="sensor-name">自来水压力</div>
              <div class="sensor-value">2.8</div>
              <div class="sensor-unit">kg/cm²</div>
              <div class="sensor-extra status-normal">正常</div>
            </div>
          </div>
        </div>
      </div>

      <button class="inspect-btn" @click="startInspection">
        开始巡检
      </button>
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

const startInspection = () => {
  console.log('开始巡检制水机')
  router.push('/inspection/form')
}
</script>

<style scoped>
.water-maker-detail {
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
  display: flex;
  flex-direction: column;
}

.device-detail-container {
  flex: 1;
}

/* 信息区块 */
.info-section {
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

/* 设备信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
}

.info-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.status-normal {
  color: #52c41a;
}

.status-warning {
  color: #faad14;
}

/* 水质监测网格 */
.monitoring-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.monitoring-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  text-align: center;
}

.monitoring-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.monitoring-value {
  font-size: 20px;
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 2px;
}

.monitoring-unit {
  font-size: 11px;
  color: #999;
}

/* 滤芯状态网格 */
.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.filter-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  text-align: center;
}

.filter-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.filter-status {
  font-size: 12px;
  margin-bottom: 4px;
}

.filter-lifespan {
  font-size: 11px;
  color: #666;
}

/* 传感器监控网格 */
.sensor-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.sensor-item {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  text-align: center;
}

.sensor-name {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.sensor-value {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 2px;
}

.sensor-unit {
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
}

.sensor-extra {
  font-size: 11px;
  color: #666;
}

/* 巡检按钮 */
.inspect-btn {
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

.inspect-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.3);
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
