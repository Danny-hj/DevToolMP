<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <!-- ASCII 艺术标题 -->
        <pre
          v-pre
          class="ascii-art"
        >
  ____                 _       _____                _         _____ _
 / ___|_ __ _   _ ___| |_    |  ___|__  _ __   ___| |__     |  ___| | _____  __
| |   | '__| | | / __| __|   | |_ / _ \| '_ \ / __| '_ \    | |_  | |/ _ \ \/ /
| |___| |  | |_| \__ \ |_    |  _| (_) | | | | (__| | | |   |  _| | |  __/>  <
 \____|_|   \__, |___/\__|   |_|  \___/|_| |_|\___|_| |_|   |_|   |_|\___/_/\_\
            |___/
        </pre>

        <h1 class="hero-title">
          开发工具市场
        </h1>
        <p class="hero-subtitle">
          发现、安装和管理最好的开发工具
        </p>

        <!-- 一键安装命令展示 -->
        <div
          class="install-command-hero"
          @click="copyInstallCommand"
        >
          <span class="command-prefix">$</span>
          <span class="command-text">npm install -g devtoolmp</span>
          <el-icon class="copy-icon">
            <DocumentCopy />
          </el-icon>
        </div>

        <div class="hero-actions">
          <router-link
            to="/tools"
            class="action-link"
          >
            <el-button
              type="primary"
              size="large"
              round
            >
              <el-icon><Tools /></el-icon>
              浏览工具
            </el-button>
          </router-link>
          <router-link
            to="/ranking"
            class="action-link"
          >
            <el-button
              size="large"
              round
            >
              <el-icon><Trophy /></el-icon>
              查看排行榜
            </el-button>
          </router-link>
        </div>
      </div>
    </div>

    <!-- 统计数据卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <StatsCard
          :value="totalTools"
          label="工具总数"
          unit="+"
          :icon="Tools"
        />
        <StatsCard
          :value="totalInstalls"
          label="总安装量"
          unit="K"
          :icon="Download"
        />
        <StatsCard
          :value="totalCategories"
          label="分类数量"
          unit="+"
          :icon="FolderOpened"
        />
        <StatsCard
          :value="activeUsers"
          label="活跃用户"
          unit="+"
          :icon="User"
        />
      </div>
    </div>

    <!-- 排行榜快速入口 -->
    <div class="ranking-preview-section">
      <div class="section-header">
        <h2>
          <el-icon><TrendCharts /></el-icon>
          热门排行
        </h2>
        <router-link
          to="/ranking"
          class="view-all-link"
        >
          查看完整排行榜
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <el-row
        :gutter="20"
        class="ranking-tabs-row"
      >
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
        >
          <div
            class="ranking-tab-card"
            @click="goToRanking('alltime')"
          >
            <div class="tab-icon">
              <el-icon><Trophy /></el-icon>
            </div>
            <div class="tab-info">
              <h3>全部总榜</h3>
              <p>所有时间最佳</p>
            </div>
          </div>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
        >
          <div
            class="ranking-tab-card"
            @click="goToRanking('weekly')"
          >
            <div class="tab-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="tab-info">
              <h3>本周热门</h3>
              <p>最近7天最佳</p>
            </div>
          </div>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
        >
          <div
            class="ranking-tab-card"
            @click="goToRanking('daily')"
          >
            <div class="tab-icon">
              <el-icon><Sunny /></el-icon>
            </div>
            <div class="tab-info">
              <h3>今日热门</h3>
              <p>最近24小时最佳</p>
            </div>
          </div>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
        >
          <div
            class="ranking-tab-card"
            @click="goToRanking('trending')"
          >
            <div class="tab-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="tab-info">
              <h3>趋势榜单</h3>
              <p>上升最快</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 热门工具 -->
    <div class="content-section">
      <div class="section-header">
        <h2>
          <el-icon><Star /></el-icon>
          热门工具
        </h2>
        <router-link
          to="/tools"
          class="view-all-link"
        >
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div class="tools-grid">
        <el-skeleton
          v-if="loading"
          :rows="2"
          animated
        />
        <tool-card
          v-for="tool in popularTools"
          v-else
          :key="`${tool.id}-${refreshKey}`"
          :tool="tool"
          :hot-score="tool.hotScoreAlltime"
          @click="handleToolClick"
          @favorite="handleFavorite"
        />
      </div>
    </div>

    <!-- 最新工具 -->
    <div class="content-section">
      <div class="section-header">
        <h2>
          <el-icon><Clock /></el-icon>
          最新工具
        </h2>
        <router-link
          to="/tools"
          class="view-all-link"
        >
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div class="tools-grid">
        <el-skeleton
          v-if="loading"
          :rows="2"
          animated
        />
        <tool-card
          v-for="tool in latestTools"
          v-else
          :key="`${tool.id}-${refreshKey}`"
          :tool="tool"
          @click="handleToolClick"
          @favorite="handleFavorite"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Tools,
  Trophy,
  Calendar,
  Sunny,
  TrendCharts,
  Star,
  Clock,
  Download,
  FolderOpened,
  User,
  ArrowRight,
  DocumentCopy
} from '@element-plus/icons-vue'
import { useToolsStore } from '@/stores/tools'
import ToolCard from '@/components/tool/ToolCard.vue'
import StatsCard from '@/components/home/StatsCard.vue'

