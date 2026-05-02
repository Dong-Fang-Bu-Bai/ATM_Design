<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { getBalance } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { useSessionStore } from '@/stores/session'
import { formatCurrency } from '@/utils/format'

const router = useRouter()
const sessionStore = useSessionStore()
const loading = ref(false)
const lastUpdatedAt = ref('')

const balanceLabel = computed(() => formatCurrency(sessionStore.balance))

async function fetchBalance() {
  loading.value = true

  try {
    const response = await getBalance(sessionStore.sessionId)
    sessionStore.setBalance(response.data.balance)
    lastUpdatedAt.value = new Intl.DateTimeFormat('zh-CN', {
      dateStyle: 'medium',
      timeStyle: 'medium'
    }).format(new Date())
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '余额查询失败'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchBalance()
})
</script>

<template>
  <AtmLayout
    eyebrow="Account Balance"
    title="账户余额"
    subtitle="该页面已接入 GET /api/atm/accounts/balance，支持重复刷新与主菜单回退。"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">当前账号</span>
          <strong>{{ sessionStore.accountNo || '--' }}</strong>
        </div>
        <div class="info-card balance-highlight">
          <span class="info-label">可用余额</span>
          <strong>{{ balanceLabel }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">最近刷新</span>
          <strong>{{ lastUpdatedAt || '等待查询' }}</strong>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>余额查询结果</h2>
      <p>可作为第一次迭代验收页面，后续能在此基础上扩展流水与凭条。</p>
    </div>

    <el-card shadow="never" class="balance-card">
      <div class="balance-overview">
        <span class="info-label">人民币余额</span>
        <strong class="balance-value">{{ balanceLabel }}</strong>
      </div>
      <div class="balance-meta">
        <span>账户编号：{{ sessionStore.accountNo || '--' }}</span>
        <span>会话 ID：{{ sessionStore.sessionId || '--' }}</span>
      </div>
    </el-card>

    <div class="footer-actions">
      <el-button :loading="loading" type="primary" @click="fetchBalance">刷新余额</el-button>
      <el-button @click="router.push('/menu')">返回主菜单</el-button>
    </div>
  </AtmLayout>
</template>
