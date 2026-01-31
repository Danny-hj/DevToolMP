import { ref, computed } from 'vue'
import request from '../api'

const rankings = ref([])
const loading = ref(false)
const error = ref(null)
const activeTab = ref('alltime')

export const useRankingStore = () => {
  // 获取排行榜数据
  const fetchRankings = async (type = 'alltime') => {
    loading.value = true
    error.value = null

    try {
      const endpoint = type === 'trending'
        ? '/tools/ranking/trending'
        : type === 'weekly'
        ? '/tools/ranking/weekly'
        : type === 'daily'
        ? '/tools/ranking/daily'
        : '/tools/ranking/alltime'

      console.log('[Ranking] Fetching rankings from:', endpoint)
      const response = await request.get(endpoint)
      console.log('[Ranking] Received data:', response)
      rankings.value = response || []
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
    await fetchRankings(tab)
  }

  // 刷新当前排行榜
  const refresh = () => {
    return fetchRankings(activeTab.value)
  }

  // 计算属性
  const isEmpty = computed(() => rankings.value.length === 0)
  const hasError = computed(() => error.value !== null)

  return {
    // 状态
    rankings,
    loading,
    error,
    activeTab,

    // 计算属性
    isEmpty,
    hasError,

    // 方法
    fetchRankings,
    switchTab,
    refresh
  }
}
