import { ref, computed } from 'vue'
import request from '../api'

const rankings = ref([])
const loading = ref(false)
const error = ref(null)
const activeTab = ref('alltime')
const pagination = ref({
  page: 0,
  size: 20,
  total: 0,
  totalPages: 0
})

export const useRankingStore = () => {
  // 获取排行榜数据（带分页）
  const fetchRankings = async (type = 'alltime', page = 0) => {
    loading.value = true
    error.value = null

    try {
      const endpoint = type === 'trending'
        ? '/tools/ranking/trending'
        : type === 'weekly'
        ? `/tools/ranking/weekly/page?page=${page}&size=20`
        : type === 'daily'
        ? `/tools/ranking/daily/page?page=${page}&size=20`
        : `/tools/ranking/alltime/page?page=${page}&size=20`

      console.log('[Ranking] Fetching rankings from:', endpoint)
      const response = await request.get(endpoint)

      // 处理分页响应或普通响应
      if (response && typeof response === 'object' && 'content' in response) {
        rankings.value = response.content || []
        pagination.value = {
          page: response.page || 0,
          size: response.size || 20,
          total: response.total || 0,
          totalPages: Math.ceil((response.total || 0) / (response.size || 20))
        }
      } else {
        rankings.value = response || []
        pagination.value = {
          page: 0,
          size: 20,
          total: response?.length || 0,
          totalPages: 1
        }
      }

      activeTab.value = type
    } catch (err) {
      error.value = err.message || 'Failed to fetch rankings'
      console.error('Error fetching rankings:', err)
    } finally {
      loading.value = false
    }
  }

  // 切换标签页
  const switchTab = async (tab) => {
    if (activeTab.value === tab) return
    await fetchRankings(tab, 0)
  }

  // 切换页码
  const changePage = async (page) => {
    await fetchRankings(activeTab.value, page)
  }

  // 刷新当前排行榜
  const refresh = () => {
    return fetchRankings(activeTab.value, pagination.value.page)
  }

  // 计算属性
  const isEmpty = computed(() => rankings.value.length === 0)
  const hasError = computed(() => error.value !== null)
  const hasPagination = computed(() => pagination.value.totalPages > 1)

  return {
    // 状态
    rankings,
    loading,
    error,
    activeTab,
    pagination,

    // 计算属性
    isEmpty,
    hasError,
    hasPagination,

    // 方法
    fetchRankings,
    switchTab,
    changePage,
    refresh
  }
}
