import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/gears'
  },
  {
    path: '/gears',
    name: 'GearList',
    component: () => import('@/views/GearList.vue'),
    meta: { title: '装备大厅' }
  },
  {
    path: '/orders',
    name: 'MyOrders',
    component: () => import('@/views/MyOrders.vue'),
    meta: { title: '我的租赁' }
  },
  {
    path: '/reviews',
    name: 'Reviews',
    component: () => import('@/views/GearReviews.vue'),
    meta: { title: '装备评测' }
  },
  {
    path: '/how-to',
    name: 'HowTo',
    component: () => import('@/views/HowToGuides.vue'),
    meta: { title: '户外技能' }
  },
  {
    path: '/local',
    name: 'Local',
    component: () => import('@/views/LocalRoutes.vue'),
    meta: { title: '周边路线' }
  },
  {
    path: '/impact',
    name: 'Impact',
    component: () => import('@/views/Impact.vue'),
    meta: { title: '环保倡议' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '运营大屏', requiresAdmin: true }
  },
  {
    path: '/admin/orders',
    name: 'AdminOrderManage',
    component: () => import('@/views/admin/OrderManage.vue'),
    meta: { title: '订单质检', requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 户外装备租赁` : '户外装备租赁'

  if (to.meta.requiresAdmin) {
    const role = localStorage.getItem('role')
    const token = localStorage.getItem('token')
    if (!token) {
      return '/login'
    }
    if (role !== '0') {
      return '/gears'
    }
  }
})

export default router
