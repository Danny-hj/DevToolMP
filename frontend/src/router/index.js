import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import Tools from '@/views/Tools.vue'
import ToolDetail from '@/views/ToolDetail.vue'
import Search from '@/views/Search.vue'
import Ranking from '@/views/Ranking.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/tools',
    name: 'Tools',
    component: Tools
  },
  {
    path: '/tools/:id',
    name: 'ToolDetail',
    component: ToolDetail,
    props: true
  },
  {
    path: '/search',
    name: 'Search',
    component: Search
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: Ranking
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：确保每次路由切换时关闭所有对话框和遮罩层
router.beforeEach((to, from, next) => {
  console.log('[Router] Navigating from', from.path, 'to', to.path)

  // 关闭所有Element Plus的对话框和遮罩层
  const overlays = document.querySelectorAll('.el-overlay')
  console.log('[Router] Found overlays:', overlays.length)
  overlays.forEach(overlay => {
    overlay.remove() // 使用 remove 而不是 display:none
  })

  // 移除可能存在的对话框body类
  document.body.classList.remove('el-popup-parent--hidden')

  // 检查并关闭所有可能的对话框
  const dialogs = document.querySelectorAll('.el-dialog')
  console.log('[Router] Found dialogs:', dialogs.length)
  dialogs.forEach(dialog => {
    dialog.remove()
  })

  next()
})

router.afterEach((to, from) => {
  console.log('[Router] Navigation completed to', to.path)
})

export default router
