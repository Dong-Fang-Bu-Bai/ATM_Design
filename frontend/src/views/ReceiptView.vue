<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { getReceipt } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { useSessionStore } from '@/stores/session'
import {
  formatCurrency,
  formatDateTime,
  formatTransactionType
} from '@/utils/format'

const route = useRoute()
const router = useRouter()
const sessionStore = useSessionStore()
const formRef = ref()
const loading = ref(false)
const receipt = ref(null)

const form = reactive({
  transactionId: route.params.transactionId || ''
})

const rules = {
  transactionId: [
    { required: true, message: '请输入交易编号', trigger: 'blur' },
    { min: 6, message: '交易编号过短', trigger: 'blur' }
  ]
}

async function fetchReceipt() {
  const valid = await formRef.value
    .validate()
    .then(() => true)
    .catch(() => false)

  if (!valid) {
    return
  }

  loading.value = true
  receipt.value = null

  try {
    const response = await getReceipt(form.transactionId.trim(), sessionStore.sessionId)
    receipt.value = response.data
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '凭条查询失败'))
  } finally {
    loading.value = false
  }
}

function printReceipt() {
  window.print()
}

onMounted(() => {
  if (form.transactionId) {
    fetchReceipt()
  }
})

watch(
  () => route.params.transactionId,
  (transactionId) => {
    form.transactionId = transactionId || ''
    receipt.value = null

    if (transactionId) {
      fetchReceipt()
    }
  }
)
</script>

<template>
  <AtmLayout
    eyebrow="Receipt"
    title="交易凭条"
    subtitle="接口：GET /api/atm/receipts/{transactionId}"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">当前账户</span>
          <strong>{{ sessionStore.accountNo || '--' }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">交易编号</span>
          <strong>{{ form.transactionId || '等待输入' }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">凭条口径</span>
          <strong>transactionId + sessionId</strong>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>凭条查询与展示</h2>
      <p>交易成功后可直接跳转，也支持手动输入交易编号查询。</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="交易编号" prop="transactionId">
        <el-input
          v-model.trim="form.transactionId"
          maxlength="32"
          placeholder="TX202603010001"
        />
      </el-form-item>
      <el-button type="primary" size="large" class="full-width" :loading="loading" @click="fetchReceipt">
        查询凭条
      </el-button>
    </el-form>

    <el-card v-if="receipt" shadow="never" class="receipt-paper">
      <div class="receipt-title">ATM 交易凭条</div>
      <div class="receipt-grid">
        <span>交易编号</span>
        <strong>{{ receipt.transactionId }}</strong>
        <span>交易类型</span>
        <strong>{{ formatTransactionType(receipt.type) }}</strong>
        <span>交易金额</span>
        <strong>{{ formatCurrency(receipt.amount) }}</strong>
        <span>交易后余额</span>
        <strong>{{ formatCurrency(receipt.balanceAfter) }}</strong>
        <span>账户编号</span>
        <strong>{{ receipt.accountNo }}</strong>
        <span>交易时间</span>
        <strong>{{ formatDateTime(receipt.time) }}</strong>
      </div>
      <div class="receipt-divider" />
      <div class="receipt-actions">
        <el-button type="primary" plain @click="printReceipt">打印预览</el-button>
        <el-button @click="router.push('/history')">返回流水</el-button>
      </div>
    </el-card>

    <div class="footer-actions">
      <el-button @click="router.push('/history')">查看流水</el-button>
      <el-button @click="router.push('/menu')">返回主菜单</el-button>
    </div>
  </AtmLayout>
</template>
