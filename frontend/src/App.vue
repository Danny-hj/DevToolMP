<template>
  <div id="app">
    <app-header />
    <main class="main-content">
      <router-view />
    </main>
    <app-footer />
  </div>
</template>

<script setup>
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
</script>

<style scoped>
.main-content {
  min-height: calc(100vh - 120px);
}
</style>
