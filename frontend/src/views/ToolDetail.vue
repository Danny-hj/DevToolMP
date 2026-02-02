<template>
  <div class="tool-detail-page">
    <el-skeleton
      v-if="loading"
      :rows="10"
      animated
      :loading="loading"
    />
    <template v-else-if="tool">
      <!-- Header Section -->
      <div class="detail-header">
        <div class="header-main">
          <div class="tool-icon">
            <el-icon size="40">
              <Tools />
            </el-icon>
          </div>
          <div class="header-info">
            <h1 class="tool-name">
              {{ tool.name }}
            </h1>
            <div class="tool-meta">
              <a
                v-if="tool.codehubUrl"
                :href="tool.codehubUrl"
                target="_blank"
                rel="noopener"
                class="repo-link"
              >
                <el-icon><Link /></el-icon>
                {{ tool.codehubOwner }}/{{ tool.codehubRepo }}
              </a>
              <span
                v-else-if="tool.codehubOwner && tool.codehubRepo"
                class="repo-text"
              >
                {{ tool.codehubOwner }}/{{ tool.codehubRepo }}
              </span>
              <span
                v-else
                class="no-repo"
              >未关联 Codehub 仓库</span>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="header-actions">
          <el-button
            :type="tool.isFavorited ? 'primary' : 'default'"
            class="action-btn"
            @click="handleFavorite"
          >
            <el-icon><CollectionTag /></el-icon>
            {{ tool.isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button
            v-if="tool.codehubUrl"
            type="default"
            class="action-btn"
            @click="openCodehub"
          >
            <el-icon><Link /></el-icon>
            Codehub
          </el-button>
        </div>
      </div>

      <!-- Description -->
      <div class="detail-section">
        <h2 class="section-title">
          About
        </h2>
        <div
          class="tool-description markdown-content"
          v-html="renderedDescription"
        />
        <div
          v-if="tool.tags && tool.tags.length"
          class="tool-tags"
        >
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
        <h2 class="section-title">
          Statistics
        </h2>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.stars) }}
              </div>
              <div class="stat-label">
                Codehub Stars
              </div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.viewCount) }}
              </div>
              <div class="stat-label">
                浏览量
              </div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.favoriteCount) }}
              </div>
              <div class="stat-label">
                收藏数
              </div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Download /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.installCount) }}
              </div>
              <div class="stat-label">
                安装量
              </div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.forks) }}
              </div>
              <div class="stat-label">
                Codehub Forks
              </div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.openIssues) }}
              </div>
              <div class="stat-label">
                Open Issues
              </div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.watchers) }}
              </div>
              <div class="stat-label">
                Watchers
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 昨日统计 -->
      <div class="detail-section">
        <h2 class="section-title">
          昨日统计
        </h2>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.viewCountYesterday) }}
              </div>
              <div class="stat-label">
                昨日浏览
              </div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.favoriteCountYesterday) }}
              </div>
              <div class="stat-label">
                昨日收藏
              </div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Download /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ formatNumber(tool.installCountYesterday) }}
              </div>
              <div class="stat-label">
                昨日安装
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 热度分数 -->
      <div class="detail-section">
        <h2 class="section-title">
          热度分数
        </h2>
        <div class="stats-grid">
          <div class="stat-card hot-score">
            <div class="stat-icon hot-icon">
              🔥
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ tool.hotScoreDaily ? tool.hotScoreDaily.toFixed(2) : '0.00' }}
              </div>
              <div class="stat-label">
                日热度
              </div>
            </div>
          </div>
          <div class="stat-card hot-score">
            <div class="stat-icon hot-icon">
              🔥
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ tool.hotScoreWeekly ? tool.hotScoreWeekly.toFixed(2) : '0.00' }}
              </div>
              <div class="stat-label">
                周热度
              </div>
            </div>
          </div>
          <div class="stat-card hot-score">
            <div class="stat-icon hot-icon">
              🔥
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ tool.hotScoreAlltime ? tool.hotScoreAlltime.toFixed(2) : '0.00' }}
              </div>
              <div class="stat-label">
                总热度
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="detail-section">
        <h2 class="section-title">
          基本信息
        </h2>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">工具名称</span>
            <span class="info-value">{{ tool.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">版本号</span>
            <span class="info-value">{{ tool.version || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">分类</span>
            <span class="info-value">{{ tool.categoryName || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">状态</span>
            <el-tag :type="tool.status === 'active' ? 'success' : 'info'">
              {{ tool.status === 'active' ? '已上架' : '已下架' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ formatDate(tool.createdAt) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">更新时间</span>
            <span class="info-value">{{ formatDate(tool.updatedAt) }}</span>
          </div>
        </div>
      </div>

      <!-- Rating Section -->
      <div class="detail-section">
        <h2 class="section-title">
          用户评价
        </h2>
        <rating-display :statistics="ratingStatistics" />
        <rating-form
          :tool-id="tool.id"
          @success="handleRatingSubmit"
        />
        <rating-list
          ref="ratingListRef"
          :tool-id="tool.id"
          @rating-updated="handleRatingUpdated"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Tools,
  Download,
  CollectionTag,
  Link,
  Star,
  View,
  Collection,
  Warning
} from '@element-plus/icons-vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { useToolsStore } from '@/stores/tools'
import { useRatingStore } from '@/stores/rating'
import RatingDisplay from '@/components/rating/RatingDisplay.vue'
import RatingForm from '@/components/rating/RatingForm.vue'
import RatingList from '@/components/rating/RatingList.vue'

const props = defineProps({
  id: {
    type: [Number, String],
    required: true
  }
})

// 将 id 转换为数字（因为路由参数是字符串）
const toolId = computed(() => Number(props.id))

const toolsStore = useToolsStore()
const ratingStore = useRatingStore()

const tool = computed(() => toolsStore.currentTool)
const loading = computed(() => toolsStore.loading)
const ratingStatistics = ref(null)
const ratingListRef = ref(null)

// Markdown 渲染描述
const renderedDescription = computed(() => {
  if (!tool.value?.description) return ''

  try {
    // 配置 marked 选项
    marked.setOptions({
      breaks: true,
      gfm: true,
    })

    // 转换 markdown 为 HTML
    const rawHtml = marked.parse(tool.value.description)

    // 净化 HTML，移除危险标签但保留基本格式
    const cleanHtml = DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'code', 'pre', 'blockquote', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'a', 'table', 'thead', 'tbody', 'tr', 'th', 'td'],
      ALLOWED_ATTR: ['href', 'title', 'target', 'rel', 'class', 'id'],
      ALLOW_DATA_ATTR: false,
      ALLOW_UNKNOWN_PROTOCOLS: false
    })

    return cleanHtml
  } catch (error) {
    console.error('Markdown 渲染失败:', error)
    // 降级为纯文本显示
    return tool.value.description
  }
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

const handleFavorite = async () => {
  try {
    await toolsStore.toggleFavorite(tool.value.id)
    ElMessage.success(tool.value.isFavorited ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('收藏失败:', error)
  }
}

const openCodehub = () => {
  if (tool.value.codehubUrl) {
    window.open(tool.value.codehubUrl, '_blank', 'noopener,noreferrer')
  }
}

const handleRatingSubmit = async (data) => {
  try {
    await ratingStore.createRating(tool.value.id, data)
    await fetchRatingStatistics(tool.value.id)
    // 刷新评价列表
    if (ratingListRef.value) {
      ratingListRef.value.refresh()
    }
  } catch (error) {
    console.error('提交评价失败:', error)
  }
}

const handleRatingUpdated = async () => {
  // 当有回复或删除操作时，刷新统计数据
  await fetchRatingStatistics(tool.value.id)
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

const formatDate = (date) => {
  if (!date) return 'N/A'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
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

.markdown-content {
  word-wrap: break-word;
  line-height: 1.8;

  h1, h2, h3, h4, h5, h6 {
    margin-top: $spacing-xl;
    margin-bottom: $spacing-md;
    font-weight: 600;
    line-height: 1.4;
    color: $text-color-primary;

    &:first-child {
      margin-top: 0;
    }
  }

  h1 {
    font-size: 32px;
    border-bottom: 2px solid $border-color-base;
    padding-bottom: $spacing-sm;
  }

  h2 {
    font-size: 28px;
    border-bottom: 1px solid $border-color-base;
    padding-bottom: $spacing-xs;
  }

  h3 {
    font-size: 24px;
  }

  h4 {
    font-size: 20px;
  }

  h5 {
    font-size: 18px;
  }

  h6 {
    font-size: 16px;
    color: $text-color-secondary;
  }

  p {
    margin: $spacing-md 0;

    &:first-child {
      margin-top: 0;
    }

    &:last-child {
      margin-bottom: 0;
    }
  }

  a {
    color: $primary-color;
    text-decoration: none;
    transition: $transition-fast;

    &:hover {
      color: $primary-hover;
      text-decoration: underline;
    }
  }

  strong, b {
    font-weight: 600;
    color: $text-color-primary;
  }

  em, i {
    font-style: italic;
  }

  code {
    background: $background-color-light;
    border: 1px solid $border-color-base;
    border-radius: $border-radius-small;
    padding: 2px 6px;
    font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
    font-size: $font-size-small;
    color: $primary-color;
  }

  pre {
    background: $background-color-darker;
    border: 1px solid $border-color-base;
    border-radius: $border-radius-base;
    padding: $spacing-lg;
    margin: $spacing-lg 0;
    overflow-x: auto;

    code {
      background: transparent;
      border: none;
      padding: 0;
      font-size: $font-size-base;
      line-height: 1.6;
      color: $text-color-regular;
    }
  }

  blockquote {
    margin: $spacing-lg 0;
    padding: $spacing-md $spacing-lg;
    border-left: 4px solid $primary-color;
    background: $background-color-light;
    color: $text-color-secondary;
    font-style: italic;

    p {
      margin: 0;
    }
  }

  ul, ol {
    margin: $spacing-md 0;
    padding-left: $spacing-xl;

    li {
      margin: $spacing-xs 0;

      > ul, > ol {
        margin: $spacing-xs 0;
      }
    }
  }

  ul {
    list-style-type: disc;

    ul {
      list-style-type: circle;

      ul {
        list-style-type: square;
      }
    }
  }

  ol {
    list-style-type: decimal;
  }

  table {
    width: 100%;
    border-collapse: collapse;
    margin: $spacing-lg 0;
    font-size: $font-size-base;

    th, td {
      border: 1px solid $border-color-base;
      padding: $spacing-md;
      text-align: left;
    }

    th {
      background: $background-color-light;
      font-weight: 600;
      color: $text-color-primary;
    }

    tr {
      &:nth-child(even) {
        background: $background-color-lighter;
      }

      &:hover {
        background: $background-color-light;
      }
    }
  }

  img {
    max-width: 100%;
    height: auto;
    border-radius: $border-radius-base;
    margin: $spacing-lg 0;
  }

  hr {
    border: none;
    border-top: 2px solid $border-color-base;
    margin: $spacing-xxl 0;
  }
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

.hot-score {
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.1), rgba(255, 152, 0, 0.05));
  border-color: rgba(255, 87, 34, 0.3);

  &:hover {
    border-color: rgba(255, 87, 34, 0.6);
    box-shadow: 0 4px 12px rgba(255, 87, 34, 0.2);
  }
}

.hot-icon {
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.2), rgba(255, 152, 0, 0.1));
  font-size: 24px;

  .el-icon {
    display: none;
  }
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md 0;
  border-bottom: 1px solid $border-color-base;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: $font-size-base;
  font-weight: 600;
  color: $text-color-secondary;
  flex-shrink: 0;
}

.info-value {
  font-size: $font-size-base;
  color: $text-color-primary;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
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

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: $spacing-xs;
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
