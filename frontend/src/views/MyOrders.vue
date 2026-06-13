<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGearPage } from '@/api/gear'
import { getOrderPage, payOrder, returnGear } from '@/api/order'

const router = useRouter()

const loading = ref(false)
const actionLoadingId = ref(null)
const orderList = ref([])
const total = ref(0)
const gearNameMap = ref({})

const userId = computed(() => localStorage.getItem('userId'))
const username = computed(() => localStorage.getItem('username') || '游客')

const queryParams = reactive({
  orderStatus: '',
  pageNum: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待支付', value: 0 },
  { label: '借出中', value: 1 },
  { label: '已逾期', value: 2 },
  { label: '已归还', value: 3 }
]

const statusConfig = {
  0: { label: '待支付', type: 'info', rowClass: 'row-pending' },
  1: { label: '借出中', type: 'warning', rowClass: 'row-renting' },
  2: { label: '已逾期', type: 'danger', rowClass: 'row-overdue' },
  3: { label: '已归还', type: 'success', rowClass: 'row-returned' }
}

function getStatusLabel(status) {
  return statusConfig[status]?.label || '未知'
}

function getStatusType(status) {
  return statusConfig[status]?.type || 'info'
}

function getRowClassName({ row }) {
  return statusConfig[row.orderStatus]?.rowClass || ''
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function formatMoney(value) {
  if (value === null || value === undefined) return '-'
  return `¥${Number(value).toFixed(2)}`
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

function handleSearch() {
  queryParams.pageNum = 1
  fetchMyOrders()
}

function handleReset() {
  queryParams.orderStatus = ''
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
      `确认支付订单 ${row.orderNo}？应付金额 ${formatMoney(row.totalFee)}`,
      '模拟支付',
      { type: 'warning', confirmButtonText: '确认支付', cancelButtonText: '取消' }
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

async function handleReturn(row) {
  try {
    await ElMessageBox.confirm(
      `确认归还订单 ${row.orderNo} 的装备？`,
      '归还装备',
      { type: 'warning', confirmButtonText: '确认归还', cancelButtonText: '取消' }
    )
    actionLoadingId.value = row.id
    await returnGear(row.id)
    ElMessage.success('归还成功，库存已恢复')
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
    <el-card shadow="never" class="header-card">
      <div class="page-header">
        <div>
          <h2>我的租赁订单</h2>
          <p>当前用户：{{ username }}（ID: {{ userId || '-' }}）</p>
        </div>
        <div class="status-legend">
          <el-tag type="info" effect="plain">待支付</el-tag>
          <el-tag type="warning" effect="plain">借出中</el-tag>
          <el-tag type="danger" effect="dark">已逾期</el-tag>
          <el-tag type="success" effect="plain">已归还</el-tag>
        </div>
      </div>

      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.orderStatus" placeholder="全部状态" style="width: 160px">
            <el-option
              v-for="item in statusOptions"
              :key="String(item.value)"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="handleSearch">查询</el-button>
          <el-button :icon="'Refresh'" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="orderList"
        stripe
        border
        style="width: 100%"
        :row-class-name="getRowClassName"
        empty-text="暂无租赁记录"
      >
        <el-table-column prop="orderNo" label="订单编号" min-width="170" fixed="left" />

        <el-table-column label="装备名称" min-width="180">
          <template #default="{ row }">
            {{ gearNameMap[row.gearId] || `装备 #${row.gearId}` }}
          </template>
        </el-table-column>

        <el-table-column prop="rentalDays" label="租赁天数" width="100" align="center">
          <template #default="{ row }">{{ row.rentalDays }} 天</template>
        </el-table-column>

        <el-table-column label="订单状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.orderStatus)"
              :effect="row.orderStatus === 2 ? 'dark' : 'light'"
              class="status-tag"
            >
              <el-icon v-if="row.orderStatus === 1" class="status-icon"><Timer /></el-icon>
              <el-icon v-if="row.orderStatus === 2" class="status-icon"><WarningFilled /></el-icon>
              {{ getStatusLabel(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="借出时间" min-width="150">
          <template #default="{ row }">{{ formatDateTime(row.rentOutTime) }}</template>
        </el-table-column>

        <el-table-column label="预计归还" min-width="150">
          <template #default="{ row }">
            <span :class="{ 'overdue-text': row.orderStatus === 2 }">
              {{ formatDateTime(row.expectedReturnTime) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="实际归还" min-width="150">
          <template #default="{ row }">{{ formatDateTime(row.actualReturnTime) }}</template>
        </el-table-column>

        <el-table-column label="总费用" width="110" align="right">
          <template #default="{ row }">
            <span class="fee-text">{{ formatMoney(row.totalFee) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.orderStatus === 0"
              type="primary"
              link
              :loading="actionLoadingId === row.id"
              @click="handlePay(row)"
            >
              去支付
            </el-button>
            <el-button
              v-if="row.orderStatus === 1 || row.orderStatus === 2"
              type="warning"
              link
              :loading="actionLoadingId === row.id"
              @click="handleReturn(row)"
            >
              归还装备
            </el-button>
            <span v-if="row.orderStatus === 3" class="action-done">已完成</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.my-orders-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.header-card,
.table-card {
  border-radius: 12px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.status-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.status-icon {
  font-size: 14px;
}

.fee-text {
  color: #f56c6c;
  font-weight: 600;
}

.overdue-text {
  color: #f56c6c;
  font-weight: 700;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.action-done {
  color: #909399;
  font-size: 13px;
}

:deep(.row-pending) {
  background-color: #fafafa !important;
}

:deep(.row-renting) {
  background-color: #fdf6ec !important;
}

:deep(.row-overdue) {
  background-color: #fef0f0 !important;
}

:deep(.row-overdue td) {
  color: #c45656;
}

:deep(.row-returned) {
  background-color: #f0f9eb !important;
}
</style>
