<template>
  <div class="tools-page">
    <div class="page-header">
      <h1>工具市场</h1>
      <div class="filters">
        <el-button
          type="primary"
          @click="handleAddTool"
        >
          <el-icon><Plus /></el-icon>
          添加工具
        </el-button>
        <el-button
          type="success"
          :loading="syncingSkills"
          @click="handleSyncAgentSkills"
        >
          <el-icon><Refresh /></el-icon>
          同步 Skills
        </el-button>
        <el-select
          v-model="sortBy"
          placeholder="排序"
          style="width: 100px"
          @change="handleSort"
        >
          <el-option
            label="最新"
            value="latest"
          />
          <el-option
            label="最热"
            value="hot"
          />
        </el-select>
      </div>
    </div>

    <!-- 分类 Tab -->
    <el-tabs
      v-model="activeTab"
      class="category-tabs"
      @tab-change="handleTabChange"
    >
      <el-tab-pane
        label="全部"
        name="all"
      >
        <template #label>
          <span class="tab-label">
            <span>全部</span>
            <span class="tab-count">{{ total }}</span>
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane
        v-for="category in categories"
        :key="category.id"
        :name="String(category.id)"
      >
        <template #label>
          <span class="tab-label">
            <span>{{ category.name }}</span>
            <span class="tab-count">{{ getCategoryCount(category.id) }}</span>
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <div class="tools-grid">
      <el-skeleton
        v-if="loading"
        :rows="6"
        animated
      />
      <div
        v-else-if="filteredTools.length === 0"
        class="empty-state"
      >
        <el-empty description="暂无工具" />
      </div>
      <tool-card
        v-for="tool in filteredTools"
        v-else
        :key="tool.id"
        :tool="tool"
        :hot-score="tool.hotScoreDaily"
        :admin-mode="adminMode"
        @click="handleToolClick"
        @favorite="handleFavorite"
        @publish="handlePublish"
        @unpublish="handleUnpublish"
        @synced="handleSynced"
        @edit="handleEdit"
      />
    </div>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      class="pagination"
      :total="total"
      :page-sizes="[24, 48, 60, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>

  <!-- 添加/编辑工具对话框 -->
  <tool-form-dialog
    v-model="showAddDialog"
    :tool="editingTool"
    @success="handleToolSuccess"
  />
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { useToolsStore } from '@/stores/tools'
import { useUserStore } from '@/stores/user'
import ToolCard from '@/components/tool/ToolCard.vue'
import ToolFormDialog from '@/components/tool/ToolFormDialog.vue'

const router = useRouter()
const toolsStore = useToolsStore()
const userStore = useUserStore()

const currentPage = ref(1)
const pageSize = ref(24)
const sortBy = ref('latest')
const activeTab = ref('all')
const showAddDialog = ref(false)
const editingTool = ref(null)
const syncingSkills = ref(false)
const allTools = ref([]) // 存储所有工具用于分类统计

// 管理模式
const adminMode = computed(() => userStore.adminMode)

const tools = computed(() => toolsStore.tools)
const loading = computed(() => toolsStore.loading)
const total = computed(() => toolsStore.total)

// 计算所有唯一分类
const categories = computed(() => {
  const categoryMap = new Map()
  // 基于所有工具（不仅仅是当前页）计算分类
  const toolsToUse = allTools.value.length > 0 ? allTools.value : tools.value
  toolsToUse.forEach(tool => {
    if (tool.categoryId && tool.categoryName) {
      categoryMap.set(tool.categoryId, {
        id: tool.categoryId,
        name: tool.categoryName
      })
    }
  })
  return Array.from(categoryMap.values()).sort((a, b) => a.id - b.id)
})

// 获取某个分类的工具数量（基于所有工具）
const getCategoryCount = (categoryId) => {
  const toolsToUse = allTools.value.length > 0 ? allTools.value : tools.value
  return toolsToUse.filter(tool => tool.categoryId === categoryId).length
}

// 根据 Tab 筛选工具
const filteredTools = computed(() => {
  if (activeTab.value === 'all') {
    return tools.value
  }
  const categoryId = parseInt(activeTab.value)
  return tools.value.filter(tool => tool.categoryId === categoryId)
})

onMounted(async () => {
  // 首先加载所有工具用于分类统计
  await fetchAllToolsForStats()
  // 然后加载第一页工具
  fetchTools()
})

watch([currentPage, pageSize], () => {
  fetchTools()
})

// 加载所有工具用于统计（后台请求，不影响UI）
const fetchAllToolsForStats = async () => {
  try {
    // 获取所有工具用于分类统计
    const response = await fetch('http://localhost:8080/api/tools?page=0&size=1000', {
      credentials: 'include'
    })
    const data = await response.json()
    allTools.value = data.content || []
    console.log('[Tools] Fetched all tools for stats:', allTools.value.length)
  } catch (error) {
    console.error('[Tools] Failed to fetch all tools for stats:', error)
  }
}

