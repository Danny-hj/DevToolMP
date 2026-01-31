<template>
  <div class="tool-detail-page">
    <el-skeleton v-if="loading" :rows="10" animated :loading="loading" />
    <template v-else-if="tool">
      <!-- Header Section -->
      <div class="detail-header">
        <div class="header-main">
          <div class="tool-icon">
            <el-icon size="40"><Tools /></el-icon>
          </div>
          <div class="header-info">
            <h1 class="tool-name">{{ tool.name }}</h1>
            <div class="tool-meta">
              <a v-if="tool.githubUrl" :href="tool.githubUrl" target="_blank" rel="noopener" class="repo-link">
                <el-icon><Link /></el-icon>
                {{ tool.githubOwner }}/{{ tool.githubRepo }}
              </a>
              <span v-else-if="tool.githubOwner && tool.githubRepo" class="repo-text">
                {{ tool.githubOwner }}/{{ tool.githubRepo }}
              </span>
              <span v-else class="no-repo">未关联 GitHub 仓库</span>
            </div>
          </div>
        </div>

        <!-- Install Command Section -->
        <div class="install-section">
          <div class="install-label">Install with npm</div>
          <div class="install-command" @click="handleInstall">
            <span class="command-prefix">$</span>
            <span class="command-text">{{ installCommand }}</span>
            <el-icon class="copy-icon"><DocumentCopy /></el-icon>
          </div>
          <div class="install-hint">Click to copy</div>
        </div>

        <!-- Actions -->
        <div class="header-actions">
          <el-button
            :type="tool.isFavorited ? 'primary' : 'default'"
            @click="handleFavorite"
            class="action-btn"
          >
            <el-icon><CollectionTag /></el-icon>
            {{ tool.isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button
            v-if="tool.githubUrl"
            type="default"
            @click="openGitHub"
            class="action-btn"
          >
            <el-icon><Link /></el-icon>
            GitHub
          </el-button>
        </div>
      </div>

      <!-- Description -->
      <div class="detail-section">
        <h2 class="section-title">About</h2>
        <p class="tool-description">{{ tool.description }}</p>
        <div v-if="tool.tags && tool.tags.length" class="tool-tags">
          <el-tag
            v-for="tag in tool.tags"
            :key="tag"
            size="small"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <!-- Stats Section -->
      <div class="detail-section">
        <h2 class="section-title">Statistics</h2>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(tool.stars) }}</div>
              <div class="stat-label">GitHub Stars</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(tool.viewCount) }}</div>
              <div class="stat-label">浏览量</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(tool.favoriteCount) }}</div>
              <div class="stat-label">收藏数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Download /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(tool.installCount) }}</div>
              <div class="stat-label">安装量</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Rating Section -->
      <div class="detail-section">
        <h2 class="section-title">用户评价</h2>
        <rating-display :statistics="ratingStatistics" />
        <rating-form
          :tool-id="tool.id"
          @success="handleRatingSubmit"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Tools,
  Download,
  CollectionTag,
  Link,
  DocumentCopy,
  Star,
  View,
  Collection
} from '@element-plus/icons-vue'
import { useToolsStore } from '@/stores/tools'
import { useRatingStore } from '@/stores/rating'
import RatingDisplay from '@/components/rating/RatingDisplay.vue'
import RatingForm from '@/components/rating/RatingForm.vue'

const props = defineProps({
  id: {
    type: [Number, String],
    required: true
  }
})

// 将 id 转换为数字（因为路由参数是字符串）
const toolId = computed(() => Number(props.id))

const route = useRoute()
const toolsStore = useToolsStore()
const ratingStore = useRatingStore()

const tool = computed(() => toolsStore.currentTool)
const loading = computed(() => toolsStore.loading)
const ratingStatistics = ref(null)

const installCommand = computed(() => {
  if (tool.value.packageName) {
    return `npm install ${tool.value.packageName}`
  }
  if (tool.value.githubOwner && tool.value.githubRepo) {
    return `npm install ${tool.value.githubOwner}/${tool.value.githubRepo}`
  }
  return '请查看项目文档了解安装方式'
})

const fetchRatingStatistics = async (toolId) => {
  try {
    ratingStatistics.value = await ratingStore.fetchStatistics(toolId)
  } catch (error) {
    console.error('获取评分统计失败:', error)
  }
}

const loadData = async (id) => {
  await toolsStore.fetchToolDetail(id)
  await toolsStore.recordView(id)
  await fetchRatingStatistics(id)
}

onMounted(async () => {
  await loadData(toolId.value)
})

// 监听 props.id 变化（当路由参数变化时）
watch(() => props.id, (newId, oldId) => {
  const newNumericId = Number(newId)
  const oldNumericId = Number(oldId)
  if (newNumericId !== oldNumericId && newNumericId) {
    console.log('[ToolDetail] Tool ID changed from', oldNumericId, 'to', newNumericId)
    loadData(newNumericId)
  }
})

