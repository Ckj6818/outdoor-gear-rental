<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGearPage } from '@/api/gear'
import { cancelOrder, getOrderPage, payOrder, returnGear } from '@/api/order'
import { getFallbackGearImage, getGearImages } from '@/utils/gearImages'

const router = useRouter()

const loading = ref(false)
const actionLoadingId = ref(null)
const orderList = ref([])
const total = ref(0)
const gearInfoMap = ref({})
const imageErrorMap = ref({})

const userId = computed(() => localStorage.getItem('userId'))

const queryParams = reactive({
  orderStatus: '',
  pageNum: 1,
  pageSize: 10
})

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待支付', value: 0 },
  { label: '借出中', value: 1 },
  { label: '已归还', value: 3 },
  { label: '已逾期', value: 2 }
]

const activeTab = ref('')

const statusConfig = {
  0: { label: '待支付', tagClass: 'tag-pending' },
  1: { label: '借出中', tagClass: 'tag-renting' },
  2: { label: '已逾期', tagClass: 'tag-overdue' },
  3: { label: '已归还', tagClass: 'tag-returned' },
  4: { label: '待质检', tagClass: 'tag-inspection' },
  5: { label: '异常完结', tagClass: 'tag-closed' }
}

function getStatusLabel(status) {
  return statusConfig[status]?.label || '未知'
}

