import { createApp } from 'vue'
import ElementPlus from 'element-plus/es'
import 'element-plus/dist/index.css'
import { zhCn } from '@/locale'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import Lenis from '@studio-freight/lenis'
import App from './App.vue'
import router from './router'
import './styles/global.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, { locale: zhCn })
app.use(router)
app.mount('#app')

const lenis = new Lenis({
  duration: 1.4,
  easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
  smoothWheel: true,
  smoothTouch: false
})

function raf(time) {
  lenis.raf(time)
  requestAnimationFrame(raf)
}

requestAnimationFrame(raf)

router.afterEach(() => {
  lenis.scrollTo(0, { immediate: false, duration: 1.2 })
})
