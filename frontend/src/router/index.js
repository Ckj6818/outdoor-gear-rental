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
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 户外装备租赁` : '户外装备租赁'
})

export default router
