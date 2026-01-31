<template>
  <div class="tool-detail-page">
    <el-skeleton v-if="loading" :rows="10" animated :loading="loading" />
    <template v-else-if="tool">
      <div class="tool-header">
          <div class="tool-icon">
            <el-icon size="48"><Tools /></el-icon>
          </div>
        <div class="tool-info">
          <h1>{{ tool.name }}</h1>
          <p class="tool-repo">
            <a v-if="tool.githubUrl" :href="tool.githubUrl" target="_blank" rel="noopener">
              {{ tool.githubOwner }}/{{ tool.githubRepo }}
            </a>
            <span v-else-if="tool.githubOwner && tool.githubRepo">
              {{ tool.githubOwner }}/{{ tool.githubRepo }}
            </span>
            <span v-else class="no-repo">未关联 GitHub 仓库</span>
          </p>
          <p class="tool-description">{{ tool.description }}</p>
          <div class="tool-tags">
            <el-tag
              v-for="tag in tool.tags"
              :key="tag"
              size="small"
              type="info"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
        <div class="tool-actions">
          <el-button type="primary" @click="handleInstall">
            <el-icon><Download /></el-icon>
            安装
          </el-button>
          <el-button @click="handleFavorite">
            <el-icon><CollectionTag /></el-icon>
            {{ tool.isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </div>

      <div class="tool-stats">
        <div class="stat-item">
          <div class="stat-value">{{ formatNumber(tool.stars) }}</div>
          <div class="stat-label">Stars</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatNumber(tool.forks) }}</div>
          <div class="stat-label">Forks</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatNumber(tool.viewCount) }}</div>
          <div class="stat-label">浏览</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatNumber(tool.favoriteCount) }}</div>
          <div class="stat-label">收藏</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatNumber(tool.installCount) }}</div>
          <div class="stat-label">安装</div>
        </div>
      </div>

      <div class="tool-rating-section">
        <h2>用户评价</h2>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Tools, Download, CollectionTag } from '@element-plus/icons-vue'
import { useToolsStore } from '@/stores/tools'
import { useRatingStore } from '@/stores/rating'
import RatingDisplay from '@/components/rating/RatingDisplay.vue'
import RatingForm from '@/components/rating/RatingForm.vue'

const route = useRoute()
const toolsStore = useToolsStore()
const ratingStore = useRatingStore()

const tool = computed(() => toolsStore.currentTool)
const loading = computed(() => toolsStore.loading)
const ratingStatistics = ref(null)

onMounted(async () => {
  const id = Number(route.params.id)
  await toolsStore.fetchToolDetail(id)
  await toolsStore.recordView(id)
  await fetchRatingStatistics(id)
})

const fetchRatingStatistics = async (toolId) => {
  try {
    ratingStatistics.value = await ratingStore.fetchStatistics(toolId)
  } catch (error) {
    console.error('获取评分统计失败:', error)
  }
}

const handleInstall = () => {
  let installCommand
  if (tool.value.packageName) {
    installCommand = `npm install ${tool.value.packageName}`
  } else if (tool.value.githubOwner && tool.value.githubRepo) {
    installCommand = `npm install ${tool.value.githubOwner}/${tool.value.githubRepo}`
  } else {
    ElMessage.warning('该工具未提供安装命令，请查看项目文档')
    return
  }

  navigator.clipboard.writeText(installCommand)
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

const handleRatingSubmit = async (data) => {
  try {
    await ratingStore.createRating(tool.value.id, data)
    await fetchRatingStatistics(tool.value.id)
  } catch (error) {
    console.error('提交评价失败:', error)
  }
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

// 组件卸载时清理遮罩层
onUnmounted(() => {
  console.log('[ToolDetail] Component unmounted, cleaning up...')
  // 确保组件卸载时清理所有可能存在的遮罩层
  const overlays = document.querySelectorAll('.el-overlay')
  overlays.forEach(overlay => {
    overlay.remove()
  })
})
</script>

<style scoped>
.tool-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.tool-header {
  display: flex;
  gap: 24px;
  background-color: #2a2a2a;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
}

.tool-icon {
  width: 80px;
  height: 80px;
  background-color: #409eff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.tool-info {
  flex: 1;
}

.tool-info h1 {
  margin: 0 0 8px;
  font-size: 32px;
  color: #e0e0e0;
}

.tool-repo {
  margin: 0 0 12px;
}

.tool-repo a {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.tool-repo a:hover {
  text-decoration: underline;
}

.tool-repo .no-repo {
  color: #909399;
  font-style: italic;
}

.tool-description {
  margin: 0 0 16px;
  color: #b0b0b0;
  font-size: 16px;
  line-height: 1.6;
}

.tool-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tool-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
}

.tool-stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  background-color: #2a2a2a;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.tool-rating-section h2 {
  margin: 0 0 16px;
  font-size: 24px;
  color: #e0e0e0;
}
</style>
