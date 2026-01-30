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

export default router
