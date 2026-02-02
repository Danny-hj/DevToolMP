<template>
  <header class="app-header">
    <div class="header-container">
      <div class="logo">
        <router-link to="/">
          <h1>DevToolMP</h1>
        </router-link>
      </div>

      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索工具..."
          prefix-icon="Search"
          @keyup.enter="handleSearch"
        />
      </div>

      <nav class="nav-links">
        <router-link
          to="/"
          class="nav-link"
        >
          <el-icon><HomeFilled /></el-icon>
          首页
        </router-link>
        <router-link
          to="/tools"
          class="nav-link"
        >
          <el-icon><Tools /></el-icon>
          工具列表
        </router-link>
        <router-link
          to="/ranking"
          class="nav-link"
        >
          <el-icon><Trophy /></el-icon>
          排行榜
        </router-link>
      </nav>

      <div class="user-actions">
        <el-button
          :type="adminMode ? 'primary' : 'default'"
          class="admin-mode-btn"
          @click="toggleAdminMode"
        >
          <el-icon><Setting /></el-icon>
          {{ adminMode ? '管理模式' : '管理模式' }}
        </el-button>
        <span class="welcome-text">
          <el-icon><User /></el-icon>
          欢迎访问 DevToolMP
        </span>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { HomeFilled, Tools, Trophy, User, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')

const adminMode = computed(() => userStore.adminMode)

const toggleAdminMode = () => {
  userStore.toggleAdminMode()
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      name: 'Search',
      query: { q: searchKeyword.value.trim() }
    })
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.app-header {
  background-color: $background-color-base;
  border-bottom: 1px solid $border-color-base;
  position: sticky;
  top: 0;
  z-index: $z-index-top;
  backdrop-filter: blur(10px);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 $spacing-xl;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo a {
  text-decoration: none;
  color: inherit;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.logo h1 {
  font-size: $font-size-extra-large;
  margin: 0;
  font-weight: 700;
  background: linear-gradient(135deg, $primary-color, #00ffcc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.search-box {
  flex: 1;
  max-width: 500px;
  margin: 0 $spacing-xxl;

  :deep(.el-input__wrapper) {
    background-color: $background-color-light;
    border: 1px solid $border-color-base;

    &:hover,
    &.is-focus {
      border-color: $primary-color;
      box-shadow: 0 0 8px rgba(0, 255, 157, 0.2);
    }
  }
}

.nav-links {
  display: flex;
  gap: $spacing-lg;
}

.nav-link {
  color: $text-color-regular;
  text-decoration: none;
  font-size: $font-size-base;
  transition: $transition-fast;
  display: inline-flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-base;
  position: relative;
  z-index: 1001; // 确保高于所有遮罩层
  pointer-events: auto; // 确保始终可点击

  &:hover,
  &.router-link-active {
    color: $primary-color;
    background: rgba(0, 255, 157, 0.1);
  }
}

// 确保导航栏始终在最上层
.nav-links {
  position: relative;
  z-index: 1001;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.admin-mode-btn {
  font-size: $font-size-small;

  &.el-button--primary {
    background: linear-gradient(135deg, $primary-color, #00ffcc);
    border-color: $primary-color;

    &:hover {
      background: linear-gradient(135deg, $primary-hover, #00ccaa);
      border-color: $primary-hover;
    }
  }
}

.welcome-text {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  color: $text-color-regular;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 $spacing-lg;
  }

  .search-box {
    display: none;
  }

  .nav-links {
    gap: $spacing-md;
  }

  .nav-link span {
    display: none;
  }

  .nav-link .el-icon {
    font-size: 20px;
  }
}
</style>