const fetchTools = () => {
  const page = currentPage.value - 1
  toolsStore.fetchTools(page, pageSize.value)
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleSort = () => {
  currentPage.value = 1
  fetchTools()
}

const handleTabChange = (tabName) => {
  activeTab.value = tabName
  // 切换tab时重置到第一页
  if (currentPage.value !== 1) {
    currentPage.value = 1
  }
}

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

const handlePublish = async (tool) => {
  try {
    await toolsStore.publishTool(tool.id)
    ElMessage.success('工具已上架')
  } catch (error) {
    ElMessage.error('上架失败')
    console.error('上架失败:', error)
  }
}

const handleUnpublish = async (tool) => {
  try {
    await toolsStore.unpublishTool(tool.id)
    ElMessage.success('工具已下架')
  } catch (error) {
    ElMessage.error('下架失败')
    console.error('下架失败:', error)
  }
}

const handleSynced = (tool) => {
  ElMessage.success(`${tool.name} 的Codehub数据已同步`)
}

const handleAddTool = () => {
  editingTool.value = null
  showAddDialog.value = true
}

const handleEdit = (tool) => {
  // 保存工具的原始分类ID，用于对比是否修改了分类
  editingTool.value = {
    ...tool,
    originalCategoryId: tool.categoryId
  }
  showAddDialog.value = true
}

const handleToolSuccess = async () => {
  // 保存编辑前的工具信息
  const editingToolId = editingTool.value?.id
  const previousCategoryId = editingTool.value?.originalCategoryId || editingTool.value?.categoryId

  editingTool.value = null

  // 刷新所有工具数据（包括分类统计）
  await fetchAllToolsForStats()

  // 刷新当前页工具列表
  await fetchTools()

  // 如果是编辑模式且修改了分类，自动切换到新的分类tab
  if (editingToolId && previousCategoryId) {
    // 从allTools中找到刚才编辑的工具，查看它的最新分类
    const updatedTool = allTools.value.find(t => t.id === editingToolId)
    if (updatedTool && updatedTool.categoryId !== previousCategoryId) {
      // 切换到新的分类tab
      activeTab.value = String(updatedTool.categoryId)
      ElMessage.success(`工具已移动到 ${updatedTool.categoryName || '新分类'}`)
      return
    }
  }

  ElMessage.success(editingToolId ? '保存成功' : '创建成功')
}

const handleSyncAgentSkills = async () => {
  try {
    await ElMessageBox.confirm(
      '将从 Codehub 搜索并同步 Agent Skills 仓库到工具市场。\n\n此操作可能需要 30-60 秒，请耐心等待。',
      '确认同步',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    syncingSkills.value = true

    // 显示进度提示
    const loadingMessage = ElMessage({
      message: '正在搜索并同步 Agent Skills，请稍候...',
      type: 'info',
      duration: 0,
      showClose: false
    })

    try {
      const result = await toolsStore.autoDiscoverAgentSkills()

      // 关闭加载提示
      loadingMessage.close()

      // 显示详细的同步结果
      const message = `同步完成！\n\n总计: ${result.total || 0} 个仓库\n新增: ${result.created || 0} 个工具\n更新: ${result.updated || 0} 个工具\n跳过: ${result.skipped || 0} 个${result.errors > 0 ? `\n\n失败: ${result.errors} 个` : ''}`

      await ElMessageBox.alert(message, '同步结果', {
        confirmButtonText: '确定',
        type: result.errors > 0 ? 'warning' : 'success',
        dangerouslyUseHTMLString: false
      })

      // 刷新列表
      fetchTools()
    } catch (innerError) {
      loadingMessage.close()
      throw innerError
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消操作
      return
    }

    // 详细的错误处理
    let errorMessage = '同步失败，请稍后重试'

    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      errorMessage = '同步超时，Codehub API 响应较慢，请稍后重试'
    } else if (error.response) {
      errorMessage = `服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`
    } else if (error.message) {
      errorMessage = `同步失败: ${error.message}`
    }

    ElMessage.error(errorMessage)
    console.error('同步 Agent Skills 失败:', error)
  } finally {
    syncingSkills.value = false
  }
}
</script>

<style scoped>
.tools-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: $spacing-lg;
  flex-wrap: wrap;
}

.page-header h1 {
  margin: 0;
  font-size: 32px;
  color: #e0e0e0;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: $spacing-md;
  }

  .page-header h1 {
    font-size: 24px;
    text-align: center;
  }

  .filters {
    justify-content: center;
  }
}

.filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;

  .el-switch {
    --el-switch-on-text: '开';
    --el-switch-off-text: '关';
  }

  .el-button {
    white-space: nowrap;
  }

  @media (max-width: 768px) {
    gap: 8px;

    .el-switch {
      flex-shrink: 0;
    }
  }
}

.category-tabs {
  margin-bottom: 24px;
  background: var(--el-bg-color);
  border-radius: $border-radius-base;
  padding: 8px;

  :deep(.el-tabs__header) {
    margin: 0;
    border-bottom: 1px solid var(--el-border-color-light);
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    padding: 0 20px;
    height: 48px;
    line-height: 48px;
    font-size: $font-size-base;
    color: var(--el-text-color-regular);
    transition: $transition-fast;

    &:hover {
      color: var(--el-color-primary);
    }

    &.is-active {
      color: var(--el-color-primary);
      font-weight: 600;
    }
  }

  :deep(.el-tabs__active-bar) {
    background-color: var(--el-color-primary);
    height: 3px;
  }
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;

  .tab-count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 24px;
    height: 20px;
    padding: 0 6px;
    background: var(--el-fill-color-light);
    border-radius: $border-radius-base;
    font-size: $font-size-small;
    color: var(--el-text-color-secondary);
    font-weight: 500;
  }
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
  min-height: 300px;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
}

@media (max-width: 768px) {
  .tools-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 16px;
  }

  .category-tabs {
    :deep(.el-tabs__item) {
      padding: 0 16px;
      font-size: $font-size-small;
    }
  }
}

@media (max-width: 480px) {
  .tools-grid {
    grid-template-columns: 1fr;
  }

  .category-tabs {
    :deep(.el-tabs__nav) {
      display: flex;
      flex-wrap: wrap;
    }

    :deep(.el-tabs__item) {
      flex: 0 0 auto;
      padding: 0 12px;
      font-size: $font-size-small;
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
}
</style>
