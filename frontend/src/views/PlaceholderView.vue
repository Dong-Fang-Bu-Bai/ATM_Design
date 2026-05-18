<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AtmLayout from '@/components/AtmLayout.vue'

const route = useRoute()
const router = useRouter()

const featureMap = {
  withdraw: {
    title: '取款服务',
    stage: '现金业务',
    api: '请从主菜单进入正式取款页面'
  },
  deposit: {
    title: '存款服务',
    stage: '现金业务',
    api: '请从主菜单进入正式存款页面'
  },
  transfer: {
    title: '转账服务',
    stage: '账户业务',
    api: '请从主菜单进入正式转账页面'
  },
  'change-password': {
    title: '密码维护',
    stage: '安全服务',
    api: '请从主菜单进入密码维护页面'
  },
  receipt: {
    title: '凭条与流水',
    stage: '交易查询',
    api: '请从主菜单进入流水或凭条页面'
  }
}

const feature = computed(() => {
  return featureMap[route.params.feature] || {
    title: '服务入口',
    stage: '账户服务',
    api: '请返回主菜单选择业务'
  }
})
</script>

<template>
  <AtmLayout
    eyebrow="Service"
    :title="feature.title"
    subtitle="当前入口已统一到主菜单，请返回后选择对应业务。"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">服务类型</span>
          <strong>{{ feature.stage }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">操作提示</span>
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

