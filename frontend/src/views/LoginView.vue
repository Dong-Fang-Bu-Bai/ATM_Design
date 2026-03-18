<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { getProfile, login } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const route = useRoute()
const sessionStore = useSessionStore()

const loading = ref(false)
const formRef = ref()
const form = reactive({
  cardNo: '',
  password: ''
})

const rules = {
  cardNo: [
    { required: true, message: '请输入银行卡号', trigger: 'blur' },
    { min: 16, max: 19, message: '卡号长度应为 16-19 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 6, message: 'ATM 密码为 6 位数字', trigger: 'blur' }
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

  try {
    const loginResponse = await login(form)
    sessionStore.setSession(loginResponse.data)

    try {
      const profileResponse = await getProfile(loginResponse.data.sessionId)
      sessionStore.setProfile(profileResponse.data)
    } catch (error) {
      ElMessage.warning(getErrorMessage(error, '账户信息暂未获取成功'))
    }

    ElMessage.success(loginResponse.message || '登录成功')
    router.push((route.query.redirect && String(route.query.redirect)) || '/menu')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '登录失败，请检查卡号或密码'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AtmLayout
    eyebrow="Card Authentication"
    title="插卡登录"
    subtitle="登录成功后进入主菜单，并将当前 ATM 会话写入 Pinia 与本地存储。"
  >
    <template #display>
      <div class="display-stack">
        <div class="atm-chip-card">
          <span class="chip-card-label">Card Input</span>
          <strong>请插入银行卡并输入 6 位密码</strong>
          <small>接口：POST /api/atm/auth/login</small>
        </div>
        <div class="hint-block">
          <span>本轮目标</span>
          <p>打通登录、主菜单跳转、会话保持与账户信息预取。</p>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>身份验证</h2>
      <p>请输入银行卡号与密码。</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="银行卡号" prop="cardNo">
        <el-input v-model="form.cardNo" maxlength="19" placeholder="6222020000000001" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          maxlength="6"
          show-password
          type="password"
          placeholder="请输入 6 位密码"
          @keyup.enter="submit"
        />
      </el-form-item>
      <el-button type="primary" size="large" class="full-width" :loading="loading" @click="submit">
        登录并进入主菜单
      </el-button>
    </el-form>
  </AtmLayout>
</template>
