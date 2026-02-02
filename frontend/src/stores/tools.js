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
      // 更新详情页的工具状态
      if (currentTool.value && currentTool.value.id === id) {
        currentTool.value.isFavorited = response
        if (response) {
          currentTool.value.favoriteCount++
        } else {
          currentTool.value.favoriteCount--
        }
      }
      // 更新列表中的工具状态
      const tool = tools.value.find(t => t.id === id)
      if (tool) {
        tool.isFavorited = response
        if (response) {
          tool.favoriteCount++
        } else {
          tool.favoriteCount--
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

  // 创建工具
  const createTool = async (toolData) => {
    try {
      const response = await request.post('/tools', toolData)
      // 刷新工具列表
      await fetchTools()
      return response
    } catch (error) {
      console.error('创建工具失败:', error)
      throw error
    }
  }

  // 更新工具
  const updateTool = async (id, toolData) => {
    try {
      const response = await request.put(`/tools/${id}`, toolData)
      // 更新详情页工具状态
      if (currentTool.value && currentTool.value.id === id) {
        Object.assign(currentTool.value, response)
      }
      // 更新列表中的工具状态（当前页）
      const tool = tools.value.find(t => t.id === id)
      if (tool) {
        Object.assign(tool, response)
      }
      return response
    } catch (error) {
      console.error('更新工具失败:', error)
      throw error
    }
  }

  // Codehub相关方法
  const syncCodehubData = async (id) => {
    try {
      const response = await request.post(`/codehub/sync/${id}`)
      // 更新当前工具数据
      if (currentTool.value && currentTool.value.id === id) {
        Object.assign(currentTool.value, response)
      }
      // 更新列表中的工具数据
      const tool = tools.value.find(t => t.id === id)
      if (tool) {
        Object.assign(tool, response)
      }
      return response
    } catch (error) {
      console.error('同步Codehub数据失败:', error)
      throw error
    }
  }

  const syncAllCodehubData = async () => {
    try {
      const response = await request.post('/codehub/sync/all')
      return response
    } catch (error) {
      console.error('批量同步Codehub数据失败:', error)
      throw error
    }
  }

  const fetchCodehubRepoInfo = async (owner, repo) => {
    try {
      const response = await request.get(`/codehub/repos/${owner}/${repo}`)
      return response
    } catch (error) {
      console.error('获取Codehub仓库信息失败:', error)
      throw error
    }
  }

  const validateCodehubRepo = async (owner, repo) => {
    try {
      const response = await request.get(`/codehub/repos/${owner}/${repo}/validate`)
      return response.valid
    } catch (error) {
      console.error('验证Codehub仓库失败:', error)
      return false
    }
  }

  const fetchCodehubReadme = async (owner, repo) => {
    try {
      const response = await request.get(`/codehub/repos/${owner}/${repo}/readme`)
      return response.content
    } catch (error) {
      console.error('获取README失败:', error)
      throw error
    }
  }

  const fetchCodehubLatestRelease = async (owner, repo) => {
    try {
      const response = await request.get(`/codehub/repos/${owner}/${repo}/releases/latest`)
      return response
    } catch (error) {
      console.error('获取最新版本失败:', error)
      throw error
    }
  }

  const autoDiscoverAgentSkills = async () => {
    try {
      // 为同步操作设置更长的超时时间（2分钟）
      const response = await request.post('/codehub/agent-skills/auto-discover', {}, {
        timeout: 120000
      })
      return response
    } catch (error) {
      console.error('自动发现 Agent Skills 失败:', error)
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
    unpublishTool,
    createTool,
    updateTool,
    syncCodehubData,
    syncAllCodehubData,
    fetchCodehubRepoInfo,
    validateCodehubRepo,
    fetchCodehubReadme,
    fetchCodehubLatestRelease,
    autoDiscoverAgentSkills
  }
})
