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
  console.log('[Router] beforeEach - From:', from.path, 'To:', to.path, 'Name:', to.name, 'FullPath:', to.fullPath)

  // 清理遮罩层和对话框
  const overlays = document.querySelectorAll('.el-overlay')
  if (overlays.length > 0) {
    console.log('[Router] Cleaning up', overlays.length, 'overlay(s)')
    overlays.forEach(overlay => overlay.remove())
  }

  const dialogs = document.querySelectorAll('.el-dialog')
  if (dialogs.length > 0) {
    console.log('[Router] Cleaning up', dialogs.length, 'dialog(s)')
    dialogs.forEach(dialog => dialog.remove())
  }

  document.body.classList.remove('el-popup-parent--hidden')
  document.body.style.overflow = ''

  console.log('[Router] Calling next() to proceed with navigation')
  next()
})

router.afterEach((to, from) => {
  console.log('[Router] Navigation completed - From:', from.path, 'To:', to.path, 'at:', new Date().toISOString())
})

export default router
