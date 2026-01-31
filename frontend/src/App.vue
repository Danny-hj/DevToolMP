<template>
  <div id="app">
    <app-header />
    <main class="main-content">
      <!-- 不使用 :key，让组件自然复用，通过组件内部的 watch 来处理数据刷新 -->
      <router-view />
    </main>
    <app-footer />
  </div>
</template>

<script setup>
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
</script>

<style>
/* 确保导航栏始终在最上层，但对话框可以显示在导航栏之上 */
.app-header {
  position: sticky !important;
  z-index: 1000 !important;
}

/* 确保对话框和遮罩层显示在导航栏之上 */
.el-overlay {
  z-index: 2000 !important;
}

.el-dialog {
  z-index: 2001 !important;
}

/* 确保导航链接始终可点击 */
.nav-link {
  pointer-events: auto !important;
}
</style>

<style scoped>
.main-content {
  min-height: calc(100vh - 120px);
}
</style>
