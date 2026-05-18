<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import AtmLayout from '@/components/AtmLayout.vue'

const router = useRouter()

const todayLabel = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    dateStyle: 'full',
    timeStyle: 'short'
  }).format(new Date())
)

function goLogin() {
  router.push({ name: 'login' })
}
</script>

<template>
  <AtmLayout
    eyebrow="Self Service Banking"
    title="欢迎使用校园 ATM 虚拟终端"
    subtitle="提供账户查询、现金交易、转账汇款、密码维护与凭条服务。"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">系统状态</span>
          <strong>在线运行中</strong>
        </div>
        <div class="info-card">
          <span class="info-label">当前时间</span>
          <strong>{{ todayLabel }}</strong>
        </div>
        <div class="feature-list">
          <span>常用服务</span>
          <ul>
            <li>插卡登录</li>
            <li>查询余额</li>
            <li>取款、存款与转账</li>
            <li>密码维护与凭条查询</li>
          </ul>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>开始操作</h2>
      <p>请先完成身份验证，再选择需要办理的账户业务。</p>
    </div>

    <div class="stack-actions">
      <el-button type="primary" size="large" class="full-width" @click="goLogin">
        插卡并继续
      </el-button>
      <el-alert
        title="服务提示"
        type="info"
        :closable="false"
        description="请确认周围环境安全，输入密码时注意遮挡键盘。"
      />
    </div>
  </AtmLayout>
</template>