const handleInstall = () => {
  if (installCommand.value.includes('请查看')) {
    ElMessage.warning('该工具未提供安装命令，请查看项目文档')
    return
  }

  navigator.clipboard.writeText(installCommand.value)
  ElMessage.success('安装命令已复制到剪贴板')
  toolsStore.recordInstall(tool.value.id)
}

const handleFavorite = async () => {
  try {
    await toolsStore.toggleFavorite(tool.value.id)
    ElMessage.success(tool.value.isFavorited ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('收藏失败:', error)
  }
}

const openGitHub = () => {
  if (tool.value.githubUrl) {
    window.open(tool.value.githubUrl, '_blank', 'noopener,noreferrer')
  }
}

const handleRatingSubmit = async (data) => {
  try {
    await ratingStore.createRating(tool.value.id, data)
    await fetchRatingStatistics(tool.value.id)
  } catch (error) {
    console.error('提交评价失败:', error)
  }
}

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}

onUnmounted(() => {
  const overlays = document.querySelectorAll('.el-overlay')
  overlays.forEach(overlay => {
    overlay.remove()
  })
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.tool-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: $spacing-xxl $spacing-xl;
}

.detail-header {
  background: $background-color-base;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-xl;
  padding: $spacing-xxl;
  margin-bottom: $spacing-xl;
}

.header-main {
  display: flex;
  gap: $spacing-lg;
  margin-bottom: $spacing-xxl;
}

.tool-icon {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, $primary-color, #00ffcc);
  border-radius: $border-radius-large;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000;
  flex-shrink: 0;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.tool-name {
  margin: 0 0 $spacing-md;
  font-size: 36px;
  font-weight: 700;
  color: $text-color-primary;
  line-height: 1.2;
}

.tool-meta {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.repo-link {
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  color: $primary-color;
  text-decoration: none;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: $font-size-base;
  transition: $transition-fast;

  &:hover {
    color: $primary-hover;
    text-decoration: underline;
  }
}

.repo-text {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: $font-size-base;
  color: $text-color-secondary;
}

.no-repo {
  color: $text-color-placeholder;
  font-style: italic;
  font-size: $font-size-small;
}

.install-section {
  background: $background-color-light;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-large;
  padding: $spacing-xl;
  margin-bottom: $spacing-xl;
}

.install-label {
  font-size: $font-size-small;
  color: $text-color-secondary;
  margin-bottom: $spacing-sm;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.install-command {
  background: $background-color-darker;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-base;
  padding: $spacing-lg $spacing-xl;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: $font-size-large;
  color: $primary-color;
  cursor: pointer;
  transition: $transition-base;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-sm;

  &:hover {
    background: $background-color-base;
    border-color: $primary-color;
    box-shadow: 0 0 20px rgba(0, 255, 157, 0.2);
    transform: translateY(-2px);
  }
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
  color: $text-color-secondary;
  flex-shrink: 0;
}

.install-hint {
  font-size: $font-size-small;
  color: $text-color-placeholder;
  text-align: center;
}

.header-actions {
  display: flex;
  gap: $spacing-md;
  flex-wrap: wrap;
}

.action-btn {
  min-width: 120px;
}

.detail-section {
  background: $background-color-base;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-xl;
  padding: $spacing-xxl;
  margin-bottom: $spacing-xl;
}

.section-title {
  margin: 0 0 $spacing-xl;
  font-size: 24px;
  font-weight: 600;
  color: $text-color-primary;
}

.tool-description {
  margin: 0 0 $spacing-lg;
  font-size: $font-size-large;
  color: $text-color-regular;
  line-height: 1.6;
}

.tool-tags {
  display: flex;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.tag-item {
  background: $background-color-light;
  border-color: $border-color-base;
  color: $text-color-regular;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: $spacing-lg;
}

.stat-card {
  background: $background-color-light;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-base;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  transition: $transition-base;

  &:hover {
    border-color: $primary-color;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 255, 157, 0.1);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, $background-color-lighter, $background-color-base);
  border-radius: $border-radius-base;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary-color;
  font-size: 20px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: $text-color-primary;
  line-height: 1.2;
  margin-bottom: $spacing-xs;
}

.stat-label {
  font-size: $font-size-small;
  color: $text-color-secondary;
}

@media (max-width: 768px) {
  .tool-detail-page {
    padding: $spacing-lg;
  }

  .detail-header,
  .detail-section {
    padding: $spacing-lg;
  }

  .header-main {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .tool-name {
    font-size: 28px;
  }

  .tool-meta {
    justify-content: center;
  }

  .install-command {
    font-size: $font-size-base;
    padding: $spacing-md;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .header-actions {
    justify-content: stretch;
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}

@media (max-width: 480px) {
  .tool-icon {
    width: 56px;
    height: 56px;
  }

  .tool-name {
    font-size: 24px;
  }

  .stat-card {
    padding: $spacing-md;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }

  .stat-value {
    font-size: 20px;
  }
}
</style>
