
<script setup>
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'

onMounted(() => {
  // 禁止页面缩放（移动端）
  document.addEventListener('touchstart', function(event) {
    if (event.touches.length > 1) {
      event.preventDefault()
    }
  }, { passive: false })

  // 禁止双击缩放
  let lastTouchEnd = 0
  document.addEventListener('touchend', function(event) {
    const now = Date.now()
    if (now - lastTouchEnd <= 300) {
      event.preventDefault()
    }
    lastTouchEnd = now
  }, false)
})
</script>

<template>
  <div class="app-container">
    <div class="mobile-frame">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f2f5;
  padding: 20px;
}

.mobile-frame {
  width: 375px;
  height: 667px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  position: relative;
  border: 1px solid #e8e8e8;
}

/* 移动端适配 */
@media (max-width: 420px) {
  .app-container {
    padding: 0;
    background: #f0f2f5;
  }

  .mobile-frame {
    width: 100%;
    height: 100vh;
    border-radius: 0;
    box-shadow: none;
    border: none;
  }
}
</style>
