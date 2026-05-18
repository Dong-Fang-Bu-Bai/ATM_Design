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
    subtitle="查看当前账户人民币可用余额，并可继续查询交易流水。"
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
      <p>余额信息将随账户交易实时更新。</p>
    </div>

    <el-card shadow="never" class="balance-card">
      <div class="balance-overview">
        <span class="info-label">人民币余额</span>
        <strong class="balance-value">{{ balanceLabel }}</strong>
      </div>
      <div class="balance-meta">
        <span>账户编号：{{ sessionStore.accountNo || '--' }}</span>
        <span>最近刷新：{{ lastUpdatedAt || '等待查询' }}</span>
      </div>
    </el-card>

    <div class="footer-actions">
      <el-button :loading="loading" type="primary" @click="fetchBalance">刷新余额</el-button>
      <el-button @click="router.push('/history')">查看流水</el-button>
      <el-button @click="router.push('/menu')">返回主菜单</el-button>
    </div>
  </AtmLayout>
</template>
