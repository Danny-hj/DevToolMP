<template>
  <div class="ranking-container">
    <!-- ASCII 艺术标题 -->
    <div class="ranking-header">
      <h1 class="title">
        <pre class="ascii-art">
  ____                 _       _____                _         _____ _
 / ___|_ __ _   _ ___| |_    |  ___|__  _ __   ___| |__     |  ___| | _____  __
| |   | '__| | | / __| __|   | |_ / _ \| '_ \ / __| '_ \    | |_  | |/ _ \ \/ /
| |___| |  | |_| \__ \ |_    |  _| (_) | | | | (__| | | |   |  _| | |  __/>  <
 \____|_|   \__, |___/\__|   |_|  \___/|_| |_|\___|_| |_|   |_|   |_|\___/_/\_\
            |___/
        </pre>
        <span class="title-text">排行榜</span>
      </h1>
      <p class="subtitle">发现最受欢迎的开发工具</p>
    </div>

    <!-- 标签页 -->
    <div class="ranking-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部总榜" name="alltime">
          <span class="tab-label">
            <el-icon><Trophy /></el-icon>
            全部总榜
          </span>
        </el-tab-pane>
        <el-tab-pane label="周榜" name="weekly">
          <span class="tab-label">
            <el-icon><Calendar /></el-icon>
            周榜
          </span>
        </el-tab-pane>
        <el-tab-pane label="日榜" name="daily">
          <span class="tab-label">
            <el-icon><Sunny /></el-icon>
            日榜
          </span>
        </el-tab-pane>
        <el-tab-pane label="趋势榜" name="trending">
          <span class="tab-label">
            <el-icon><TrendCharts /></el-icon>
            趋势榜
          </span>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 排行榜内容 -->
    <div v-loading="loading" class="ranking-content">
      <!-- 表格展示 -->
      <div v-if="!isEmpty && !loading" class="ranking-table-wrapper">
        <el-table :data="rankings" class="ranking-table">
          <!-- 排名 -->
          <el-table-column label="排名" width="80" align="center">
            <template #default="{ $index }">
              <span
                class="rank-number"
                :class="getRankClass($index)"
              >
                {{ $index + 1 }}
              </span>
            </template>
          </el-table-column>

          <!-- 工具信息 -->
          <el-table-column label="工具名称" min-width="300">
            <template #default="{ row }">
              <div class="tool-name-cell">
                <div class="tool-icon">
                  {{ row.name.charAt(0).toUpperCase() }}
                </div>
                <div class="tool-info">
                  <router-link :to="`/tools/${row.id}`" class="name">
                    {{ row.name }}
                  </router-link>
                  <p class="description">{{ row.description }}</p>
                </div>
              </div>
            </template>
          </el-table-column>

          <!-- 热度分数 -->
          <el-table-column label="热度分数" width="140" align="center">
            <template #default="{ row }">
              <div
                class="hot-score-badge"
                :class="getHotScoreClass(row.hotScore)"
              >
                <span class="hot-icon">🔥</span>
                <span>{{ row.hotScore?.toFixed(0) || '0' }}</span>
              </div>
            </template>
          </el-table-column>

          <!-- 安装量 -->
          <el-table-column label="安装量" width="120" align="right">
            <template #default="{ row }">
              <span class="stat-number">{{ formatNumber(row.installCount) }}</span>
            </template>
          </el-table-column>

          <!-- 收藏数 -->
          <el-table-column label="收藏数" width="120" align="right">
            <template #default="{ row }">
              <span class="stat-number">{{ formatNumber(row.favoriteCount) }}</span>
            </template>
          </el-table-column>

          <!-- 浏览数 -->
          <el-table-column label="浏览数" width="120" align="right">
            <template #default="{ row }">
              <span class="stat-number">{{ formatNumber(row.viewCount) }}</span>
            </template>
          </el-table-column>

          <!-- 变化 -->
          <el-table-column label="变化" width="100" align="center">
            <template #default="{ row }">
              <div
                class="change-indicator"
                :class="getChangeClass(row.changePercentage)"
              >
                <el-icon>
                  <component :is="getChangeIcon(row.changePercentage)" />
                </el-icon>
                <span>{{ Math.abs(row.changePercentage)?.toFixed(1) || '0' }}%</span>
              </div>
            </template>
          </el-table-column>

          <!-- 操作 -->
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <div
                class="install-command"
                @click="copyInstallCommand(row)"
              >
                <span>安装</span>
                <el-icon class="copy-icon"><DocumentCopy /></el-icon>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 空状态 -->
      <div v-if="isEmpty && !loading" class="empty-state">
        <div class="icon">📊</div>
        <p class="text">暂无排行榜数据</p>
      </div>

      <!-- 错误状态 -->
      <el-alert
        v-if="hasError"
        type="error"
        :title="error"
        :closable="false"
      />

      <!-- 分页组件 -->
      <div v-if="hasPagination && !loading" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pagination.size"
          :total="pagination.total"
          :page-count="pagination.totalPages"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Trophy,
  Calendar,
  Sunny,
  TrendCharts,
  ArrowUp,
  ArrowDown,
  Minus,
  DocumentCopy
} from '@element-plus/icons-vue'
import { useRankingStore } from '../stores/ranking'

