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
    path: '/withdraw',
    name: 'withdraw',
    component: () => import('@/views/WithdrawView.vue'),
    meta: { title: '取款', requiresAuth: true }
  },
  {
    path: '/deposit',
    name: 'deposit',
    component: () => import('@/views/DepositView.vue'),
    meta: { title: '存款', requiresAuth: true }
  },
  {
    path: '/transfer',
    name: 'transfer',
    component: () => import('@/views/TransferView.vue'),
    meta: { title: '转账', requiresAuth: true }
  },
  {
    path: '/change-password',
    name: 'change-password',
    component: () => import('@/views/ChangePasswordView.vue'),
    meta: { title: '修改密码', requiresAuth: true }
  },
  {
    path: '/history',
    name: 'history',
    component: () => import('@/views/TransactionHistoryView.vue'),
    meta: { title: '交易流水', requiresAuth: true }
  },
  {
    path: '/receipt/:transactionId?',
    name: 'receipt',
    component: () => import('@/views/ReceiptView.vue'),
    meta: { title: '交易凭条', requiresAuth: true }
  },
  {
    path: '/device-status',
    name: 'device-status',
    component: () => import('@/views/DeviceStatusView.vue'),
    meta: { title: '设备状态', requiresAuth: true }
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

