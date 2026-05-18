<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { checkCashAvailability, getDeviceStatus } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { formatCurrency, formatDateTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const checkLoading = ref(false)
const deviceStatus = ref(null)
const cashCheckResult = ref(null)
const lastUpdatedAt = ref('')

const form = reactive({
  amount: 1000
})

const statusLabel = computed(() => {
  if (!deviceStatus.value) {
    return '--'
  }

  return deviceStatus.value.status === 'RUNNING' ? '运行正常' : deviceStatus.value.status
})
const cashAvailableLabel = computed(() => {
  if (!deviceStatus.value) {
    return '--'
  }

  return formatCurrency(deviceStatus.value.cashAvailable)
})
const checkMessage = computed(() => {
  if (!cashCheckResult.value) {
    return ''
  }

  return cashCheckResult.value.available
    ? `设备可满足 ${formatCurrency(cashCheckResult.value.amount)} 的取款请求`
    : `设备当前可用现金为 ${formatCurrency(cashCheckResult.value.cashAvailable)}，不足以完成本次取款`
})

async function fetchDeviceStatus() {
  loading.value = true

  try {
    const response = await getDeviceStatus()
    deviceStatus.value = response.data
    lastUpdatedAt.value = formatDateTime(new Date().toISOString())
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '设备状态查询失败'))
  } finally {
    loading.value = false
  }
}

async function submitCashCheck() {
  checkLoading.value = true
  cashCheckResult.value = null

  try {
    const response = await checkCashAvailability(Number(form.amount))
    cashCheckResult.value = response.data
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '吐钞能力检查失败'))
  } finally {
    checkLoading.value = false
  }
}

onMounted(() => {
  fetchDeviceStatus()
})
</script>

<template>
  <AtmLayout
    eyebrow="Device"
    title="设备状态"
    subtitle="查看 ATM 终端运行状态、设备位置和当前可用现金。"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">设备编号</span>
          <strong>{{ deviceStatus?.atmCode || '--' }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">运行状态</span>
          <strong>{{ loading ? '查询中' : statusLabel }}</strong>
        </div>
        <div class="info-card balance-highlight">
          <span class="info-label">可用现金</span>
          <strong>{{ cashAvailableLabel }}</strong>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>ATM 终端状态</h2>
      <p>设备状态会影响取款服务，请在办理大额取款前先确认现金余量。</p>
    </div>

    <el-card shadow="never" class="result-card">
      <div class="result-grid">
        <span>设备位置</span>
        <strong>{{ deviceStatus?.location || '--' }}</strong>
        <span>状态代码</span>
        <strong>{{ deviceStatus?.status || '--' }}</strong>
        <span>最近刷新</span>
        <strong>{{ lastUpdatedAt || '等待查询' }}</strong>
      </div>
    </el-card>

    <el-card shadow="never" class="cash-check-card">
      <div class="panel-header compact-header">
        <h2>吐钞能力检查</h2>
        <p>输入计划取款金额，提前确认设备是否能够完成吐钞。</p>
      </div>
      <el-form :model="form" label-position="top">
        <el-form-item label="计划取款金额">
          <el-input-number v-model="form.amount" :min="100" :step="100" class="full-width" />
        </el-form-item>
        <el-button
          type="primary"
          class="full-width"
          :loading="checkLoading"
          @click="submitCashCheck"
        >
          检查吐钞能力
        </el-button>
      </el-form>
      <el-alert
        v-if="cashCheckResult"
        class="cash-check-alert"
        :type="cashCheckResult.available ? 'success' : 'warning'"
        :closable="false"
        :title="checkMessage"
      />
    </el-card>

    <div class="footer-actions">
      <el-button :loading="loading" type="primary" @click="fetchDeviceStatus">刷新状态</el-button>
      <el-button @click="router.push('/withdraw')">进入取款</el-button>
      <el-button @click="router.push('/menu')">返回主菜单</el-button>
    </div>
  </AtmLayout>
</template>
