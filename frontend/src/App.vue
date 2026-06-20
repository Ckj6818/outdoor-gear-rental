<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const username = computed(() => localStorage.getItem('username') || '')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const isAdmin = computed(() => localStorage.getItem('role') === '0')

const activeMenu = computed(() => route.path)

function goLogin() {
  router.push('/login')
}

function handleUserCommand(cmd) {
  if (cmd === 'admin') {
    goAdminOrders()
  } else if (cmd === 'logout') {
    logout()
  }
}

function goAdminOrders() {
  router.push('/admin/orders')
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('role')
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
          <el-dropdown trigger="click" @command="handleUserCommand">
            <span class="user-dropdown">
              <el-tag type="success" effect="plain">{{ username }}</el-tag>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isAdmin" command="admin">
                  订单大盘 / 质检中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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

.user-dropdown {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.dropdown-icon {
  color: #909399;
  font-size: 12px;
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