const route = useRoute()
const router = useRouter()
const {
  rankings,
  loading,
  error,
  activeTab,
  pagination,
  isEmpty,
  hasError,
  hasPagination,
  fetchRankings,
  switchTab,
  changePage
} = useRankingStore()

// 当前页码（从0开始）
const currentPage = computed({
  get: () => pagination.value.page + 1,
  set: (val) => {
    // Element Plus 分页组件从1开始，需要转换
  }
})

const loadData = async () => {
  await fetchRankings(activeTab.value, 0)
}

onMounted(() => {
  // 检查路由参数中的 tab
  const tabFromQuery = route.query.tab
  if (tabFromQuery && tabFromQuery !== activeTab.value) {
    // 如果有 tab 参数且与当前不同，先切换 tab
    switchTab(tabFromQuery)
  } else {
    // 否则加载当前 tab 的数据
    loadData()
  }
})

// 监听路由 query 参数变化
watch(() => route.query.tab, (newTab) => {
  if (newTab && newTab !== activeTab.value) {
    switchTab(newTab)
  }
})

const handleTabChange = (tab) => {
  // 如果 tab 没有变化，不执行任何操作
  if (tab === activeTab.value) return

  // 更新 URL query 参数
  router.push({ path: '/ranking', query: { tab } })
  // 注意：不在这里调用 switchTab，让 URL 变化触发下面的 watch
}

// 处理页码变化
const handlePageChange = (page) => {
  // Element Plus 分页组件从1开始，需要转换为从0开始
  changePage(page - 1)
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听路由 query 参数变化（用于处理从首页跳转的情况）
watch(() => route.query.tab, (newTab) => {
  if (newTab && newTab !== activeTab.value) {
    switchTab(newTab)
  }
})

const getRankClass = (index) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return 'rank-other'
}

const getHotScoreClass = (score) => {
  if (!score) return 'low'
  if (score > 1000) return 'high'
  if (score > 500) return 'medium'
  return 'low'
}

const getChangeClass = (percentage) => {
  if (!percentage) return 'neutral'
  if (percentage > 0) return 'up'
  if (percentage < 0) return 'down'
  return 'neutral'
}

const getChangeIcon = (percentage) => {
  if (!percentage) return Minus
  if (percentage > 0) return ArrowUp
  if (percentage < 0) return ArrowDown
  return Minus
}

const formatNumber = (num) => {
  if (!num) return '0'
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
  return num.toString()
}

const copyInstallCommand = async (tool) => {
  const command = tool.packageName
    ? `npm install ${tool.packageName}`
    : `npm install ${tool.codehubOwner}/${tool.codehubRepo}`

  try {
    await navigator.clipboard.writeText(command)
    ElMessage.success('安装命令已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败,请手动复制')
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables.scss' as *;
@use '@/assets/styles/ranking.scss';

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: $spacing-sm;
}
</style>
