<script setup>
import { useRouter } from 'vue-router'
import { useAuth } from '@/utils/auth'

const router = useRouter()
const { isAdmin } = useAuth()

function goBack() {
  router.push(isAdmin.value ? '/admin/dashboard' : '/gears')
}
</script>

<template>
  <div class="forbidden-page">
    <el-result icon="warning" title="403" sub-title="您没有权限访问该页面">
      <template #extra>
        <el-button type="primary" @click="goBack">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.forbidden-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
</style>
