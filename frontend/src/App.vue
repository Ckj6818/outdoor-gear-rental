<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const globalKeyword = ref('')

const username = computed(() => localStorage.getItem('username') || '游客')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const isAdmin = computed(() => localStorage.getItem('role') === '0')
const isLoginPage = computed(() => route.path === '/login')

const avatarText = computed(() => (username.value ? username.value.charAt(0).toUpperCase() : 'U'))
const unreadCount = 3

function goLogin() {
  router.push('/login')
}

function goGears() {
  router.push('/gears')
}

function handleGlobalSearch() {
  const keyword = globalKeyword.value.trim()
  router.push({
    path: '/gears',
    query: keyword ? { keyword } : {}
  })
}

function handleUserCommand(cmd) {
  switch (cmd) {
    case 'profile':
      router.push('/gears')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'admin':
      router.push('/admin/dashboard')
      break
    case 'inspect':
      router.push('/admin/orders')
      break
    case 'logout':
      logout()
      break
    default:
      break
  }
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
    <el-header v-if="!isLoginPage" class="header">
      <div class="header-inner">
        <!-- 左侧：Logo + 导航 -->
        <div class="header-left">
          <div class="brand" @click="goGears">
            <span class="brand-mark">山行</span>
            <span class="brand-sub">Outdoor Rental</span>
          </div>
          <nav class="nav-links">
            <router-link to="/gears" class="nav-link" :class="{ active: route.path === '/gears' }">
              装备大厅
            </router-link>
          </nav>
        </div>

        <!-- 中间：全局搜索 -->
        <div class="header-center">
          <el-input
            v-model="globalKeyword"
            placeholder="搜索装备名称、品牌..."
            clearable
            class="global-search"
            @keyup.enter="handleGlobalSearch"
            @clear="handleGlobalSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 右侧：消息 + 用户 -->
        <div class="header-right">
          <el-badge :value="unreadCount" :max="99" class="notice-badge">
            <el-button circle plain class="notice-btn">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>

          <template v-if="isLoggedIn">
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="user-trigger">
                <el-avatar :size="36" class="user-avatar">{{ avatarText }}</el-avatar>
                <span class="user-name">{{ username }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" command="admin">运营数据大屏</el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" command="inspect">后台质检中心</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <el-button v-else type="primary" round @click="goLogin">登录</el-button>
        </div>
      </div>
    </el-header>

    <el-main class="main" :class="{ 'main--full': route.path === '/gears', 'main--login': isLoginPage }">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background: var(--color-bg, #f5f7fa);
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px;
  padding: 0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: 60px;
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 28px;
  flex-shrink: 0;
}

.brand {
  display: flex;
  flex-direction: column;
  cursor: pointer;
  user-select: none;
  line-height: 1.1;
}

.brand-mark {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #1a1a1a;
}

.brand-sub {
  font-size: 10px;
  color: #909399;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  text-decoration: none;
  transition: all 0.2s ease;
}

.nav-link:hover,
.nav-link.active {
  color: #409eff;
  background: rgba(64, 158, 255, 0.08);
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 0;
  max-width: 520px;
  margin: 0 auto;
}

.global-search {
  width: 100%;
}

.global-search :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 0 0 1px #e4e7ed inset;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.notice-badge :deep(.el-badge__content) {
  border: none;
}

.notice-btn {
  border: none;
  background: #f4f4f5;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 24px;
  transition: background 0.2s ease;
}

.user-trigger:hover {
  background: #f5f7fa;
}

.user-avatar {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #303133;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  color: #909399;
  font-size: 12px;
}

.main {
  padding: 24px 16px;
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
}

.main--full {
  max-width: none;
  padding: 0;
  overflow-x: hidden;
}

.main--login {
  max-width: none;
  padding: 0;
}

@media (max-width: 900px) {
  .header-inner {
    padding: 0 12px;
    gap: 10px;
  }

  .brand-sub,
  .user-name,
  .nav-links {
    display: none;
  }

  .header-center {
    max-width: none;
  }
}

@media (max-width: 480px) {
  .header-right {
    gap: 8px;
  }
}
</style>
