<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const username = computed(() => localStorage.getItem('username') || '')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const activeMenu = computed(() => route.path)

function goLogin() {
  router.push('/login')
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">
        <el-icon><Compass /></el-icon>
        <span>户外装备租赁系统</span>
      </div>
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        router
        class="nav-menu"
      >
        <el-menu-item index="/gears">装备大厅</el-menu-item>
        <el-menu-item index="/orders">我的租赁</el-menu-item>
      </el-menu>
      <div class="user-area">
        <template v-if="isLoggedIn">
          <el-tag type="success" effect="plain">{{ username }}</el-tag>
          <el-button link type="danger" @click="logout">退出</el-button>
        </template>
        <el-button v-else type="primary" @click="goLogin">登录</el-button>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-right: 32px;
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main {
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
}
</style>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}
</style>
