<template>
  <div class="search-page">
    <div class="search-header">
      <h1>搜索结果: "{{ query }}"</h1>
      <p>找到 {{ total }} 个结果</p>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <div v-if="tools.length > 0" class="tools-grid">
        <tool-card
          v-for="tool in tools"
          :key="tool.id"
          :tool="tool"
          @click="handleToolClick"
          @favorite="handleFavorite"
        />
      </div>
      <div v-else class="empty-state">
        <el-icon size="64" color="#909399"><Search /></el-icon>
        <p>没有找到相关工具</p>
      </div>
    </template>

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
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useToolsStore } from '@/stores/tools'
import ToolCard from '@/components/tool/ToolCard.vue'

const route = useRoute()
const router = useRouter()
const toolsStore = useToolsStore()

const query = ref('')
const currentPage = ref(1)
const pageSize = ref(12)

const tools = computed(() => toolsStore.tools)
const loading = computed(() => toolsStore.loading)
const total = computed(() => toolsStore.total)

watch(() => route.query.q, (newQuery) => {
  query.value = newQuery || ''
  currentPage.value = 1
  fetchTools()
}, { immediate: true })

watch([currentPage, pageSize], () => {
  fetchTools()
})

const fetchTools = () => {
  if (query.value) {
    const page = currentPage.value - 1
    toolsStore.searchTools(query.value, page, pageSize.value)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
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
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  margin-bottom: 24px;
}

.search-header h1 {
  margin: 0 0 8px;
  font-size: 32px;
  color: #e0e0e0;
}

.search-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.empty-state {
  text-align: center;
  padding: 64px;
  background-color: #2a2a2a;
  border-radius: 12px;
}

.empty-state p {
  margin: 16px 0 0;
  color: #909399;
  font-size: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
}
</style>
