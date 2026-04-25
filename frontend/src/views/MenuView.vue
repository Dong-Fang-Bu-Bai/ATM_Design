<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { getProfile, logout } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { useSessionStore } from '@/stores/session'
import { maskCardNo } from '@/utils/format'

const router = useRouter()
const sessionStore = useSessionStore()
const logoutLoading = ref(false)

const menuItems = [
  { label: '查询余额', route: '/balance', status: '本轮已交付' },
  { label: '取款', route: '/feature/withdraw', status: '第二次迭代' },
  { label: '存款', route: '/feature/deposit', status: '第二次迭代' },
  { label: '转账', route: '/feature/transfer', status: '第二次迭代' },
  { label: '修改密码', route: '/feature/change-password', status: '第二次迭代' },
  { label: '交易凭条', route: '/feature/receipt', status: '第三次迭代' }
]

const profileCardNo = computed(() => maskCardNo(sessionStore.profile?.cardNo))

async function ensureProfile() {
  if (sessionStore.profile || !sessionStore.sessionId) {
    return
  }

  try {
    const response = await getProfile(sessionStore.sessionId)
    sessionStore.setProfile(response.data)
  } catch (error) {
    ElMessage.warning(getErrorMessage(error, '账户信息载入失败'))
  }
}

function navigateTo(route) {
  router.push(route)
}

async function exitSession() {
  logoutLoading.value = true

  try {
    await logout(sessionStore.sessionId)
  } catch (error) {
    ElMessage.warning(getErrorMessage(error, '退出接口未成功返回，已清除本地会话'))
  } finally {
    sessionStore.clearSession()
    logoutLoading.value = false
    router.push('/')
  }
}

onMounted(() => {
  ensureProfile()
})
</script>

<template>
  <AtmLayout
    eyebrow="Main Menu"
    title="请选择业务类型"
    subtitle="主菜单按照 ATM 终端的单屏多入口方式组织，后续功能只需继续补充对应页面与接口调用。"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">当前用户</span>
          <strong>{{ sessionStore.customerName || '--' }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">账户编号</span>
          <strong>{{ sessionStore.maskedAccountNo }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">银行卡号</span>
          <strong>{{ profileCardNo }}</strong>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>ATM 主菜单</h2>
      <p>余额查询已可用，其余入口已预留到后续迭代。</p>
    </div>

    <div class="menu-grid">
      <button
        v-for="item in menuItems"
        :key="item.label"
        class="menu-button"
        type="button"
        @click="navigateTo(item.route)"
      >
        <span>{{ item.label }}</span>
        <small>{{ item.status }}</small>
      </button>
    </div>

    <div class="footer-actions">
      <el-button text @click="navigateTo('/balance')">快速查看余额</el-button>
      <el-button :loading="logoutLoading" @click="exitSession">退卡退出</el-button>
    </div>
  </AtmLayout>
</template>

