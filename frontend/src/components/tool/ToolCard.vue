<template>
  <div class="tool-card" @click="handleClick">
    <!-- 状态徽章 -->
    <div class="status-badge" :class="statusClass">
      {{ statusText }}
    </div>

    <!-- 热度分数徽章 -->
    <div v-if="hotScore" class="hot-score-badge" :class="hotScoreClass">
      <span class="hot-icon">🔥</span>
      <span>{{ hotScore.toFixed(0) }}</span>
    </div>

    <div class="tool-header">
      <div class="tool-icon">
        <el-icon size="24"><Tools /></el-icon>
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

    <div class="tool-stats">
      <div class="stat-item" title="GitHub Stars">
        <el-icon><Star /></el-icon>
        <span>{{ formatNumber(tool.stars) }}</span>
      </div>
      <div class="stat-item" title="浏览量">
        <el-icon><View /></el-icon>
        <span>{{ formatNumber(tool.viewCount) }}</span>
      </div>
      <div class="stat-item" title="收藏数">
        <el-icon><Collection /></el-icon>
        <span>{{ formatNumber(tool.favoriteCount) }}</span>
      </div>
      <div class="stat-item" title="安装量">
        <el-icon><Download /></el-icon>
        <span>{{ formatNumber(tool.installCount) }}</span>
      </div>
    </div>

    <!-- 安装命令 -->
    <div class="install-command" @click.stop="copyInstallCommand">
      <span class="command-text">{{ installCommand }}</span>
      <el-icon class="copy-icon"><DocumentCopy /></el-icon>
    </div>

    <div class="tool-footer">
      <div class="tags">
        <el-tag
          v-for="tag in displayTags"
          :key="tag"
          size="small"
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
        >
          <el-icon><Link /></el-icon>
          GitHub
        </el-button>
        <el-button
          type="primary"
          size="small"
          :plain="!tool.isFavorited"
          @click.stop="handleFavorite"
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
import { Tools, Star, View, Collection, CollectionTag, Download, DocumentCopy,
         CircleCheck, CircleClose, Link, Refresh, Edit, FolderOpened } from '@element-plus/icons-vue'
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
  background-color: $background-color-base;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-large;
  padding: $spacing-xl;
  padding-top: 48px; /* 为徽章留出空间 */
  cursor: pointer;
  transition: $transition-base;

  &:hover {
    border-color: $primary-color;
    transform: translateY(-4px);
    box-shadow: $box-shadow-glow;
  }
}

.status-badge {
  position: absolute;
  top: $spacing-md;
  left: $spacing-md;
  padding: $spacing-xs $spacing-sm;
  border-radius: $border-radius-base;
  font-weight: 600;
  font-size: 11px;
  z-index: 1;

  &.active {
    background: rgba($success-color, 0.15);
    color: $success-color;
    border: 1px solid rgba($success-color, 0.3);
  }

  &.inactive {
    background: rgba($info-color, 0.15);
    color: $info-color;
    border: 1px solid rgba($info-color, 0.3);
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
  border-radius: $border-radius-base;
  font-weight: 600;
  font-size: 11px;
  z-index: 1;

  &.high {
    background: rgba($hot-score-high, 0.15);
    color: $hot-score-high;
    border: 1px solid rgba($hot-score-high, 0.3);
  }

  &.medium {
    background: rgba($hot-score-medium, 0.15);
    color: $hot-score-medium;
    border: 1px solid rgba($hot-score-medium, 0.3);
  }

  &.low {
    background: rgba($hot-score-low, 0.15);
    color: $hot-score-low;
    border: 1px solid rgba($hot-score-low, 0.3);
  }

  .hot-icon {
    font-size: 12px;
  }
}

.tool-header {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
  align-items: flex-start;
}

.tool-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, $primary-color, #00ffcc);
  border-radius: $border-radius-base;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000;
  flex-shrink: 0;

  .el-icon {
    font-size: 20px;
  }
}

.tool-title {
  flex: 1;
  min-width: 0;
  overflow: hidden;

  h3 {
    margin: 0 0 $spacing-sm;
    font-size: $font-size-large;
    font-weight: 600;
    color: $text-color-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding-right: $spacing-md;
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
  min-height: 42px;

  p {
    margin: 0;
    color: $text-color-regular;
    font-size: $font-size-base;
    line-height: 1.5;
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
  background: linear-gradient(135deg, rgba($primary-color, 0.1), rgba($primary-color, 0.05));
  border: 1px solid rgba($primary-color, 0.3);
  border-radius: $border-radius-base;
  color: $primary-color;
  font-size: $font-size-small;
  font-weight: 600;
}

.tool-stats {
  display: flex;
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;
  padding: $spacing-md 0;
  border-top: 1px solid $border-color-base;
  border-bottom: 1px solid $border-color-base;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  color: $text-color-regular;
  font-size: $font-size-small;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;

  .el-icon {
    color: $text-color-secondary;
  }
}

.install-command {
  background: $background-color-light;
  border: 1px solid $border-color-base;
  border-radius: $border-radius-base;
  padding: $spacing-sm $spacing-md;
  margin-bottom: $spacing-lg;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: $font-size-small;
  color: $primary-color;
  cursor: pointer;
  transition: $transition-fast;
  display: flex;
  align-items: center;
  justify-content: space-between;

  &:hover {
    background: $background-color-lighter;
    border-color: $primary-color;
    box-shadow: 0 0 8px rgba(0, 255, 157, 0.2);
  }

  .command-text {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .copy-icon {
    font-size: 14px;
    flex-shrink: 0;
    margin-left: $spacing-sm;
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

  :deep(.el-tag) {
    background: $background-color-light;
    border-color: $border-color-base;
    color: $text-color-regular;
    flex-shrink: 0;
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

  .el-button {
    padding: 4px 8px;
    font-size: $font-size-small;

    .el-icon {
      margin-right: 2px;
    }
  }
}

@media (max-width: 768px) {
  .tool-card {
    padding: $spacing-lg;
  }

  .tool-icon {
    width: 40px;
    height: 40px;

    .el-icon {
      font-size: 18px;
    }
  }

  .tool-title h3 {
    font-size: $font-size-base;
  }

  .tool-stats {
    gap: $spacing-md;
  }

  .stat-item {
    font-size: 12px;
  }

  .tool-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .tags {
    width: 100%;
    margin-bottom: $spacing-sm;
  }

  .actions {
    width: 100%;
    justify-content: center;
  }
}
</style>
