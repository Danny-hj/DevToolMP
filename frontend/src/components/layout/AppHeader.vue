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
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/tools" class="nav-link">工具列表</router-link>
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

<style scoped>
.app-header {
  background-color: #1a1a1a;
  border-bottom: 1px solid #333;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo a {
  text-decoration: none;
  color: inherit;
}

.logo h1 {
  font-size: 24px;
  margin: 0;
  color: #409eff;
}

.search-box {
  flex: 1;
  max-width: 400px;
  margin: 0 40px;
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-link {
  color: #e0e0e0;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #409eff;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  color: #e0e0e0;
}

.user-name:hover {
  color: #409eff;
}
</style>
