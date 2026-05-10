<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AtmLayout from '@/components/AtmLayout.vue'

const route = useRoute()
const router = useRouter()

const featureMap = {
  withdraw: {
    title: '取款功能已接入',
    stage: '第二次迭代已交付',
    api: 'POST /api/atm/transactions/withdraw'
  },
  deposit: {
    title: '存款功能已接入',
    stage: '第二次迭代已交付',
    api: 'POST /api/atm/transactions/deposit'
  },
  transfer: {
    title: '转账功能已接入',
    stage: '第二次迭代已交付',
    api: 'POST /api/atm/transactions/transfer'
  },
  'change-password': {
    title: '修改密码功能已接入',
    stage: '第二次迭代已交付',
    api: 'POST /api/atm/auth/change-password'
  },
  receipt: {
    title: '凭条与流水已接入',
    stage: '第三次迭代已交付',
    api: 'GET /api/atm/receipts/{transactionId}；GET /api/atm/transactions/history'
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
    subtitle="本页面用于兼容早期占位入口，正式演示请从主菜单进入对应功能。"
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
      <el-empty description="当前入口已迁移至正式业务页面或保留给后续扩展" />
    </div>

    <div class="footer-actions">
      <el-button type="primary" @click="router.push('/menu')">返回主菜单</el-button>
      <el-button @click="router.push('/balance')">查看余额</el-button>
    </div>
  </AtmLayout>
</template>