function getStatusTagClass(status) {
  return statusConfig[status]?.tagClass || 'tag-default'
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function formatMoney(value) {
  if (value === null || value === undefined) return '-'
  return `¥ ${Number(value).toFixed(2)}`
}

function getGearName(order) {
  return gearInfoMap.value[order.gearId]?.gearName || `装备 #${order.gearId}`
}

function getGearThumb(order) {
  const gearId = order.gearId
  if (imageErrorMap.value[gearId]) {
    return getFallbackGearImage()
  }
  const gear = gearInfoMap.value[gearId]
  if (gear?.mainImage) {
    return gear.mainImage
  }
  return getGearImages({ id: gearId }).main
}

function handleImageError(gearId) {
  imageErrorMap.value = { ...imageErrorMap.value, [gearId]: true }
}

async function loadGearInfoMap() {
  try {
    const res = await getGearPage({ pageNum: 1, pageSize: 100, status: 1 })
    const map = {}
    ;(res.data.records || []).forEach((gear) => {
      map[gear.id] = gear
    })
    gearInfoMap.value = map
  } catch {
    gearInfoMap.value = {}
  }
}

async function fetchMyOrders() {
  if (!userId.value) {
    ElMessage.warning('请先登录查看租赁记录')
    router.push('/login')
    return
  }

  loading.value = true
  try {
    const params = {
      userId: userId.value,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (queryParams.orderStatus !== '' && queryParams.orderStatus !== null) {
      params.orderStatus = queryParams.orderStatus
    }

    const res = await getOrderPage(params)
    orderList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function switchTab(value) {
  activeTab.value = value
  queryParams.orderStatus = value
  queryParams.pageNum = 1
  fetchMyOrders()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  fetchMyOrders()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchMyOrders()
}

async function handlePay(order) {
  try {
    await ElMessageBox.confirm(
      `确认支付订单 ${order.orderNo}？应付 ${formatMoney(order.totalFee)}`,
      '立即支付',
      { confirmButtonText: '确认支付', cancelButtonText: '取消' }
    )
    actionLoadingId.value = order.id
    await payOrder(order.id)
    ElMessage.success('支付成功，装备已借出')
    await fetchMyOrders()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      // 错误已由 axios 拦截器提示
    }
  } finally {
    actionLoadingId.value = null
  }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm(
      `确认取消订单 ${order.orderNo}？取消后无法恢复。`,
      '取消订单',
      { confirmButtonText: '确认取消', cancelButtonText: '返回' }
    )
    actionLoadingId.value = order.id
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    await fetchMyOrders()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      // 错误已由 axios 拦截器提示
    }
  } finally {
    actionLoadingId.value = null
  }
}

async function handleReturn(order) {
  try {
    await ElMessageBox.confirm(
      `确认归还订单 ${order.orderNo} 的装备？`,
      '确认归还',
      { confirmButtonText: '确认归还', cancelButtonText: '取消' }
    )
    actionLoadingId.value = order.id
    await returnGear(order.id)
    ElMessage.success('归还成功，装备已进入待质检流程')
    await fetchMyOrders()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      // 错误已由 axios 拦截器提示
    }
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(async () => {
  await loadGearInfoMap()
  await fetchMyOrders()
})
</script>

<template>
  <div class="my-orders-page">
    <header class="page-header">
      <h1 class="page-title">我的订单</h1>
      <p class="page-subtitle">租赁记录与订单管理</p>
    </header>

    <nav class="status-tabs" aria-label="订单状态筛选">
      <button
        v-for="tab in statusTabs"
        :key="String(tab.value)"
        type="button"
        class="status-tab"
        :class="{ 'status-tab--active': activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </nav>

    <div v-loading="loading" class="order-list">
      <template v-if="orderList.length">
        <article v-for="order in orderList" :key="order.id" class="order-card">
          <div class="order-card__head">
            <div class="order-card__meta">
              <span class="order-no">{{ order.orderNo }}</span>
              <span class="order-sep">·</span>
              <time class="order-time">{{ formatDateTime(order.createTime) }}</time>
            </div>
            <span class="status-tag" :class="getStatusTagClass(order.orderStatus)">
              {{ getStatusLabel(order.orderStatus) }}
            </span>
          </div>

          <div class="order-card__body">
            <div class="order-card__thumb">
              <img
                :src="getGearThumb(order)"
                :alt="getGearName(order)"
                loading="lazy"
                @error="handleImageError(order.gearId)"
              />
            </div>

            <div class="order-card__info">
              <h2 class="gear-name">{{ getGearName(order) }}</h2>
              <p class="rental-days">租赁 {{ order.rentalDays }} 天</p>
              <p class="return-hint">
                预计归还 {{ formatDateTime(order.expectedReturnTime) }}
              </p>
              <p v-if="order.hasDamageWaiver" class="waiver-hint">含意外损坏豁免金</p>
            </div>

            <div class="order-card__aside">
              <p class="order-fee">{{ formatMoney(order.totalFee) }}</p>
              <div class="action-group">
                <template v-if="order.orderStatus === 0">
                  <button
                    type="button"
                    class="btn btn-pay"
                    :disabled="actionLoadingId === order.id"
                    @click="handlePay(order)"
                  >
                    {{ actionLoadingId === order.id ? '处理中…' : '立即支付' }}
                  </button>
                  <button
                    type="button"
                    class="btn btn-ghost"
                    :disabled="actionLoadingId === order.id"
                    @click="handleCancel(order)"
                  >
                    取消订单
                  </button>
                </template>
                <template v-else-if="order.orderStatus === 1 || order.orderStatus === 2">
                  <button
                    type="button"
                    class="btn btn-outline"
                    :disabled="actionLoadingId === order.id"
                    @click="handleReturn(order)"
                  >
                    {{ actionLoadingId === order.id ? '处理中…' : '确认归还' }}
                  </button>
                </template>
                <span v-else-if="order.orderStatus === 3" class="action-note">已完成</span>
                <span v-else-if="order.orderStatus === 4" class="action-note">待质检</span>
                <span v-else class="action-note">{{ getStatusLabel(order.orderStatus) }}</span>
              </div>
            </div>
          </div>
        </article>
      </template>

      <div v-else-if="!loading" class="empty-state">
        <p class="empty-title">暂无订单</p>
        <p class="empty-desc">当前筛选条件下没有租赁记录</p>
      </div>
    </div>

    <footer v-if="total > queryParams.pageSize" class="pagination-wrap">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </footer>
  </div>
</template>

<style scoped>
.my-orders-page {
  max-width: 920px;
  margin: 0 auto;
  padding: var(--space-lg, 48px) var(--space-md, 24px) var(--space-xl, 80px);
}

.page-header {
  margin-bottom: var(--space-md, 24px);
}

.page-title {
  margin: 0 0 8px;
  font-size: clamp(24px, 4vw, 32px);
  font-weight: 600;
  letter-spacing: 0.06em;
  color: var(--color-text, #222);
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-subtle, #999);
  letter-spacing: var(--letter-spacing-base, 0.02em);
}

/* Top Tabs — 与全局导航一致的极简指示线 */
.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  margin-bottom: var(--space-lg, 48px);
}

.status-tab {
  position: relative;
  margin: 0;
  padding: 6px 0 12px;
  border: none;
  background: none;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-subtle, #999);
  cursor: pointer;
  transition: color 0.25s ease;
}

.status-tab::after {
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

.status-tab:hover {
  color: var(--color-text, #222);
}

.status-tab--active {
  color: var(--color-text, #222);
  font-weight: 600;
}

.status-tab--active::after {
  transform: scaleX(1);
}

/* Order list */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-height: 160px;
}

.order-card {
  background: #fff;
  border: 1px solid var(--color-border, #e8e8e6);
  border-radius: 2px;
  transition: box-shadow 0.28s ease;
}

.order-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.04);
}

.order-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--color-border, #e8e8e6);
}

.order-card__meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: var(--color-text-subtle, #999);
  letter-spacing: 0.03em;
}

.order-no {
  font-weight: 500;
  color: var(--color-text-muted, #6b6b6b);
}

.order-sep {
  opacity: 0.45;
}

.status-tag {
  flex-shrink: 0;
  padding: 4px 12px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.05em;
  border-radius: 2px;
  line-height: 1.3;
}

.tag-pending {
  color: var(--color-text-muted, #6b6b6b);
  background: transparent;
  border: 1px solid var(--color-border, #e8e8e6);
}

.tag-renting {
  color: #fff;
  background: var(--color-text, #222);
  border: 1px solid var(--color-text, #222);
}

.tag-overdue {
  color: var(--color-text, #222);
  background: transparent;
  border: 1px solid var(--color-text, #222);
}

.tag-returned {
  color: var(--color-text-muted, #6b6b6b);
  background: var(--color-border-light, #f0f0ee);
  border: 1px solid transparent;
}

.tag-inspection,
.tag-closed,
.tag-default {
  color: var(--color-text-muted, #6b6b6b);
  background: var(--color-bg, #f7f7f5);
  border: 1px solid var(--color-border, #e8e8e6);
}

.order-card__body {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px;
}

.order-card__thumb {
  flex-shrink: 0;
  width: 96px;
  height: 96px;
  overflow: hidden;
  background: var(--color-border-light, #f0f0ee);
  border: 1px solid var(--color-border, #e8e8e6);
  border-radius: 2px;
}

.order-card__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.order-card__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.gear-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.02em;
  line-height: 1.35;
  color: var(--color-text, #222);
}

.rental-days {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text, #222);
}

.return-hint,
.waiver-hint {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-subtle, #999);
  letter-spacing: 0.02em;
}

.order-card__aside {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  min-width: 148px;
  align-self: stretch;
}

.order-fee {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--color-text, #222);
  font-variant-numeric: tabular-nums;
}

.action-group {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.btn {
  margin: 0;
  padding: 9px 20px;
  border-radius: 2px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: opacity 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-pay {
  border: 1px solid var(--color-text, #222);
  background: var(--color-text, #222);
  color: #fff;
}

.btn-pay:hover:not(:disabled) {
  opacity: 0.86;
}

.btn-outline {
  border: 1px solid var(--color-text, #222);
  background: transparent;
  color: var(--color-text, #222);
}

.btn-outline:hover:not(:disabled) {
  background: var(--color-border-light, #f0f0ee);
}

.btn-ghost {
  padding: 4px 0;
  border: none;
  background: none;
  color: var(--color-text-subtle, #999);
}

.btn-ghost:hover:not(:disabled) {
  color: var(--color-text, #222);
}

.action-note {
  font-size: 12px;
  letter-spacing: 0.04em;
  color: var(--color-text-subtle, #999);
}

.empty-state {
  padding: 72px 24px;
  text-align: center;
  border: 1px dashed var(--color-border, #e8e8e6);
  border-radius: 2px;
}

.empty-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text, #222);
}

.empty-desc {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-subtle, #999);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-lg, 48px);
}

.pagination-wrap :deep(.el-pagination) {
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: var(--color-text, #222);
}

@media (max-width: 720px) {
  .my-orders-page {
    padding-top: var(--space-md, 24px);
  }

  .order-card__body {
    flex-wrap: wrap;
    align-items: flex-start;
  }

  .order-card__aside {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    min-width: 0;
    align-self: auto;
  }

  .action-group {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .status-tabs {
    gap: 20px;
  }
}

@media (max-width: 480px) {
  .order-card__thumb {
    width: 80px;
    height: 80px;
  }

  .order-fee {
    font-size: 20px;
  }

  .gear-name {
    font-size: 16px;
  }
}
</style>