const router = useRouter()
const route = useRoute()
const toolsStore = useToolsStore()

// 添加一个刷新计数器，用于强制重新渲染
const refreshKey = ref(0)

// 统计数据（从API动态获取）
const totalTools = computed(() => toolsStore.total || 60)
const totalInstalls = computed(() => {
  const tools = toolsStore.tools
  const total = tools.reduce((sum, tool) => sum + (tool.installCount || 0), 0)
  return total > 0 ? (total / 1000).toFixed(0) : 89
})
const totalCategories = computed(() => {
  const categoryMap = new Map()
  toolsStore.tools.forEach(tool => {
    if (tool.categoryId && tool.categoryName) {
      categoryMap.set(tool.categoryId, tool.categoryName)
    }
  })
  return categoryMap.size || 4
})
const activeUsers = ref(3.2) // 会显示为 3.2K

const popularTools = computed(() => {
  const tools = toolsStore.tools
  return tools
    .filter(t => t.hotScoreAlltime && t.hotScoreAlltime > 0)
    .sort((a, b) => (b.hotScoreAlltime || 0) - (a.hotScoreAlltime || 0))
    .slice(0, 4)
})

const latestTools = computed(() => {
  const tools = toolsStore.tools
  return tools
    .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
    .slice(0, 4)
})

const loading = computed(() => toolsStore.loading)

// 刷新数据的函数
const refreshData = async () => {
  console.log('[Home] refreshData called, refreshKey:', refreshKey.value)
  // 强制清空现有数据，确保视觉刷新
  toolsStore.tools = []
  // 增加刷新计数器
  refreshKey.value++
  // 重新获取所有数据（设置足够大的size以获取所有工具）
  await toolsStore.fetchTools(0, 100)
  console.log('[Home] Data refresh completed, tools count:', toolsStore.tools.length)
}

onMounted(() => {
  console.log('[Home] Component mounted, route:', route.path)
  refreshData()
})

// 监听路由变化，确保每次进入首页都刷新数据
// 使用 immediate: false 确保不会在首次挂载时重复执行
watch(
  () => route.path,
  (newPath, oldPath) => {
    console.log('[Home] Route path changed:', { oldPath, newPath, currentRefreshKey: refreshKey.value })
    // 当从其他页面导航到首页时，刷新数据
    if (newPath === '/' && oldPath && oldPath !== '/') {
      console.log('[Home] Navigated to home from another page, forcing refresh...')
      refreshData()
    }
  },
  { flush: 'post' }
)

const handleToolClick = (tool) => {
  router.push(`/tools/${tool.id}`)
}

