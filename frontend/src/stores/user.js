import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)

  // 管理模式开关
  const adminMode = ref(false)

  // 切换管理模式
  const toggleAdminMode = () => {
    adminMode.value = !adminMode.value
  }

  // 设置用户昵称（用于评分等功能的匿名展示）
  const setNickname = (nickname) => {
    userInfo.value = { nickname }
  }

  const clearUserInfo = () => {
    userInfo.value = null
    adminMode.value = false
  }

  return {
    userInfo,
    adminMode,
    toggleAdminMode,
    setNickname,
    clearUserInfo
  }
})
