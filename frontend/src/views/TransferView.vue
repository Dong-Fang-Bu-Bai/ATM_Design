<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { transfer } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { useSessionStore } from '@/stores/session'
import { formatCurrency } from '@/utils/format'

const router = useRouter()
const sessionStore = useSessionStore()
const formRef = ref()
const loading = ref(false)
const result = ref(null)

const form = reactive({
  targetAccountNo: '',
  amount: 100,
  printReceipt: false
})

const balanceLabel = computed(() => formatCurrency(sessionStore.balance))

function validateAmount(rule, value, callback) {
  const amount = Number(value)

  if (!Number.isFinite(amount) || amount <= 0) {
    callback(new Error('请输入大于 0 的转账金额'))
    return
  }

  callback()
}

const rules = {
  targetAccountNo: [
    { required: true, message: '请输入目标账户编号', trigger: 'blur' },
    { min: 6, message: '目标账户编号过短', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入转账金额', trigger: 'blur' },
    { validator: validateAmount, trigger: 'blur' }
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
    const response = await transfer({
      sessionId: sessionStore.sessionId,
      targetAccountNo: form.targetAccountNo.trim(),
      amount: Number(form.amount),
      printReceipt: form.printReceipt
    })

    result.value = response.data
    sessionStore.setBalance(response.data.remainingBalance)
    ElMessage.success(response.data.message || '转账成功')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '转账失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AtmLayout
    eyebrow="Transfer"
    title="转账"
    subtitle="接口：POST /api/atm/transactions/transfer"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">转出账户</span>
          <strong>{{ sessionStore.accountNo || '--' }}</strong>
        </div>
        <div class="info-card balance-highlight">
          <span class="info-label">当前余额</span>
          <strong>{{ balanceLabel }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">Mock 收款账户</span>
          <strong>ACC20001 / ACC30001</strong>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>转账交易</h2>
      <p>提交字段按 YAML 统一为 sessionId、targetAccountNo、amount、printReceipt。</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="目标账户编号" prop="targetAccountNo">
        <el-input v-model.trim="form.targetAccountNo" maxlength="32" placeholder="ACC20001" />
      </el-form-item>
      <el-form-item label="转账金额" prop="amount">
        <el-input-number v-model="form.amount" :min="1" :step="100" class="full-width" />
      </el-form-item>
      <el-form-item label="打印凭条">
        <el-switch v-model="form.printReceipt" active-text="需要" inactive-text="不需要" />
      </el-form-item>
      <el-button type="primary" size="large" class="full-width" :loading="loading" @click="submit">
        确认转账
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
    </el-card>

    <div class="footer-actions">
      <el-button @click="router.push('/menu')">返回主菜单</el-button>
      <el-button @click="router.push('/balance')">查看余额</el-button>
    </div>
  </AtmLayout>
</template>
