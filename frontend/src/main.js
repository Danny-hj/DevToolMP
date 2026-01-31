import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './assets/styles/variables.scss'
import './assets/styles/dark-theme.scss'
import './assets/styles/ranking.scss'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 全局点击监听器 - 确保没有遮罩层阻止导航
document.addEventListener('click', (event) => {
  // 检查是否有遮罩层存在
  const overlays = document.querySelectorAll('.el-overlay')
  if (overlays.length > 0) {
    // 如果点击的不是对话框内部，则移除遮罩层
    const clickedDialog = event.target.closest('.el-dialog')
    if (!clickedDialog) {
      console.log('[Global] Removing', overlays.length, 'overlay(s) on document click')
      overlays.forEach(overlay => overlay.remove())
      document.body.classList.remove('el-popup-parent--hidden')
      document.body.style.overflow = ''
    }
  }
}, true) // 使用捕获阶段，确保在事件冒泡前处理

app.mount('#app')
