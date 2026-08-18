import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: 'HLH AI Agent' }
    },
    {
      path: '/love',
      name: 'love',
      component: () => import('../views/LoveAppView.vue'),
      meta: { title: 'AI 恋爱大师' }
    },
    {
      path: '/manus',
      name: 'manus',
      component: () => import('../views/ManusView.vue'),
      meta: { title: 'AI 超级智能体' }
    }
  ]
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · HLH AI Agent` : 'HLH AI Agent'
})

export default router
