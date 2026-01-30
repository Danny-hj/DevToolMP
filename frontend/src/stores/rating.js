import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/api'

export const useRatingStore = defineStore('rating', () => {
  const ratings = ref([])
  const statistics = ref(null)
  const loading = ref(false)
  const total = ref(0)

  const fetchRatings = async (toolId, page = 0, size = 20) => {
    loading.value = true
    try {
      const response = await request.get(`/ratings/tool/${toolId}`, {
        params: { page, size }
      })
      ratings.value = response.content
      total.value = response.totalElements
    } catch (error) {
      console.error('获取评论列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchStatistics = async (toolId) => {
    try {
      const response = await request.get(`/ratings/tool/${toolId}/statistics`)
      statistics.value = response
      return response
    } catch (error) {
      console.error('获取评分统计失败:', error)
      throw error
    }
  }

  const createRating = async (toolId, data) => {
    try {
      const response = await request.post(`/ratings/tool/${toolId}`, data)
      return response
    } catch (error) {
      console.error('创建评论失败:', error)
      throw error
    }
  }

  const updateRating = async (ratingId, data) => {
    try {
      const response = await request.put(`/ratings/${ratingId}`, data)
      return response
    } catch (error) {
      console.error('更新评论失败:', error)
      throw error
    }
  }

  const deleteRating = async (ratingId) => {
    try {
      await request.delete(`/ratings/${ratingId}`)
    } catch (error) {
      console.error('删除评论失败:', error)
      throw error
    }
  }

  const createReply = async (ratingId, content) => {
    try {
      const response = await request.post(`/ratings/${ratingId}/replies`, { content })
      return response
    } catch (error) {
      console.error('创建回复失败:', error)
      throw error
    }
  }

  const deleteReply = async (replyId) => {
    try {
      await request.delete(`/ratings/replies/${replyId}`)
    } catch (error) {
      console.error('删除回复失败:', error)
      throw error
    }
  }

  return {
    ratings,
    statistics,
    loading,
    total,
    fetchRatings,
    fetchStatistics,
    createRating,
    updateRating,
    deleteRating,
    createReply,
    deleteReply
  }
})
