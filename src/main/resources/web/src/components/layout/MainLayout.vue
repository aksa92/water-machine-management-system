<template>
  <div class="main-layout">
    <AppHeader />
    <AppSidebar />
    <main class="main-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
</script>

<style scoped>
.main-layout {
  display: grid;
  grid-template-areas: 
    "header header"
    "sidebar content";
  grid-template-rows: 60px 1fr;
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.main-content {
  grid-area: content;
  background: #f8f9fa;
  overflow-y: auto;
}

.content-wrapper {
  padding: 20px;
  min-height: calc(100vh - 60px);
}
</style>