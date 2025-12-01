<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统概览</h2>
      <div class="breadcrumb">校园矿化水平台/仪表盘</div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stats-card">
        <div class="stats-icon">📊</div>
        <div class="stats-number">156</div>
        <div class="stats-label">设备总数</div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">🟢</div>
        <div class="stats-number">142</div>
        <div class="stats-label">在线数量</div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">⚠️</div>
        <div class="stats-number">8</div>
        <div class="stats-label">告警设备</div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">📋</div>
        <div class="stats-number">12</div>
        <div class="stats-label">待处理工单</div>
      </div>
      <div class="stats-card">
        <div class="stats-icon">🔴</div>
        <div class="stats-number">1</div>
        <div class="stats-label">离线设备</div>
      </div>
    </div>

    <!-- 警告通知 -->
    <div class="alert-warning">
      <div class="alert-icon">⚠️</div>
      <div class="alert-content">
        <span>设备 A08制水机 异常，请关注！</span>
      </div>
      <div class="alert-close">×</div>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-grid">
      <!-- 左侧：设备状态分布 -->
      <div class="content-card">
        <h3 class="card-title">设备状态分布</h3>
        <div class="chart-container">
          <div class="pie-chart">
            <div class="pie-background"></div>
            <div class="pie-segment" style="--percentage: 75;"></div>
            <div class="pie-center">
              <span class="pie-value">75</span>
            </div>
          </div>
          <div class="chart-info">
            <div class="chart-title">矿化水优良情况</div>
            <div class="chart-subtitle">所有设备总体情况</div>
          </div>
        </div>
      </div>

      <!-- 右侧：最新告警和工单统计 -->
      <div class="right-column">
        <!-- 最新告警 -->
        <div class="content-card">
          <h3 class="card-title">最新告警</h3>
          <div class="alert-list">
            <div class="alert-item">
              <div class="alert-text">A08制水机：TDS值超标</div>
              <div class="alert-level critical">紧急</div>
            </div>
            <div class="alert-divider"></div>
            <div class="alert-item">
              <div class="alert-text">C11制水机：离线</div>
              <div class="alert-level warning">警告</div>
            </div>
          </div>
        </div>

        <!-- 工单状态统计 -->
        <div class="content-card">
          <h3 class="card-title">工单状态统计</h3>
          <div class="chart-placeholder">
            <div class="placeholder-text">
              <div style="font-size: 16px; margin-bottom: 8px;">Axhub Charts</div>
              <div style="font-size: 24px; margin-bottom: 12px;">柱状图</div>
              <div style="color: #666; font-size: 12px;">
                通过Group内data和config中继器可更改数据及配置
              </div>
              <div style="color: #666; font-size: 12px;">
                详情访问：https://axhub.im/charts
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.dashboard {
  /* 移除 padding，因为 MainLayout 的 content-wrapper 已经有 padding */
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.breadcrumb {
  color: #666;
  font-size: 14px;
}

/* 其他样式保持不变，但移除最外层的 padding */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stats-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
}

.stats-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.stats-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stats-label {
  font-size: 14px;
  color: #666;
}

/* 其他样式... 保持原有的所有样式 */
</style>