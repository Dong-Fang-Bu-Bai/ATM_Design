<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AtmLayout from '@/components/AtmLayout.vue'

const route = useRoute()
const router = useRouter()

const featureMap = {
  withdraw: {
    title: '取款模块建设中',
    stage: '第二次迭代',
    api: 'POST /api/atm/transactions/withdraw'
  },
  deposit: {
    title: '存款模块建设中',
    stage: '第二次迭代',
    api: 'POST /api/atm/transactions/deposit'
  },
  transfer: {
    title: '转账模块建设中',
    stage: '第二次迭代',
    api: 'POST /api/atm/transactions/transfer'
  },
  'change-password': {
    title: '修改密码模块建设中',
    stage: '第二次迭代',
    api: 'POST /api/atm/auth/change-password'
  },
  receipt: {
    title: '凭条与流水模块建设中',
    stage: '第三次迭代',
    api: 'GET /api/atm/receipts/{transactionId}'
  }
}

const feature = computed(() => {
  return featureMap[route.params.feature] || {
    title: '功能建设中',
    stage: '后续迭代',
    api: '待补充'
  }
})
</script>

<template>
  <AtmLayout
    eyebrow="Coming Next"
    :title="feature.title"
    subtitle="本页面用于承接第一次迭代尚未开发完成的交易能力，保证主菜单流程完整。"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">计划迭代</span>
          <strong>{{ feature.stage }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">目标接口</span>
          <strong>{{ feature.api }}</strong>
        </div>
      </div>
    </template>

    <div class="placeholder-body">
      <el-empty description="当前仅保留页面入口与接口对接位" />
    </div>

    <div class="footer-actions">
      <el-button type="primary" @click="router.push('/menu')">返回主菜单</el-button>
      <el-button @click="router.push('/balance')">查看余额</el-button>
    </div>
  </AtmLayout>
</template>

