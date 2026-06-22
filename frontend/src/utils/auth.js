import { computed, reactive } from 'vue'

/** 后端 SysUser.role：0=管理员，1=普通用户 */
export const ROLE_ADMIN = 0
export const ROLE_USER = 1

function readStorage() {
  const token = localStorage.getItem('token') || ''
  return {
    token,
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    role: token ? Number(localStorage.getItem('role') ?? ROLE_USER) : ROLE_USER
  }
}

export const authState = reactive(readStorage())

export function isAdminRole(role) {
  return Number(role) === ROLE_ADMIN
}

/** 登录成功后写入会话（localStorage + 响应式状态） */
export function setAuthSession({ token, userId, username, role }) {
  const normalizedRole = role ?? ROLE_USER
  localStorage.setItem('token', token)
  localStorage.setItem('userId', String(userId))
  localStorage.setItem('username', username)
  localStorage.setItem('role', String(normalizedRole))

  authState.token = token
  authState.userId = String(userId)
  authState.username = username
  authState.role = normalizedRole
}

export function clearAuthSession() {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('role')

  authState.token = ''
  authState.userId = ''
  authState.username = ''
  authState.role = ROLE_USER
}

export function useAuth() {
  return {
    authState,
    isLoggedIn: computed(() => !!authState.token),
    isAdmin: computed(() => isAdminRole(authState.role)),
    setAuthSession,
    clearAuthSession
  }
}
