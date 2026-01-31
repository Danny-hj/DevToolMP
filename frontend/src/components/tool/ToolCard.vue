<template>
  <div class="tool-card" @click="handleClick">
    <!-- 状态徽章 -->
    <div class="status-badge" :class="statusClass">
      <el-icon><component :is="statusIcon" /></el-icon>
      <span>{{ statusText }}</span>
    </div>

    <!-- 热度分数徽章 -->
    <div v-if="hotScore" class="hot-score-badge" :class="hotScoreClass">
      <span class="hot-icon">🔥</span>
      <span class="hot-value">{{ hotScore.toFixed(0) }}</span>
    </div>

    <div class="tool-header">
      <div class="tool-icon">
        {{ tool.name.charAt(0).toUpperCase() }}
      </div>
      <div class="tool-title">
        <h3>{{ tool.name }}</h3>
        <p class="tool-repo">
          <a
            v-if="tool.githubUrl"
            :href="tool.githubUrl"
            target="_blank"
            rel="noopener noreferrer"
            @click.stop
            class="github-link"
          >
            {{ tool.githubOwner }}/{{ tool.githubRepo }}
            <el-icon class="external-link-icon"><Link /></el-icon>
          </a>
          <span v-else-if="tool.githubOwner && tool.githubRepo">
            {{ tool.githubOwner }}/{{ tool.githubRepo }}
          </span>
          <span v-else class="no-repo">未关联 GitHub 仓库</span>
        </p>
      </div>
    </div>

    <div class="tool-description">
      <p>{{ tool.description || '暂无描述' }}</p>
    </div>

    <!-- 分类标签 -->
    <div v-if="tool.categoryName" class="tool-category">
      <el-icon><FolderOpened /></el-icon>
      <span>{{ tool.categoryName }}</span>
    </div>

    <!-- 统计数据 -->
    <div class="tool-stats">
      <div class="stat-item" title="GitHub Stars">
        <div class="stat-icon">
          <el-icon><Star /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatNumber(tool.stars) }}</span>
          <span class="stat-label">Stars</span>
        </div>
      </div>
      <div class="stat-item" title="浏览量">
        <div class="stat-icon">
          <el-icon><View /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatNumber(tool.viewCount) }}</span>
          <span class="stat-label">浏览</span>
        </div>
      </div>
      <div class="stat-item" title="收藏数">
        <div class="stat-icon">
          <el-icon><Collection /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatNumber(tool.favoriteCount) }}</span>
          <span class="stat-label">收藏</span>
        </div>
      </div>
      <div class="stat-item" title="安装量">
        <div class="stat-icon">
          <el-icon><Download /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ formatNumber(tool.installCount) }}</span>
          <span class="stat-label">安装</span>
        </div>
      </div>
    </div>

    <!-- 安装命令 -->
    <div class="install-command" @click.stop="copyInstallCommand">
      <span class="command-icon">$</span>
      <span class="command-text">{{ installCommand }}</span>
      <el-icon class="copy-icon"><DocumentCopy /></el-icon>
    </div>

    <div class="tool-footer">
      <div class="tags">
        <el-tag
          v-for="tag in displayTags"
          :key="tag"
          size="small"
          class="tool-tag"
        >
          {{ tag }}
        </el-tag>
        <span v-if="tool.tags && tool.tags.length > 3" class="more-tags">
          +{{ tool.tags.length - 3 }}
        </span>
      </div>
      <div class="actions">
        <!-- 同步GitHub按钮 -->
        <el-button
          v-if="showSyncButton"
          :loading="syncing"
          type="info"
          size="small"
          plain
          @click.stop="handleSyncGitHub"
          class="action-btn"
        >
          <el-icon><Refresh /></el-icon>
          同步
        </el-button>
        <!-- 编辑按钮 -->
        <el-button
          v-if="showEditButton"
          type="warning"
          size="small"
          plain
          @click.stop="handleEdit"
          class="action-btn"
        >
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <!-- 上架/下架按钮 -->
        <el-button
          v-if="showPublishButton"
          :type="tool.status === 'active' ? 'warning' : 'success'"
          size="small"
          @click.stop="handleTogglePublish"
          class="action-btn"
        >
          <el-icon><component :is="tool.status === 'active' ? 'CircleClose' : 'CircleCheck'" /></el-icon>
          {{ tool.status === 'active' ? '下架' : '上架' }}
        </el-button>
        <!-- GitHub跳转按钮 -->
        <el-button
          v-if="tool.githubUrl"
          type="primary"
          size="small"
          plain
          @click.stop="openGitHub"
          class="action-btn"
        >
          <el-icon><Link /></el-icon>
          GitHub
        </el-button>
        <el-button
          type="primary"
          size="small"
          :plain="!tool.isFavorited"
          @click.stop="handleFavorite"
          class="action-btn favorite-btn"
        >
          <el-icon><CollectionTag /></el-icon>
          {{ tool.isFavorited ? '已收藏' : '收藏' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, View, Collection, CollectionTag, Download, DocumentCopy,
         CircleCheck, CircleClose, Link, Refresh, Edit, FolderOpened, Select, CircleCheckFilled } from '@element-plus/icons-vue'
import { useToolsStore } from '@/stores/tools'

const props = defineProps({
  tool: {
    type: Object,
    required: true
  },
  hotScore: {
    type: Number,
    default: null
  },
  showPublishButton: {
    type: Boolean,
    default: false
  },
  showSyncButton: {
    type: Boolean,
    default: false
  },
  showEditButton: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'favorite', 'publish', 'unpublish', 'synced', 'edit'])

const toolsStore = useToolsStore()
const syncing = ref(false)

const installCommand = computed(() => {
  if (props.tool.packageName) {
    return `npm install ${props.tool.packageName}`
  }
  if (props.tool.githubOwner && props.tool.githubRepo) {
    return `npm install ${props.tool.githubOwner}/${props.tool.githubRepo}`
  }
  return '请查看项目文档了解安装方式'
})

const displayTags = computed(() => {
  if (!props.tool.tags) return []
  return props.tool.tags.slice(0, 3)
})

const hotScoreClass = computed(() => {
  if (!props.hotScore) return 'low'
  if (props.hotScore > 1000) return 'high'
  if (props.hotScore > 500) return 'medium'
  return 'low'
})

const statusClass = computed(() => {
  return props.tool.status === 'active' ? 'active' : 'inactive'
})

const statusIcon = computed(() => {
  return props.tool.status === 'active' ? 'CircleCheckFilled' : 'WarningFilled'
})

const statusText = computed(() => {
  return props.tool.status === 'active' ? '已上架' : '已下架'
})

const handleClick = () => {
  emit('click', props.tool)
}

const handleFavorite = async () => {
  emit('favorite', props.tool)
}

const handleTogglePublish = async () => {
  if (props.tool.status === 'active') {
    emit('unpublish', props.tool)
  } else {
    emit('publish', props.tool)
  }
}

const handleSyncGitHub = async () => {
  syncing.value = true
  try {
    await toolsStore.syncGitHubData(props.tool.id)
    ElMessage.success('GitHub数据同步成功')
    emit('synced', props.tool)
  } catch (error) {
    ElMessage.error('GitHub数据同步失败')
  } finally {
    syncing.value = false
  }
}

const handleEdit = () => {
  emit('edit', props.tool)
}

const openGitHub = () => {
  if (props.tool.githubUrl) {
    window.open(props.tool.githubUrl, '_blank', 'noopener,noreferrer')
  }
}

const copyInstallCommand = async () => {
  try {
    await navigator.clipboard.writeText(installCommand.value)
    ElMessage.success('安装命令已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败,请手动复制')
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
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;

.tool-card {
  position: relative;
  background: linear-gradient(135deg, rgba($background-color-light, 0.5) 0%, rgba($background-color-base, 0.8) 100%);
  border: 1px solid $border-color-base;
  border-radius: $border-radius-xl;
  padding: $spacing-xl;
  padding-top: 56px; /* 为徽章留出更多空间 */
  cursor: pointer;
  transition: all $transition-base;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, $primary-color 0%, #00ffcc 100%);
    opacity: 0;
    transition: opacity $transition-base;
  }

  &:hover {
    border-color: rgba($primary-color, 0.5);
    transform: translateY(-6px);
    box-shadow:
      0 8px 24px rgba(0, 255, 157, 0.15),
      0 4px 12px rgba(0, 0, 0, 0.3);

    &::before {
      opacity: 1;
    }
  }
}

.status-badge {
  position: absolute;
  top: $spacing-md;
  left: $spacing-md;
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius-base;
  font-weight: 600;
  font-size: 11px;
  z-index: 1;
  backdrop-filter: blur(10px);

  .el-icon {
    font-size: 12px;
  }

  &.active {
    background: linear-gradient(135deg, rgba($success-color, 0.2), rgba($success-color, 0.1));
    color: $success-color;
    border: 1px solid rgba($success-color, 0.4);
    box-shadow: 0 2px 8px rgba($success-color, 0.3);
  }

  &.inactive {
    background: linear-gradient(135deg, rgba($warning-color, 0.2), rgba($warning-color, 0.1));
    color: $warning-color;
    border: 1px solid rgba($warning-color, 0.4);
    box-shadow: 0 2px 8px rgba($warning-color, 0.3);
  }
}

.hot-score-badge {
  position: absolute;
  top: $spacing-md;
  right: $spacing-md;
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius-large;
  font-weight: 700;
  font-size: 12px;
  z-index: 1;
  backdrop-filter: blur(10px);

  .hot-icon {
    font-size: 14px;
    animation: pulse 2s ease-in-out infinite;
  }

  .hot-value {
    font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  }

  &.high {
    background: linear-gradient(135deg, rgba(255, 87, 34, 0.2), rgba(255, 193, 7, 0.15));
    color: #ff5722;
    border: 1px solid rgba(255, 87, 34, 0.4);
    box-shadow: 0 2px 8px rgba(255, 87, 34, 0.3);
  }

  &.medium {
    background: linear-gradient(135deg, rgba(255, 152, 0, 0.2), rgba(255, 193, 7, 0.15));
    color: #ff9800;
    border: 1px solid rgba(255, 152, 0, 0.4);
    box-shadow: 0 2px 8px rgba(255, 152, 0, 0.3);
  }

  &.low {
    background: linear-gradient(135deg, rgba(76, 175, 80, 0.2), rgba(69, 90, 100, 0.15));
    color: #4caf50;
    border: 1px solid rgba(76, 175, 80, 0.4);
    box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.tool-header {
  display: flex;
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;
  align-items: flex-start;
}

.tool-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, $primary-color, #00ffcc);
  border-radius: $border-radius-large;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000;
  font-size: 24px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 255, 157, 0.3);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: inherit;
    padding: 2px;
    background: linear-gradient(135deg, $primary-color, #00ffcc);
    z-index: -1;
    opacity: 0.5;
    filter: blur(8px);
  }
}

.tool-title {
  flex: 1;
  min-width: 0;
  overflow: hidden;

  h3 {
    margin: 0 0 $spacing-sm;
    font-size: $font-size-large;
    font-weight: 700;
    color: $text-color-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding-right: $spacing-md;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
}

.tool-repo {
  margin: 0;
  font-size: $font-size-small;
  color: $text-color-secondary;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;

  .github-link {
    color: $primary-color;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    transition: $transition-fast;

    &:hover {
      color: lighten($primary-color, 10%);
      text-decoration: underline;
      text-shadow: 0 0 8px rgba($primary-color, 0.3);
    }

    .external-link-icon {
      font-size: 12px;
    }
  }

  .no-repo {
    color: $text-color-placeholder;
    font-style: italic;
  }
}

.tool-description {
  margin-bottom: $spacing-lg;
  min-height: 48px;

  p {
    margin: 0;
    color: $text-color-regular;
    font-size: $font-size-base;
    line-height: 1.6;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.tool-category {
  display: inline-flex;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-lg;
  padding: $spacing-xs $spacing-sm;
  background: linear-gradient(135deg, rgba($primary-color, 0.15), rgba($primary-color, 0.08));
  border: 1px solid rgba($primary-color, 0.4);
  border-radius: $border-radius-base;
  color: $primary-color;
  font-size: $font-size-small;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 255, 157, 0.15);
}

.tool-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
  padding: $spacing-md 0;
  border-top: 1px solid rgba($border-color-base, 0.5);
  border-bottom: 1px solid rgba($border-color-base, 0.5);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  padding: $spacing-sm;
  background: $background-color-light;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-base;
  transition: $transition-base;

  &:hover {
    background: $background-color-lighter;
    border-color: rgba($primary-color, 0.3);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 255, 157, 0.1);
  }
}

.stat-icon {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, rgba($primary-color, 0.1), rgba($primary-color, 0.05));
  border-radius: $border-radius-base;
  display: flex;
  align-items: center;
  justify-content: center;

  .el-icon {
    font-size: 14px;
    color: $primary-color;
  }
}

.stat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-value {
  font-size: $font-size-base;
  font-weight: 700;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  color: $text-color-primary;
  line-height: 1;
}

.stat-label {
  font-size: 10px;
  color: $text-color-secondary;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.install-command {
  background: linear-gradient(135deg, rgba($background-color-darker, 0.8), rgba($background-color-base, 0.6));
  border: 1px solid $border-color-base;
  border-radius: $border-radius-base;
  padding: $spacing-md $spacing-lg;
  margin-bottom: $spacing-lg;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: $font-size-small;
  color: $primary-color;
  cursor: pointer;
  transition: $transition-base;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, transparent, rgba($primary-color, 0.1), transparent);
    opacity: 0;
    transition: opacity $transition-base;
  }

  &:hover {
    background: linear-gradient(135deg, rgba($background-color-darker, 0.9), rgba($background-color-base, 0.7));
    border-color: $primary-color;
    box-shadow: 0 4px 12px rgba(0, 255, 157, 0.2);
    transform: translateY(-2px);

    &::before {
      opacity: 1;
    }
  }

  .command-icon {
    color: $text-color-secondary;
    font-weight: 600;
    user-select: none;
  }

  .command-text {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .copy-icon {
    font-size: 16px;
    flex-shrink: 0;
    transition: transform $transition-fast;
  }

  &:hover .copy-icon {
    transform: scale(1.1);
  }
}

.tool-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: $spacing-md;
  flex-wrap: wrap;
}

