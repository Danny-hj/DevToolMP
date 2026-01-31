<template>
  <div class="tools-page">
    <div class="page-header">
      <h1>工具列表</h1>
      <div class="filters">
        <el-switch
          v-model="showPublishButtons"
          active-text="显示管理按钮"
          inactive-text="隐藏管理按钮"
        />
        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          @change="handleSort"
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
        v-for="tool in tools"
        :key="tool.id"
        :tool="tool"
        :show-publish-button="showPublishButtons"
        @click="handleToolClick"
        @favorite="handleFavorite"
        @publish="handlePublish"
        @unpublish="handleUnpublish"
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
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useToolsStore } from '@/stores/tools'
import ToolCard from '@/components/tool/ToolCard.vue'

const router = useRouter()
const toolsStore = useToolsStore()

const currentPage = ref(1)
const pageSize = ref(12)
const sortBy = ref('latest')
const showPublishButtons = ref(false) // 默认不显示管理按钮

const tools = computed(() => toolsStore.tools)
const loading = computed(() => toolsStore.loading)
const total = computed(() => toolsStore.total)

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
}

.page-header h1 {
  margin: 0;
  font-size: 32px;
  color: #e0e0e0;
}

.filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.pagination {
  display: flex;
  justify-content: center;
}
</style>
