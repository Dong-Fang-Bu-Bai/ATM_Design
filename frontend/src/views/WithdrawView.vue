<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { checkCashAvailability, validateTransactionSession, withdraw } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { useSessionStore } from '@/stores/session'
import { formatCurrency } from '@/utils/format'

const router = useRouter()
const sessionStore = useSessionStore()
const formRef = ref()
const loading = ref(false)
const result = ref(null)

const form = reactive({
  amount: 100,
  printReceipt: false
})

const balanceLabel = computed(() => formatCurrency(sessionStore.balance))

function validateWithdrawAmount(rule, value, callback) {
  const amount = Number(value)

  if (!Number.isFinite(amount) || amount <= 0) {
    callback(new Error('请输入大于 0 的取款金额'))
    return
  }

  if (amount % 100 !== 0) {
    callback(new Error('取款金额应为 100 的整数倍'))
    return
  }

  callback()
}

const rules = {
  amount: [
    { required: true, message: '请输入取款金额', trigger: 'blur' },
    { validator: validateWithdrawAmount, trigger: 'blur' }
  ]
}

async function submit() {
  const valid = await formRef.value
    .validate()
    .then(() => true)
    .catch(() => false)

  if (!valid) {
    return
  }

  loading.value = true
  result.value = null

  try {
    await validateTransactionSession(sessionStore.sessionId)
    const cashResponse = await checkCashAvailability(Number(form.amount))

    if (!cashResponse.data?.available) {
      ElMessage.error('ATM 设备现金不足，无法完成取款')
      return
    }

    const response = await withdraw({
      sessionId: sessionStore.sessionId,
      amount: Number(form.amount),
      printReceipt: form.printReceipt
    })

    result.value = response.data
    sessionStore.setBalance(response.data.remainingBalance)
    ElMessage.success(response.data.message || '取款成功')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '取款失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AtmLayout
    eyebrow="Withdraw"
    title="取款"
    subtitle="输入取款金额并选择是否打印凭条，系统会先检查设备现金状态。"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">当前账户</span>
          <strong>{{ sessionStore.accountNo || '--' }}</strong>
        </div>
        <div class="info-card balance-highlight">
          <span class="info-label">当前余额</span>
          <strong>{{ balanceLabel }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">取款规则</span>
          <strong>金额为 100 的整数倍</strong>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>取款交易</h2>
      <p>取款金额需为 100 的整数倍，交易完成后余额会自动更新。</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="取款金额" prop="amount">
        <el-input-number v-model="form.amount" :min="100" :step="100" class="full-width" />
      </el-form-item>
      <el-form-item label="打印凭条">
        <el-switch v-model="form.printReceipt" active-text="需要" inactive-text="不需要" />
      </el-form-item>
      <el-button type="primary" size="large" class="full-width" :loading="loading" @click="submit">
        确认取款
      </el-button>
    </el-form>

    <el-card v-if="result" shadow="never" class="result-card">
      <div class="result-grid">
        <span>交易编号</span>
        <strong>{{ result.transactionId }}</strong>
        <span>交易结果</span>
        <strong>{{ result.message }}</strong>
        <span>剩余余额</span>
        <strong>{{ formatCurrency(result.remainingBalance) }}</strong>
      </div>
      <div class="result-actions">
        <el-button type="primary" plain @click="router.push(`/receipt/${result.transactionId}`)">
          查看凭条
        </el-button>
      </div>
    </el-card>

    <div class="footer-actions">
      <el-button @click="router.push('/menu')">返回主菜单</el-button>
      <el-button @click="router.push('/balance')">查看余额</el-button>
    </div>
  </AtmLayout>
</template>
