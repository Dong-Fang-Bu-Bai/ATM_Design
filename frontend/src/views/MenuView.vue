<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { getDeviceStatus, getProfile, logout } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { useSessionStore } from '@/stores/session'
import { formatCurrency, maskCardNo } from '@/utils/format'

const router = useRouter()
const sessionStore = useSessionStore()
const logoutLoading = ref(false)
const deviceStatus = ref(null)
const deviceLoading = ref(false)

const menuItems = [
  { label: '查询余额', route: '/balance', code: '01', desc: '查看当前账户可用余额' },
  { label: '取款', route: '/withdraw', code: '02', desc: '按百元整数提取现金' },
  { label: '存款', route: '/deposit', code: '03', desc: '存入现金并更新余额' },
  { label: '转账', route: '/transfer', code: '04', desc: '向指定账户发起转账' },
  { label: '修改密码', route: '/change-password', code: '05', desc: '维护银行卡登录密码' },
  { label: '交易流水', route: '/history', code: '06', desc: '查看近期账户交易记录' },
  { label: '交易凭条', route: '/receipt', code: '07', desc: '查询或打印交易凭条' },
  { label: '设备状态', route: '/device-status', code: '08', desc: '查看终端运行与现金状态' }
]

const profileCardNo = computed(() => maskCardNo(sessionStore.profile?.cardNo))
const deviceLabel = computed(() => {
  if (!deviceStatus.value) {
    return '等待查询'
  }

  return deviceStatus.value.status === 'RUNNING' ? '运行正常' : deviceStatus.value.status
})
const cashAvailableLabel = computed(() => formatCurrency(deviceStatus.value?.cashAvailable))

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

async function fetchDeviceStatus() {
  deviceLoading.value = true

  try {
    const response = await getDeviceStatus()
    deviceStatus.value = response.data
  } catch (error) {
    ElMessage.warning(getErrorMessage(error, '设备状态载入失败'))
  } finally {
    deviceLoading.value = false
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
  fetchDeviceStatus()
})
</script>

<template>
  <AtmLayout
    eyebrow="Main Menu"
    title="请选择业务类型"
    subtitle="请选择需要办理的账户服务，操作完成后请及时退卡。"
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
        <div class="info-card">
          <span class="info-label">设备状态</span>
          <strong>{{ deviceLoading ? '查询中' : deviceLabel }}</strong>
          <small v-if="deviceStatus">可用现金：{{ cashAvailableLabel }}</small>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>ATM 主菜单</h2>
      <p>常用服务已集中在此处，可直接进入对应业务。</p>
    </div>

    <div class="menu-grid">
      <button
        v-for="item in menuItems"
        :key="item.label"
        class="menu-button"
        type="button"
        @click="navigateTo(item.route)"
      >
        <span class="menu-button-code">{{ item.code }}</span>
        <span class="menu-button-text">
          <strong>{{ item.label }}</strong>
          <small>{{ item.desc }}</small>
        </span>
      </button>
    </div>

    <div class="footer-actions">
      <el-button text @click="navigateTo('/balance')">快速查看余额</el-button>
      <el-button text @click="navigateTo('/history')">查看流水</el-button>
      <el-button :loading="logoutLoading" @click="exitSession">退卡退出</el-button>
    </div>
  </AtmLayout>
</template>

