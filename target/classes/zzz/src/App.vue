<script setup>
// App.vue 已经是多单词
</script>

<template>
  <div class="app-container">
    <div class="mobile-frame">
      <router-view></router-view>
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
