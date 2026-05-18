<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AtmLayout from '@/components/AtmLayout.vue'
import { changePassword } from '@/api/atm'
import { getErrorMessage } from '@/api/http'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const sessionStore = useSessionStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

function validatePassword(rule, value, callback) {
  if (!/^\d{6}$/.test(value || '')) {
    callback(new Error('请输入 6 位数字密码'))
    return
  }

  callback()
}

function validateConfirmPassword(rule, value, callback) {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }

  callback()
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
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
    const response = await changePassword({
      sessionId: sessionStore.sessionId,
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })

    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    formRef.value.clearValidate()
    ElMessage.success(response.message || '密码修改成功')
    sessionStore.clearSession()
    router.push('/login')
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '密码修改失败，请稍后重试'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AtmLayout
    eyebrow="Password"
    title="修改密码"
    subtitle="修改银行卡登录密码。修改成功后需要重新登录。"
  >
    <template #display>
      <div class="display-stack">
        <div class="info-card">
          <span class="info-label">当前用户</span>
          <strong>{{ sessionStore.customerName || '--' }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">账户编号</span>
          <strong>{{ sessionStore.accountNo || '--' }}</strong>
        </div>
        <div class="info-card">
          <span class="info-label">密码规则</span>
          <strong>请输入 6 位数字密码</strong>
        </div>
      </div>
    </template>

    <div class="panel-header">
      <h2>修改登录密码</h2>
      <p>为保障账户安全，新密码需要二次确认。</p>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          maxlength="6"
          show-password
          type="password"
          placeholder="请输入原 6 位密码"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          maxlength="6"
          show-password
          type="password"
          placeholder="请输入新 6 位密码"
        />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          maxlength="6"
          show-password
          type="password"
          placeholder="请再次输入新密码"
          @keyup.enter="submit"
        />
      </el-form-item>
      <el-button type="primary" size="large" class="full-width" :loading="loading" @click="submit">
        确认修改
      </el-button>
    </el-form>

    <div class="footer-actions">
      <el-button @click="router.push('/menu')">返回主菜单</el-button>
    </div>
  </AtmLayout>
</template>