.tags {
  display: flex;
  gap: $spacing-sm;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  flex-wrap: wrap;
}

.tool-tag {
  background: linear-gradient(135deg, rgba($primary-color, 0.1), rgba($primary-color, 0.05)) !important;
  border-color: rgba($primary-color, 0.3) !important;
  color: $text-color-primary !important;
  font-weight: 500;
  transition: $transition-base !important;

  &:hover {
    background: rgba($primary-color, 0.15) !important;
    border-color: rgba($primary-color, 0.5) !important;
  }
}

.more-tags {
  font-size: $font-size-small;
  color: $text-color-secondary;
  padding: $spacing-sm;
  flex-shrink: 0;
}

.actions {
  flex-shrink: 0;
  display: flex;
  gap: $spacing-xs;
  flex-wrap: wrap;
  align-items: center;

  .action-btn {
    padding: 6px 12px;
    font-size: $font-size-small;
    font-weight: 500;
    transition: all $transition-fast;

    &.favorite-btn {
      background: linear-gradient(135deg, rgba($primary-color, 0.1), rgba($primary-color, 0.05)) !important;
      border-color: rgba($primary-color, 0.3) !important;

      &:hover {
        background: rgba($primary-color, 0.2) !important;
        border-color: $primary-color !important;
        box-shadow: 0 2px 8px rgba(0, 255, 157, 0.3);
      }
    }

    .el-icon {
      margin-right: 4px;
    }
  }
}

@media (max-width: 768px) {
  .tool-card {
    padding: $spacing-lg;
    padding-top: 52px;
  }

  .tool-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }

  .tool-title h3 {
    font-size: $font-size-base;
  }

  .tool-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: $spacing-sm;
  }

  .stat-item {
    padding: $spacing-xs;
  }

  .stat-icon {
    width: 24px;
    height: 24px;

    .el-icon {
      font-size: 12px;
    }
  }

  .stat-value {
    font-size: 12px;
  }

  .stat-label {
    font-size: 9px;
  }

  .install-command {
    padding: $spacing-sm;
    font-size: $font-size-small;
  }

  .tool-footer {
    flex-direction: column;
    align-items: stretch;
    gap: $spacing-sm;
  }

  .tags {
    width: 100%;
    margin-bottom: $spacing-sm;
  }

  .actions {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
    gap: $spacing-xs;

    .action-btn {
      padding: 5px 10px;
      font-size: 12px;
    }
  }
}

@media (max-width: 480px) {
  .tool-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-icon {
    width: 22px;
    height: 22px;
  }
}
</style>
