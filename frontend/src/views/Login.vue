<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { isAdminRole, setAuthSession } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: 'zhangsan',
  password: '123456'
})

async function handleLogin() {
  loading.value = true
  try {
    const res = await login(form)
    const data = res.data
    setAuthSession({
      token: data.token,
      userId: data.userId,
      username: data.username,
      role: data.role
    })
    ElMessage.success('登录成功')
    router.push(isAdminRole(data.role) ? '/admin/dashboard' : '/gears')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h2>用户登录</h2>
      <el-form :model="form" label-width="80px" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" native-type="submit" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
}

.login-card {
  width: 420px;
}

.login-card h2 {
  text-align: center;
  margin: 0 0 24px;
  color: #303133;
}
</style>
