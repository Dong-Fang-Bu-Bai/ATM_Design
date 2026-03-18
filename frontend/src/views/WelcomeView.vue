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
    subtitle="第一次迭代聚焦登录、主菜单与余额查询原型，页面流程已按 ATM 自助机交互组织。"
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
          <span>支持功能</span>
          <ul>
            <li>插卡登录</li>
            <li>主菜单导航</li>
            <li>查询余额</li>
            <li>交易功能入口预留</li>
          </ul>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>开始操作</h2>
      <p>模拟 ATM 欢迎页，点击下方按钮进入插卡登录流程。</p>
    </div>

    <div class="stack-actions">
      <el-button type="primary" size="large" class="full-width" @click="goLogin">
        插卡并继续
      </el-button>
      <el-alert
        title="演示账号"
        type="info"
        :closable="false"
        description="若本地未接后端，可在 .env 中设置 VITE_USE_MOCK=true，使用卡号 6222020000000001 / 密码 123456 体验流程。"
      />
    </div>
  </AtmLayout>
</template>

