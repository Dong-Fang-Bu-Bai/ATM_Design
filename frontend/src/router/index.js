import { createRouter, createWebHistory } from 'vue-router'
import pinia from '@/stores'
import { useSessionStore } from '@/stores/session'

const routes = [
  {
    path: '/',
    name: 'welcome',
    component: () => import('@/views/WelcomeView.vue'),
    meta: { title: 'ATM 自助终端' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '插卡登录' }
  },
  {
    path: '/menu',
    name: 'menu',
    component: () => import('@/views/MenuView.vue'),
    meta: { title: '主菜单', requiresAuth: true }
  },
  {
    path: '/balance',
    name: 'balance',
    component: () => import('@/views/BalanceView.vue'),
    meta: { title: '查询余额', requiresAuth: true }
  },
  {
    path: '/feature/:feature',
    name: 'feature',
    component: () => import('@/views/PlaceholderView.vue'),
    meta: { title: '功能建设中', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const sessionStore = useSessionStore(pinia)
  sessionStore.hydrateFromStorage()

  if (to.meta.requiresAuth && !sessionStore.isAuthenticated) {
    return {
      name: 'login',
      query: { redirect: to.fullPath }
    }
  }

  if ((to.name === 'welcome' || to.name === 'login') && sessionStore.isAuthenticated) {
    return { name: 'menu' }
  }

  document.title = `${to.meta.title || 'ATM'} | ATM Frontend`
  return true
})

export default router

