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
    <el-main class="main" :class="{ 'main--full': route.path === '/gears' }">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background: var(--color-bg);
}

.header {
  display: flex;
  align-items: center;
  background: var(--color-bg-elevated);
  border-bottom: 1px solid var(--color-border);
  padding: 0 var(--space-md);
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: 13px;
  font-weight: 500;
  letter-spacing: var(--letter-spacing-wide);
  text-transform: uppercase;
  color: var(--color-text);
  margin-right: var(--space-lg);
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
}

.user-area {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.main {
  padding: var(--space-lg) var(--space-md);
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
}

.main--full {
  max-width: none;
  padding: 0;
  overflow-x: hidden;
}
</style>
