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
        <router-link to="/" class="nav-link">
          <el-icon><HomeFilled /></el-icon>
          首页
        </router-link>
        <router-link to="/tools" class="nav-link">
          <el-icon><Tools /></el-icon>
          工具列表
        </router-link>
        <router-link to="/ranking" class="nav-link">
          <el-icon><Trophy /></el-icon>
          排行榜
        </router-link>
      </nav>

      <div class="user-actions">
        <el-button v-if="!token" type="primary" @click="handleLogin">
          登录
        </el-button>
        <el-dropdown v-else @command="handleCommand">
          <span class="user-name">
            <el-icon><User /></el-icon>
            用户
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { HomeFilled, Tools, Trophy, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')

const token = computed(() => userStore.token)

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      name: 'Search',
      query: { q: searchKeyword.value.trim() }
    })
  }
}

const handleLogin = () => {
  router.push('/login')
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
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

  &:hover,
  &.router-link-active {
    color: $primary-color;
    background: rgba(0, 255, 157, 0.1);
  }
}

.user-actions {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  color: $text-color-regular;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-base;
  transition: $transition-fast;

  &:hover {
    color: $primary-color;
    background: rgba(0, 255, 157, 0.1);
  }
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
