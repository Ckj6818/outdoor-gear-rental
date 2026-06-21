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
const gearNameMap = ref({})
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
  return gearNameMap.value[order.gearId] || `装备 #${order.gearId}`
}

function getGearThumb(gearId) {
  if (imageErrorMap.value[gearId]) {
    return getFallbackGearImage()
  }
  return getGearImages({ id: gearId }).main
}

function handleImageError(gearId) {
  imageErrorMap.value = { ...imageErrorMap.value, [gearId]: true }
}

async function loadGearNameMap() {
  try {
    const res = await getGearPage({ pageNum: 1, pageSize: 100 })
    const map = {}
    ;(res.data.records || []).forEach((gear) => {
      map[gear.id] = gear.gearName
    })
    gearNameMap.value = map
  } catch {
    gearNameMap.value = {}
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

async function handlePay(row) {
  try {
    await ElMessageBox.confirm(
      `确认支付订单 ${row.orderNo}？应付 ${formatMoney(row.totalFee)}`,
      '立即支付',
      { confirmButtonText: '确认支付', cancelButtonText: '取消', customClass: 'minimal-message-box' }
    )
    actionLoadingId.value = row.id
    await payOrder(row.id)
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

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(
      `确认取消订单 ${row.orderNo}？取消后无法恢复。`,
      '取消订单',
      { confirmButtonText: '确认取消', cancelButtonText: '返回', customClass: 'minimal-message-box' }
    )
    actionLoadingId.value = row.id
    await cancelOrder(row.id)
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

async function handleReturn(row) {
  try {
    await ElMessageBox.confirm(
      `确认归还订单 ${row.orderNo} 的装备？`,
      '确认归还',
      { confirmButtonText: '确认归还', cancelButtonText: '取消', customClass: 'minimal-message-box' }
    )
    actionLoadingId.value = row.id
    await returnGear(row.id)
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
  await loadGearNameMap()
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
              <span class="order-dot">·</span>
              <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
            </div>
            <span class="status-tag" :class="getStatusTagClass(order.orderStatus)">
              {{ getStatusLabel(order.orderStatus) }}
            </span>
          </div>

          <div class="order-card__body">
            <div class="order-card__thumb">
              <img
                :src="getGearThumb(order.gearId)"
                :alt="getGearName(order)"
                loading="lazy"
                @error="handleImageError(order.gearId)"
              />
            </div>

            <div class="order-card__info">
              <div class="gear-title-row">
                <h2 class="gear-name">{{ getGearName(order) }}</h2>
                <span v-if="order.hasDamageWaiver" class="waiver-badge" title="已购买意外损坏豁免金">
                  🛡️
                </span>
              </div>
              <p class="rental-days">租赁 {{ order.rentalDays }} 天</p>
              <p class="return-hint">
                预计归还 {{ formatDateTime(order.expectedReturnTime) }}
              </p>
            </div>

            <div class="order-card__actions">
              <p class="order-fee">{{ formatMoney(order.totalFee) }}</p>
              <div class="action-buttons">
                <template v-if="order.orderStatus === 0">
                  <button
                    type="button"
                    class="btn btn-primary"
                    :disabled="actionLoadingId === order.id"
                    @click="handlePay(order)"
                  >
                    {{ actionLoadingId === order.id ? '处理中…' : '立即支付' }}
                  </button>
                  <button
                    type="button"
                    class="btn btn-text"
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
                <span v-else-if="order.orderStatus === 3" class="action-muted">已完成</span>
                <span v-else-if="order.orderStatus === 4" class="action-muted">待质检</span>
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

    <div v-if="total > queryParams.pageSize" class="pagination-wrap">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<style scoped>
.my-orders-page {
  max-width: 960px;
  margin: 0 auto;
  padding: var(--space-md, 24px) var(--space-sm, 16px) var(--space-lg, 48px);
}

.page-header {
  margin-bottom: var(--space-md, 24px);
}

.page-title {
  margin: 0 0 6px;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: var(--letter-spacing-wide, 0.06em);
  color: var(--color-text, #222);
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-subtle, #999);
  letter-spacing: var(--letter-spacing-base, 0.02em);
}

.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  margin-bottom: var(--space-md, 24px);
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border, #e8e8e6);
}

.status-tab {
  position: relative;
  margin: 0;
  padding: 4px 0 10px;
  border: none;
  background: none;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-muted, #6b6b6b);
  cursor: pointer;
  transition: color 0.25s ease;
}

.status-tab::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -1px;
  width: 100%;
  height: 1px;
  background: var(--color-accent-hover, #1a1a1a);
  transform: scaleX(0);
  transition: transform 0.25s ease;
}

.status-tab:hover {
  color: var(--color-text, #222);
}

.status-tab--active {
  color: var(--color-accent-hover, #1a1a1a);
  font-weight: 600;
}

.status-tab--active::after {
  transform: scaleX(1);
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 120px;
}

.order-card {
  background: var(--color-bg-elevated, #fff);
  border: 1px solid var(--color-border, #e8e8e6);
  border-radius: 2px;
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}

.order-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.order-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--color-border, #e8e8e6);
}

.order-card__meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: var(--color-text-subtle, #999);
  letter-spacing: 0.04em;
}

.order-no {
  color: var(--color-text-muted, #6b6b6b);
  font-weight: 500;
}

.order-dot {
  opacity: 0.5;
}

.status-tag {
  flex-shrink: 0;
  padding: 3px 10px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  border-radius: 2px;
  line-height: 1.4;
}

.tag-pending {
  color: var(--color-text-muted, #6b6b6b);
  background: transparent;
  border: 1px solid var(--color-border, #e8e8e6);
}

.tag-renting {
  color: #fff;
  background: var(--color-accent-hover, #1a1a1a);
  border: 1px solid var(--color-accent-hover, #1a1a1a);
}

.tag-overdue {
  color: var(--color-accent-hover, #1a1a1a);
  background: transparent;
  border: 1px solid var(--color-accent, #3d3d3d);
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
  align-items: stretch;
  gap: 20px;
  padding: 20px;
}

.order-card__thumb {
  flex-shrink: 0;
  width: 88px;
  height: 88px;
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
  justify-content: center;
  gap: 6px;
}

.gear-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.gear-name {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--color-text, #222);
  line-height: var(--line-height-tight, 1.4);
}

.waiver-badge {
  font-size: 13px;
  line-height: 1;
  opacity: 0.75;
}

.rental-days {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text, #222);
}

.return-hint {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-subtle, #999);
  letter-spacing: 0.02em;
}

.order-card__actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  min-width: 140px;
}

.order-fee {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--color-accent-hover, #1a1a1a);
  font-variant-numeric: tabular-nums;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.btn {
  margin: 0;
  padding: 8px 18px;
  border-radius: 2px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: opacity 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn-primary {
  border: 1px solid var(--color-accent-hover, #1a1a1a);
  background: var(--color-accent-hover, #1a1a1a);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.88;
}

.btn-outline {
  border: 1px solid var(--color-accent, #3d3d3d);
  background: transparent;
  color: var(--color-accent-hover, #1a1a1a);
}

.btn-outline:hover:not(:disabled) {
  background: var(--color-bg, #f7f7f5);
}

.btn-text {
  padding: 4px 0;
  border: none;
  background: none;
  color: var(--color-text-subtle, #999);
}

.btn-text:hover:not(:disabled) {
  color: var(--color-text, #222);
}

.action-muted {
  font-size: 12px;
  color: var(--color-text-subtle, #999);
  letter-spacing: 0.04em;
}

.empty-state {
  padding: 64px 24px;
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
  margin-top: var(--space-md, 24px);
}

.pagination-wrap :deep(.el-pagination) {
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: var(--color-accent-hover, #1a1a1a);
}

@media (max-width: 720px) {
  .order-card__body {
    flex-wrap: wrap;
  }

  .order-card__actions {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    min-width: 0;
  }

  .action-buttons {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: flex-end;
  }

  .status-tabs {
    gap: 18px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 22px;
  }

  .order-card__thumb {
    width: 72px;
    height: 72px;
  }

  .order-fee {
    font-size: 18px;
  }
}
</style>
