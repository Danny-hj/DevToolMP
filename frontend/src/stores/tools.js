import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/api'

export const useToolsStore = defineStore('tools', () => {
  const tools = ref([])
  const currentTool = ref(null)
  const loading = ref(false)
  const total = ref(0)

  const fetchTools = async (page = 0, size = 20) => {
    loading.value = true
    try {
      const response = await request.get('/tools', {
        params: { page, size }
      })
      tools.value = response.content
      total.value = response.totalElements
    } catch (error) {
      console.error('获取工具列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchToolDetail = async (id) => {
    loading.value = true
    try {
      const response = await request.get(`/tools/${id}/detail`)
      currentTool.value = response
      return response
    } catch (error) {
      console.error('获取工具详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const searchTools = async (keyword, page = 0, size = 20) => {
    loading.value = true
    try {
      const response = await request.get('/tools/search', {
        params: { keyword, page, size }
      })
      tools.value = response.content
      total.value = response.totalElements
    } catch (error) {
      console.error('搜索工具失败:', error)
    } finally {
      loading.value = false
    }
  }

  const recordView = async (id) => {
    try {
      await request.post(`/tools/${id}/view`)
    } catch (error) {
      console.error('记录浏览失败:', error)
    }
  }

  const toggleFavorite = async (id) => {
    try {
      const response = await request.post(`/tools/${id}/favorite`)
      if (currentTool.value && currentTool.value.id === id) {
        currentTool.value.isFavorited = response
        if (response) {
          currentTool.value.favoriteCount++
        } else {
          currentTool.value.favoriteCount--
        }
      }
      return response
    } catch (error) {
      console.error('切换收藏失败:', error)
      throw error
    }
  }

  const recordInstall = async (id) => {
    try {
      await request.post(`/tools/${id}/install`)
    } catch (error) {
      console.error('记录安装失败:', error)
    }
  }

  const publishTool = async (id) => {
    try {
      await request.put(`/tools/${id}/publish`)
      // 更新当前工具状态
      if (currentTool.value && currentTool.value.id === id) {
        currentTool.value.status = 'active'
      }
      // 更新列表中的工具状态
      const tool = tools.value.find(t => t.id === id)
      if (tool) {
        tool.status = 'active'
      }
    } catch (error) {
      console.error('上架工具失败:', error)
      throw error
    }
  }

  const unpublishTool = async (id) => {
    try {
      await request.put(`/tools/${id}/unpublish`)
      // 更新当前工具状态
      if (currentTool.value && currentTool.value.id === id) {
        currentTool.value.status = 'inactive'
      }
      // 更新列表中的工具状态
      const tool = tools.value.find(t => t.id === id)
      if (tool) {
        tool.status = 'inactive'
      }
    } catch (error) {
      console.error('下架工具失败:', error)
      throw error
    }
  }

  return {
    tools,
    currentTool,
    loading,
    total,
    fetchTools,
    fetchToolDetail,
    searchTools,
    recordView,
    toggleFavorite,
    recordInstall,
    publishTool,
    unpublishTool
  }
})
