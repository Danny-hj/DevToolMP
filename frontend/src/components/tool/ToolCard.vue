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
        <p class="tool-repo">{{ tool.githubOwner }}/{{ tool.githubRepo }}</p>
      </div>
    </div>

    <div class="tool-description">
      <p>{{ tool.description || '暂无描述' }}</p>
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
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Tools, Star, View, Collection, CollectionTag, Download, DocumentCopy, CircleCheck, CircleClose } from '@element-plus/icons-vue'

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
    default: false // 默认不显示上架按钮，可根据需求控制
  }
})

const emit = defineEmits(['click', 'favorite', 'publish', 'unpublish'])

const installCommand = computed(() => {
  if (props.tool.packageName) {
    return `npm install ${props.tool.packageName}`
  }
  return `npm install ${props.tool.githubOwner}/${props.tool.githubRepo}`
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
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-large;
  font-weight: 600;
  font-size: $font-size-small;

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
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-radius: $border-radius-large;
  font-weight: 600;
  font-size: $font-size-small;

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
    font-size: 14px;
  }
}

.tool-header {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;
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
  flex-shrink: 0;
}

.tool-title {
  flex: 1;
  min-width: 0;

  h3 {
    margin: 0 0 $spacing-sm;
    font-size: $font-size-large;
    font-weight: 600;
    color: $text-color-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.tool-repo {
  margin: 0;
  font-size: $font-size-small;
  color: $text-color-secondary;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
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
  align-items: center;
  gap: $spacing-md;
}

.tags {
  display: flex;
  gap: $spacing-sm;
  flex: 1;
  overflow: hidden;
  flex-wrap: wrap;

  :deep(.el-tag) {
    background: $background-color-light;
    border-color: $border-color-base;
    color: $text-color-regular;
  }
}

.more-tags {
  font-size: $font-size-small;
  color: $text-color-secondary;
  padding: $spacing-sm;
}

.actions {
  flex-shrink: 0;
  display: flex;
  gap: $spacing-sm;
}

@media (max-width: 768px) {
  .tool-card {
    padding: $spacing-lg;
  }

  .tool-stats {
    gap: $spacing-md;
  }

  .stat-item {
    font-size: 12px;
  }

  .actions {
    flex-direction: column;
  }
}
</style>