const handleFavorite = async (tool) => {
  try {
    const isFavorited = await toolsStore.toggleFavorite(tool.id)
    ElMessage.success(isFavorited ? '已收藏' : '已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
    console.error('收藏失败:', error)
  }
}

const goToRanking = (tab) => {
  router.push({ path: '/ranking', query: { tab } })
}

const copyInstallCommand = async () => {
  try {
    await navigator.clipboard.writeText('npm install -g devtoolmp')
    ElMessage.success('安装命令已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败,请手动复制')
  }
}

// 组件卸载时清理
onUnmounted(() => {
  console.log('[Home] Component unmounted')
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.home-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: $spacing-xl;
}

.hero-section {
  background: linear-gradient(135deg, rgba(0, 255, 157, 0.08) 0%, rgba(0, 255, 204, 0.04) 100%);
  border: 1px solid $border-color-base;
  border-radius: $border-radius-xl;
  padding: $spacing-xxl $spacing-xl;
  text-align: center;
  margin-bottom: $spacing-xxl;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(0, 255, 157, 0.06) 0%, transparent 70%);
    animation: rotate 20s linear infinite;
    z-index: 0;
    pointer-events: none;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.hero-content {
  position: relative;
  z-index: 1;
}

.ascii-art {
  font-size: 8px;
  line-height: 1.1;
  color: $primary-color;
  margin: 0 0 $spacing-lg;
  text-shadow: 0 0 20px rgba(0, 255, 157, 0.5);
  font-family: 'Courier New', monospace;
  white-space: pre;
  overflow-x: auto;
  overflow-y: hidden;

  @media (max-width: 768px) {
    display: none;
  }
}

.hero-title {
  font-size: $font-size-hero;
  font-weight: 700;
  color: $text-color-primary;
  margin: 0 0 $spacing-md;
  background: linear-gradient(135deg, $primary-color, #00ffcc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
  word-break: keep-all;
}

.hero-subtitle {
  font-size: $font-size-large;
  color: $text-color-secondary;
  margin: 0 0 $spacing-xl;
  line-height: 1.5;
}

.install-command-hero {
  background: $background-color-light;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-large;
  padding: $spacing-lg $spacing-xl;
  margin: 0 auto $spacing-xl;
  max-width: 600px;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: $font-size-base;
  color: $primary-color;
  cursor: pointer;
  transition: $transition-base;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-md;

  &:hover {
    background: $background-color-lighter;
    border-color: $primary-color;
    box-shadow: 0 0 20px rgba(0, 255, 157, 0.3);
    transform: scale(1.02);
  }

  .command-prefix {
    color: $text-color-secondary;
    user-select: none;
  }

  .command-text {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .copy-icon {
    font-size: 18px;
    flex-shrink: 0;
  }
}

.hero-actions {
  display: flex;
  gap: $spacing-lg;
  justify-content: center;
  flex-wrap: wrap;

  .action-link {
    text-decoration: none;
  }
}

.stats-section {
  margin-bottom: $spacing-xxl;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: $spacing-lg;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-md;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
    gap: $spacing-sm;
  }
}

.ranking-preview-section {
  margin-bottom: $spacing-xxl;
}

.ranking-tabs-row {
  margin-top: $spacing-xl;
}

.ranking-tab-card {
  background: $background-color-base;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-large;
  padding: $spacing-xl;
  cursor: pointer;
  transition: $transition-base;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  min-height: 80px;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 0 20px rgba(0, 255, 157, 0.2);
    transform: translateY(-4px);
  }

  .tab-icon {
    width: 56px;
    height: 56px;
    background: linear-gradient(135deg, $background-color-light, $background-color-lighter);
    border-radius: $border-radius-large;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: $primary-color;
    flex-shrink: 0;
  }

  .tab-info {
    flex: 1;
    min-width: 0;
    overflow: hidden;

    h3 {
      margin: 0 0 $spacing-sm 0;
      font-size: $font-size-large;
      font-weight: 600;
      color: $text-color-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    p {
      margin: 0;
      font-size: $font-size-small;
      color: $text-color-secondary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.content-section {
  margin-bottom: $spacing-xxl;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-xl;
  flex-wrap: wrap;
  gap: $spacing-md;

  h2 {
    margin: 0;
    font-size: $font-size-title;
    font-weight: 600;
    color: $text-color-primary;
    display: flex;
    align-items: center;
    gap: $spacing-md;
    flex-wrap: wrap;
  }
}

.view-all-link {
  color: $primary-color;
  text-decoration: none;
  font-size: $font-size-base;
  display: inline-flex;
  align-items: center;
  gap: $spacing-sm;
  transition: $transition-fast;
  white-space: nowrap;

  &:hover {
    color: $primary-hover;
    gap: $spacing-md;
  }
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-xl;
}

@media (max-width: 768px) {
  .home-page {
    padding: $spacing-lg;
  }

  .hero-section {
    padding: $spacing-xl $spacing-lg;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: $font-size-base;
  }

  .install-command-hero {
    padding: $spacing-md;
    font-size: $font-size-small;

    .command-text {
      max-width: 200px;
    }
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;

    h2 {
      font-size: $font-size-large;
    }
  }

  .ranking-tab-card {
    padding: $spacing-lg;
    min-height: 70px;

    .tab-icon {
      width: 48px;
      height: 48px;
      font-size: 24px;
    }

    .tab-info h3 {
      font-size: $font-size-base;
    }

    .tab-info p {
      font-size: 11px;
    }
  }
}

@media (max-width: 480px) {
  .home-page {
    padding: $spacing-md;
  }

  .hero-title {
    font-size: 24px;
  }

  .install-command-hero {
    flex-direction: column;
    gap: $spacing-sm;

    .command-text {
      max-width: 100%;
      text-align: center;
    }
  }

  .hero-actions {
    flex-direction: column;
    width: 100%;

    .el-button {
      width: 100%;
    }
  }
}
</style>
