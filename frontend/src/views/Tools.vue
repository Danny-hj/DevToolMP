<template>
  <div class="tools-page">
    <div class="page-header">
      <h1>工具列表</h1>
      <div class="filters">
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加工具
        </el-button>
        <el-select
          v-model="selectedCategory"
          placeholder="全部分类"
          clearable
          @change="handleCategoryChange"
          style="width: 150px"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
        <el-switch
          v-model="showPublishButtons"
          active-text="管理"
          inactive-text="管理"
        />
        <el-switch
          v-model="showSyncButtons"
          active-text="同步"
          inactive-text="同步"
        />
        <el-select
          v-model="sortBy"
          placeholder="排序"
          @change="handleSort"
          style="width: 100px"
        >
          <el-option label="最新" value="latest" />
          <el-option label="最热" value="hot" />
        </el-select>
      </div>
    </div>

    <div class="tools-grid">
      <el-skeleton v-if="loading" :rows="6" animated />
      <tool-card
        v-else
        v-for="tool in filteredTools"
        :key="tool.id"
        :tool="tool"
        :show-publish-button="showPublishButtons"
        :show-sync-button="showSyncButtons"
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
      class="pagination"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[12, 24, 48]"
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
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useToolsStore } from '@/stores/tools'
import ToolCard from '@/components/tool/ToolCard.vue'
import ToolFormDialog from '@/components/tool/ToolFormDialog.vue'

const router = useRouter()
const toolsStore = useToolsStore()

const currentPage = ref(1)
const pageSize = ref(12)
const sortBy = ref('latest')
const selectedCategory = ref(null)
const showPublishButtons = ref(false)
const showSyncButtons = ref(false)
const showAddDialog = ref(false)
const editingTool = ref(null)

const tools = computed(() => toolsStore.tools)
const loading = computed(() => toolsStore.loading)
const total = computed(() => toolsStore.total)

// 计算所有唯一分类
const categories = computed(() => {
  const categoryMap = new Map()
  tools.value.forEach(tool => {
    if (tool.categoryId && tool.categoryName) {
      categoryMap.set(tool.categoryId, {
        id: tool.categoryId,
        name: tool.categoryName
      })
    }
  })
  return Array.from(categoryMap.values()).sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
})

// 根据分类筛选工具
const filteredTools = computed(() => {
  if (!selectedCategory.value) {
    return tools.value
  }
  return tools.value.filter(tool => tool.categoryId === selectedCategory.value)
})

onMounted(() => {
  fetchTools()
})

watch([currentPage, pageSize], () => {
  fetchTools()
})

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

const handleCategoryChange = () => {
  currentPage.value = 1
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
  ElMessage.success(`${tool.name} 的GitHub数据已同步`)
}

const handleEdit = (tool) => {
  editingTool.value = tool
  showAddDialog.value = true
}

const handleToolSuccess = () => {
  editingTool.value = null
  // 刷新列表
  fetchTools()
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

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

@media (max-width: 768px) {
  .tools-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .tools-grid {
    grid-template-columns: 1fr;
  }
}

.pagination {
  display: flex;
  justify-content: center;
}
</style>
