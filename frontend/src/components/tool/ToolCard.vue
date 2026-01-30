<template>
  <div class="tool-card" @click="handleClick">
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
      <div class="stat-item">
        <el-icon><Star /></el-icon>
        <span>{{ formatNumber(tool.stars) }}</span>
      </div>
      <div class="stat-item">
        <el-icon><View /></el-icon>
        <span>{{ formatNumber(tool.viewCount) }}</span>
      </div>
      <div class="stat-item">
        <el-icon><Collection /></el-icon>
        <span>{{ formatNumber(tool.favoriteCount) }}</span>
      </div>
    </div>

    <div class="tool-footer">
      <div class="tags">
        <el-tag
          v-for="tag in tool.tags"
          :key="tag"
          size="small"
          type="info"
        >
          {{ tag }}
        </el-tag>
      </div>
      <div class="actions">
        <el-button type="primary" size="small" @click.stop="handleFavorite">
          <el-icon><CollectionTag /></el-icon>
          {{ tool.isFavorited ? '已收藏' : '收藏' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Tools, Star, View, Collection, CollectionTag } from '@element-plus/icons-vue'

const props = defineProps({
  tool: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click', 'favorite'])

const handleClick = () => {
  emit('click', props.tool)
}

const handleFavorite = async () => {
  emit('favorite', props.tool)
}

const formatNumber = (num) => {
  if (!num) return 0
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num.toString()
}
</script>

<style scoped>
.tool-card {
  background-color: #2a2a2a;
  border: 1px solid #333;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.tool-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.tool-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.tool-icon {
  width: 48px;
  height: 48px;
  background-color: #409eff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.tool-title {
  flex: 1;
}

.tool-title h3 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #e0e0e0;
}

.tool-repo {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.tool-description {
  margin-bottom: 16px;
}

.tool-description p {
  margin: 0;
  color: #b0b0b0;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.tool-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  padding: 12px 0;
  border-top: 1px solid #333;
  border-bottom: 1px solid #333;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #b0b0b0;
  font-size: 14px;
}

.stat-item .el-icon {
  color: #909399;
}

.tool-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tags {
  display: flex;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.actions {
  margin-left: 12px;
}
</style>
