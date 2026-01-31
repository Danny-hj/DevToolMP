import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)

  // 设置用户昵称（用于评分等功能的匿名展示）
  const setNickname = (nickname) => {
    userInfo.value = { nickname }
  }

  const clearUserInfo = () => {
    userInfo.value = null
  }

  return {
    userInfo,
    setNickname,
    clearUserInfo
  }
})
