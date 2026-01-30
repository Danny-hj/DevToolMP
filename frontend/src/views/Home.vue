<template>
  <div class="home-page">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">DevToolMP</h1>
        <p class="hero-subtitle">发现最好的开发工具</p>
        <div class="hero-actions">
          <router-link to="/tools">
            <el-button type="primary" size="large">浏览工具</el-button>
          </router-link>
        </div>
      </div>
    </div>

    <div class="content-section">
      <div class="section-header">
        <h2>热门工具</h2>
        <router-link to="/tools">查看全部 →</router-link>
      </div>

      <div class="tools-grid">
        <el-skeleton v-if="loading" :rows="3" animated />
        <tool-card
          v-else
          v-for="tool in tools"
          :key="tool.id"
          :tool="tool"
          @click="handleToolClick"
          @favorite="handleFavorite"
        />
      </div>
    </div>

    <div class="content-section">
      <div class="section-header">
        <h2>最新工具</h2>
      </div>

      <div class="tools-grid">
        <el-skeleton v-if="loading" :rows="3" animated />
        <tool-card
          v-else
          v-for="tool in latestTools"
          :key="tool.id"
          :tool="tool"
          @click="handleToolClick"
          @favorite="handleFavorite"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useToolsStore } from '@/stores/tools'
import ToolCard from '@/components/tool/ToolCard.vue'

const router = useRouter()
const toolsStore = useToolsStore()

const tools = computed(() => toolsStore.tools.slice(0, 4))
const latestTools = computed(() => toolsStore.tools.slice(4, 8))
const loading = computed(() => toolsStore.loading)

onMounted(() => {
  toolsStore.fetchTools(0, 20)
})

const handleToolClick = (tool) => {
  router.push(`/tools/${tool.id}`)
}

const handleFavorite = async (tool) => {
  try {
    await toolsStore.toggleFavorite(tool.id)
  } catch (error) {
    console.error('收藏失败:', error)
  }
}
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.hero-section {
  background: linear-gradient(135deg, #409eff 0%, #1a73e8 100%);
  border-radius: 12px;
  padding: 80px 40px;
  text-align: center;
  margin-bottom: 40px;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  color: white;
  margin: 0 0 16px;
}

.hero-subtitle {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 32px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.hero-actions a {
  text-decoration: none;
}

.content-section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  color: #e0e0e0;
}

.section-header a {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
</style>
