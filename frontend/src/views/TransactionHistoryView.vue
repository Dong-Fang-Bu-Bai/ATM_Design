<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { getTransactionHistory } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { useSessionStore } from '@/stores/session'
import {
  formatCurrency,
  formatDateTime,
  formatTransactionStatus,
  formatTransactionType
} from '@/utils/format'

const router = useRouter()
const sessionStore = useSessionStore()
const loading = ref(false)
const records = ref([])
const pager = reactive({
  page: 1,
  size: 5,
  total: 0
})

const totalLabel = computed(() => `${pager.total} 条`)

function getStatusTagType(status) {
  if (status === 'SUCCESS' || status === 1) {
    return 'success'
  }

  if (status === 'FAILED' || status === 2) {
    return 'danger'
  }

  if (status === 'PENDING' || status === 0) {
    return 'warning'
  }

  return 'info'
}

async function fetchHistory(page = pager.page) {
  loading.value = true

  try {
    const response = await getTransactionHistory(sessionStore.sessionId, {
      page,
      size: pager.size
    })

    records.value = response.data?.records || []
    pager.page = response.data?.page || page
    pager.size = response.data?.size || pager.size
    pager.total = response.data?.total || 0
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '交易流水查询失败'))
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  fetchHistory(page)
}

onMounted(() => {
  fetchHistory()
})
</script>

<template>
  <AtmLayout
    eyebrow="Transaction History"
    title="交易流水"
    subtitle="按时间查看当前账户交易记录，并可进入单笔凭条。"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">当前账户</span>
          <strong>{{ sessionStore.accountNo || '--' }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">流水总数</span>
          <strong>{{ totalLabel }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">当前页</span>
          <strong>第 {{ pager.page }} 页</strong>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>交易流水列表</h2>
      <p>可根据交易编号继续查询和打印凭条。</p>
    </div>

    <el-table
      v-loading="loading"
      :data="records"
      row-key="transactionId"
      class="history-table"
      empty-text="暂无交易流水"
    >
      <el-table-column label="时间" min-width="150">
        <template #default="{ row }">
          {{ formatDateTime(row.transactionTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="transactionId" label="交易编号" min-width="156" />
      <el-table-column label="类型" width="88">
        <template #default="{ row }">
          {{ formatTransactionType(row.transactionType) }}
        </template>
      </el-table-column>
      <el-table-column label="金额" min-width="112">
        <template #default="{ row }">
          {{ formatCurrency(row.amount) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="92">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.transactionStatus)" effect="dark">
            {{ formatTransactionStatus(row.transactionStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="88">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/receipt/${row.transactionId}`)">
            凭条
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="pager.page"
        :page-size="pager.size"
        :total="pager.total"
        @current-change="handlePageChange"
      />
    </div>

    <div class="footer-actions">
      <el-button :loading="loading" type="primary" @click="fetchHistory()">刷新流水</el-button>
      <el-button @click="router.push('/menu')">返回主菜单</el-button>
    </div>
  </AtmLayout>
</template>
