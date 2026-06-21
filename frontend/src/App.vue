<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const searchOpen = ref(false)
const searchKeyword = ref('')
const searchInputRef = ref(null)

const username = computed(() => localStorage.getItem('username') || '')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const user = computed(() => {
  if (!isLoggedIn.value) return null
  const roleRaw = localStorage.getItem('role')
  return {
    userId: localStorage.getItem('userId'),
    username: localStorage.getItem('username') || '',
    role: roleRaw === null || roleRaw === '' ? 1 : Number(roleRaw)
  }
})

const isAdmin = computed(() => user.value?.role === 0)
const isLoginPage = computed(() => route.path === '/login')

const avatarText = computed(() => {
  const name = username.value.trim()
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const subNavItems = [
  { label: '装备评测', en: 'GEAR', to: '/reviews' },
  { label: '户外技能', en: 'HOW-TO', to: '/how-to' },
  { label: '周边路线', en: 'LOCAL', to: '/local' },
  { label: '环保倡议', en: 'IMPACT', to: '/impact' }
]

function goHome() {
  router.push('/gears')
}

function goLogin() {
  router.push('/login')
}

function goSignup() {
  router.push('/login')
}

function openSearch() {
  searchOpen.value = true
}

function closeSearch() {
  searchOpen.value = false
}

function handleSearchSubmit() {
  const keyword = searchKeyword.value.trim()
  closeSearch()
  router.push({
    path: '/gears',
    query: keyword ? { keyword } : {}
  })
}

function handleSearchKeydown(event) {
  if (event.key === 'Escape') {
    closeSearch()
  }
}

function handleUserCommand(cmd) {
  switch (cmd) {
    case 'orders':
      router.push('/orders')
      break
    case 'gear-manage':
      router.push('/gears')
      break
    case 'order-manage':
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

watch(searchOpen, async (open) => {
  if (open) {
    await nextTick()
    searchInputRef.value?.focus()
  }
})

watch(
  () => route.path,
  () => {
    if (searchOpen.value) {
      closeSearch()
    }
  }
)
</script>

<template>
  <div class="layout">
    <header v-if="!isLoginPage" class="global-header">
      <div class="global-header__shell">
        <!-- 主导航行 -->
        <div class="primary-nav">
          <div class="header-left">
            <button type="button" class="brand" @click="goHome">GEAR RENTAL</button>
          </div>

          <nav class="header-center" aria-label="主导航">
            <router-link to="/gears" class="nav-link">装备大厅</router-link>
            <router-link to="/orders" class="nav-link">我的订单</router-link>
          </nav>

          <div class="header-right">
            <button
              type="button"
              class="search-trigger"
              aria-label="打开搜索"
              :aria-expanded="searchOpen"
              @click="openSearch"
            >
              <el-icon :size="18"><Search /></el-icon>
            </button>

            <template v-if="isLoggedIn">
              <el-dropdown trigger="click" @command="handleUserCommand">
                <button type="button" class="avatar-trigger" aria-label="用户菜单">
                  <span class="avatar-circle">{{ avatarText }}</span>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                    <template v-if="isAdmin">
                      <el-dropdown-item divided command="gear-manage">⚙️ 装备管理</el-dropdown-item>
                      <el-dropdown-item command="order-manage">📦 全部订单管理</el-dropdown-item>
                    </template>
                    <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <button type="button" class="auth-link" @click="goLogin">Log In</button>
              <button type="button" class="auth-link auth-link--emphasis" @click="goSignup">Sign Up</button>
            </template>
          </div>
        </div>

        <!-- REI 风格二级导航 -->
        <nav class="rei-sub-nav" aria-label="内容导航">
          <router-link
            v-for="item in subNavItems"
            :key="item.en"
            :to="item.to"
            class="sub-nav-link"
          >
            <span class="sub-nav-label">{{ item.label }}</span>
            <span class="sub-nav-en">{{ item.en }}</span>
          </router-link>
        </nav>

        <!-- 沉浸式搜索面板（绝对定位覆盖二级导航区域） -->
        <div v-show="searchOpen" class="search-panel" role="search">
          <button type="button" class="search-panel__close" aria-label="关闭搜索" @click="closeSearch">
            ✕
          </button>
          <input
            ref="searchInputRef"
            v-model="searchKeyword"
            type="search"
            class="search-panel__input"
            placeholder="搜索装备、户外技能或目的地路线..."
            @keydown="handleSearchKeydown"
            @keydown.enter.prevent="handleSearchSubmit"
          />
        </div>
      </div>
    </header>

    <main class="main" :class="{ 'main--full': route.path === '/gears', 'main--login': isLoginPage }">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  background: var(--color-bg, #f5f7fa);
}

.global-header {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100vw;
  margin-left: calc(50% - 50vw);
  box-sizing: border-box;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  background-color: rgba(255, 255, 255, 0.85);
  border-bottom: 1px solid var(--color-border, #e8e8e6);
}

.global-header__shell {
  position: relative;
  width: 100%;
  box-sizing: border-box;
}

.primary-nav {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: 64px;
  padding: 0 40px;
  box-sizing: border-box;
}

.header-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-shrink: 0;
}

.brand {
  margin: 0;
  padding: 0;
  border: none;
  background: none;
  white-space: nowrap;
  font-family: var(--font-sans, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.1em;
  color: #1a1a1a;
  cursor: pointer;
  transition: color 0.25s ease, opacity 0.25s ease;
}

.brand:hover {
  color: #000;
  opacity: 0.85;
}

.header-center {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 32px;
  flex-shrink: 0;
}

.nav-link {
  position: relative;
  padding: 6px 0;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  text-decoration: none;
  transition: color 0.25s ease;
}

.nav-link::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 1px;
  background: #1a1a1a;
  transform: scaleX(0);
  transform-origin: center;
  transition: transform 0.25s ease;
}

.nav-link:hover {
  color: #1a1a1a;
}

.nav-link.router-link-active {
  color: #1a1a1a;
  font-weight: 600;
}

.nav-link.router-link-active::after {
  transform: scaleX(1);
}

.header-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  gap: 20px;
  flex-shrink: 0;
}

.search-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  margin: 0;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--color-text-muted, #6b6b6b);
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.25s ease, background 0.25s ease;
}

.search-trigger:hover {
  color: #1a1a1a;
  background: rgba(0, 0, 0, 0.04);
}

.auth-link {
  margin: 0;
  padding: 0;
  border: none;
  background: none;
  white-space: nowrap;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.02em;
  color: #666;
  cursor: pointer;
  transition: color 0.25s ease, opacity 0.25s ease;
}

.auth-link:hover {
  color: #1a1a1a;
}

.auth-link--emphasis {
  color: #1a1a1a;
  font-weight: 600;
}

.auth-link--emphasis:hover {
  opacity: 0.72;
}

.avatar-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
  flex-shrink: 0;
}

.avatar-circle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #1a1a1a;
  color: #fafafa;
  font-size: 13px;
  font-weight: 600;
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.avatar-trigger:hover .avatar-circle {
  opacity: 0.82;
  transform: scale(1.03);
}

/* REI 二级导航 */
.rei-sub-nav {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  justify-content: center;
  align-items: center;
  gap: 48px;
  width: 100%;
  padding: 12px 40px;
  box-sizing: border-box;
  overflow-x: auto;
  border-top: 1px solid var(--color-border-light, #f0f0ee);
  scrollbar-width: none;
}

.rei-sub-nav::-webkit-scrollbar {
  display: none;
}

.sub-nav-link {
  position: relative;
  display: inline-flex;
  flex-direction: row;
  flex-shrink: 0;
  align-items: baseline;
  gap: 6px;
  padding: 4px 0 10px;
  white-space: nowrap;
  text-decoration: none;
  color: var(--color-text-subtle, #999);
  transition: color 0.25s ease;
}

.sub-nav-link::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 2px;
  background: var(--color-text, #222);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.22s ease;
}

.sub-nav-link:hover {
  color: var(--color-text, #222);
  text-decoration: none;
}

.sub-nav-link:hover::after {
  transform: scaleX(1);
}

.sub-nav-link.router-link-active {
  color: var(--color-text, #222);
  font-weight: 600;
  text-decoration: none;
}

.sub-nav-link.router-link-active::after {
  transform: scaleX(1);
}

.sub-nav-link.router-link-active .sub-nav-label,
.sub-nav-link.router-link-active .sub-nav-en {
  color: var(--color-text, #222);
  opacity: 1;
  font-weight: 600;
}

.sub-nav-label,
.sub-nav-en {
  white-space: nowrap;
  flex-shrink: 0;
}

.sub-nav-label {
  font-size: 13px;
  font-weight: 500;
  color: inherit;
  letter-spacing: 0.04em;
  transition: color 0.25s ease;
}

.sub-nav-en {
  font-size: 11px;
  font-weight: 500;
  color: inherit;
  letter-spacing: 0.08em;
  opacity: 0.72;
  transition: color 0.25s ease, opacity 0.25s ease;
}

.sub-nav-link:hover .sub-nav-label,
.sub-nav-link:hover .sub-nav-en {
  color: var(--color-text, #222);
  opacity: 1;
}

/* 沉浸式搜索面板 */
.search-panel {
  position: absolute;
  top: 64px;
  left: 0;
  z-index: 20;
  width: 100%;
  box-sizing: border-box;
  padding: 28px 40px 36px;
  background-color: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-top: 1px solid var(--color-border-light, #f0f0ee);
  border-bottom: 1px solid var(--color-border, #e8e8e6);
  animation: search-panel-in 0.28s ease;
}

@keyframes search-panel-in {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-panel__close {
  position: absolute;
  top: 20px;
  right: 40px;
  margin: 0;
  padding: 4px 8px;
  border: none;
  background: none;
  font-size: 18px;
  line-height: 1;
  color: var(--color-text-subtle, #999);
  cursor: pointer;
  transition: color 0.2s ease;
}

.search-panel__close:hover {
  color: #1a1a1a;
}

.search-panel__input {
  display: block;
  width: 100%;
  box-sizing: border-box;
  margin: 0;
  padding: 12px 48px 14px 0;
  border: none;
  border-bottom: 1px solid var(--color-border, #e8e8e6);
  border-radius: 0;
  background: transparent;
  font-family: var(--font-sans, inherit);
  font-size: 18px;
  font-weight: 400;
  letter-spacing: 0.02em;
  color: #1a1a1a;
  outline: none;
  transition: border-color 0.25s ease;
}

.search-panel__input::placeholder {
  color: var(--color-text-subtle, #999);
  font-weight: 400;
}

.search-panel__input:focus {
  border-bottom-color: #1a1a1a;
}

.search-panel__input::-webkit-search-cancel-button {
  -webkit-appearance: none;
}

.main {
  flex: 1;
  padding: 24px 16px;
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
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
  .rei-sub-nav {
    justify-content: flex-start;
    gap: 32px;
  }

  .sub-nav-en {
    display: inline;
  }
}

@media (max-width: 768px) {
  .primary-nav {
    padding: 0 16px;
    height: 56px;
  }

  .header-center {
    gap: 20px;
  }

  .nav-link {
    font-size: 13px;
  }

  .brand {
    font-size: 11px;
  }

  .rei-sub-nav {
    gap: 24px;
    padding: 10px 16px;
  }

  .search-panel {
    top: 56px;
    padding: 24px 16px 32px;
  }

  .search-panel__close {
    right: 16px;
  }

  .search-panel__input {
    font-size: 16px;
    padding-right: 40px;
  }
}

@media (max-width: 480px) {
  .header-center {
    gap: 14px;
  }

  .auth-link {
    font-size: 12px;
  }

  .header-right {
    gap: 12px;
  }

  .sub-nav-en {
    display: none;
  }
}
</style>
