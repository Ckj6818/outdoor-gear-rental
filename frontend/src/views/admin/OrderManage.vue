<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getGearPage } from '@/api/gear'
import { getAdminOrderPage, inspectOrder } from '@/api/order'

const loading = ref(false)
const submitLoading = ref(false)
const orderList = ref([])
const total = ref(0)
const gearNameMap = ref({})

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
  { label: '已归还', value: 3 },
  { label: '待质检', value: 4 },
  { label: '异常完结', value: 5 }
]

const statusConfig = {
  0: { label: '待支付', type: 'info' },
  1: { label: '借出中', type: 'warning' },
  2: { label: '已逾期', type: 'danger' },
  3: { label: '已归还', type: 'success' },
  4: { label: '待质检', type: 'warning' },
  5: { label: '异常完结', type: 'danger' }
}

const inspectDialogVisible = ref(false)
const inspectForm = reactive({
  orderId: null,
  orderNo: '',
  isPassed: true,
  remark: ''
})

function getStatusLabel(status) {
  return statusConfig[status]?.label || '未知'
}

function getStatusType(status) {
  return statusConfig[status]?.type || 'info'
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
    const res = await getGearPage({ pageNum: 1, pageSize: 200 })
    const map = {}
    ;(res.data.records || []).forEach((gear) => {
      map[gear.id] = gear.gearName
    })
    gearNameMap.value = map
  } catch {
    gearNameMap.value = {}
  }
}

async function fetchOrders() {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (queryParams.orderStatus !== '' && queryParams.orderStatus !== null) {
      params.orderStatus = queryParams.orderStatus
    }
    const res = await getAdminOrderPage(params)
    orderList.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchOrders()
}

function handleReset() {
  queryParams.orderStatus = ''
  queryParams.pageNum = 1
  fetchOrders()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  fetchOrders()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchOrders()
}

function openInspectDialog(row) {
  inspectForm.orderId = row.id
  inspectForm.orderNo = row.orderNo
  inspectForm.isPassed = true
  inspectForm.remark = ''
  inspectDialogVisible.value = true
}

function closeInspectDialog() {
  inspectDialogVisible.value = false
  inspectForm.orderId = null
  inspectForm.orderNo = ''
  inspectForm.isPassed = true
  inspectForm.remark = ''
}

async function handleInspectSubmit() {
  if (!inspectForm.orderId) return

  submitLoading.value = true
  try {
    await inspectOrder({
      orderId: inspectForm.orderId,
      isPassed: inspectForm.isPassed,
      remark: inspectForm.remark
    })
    ElMessage.success(inspectForm.isPassed ? '质检通过，装备已入库' : '已标记异常，装备转入维修')
    closeInspectDialog()
    await fetchOrders()
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await loadGearNameMap()
  await fetchOrders()
})
</script>

<template>
  <div class="order-manage-page">
    <el-card shadow="never" class="header-card">
      <div class="page-header">
        <div>
          <h2>订单大盘 / 质检中心</h2>
          <p>查看全站租赁订单，对待质检订单执行入库或转维修操作</p>
        </div>
        <div class="status-legend">
          <el-tag type="warning" effect="plain">待质检</el-tag>
          <el-tag type="success" effect="plain">已归还</el-tag>
          <el-tag type="danger" effect="plain">异常完结</el-tag>
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
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
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
        empty-text="暂无订单数据"
      >
        <el-table-column prop="orderNo" label="订单编号" min-width="170" fixed="left" />

        <el-table-column prop="userId" label="用户ID" width="90" align="center" />

        <el-table-column label="装备名称" min-width="160">
          <template #default="{ row }">
            {{ gearNameMap[row.gearId] || `装备 #${row.gearId}` }}
          </template>
        </el-table-column>

        <el-table-column prop="itemId" label="实例ID" width="90" align="center">
          <template #default="{ row }">{{ row.itemId ?? '-' }}</template>
        </el-table-column>

        <el-table-column label="订单状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.orderStatus)" effect="light">
              {{ getStatusLabel(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="借出时间" min-width="150">
          <template #default="{ row }">{{ formatDateTime(row.rentOutTime) }}</template>
        </el-table-column>

        <el-table-column label="实际归还" min-width="150">
          <template #default="{ row }">{{ formatDateTime(row.actualReturnTime) }}</template>
        </el-table-column>

        <el-table-column label="总费用" width="110" align="right">
          <template #default="{ row }">
            <span class="fee-text">{{ formatMoney(row.totalFee) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />

        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.orderStatus === 4"
              type="primary"
              link
              @click="openInspectDialog(row)"
            >
              进行质检
            </el-button>
            <span v-else class="action-muted">-</span>
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

    <el-dialog
      v-model="inspectDialogVisible"
      :title="`订单质检 - ${inspectForm.orderNo}`"
      width="520px"
      destroy-on-close
      @close="closeInspectDialog"
    >
      <el-form label-width="100px">
        <el-form-item label="质检结果" required>
          <el-radio-group v-model="inspectForm.isPassed">
            <el-radio :value="true">通过并入库</el-radio>
            <el-radio :value="false">异常并转维修</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="质检备注">
          <el-input
            v-model="inspectForm.remark"
            type="textarea"
            :rows="4"
            :placeholder="inspectForm.isPassed ? '如：完好无损，功能正常' : '如：帐篷底部有划痕破损'"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeInspectDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleInspectSubmit">
          确认质检
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-manage-page {
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

.fee-text {
  color: #f56c6c;
  font-weight: 600;
}

.action-muted {
  color: #c0c4cc;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
