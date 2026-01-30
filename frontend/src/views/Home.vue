<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <!-- ASCII 艺术标题 -->
        <pre class="ascii-art">
  ____                 _       _____                _         _____ _
 / ___|_ __ _   _ ___| |_    |  ___|__  _ __   ___| |__     |  ___| | _____  __
| |   | '__| | | / __| __|   | |_ / _ \| '_ \ / __| '_ \    | |_  | |/ _ \ \/ /
| |___| |  | |_| \__ \ |_    |  _| (_) | | | | (__| | | |   |  _| | |  __/>  <
 \____|_|   \__, |___/\__|   |_|  \___/|_| |_|\___|_| |_|   |_|   |_|\___/_/\_\
            |___/
        </pre>

        <h1 class="hero-title">开发工具市场</h1>
        <p class="hero-subtitle">发现、安装和管理最好的开发工具</p>

        <!-- 一键安装命令展示 -->
        <div class="install-command-hero" @click="copyInstallCommand">
          <span class="command-text">npm install -g devtoolmp</span>
          <el-icon class="copy-icon"><DocumentCopy /></el-icon>
        </div>

        <div class="hero-actions">
          <router-link to="/tools" class="action-link">
            <el-button type="primary" size="large" round>
              <el-icon><Tools /></el-icon>
              浏览工具
            </el-button>
          </router-link>
          <router-link to="/ranking" class="action-link">
            <el-button size="large" round>
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
        <router-link to="/ranking" class="view-all-link">
          查看完整排行榜
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <el-row :gutter="20" class="ranking-tabs-row">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="ranking-tab-card" @click="goToRanking('alltime')">
            <div class="tab-icon">
              <el-icon><Trophy /></el-icon>
            </div>
            <div class="tab-info">
              <h3>全部总榜</h3>
              <p>所有时间最佳</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="ranking-tab-card" @click="goToRanking('weekly')">
            <div class="tab-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="tab-info">
              <h3>本周热门</h3>
              <p>最近7天最佳</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="ranking-tab-card" @click="goToRanking('daily')">
            <div class="tab-icon">
              <el-icon><Sunny /></el-icon>
            </div>
            <div class="tab-info">
              <h3>今日热门</h3>
              <p>最近24小时最佳</p>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="ranking-tab-card" @click="goToRanking('trending')">
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
        <router-link to="/tools" class="view-all-link">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div class="tools-grid">
        <el-skeleton v-if="loading" :rows="2" animated />
        <tool-card
          v-else
          v-for="tool in popularTools"
          :key="tool.id"
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
        <router-link to="/tools" class="view-all-link">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div class="tools-grid">
        <el-skeleton v-if="loading" :rows="2" animated />
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
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
const toolsStore = useToolsStore()

// 统计数据
const totalTools = ref(156)
const totalInstalls = ref(89)
const totalCategories = ref(12)
const activeUsers = ref(3245)

const popularTools = computed(() => {
  const tools = toolsStore.tools
  return tools
    .filter(t => t.hotScoreAlltime > 500)
    .sort((a, b) => b.hotScoreAlltime - a.hotScoreAlltime)
    .slice(0, 4)
})

const latestTools = computed(() => {
  const tools = toolsStore.tools
  return tools
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .slice(0, 4)
})

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

const goToRanking = (tab) => {
  router.push({ path: '/ranking', query: { tab } })
}

const copyInstallCommand = async () => {
  try {
    await navigator.clipboard.writeText('npm install -g devtoolmp')
    ElMessage.success('安装命令已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败,请手动复制')
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.home-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: $spacing-xl;
}

.hero-section {
  background: linear-gradient(135deg, rgba(0, 255, 157, 0.1) 0%, rgba(0, 255, 204, 0.05) 100%);
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
    background: radial-gradient(circle, rgba(0, 255, 157, 0.1) 0%, transparent 70%);
    animation: rotate 20s linear infinite;
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
  line-height: 1;
  color: $primary-color;
  margin: 0 0 $spacing-lg;
  text-shadow: 0 0 20px rgba(0, 255, 157, 0.5);
  font-family: 'Courier New', monospace;

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
}

.hero-subtitle {
  font-size: $font-size-large;
  color: $text-color-secondary;
  margin: 0 0 $spacing-xl;
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

  .command-text {
    flex: 1;
  }

  .copy-icon {
    font-size: 18px;
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
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: $spacing-xl;
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
  height: 100%;

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

    h3 {
      margin: 0 0 $spacing-sm;
      font-size: $font-size-large;
      font-weight: 600;
      color: $text-color-primary;
    }

    p {
      margin: 0;
      font-size: $font-size-small;
      color: $text-color-secondary;
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

  h2 {
    margin: 0;
    font-size: $font-size-title;
    font-weight: 600;
    color: $text-color-primary;
    display: flex;
    align-items: center;
    gap: $spacing-md;
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

  .hero-title {
    font-size: 32px;
  }

  .tools-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
