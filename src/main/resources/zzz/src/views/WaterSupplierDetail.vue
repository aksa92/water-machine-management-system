<template>
  <div class="water-supplier-detail">
    <!-- 顶部栏 -->
    <div class="header">
      <div class="header-left">
        <span class="back-btn" @click="goBack">返回</span>
      </div>
      <div class="header-title">供水机详情</div>
      <div class="header-right"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="detail-container">
        <!-- 设备信息 -->
        <div class="detail-section">
          <div class="section-title">设备信息</div>
          <div class="detail-grid">
            <div class="detail-item">
              <div class="item-label">设备ID</div>
              <div class="item-value">{{ deviceInfo.id }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">安装位置</div>
              <div class="item-value">{{ deviceInfo.location }}</div>
            </div>
            <div class="detail-item">
              <div class="item-label">设备状态</div>
              <div class="item-value status-normal">{{ deviceInfo.status }}</div>
            </div>
          </div>
        </div>

        <!-- 水位信息 -->
        <div class="detail-section">
          <div class="section-title">水位信息</div>
          <div class="water-level-content">
            <div class="water-level-chart">
              <div class="level-box">
                <div class="level-percentage">{{ deviceInfo.waterLevel }}%</div>
                <div class="level-label">当前水位</div>
              </div>

              <div class="storage-box">
                <div class="storage-value">{{ deviceInfo.storageCapacity }}L</div>
                <div class="storage-label">储水量</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 浮球阀状态 -->
        <div class="detail-section">
          <div class="section-title">浮球阀状态</div>
          <div class="float-valve-content">
            <!-- 高水位浮球阀 -->
            <div class="float-valve-item">
              <div class="valve-info">
                <div class="valve-label">高水位浮球阀</div>
                <div class="valve-value" :class="getValveClass(deviceInfo.highValve)">
                  {{ deviceInfo.highValve }}
                </div>
              </div>
              <div class="valve-description">水位达到90%时自动关闭进水</div>
            </div>

            <!-- 低水位浮球阀 -->
            <div class="float-valve-item">
              <div class="valve-info">
                <div class="valve-label">低水位浮球阀</div>
                <div class="valve-value" :class="getValveClass(deviceInfo.lowValve)">
                  {{ deviceInfo.lowValve }}
                </div>
              </div>
              <div class="valve-description">水位低于10%时自动开启进水</div>
            </div>
          </div>
        </div>

        <!-- 漏水检测 -->
        <div class="detail-section">
          <div class="section-title">漏水检测</div>
          <div class="leak-detection-content">
            <div class="detection-info">
              <div class="sensor-info">
                <div class="sensor-label">浮球阀传感器</div>
              </div>
              <div class="detection-result">
                <div class="result-info">
                  <div class="result-label">检测结果</div>
                  <div class="result-value" :class="getLeakClass(deviceInfo.leakStatus)">
                    {{ deviceInfo.leakStatus }}
                  </div>
                </div>
                <div class="result-description">{{ deviceInfo.leakDescription }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 开始巡检按钮 -->
      <button class="start-inspection-btn" @click="startInspection">
        开始巡检
      </button>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 设备数据 - 根据HTML内容调整
const deviceInfo = ref({
  id: '供水机 #A106',
  name: '供水机 #A106',
  location: 'A区教学楼 1楼走廊',
  status: '正常运行',
  waterLevel: 66,
  storageCapacity: 360,
  highValve: '开启',
  lowValve: '关闭',
  leakSensor: '浮球阀传感器',
  leakStatus: '无漏水',
  leakDescription: '设备周边未检测到漏水情况'
})

// 返回上一页
const goBack = () => {
  router.back()
}

// 开始巡检
const startInspection = () => {
  console.log('开始巡检供水机', deviceInfo.value.id)
  router.push('/inspection/form')
}

// 导航函数
const goToHome = () => {
  router.push('/home')
}

const goToWorkOrders = () => {
  router.push('/work-orders')
}

const goToProfile = () => {
  router.push('/profile')
}

// 获取阀门状态样式类
const getValveClass = (valveStatus) => {
  return valveStatus === '开启' ? 'valve-open' : 'valve-closed'
}

// 获取漏水状态样式类
const getLeakClass = (leakStatus) => {
  return leakStatus === '无漏水' ? 'leak-none' : 'leak-detected'
}

// 模拟从API获取设备详情
onMounted(() => {
  const deviceId = router.currentRoute.value.params.id
  console.log('加载供水机详情，设备ID:', deviceId)

  // 根据设备ID更新数据
  if (deviceId) {
    deviceInfo.value.id = `供水机 #${deviceId}`
  }
})
</script>

<style scoped>
.water-supplier-detail {
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

.detail-container {
  flex: 1;
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

/* 设备信息网格 */
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
}

.item-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.status-normal {
  color: #52c41a;
}

/* 水位信息 */
.water-level-content {
  padding: 8px 0;
}

.water-level-chart {
  display: flex;
  gap: 20px;
  align-items: center;
}

.level-box {
  flex: 1;
  text-align: center;
  padding: 16px;
  background: #f0f7ff;
  border-radius: 8px;
  border: 1px solid #d6e4ff;
}

.level-percentage {
  font-size: 32px;
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 4px;
}

.level-label {
  font-size: 14px;
  color: #666;
}

.storage-box {
  flex: 1;
  text-align: center;
  padding: 16px;
  background: #f6ffed;
  border-radius: 8px;
  border: 1px solid #b7eb8f;
}

.storage-value {
  font-size: 28px;
  font-weight: 700;
  color: #52c41a;
  margin-bottom: 4px;
}

.storage-label {
  font-size: 14px;
  color: #666;
}

/* 浮球阀状态 */
.float-valve-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.float-valve-item {
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.valve-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.valve-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.valve-value {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 4px;
}

.valve-open {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.valve-closed {
  background: #fff2e8;
  color: #fa541c;
  border: 1px solid #ffbb96;
}

.valve-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

/* 漏水检测 */
.leak-detection-content {
  padding: 8px 0;
}

.detection-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sensor-info {
  padding: 8px 0;
}

.sensor-label {
  font-size: 14px;
  color: #666;
}

.detection-result {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-label {
  font-size: 14px;
  color: #666;
}

.result-value {
  font-size: 14px;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 4px;
}

.leak-none {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.leak-detected {
  background: #fff2e8;
  color: #fa541c;
  border: 1px solid #ffbb96;
}

.result-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

/* 开始巡检按钮 */
.start-inspection-btn {
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

.start-inspection-btn:hover {
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
