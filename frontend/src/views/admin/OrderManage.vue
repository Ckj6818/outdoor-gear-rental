<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'

/* ── 低饱和度 Morandi 配色（各状态/系列可区分） ── */
const STATUS_COLORS = {
  待支付: '#8e9aaf',
  租赁中: '#c4a882',
  已逾期: '#b86b6b',
  待质检: '#d4a574',
  已完成: '#6b9080',
  异常完结: '#7d6b8a'
}
const BAR_COLORS = ['#5b7c99', '#6b9080', '#c4a882', '#8e9aaf', '#7d6b8a']
const LINE_COLOR = '#5b7c99'
const AXIS_COLOR = '#b0b0b0'
const GRID_BORDER = '#e8e8e6'

/* ── Mock：图表数据 ── */
const mockStatusDistribution = [
  { name: '待支付', value: 12, itemStyle: { color: STATUS_COLORS['待支付'] } },
  { name: '租赁中', value: 28, itemStyle: { color: STATUS_COLORS['租赁中'] } },
  { name: '已逾期', value: 5, itemStyle: { color: STATUS_COLORS['已逾期'] } },
  { name: '待质检', value: 9, itemStyle: { color: STATUS_COLORS['待质检'] } },
  { name: '已完成', value: 46, itemStyle: { color: STATUS_COLORS['已完成'] } },
  { name: '异常完结', value: 3, itemStyle: { color: STATUS_COLORS['异常完结'] } }
]

const mockWeeklyRevenue = [
  { date: '06-14', amount: 2180 },
  { date: '06-15', amount: 2640 },
  { date: '06-16', amount: 1920 },
  { date: '06-17', amount: 3050 },
  { date: '06-18', amount: 2780 },
  { date: '06-19', amount: 3320 },
  { date: '06-20', amount: 2960 }
]

const mockTopGears = [
  { name: 'Osprey Stratos 36L', count: 38 },
  { name: 'MSR Hubba Hubba NX', count: 31 },
  { name: 'Salomon X Ultra 4', count: 27 },
  { name: 'Hikelite 26', count: 22 },
  { name: 'Black Diamond 登山杖', count: 18 }
]

/* ── Mock：订单列表 ── */
const mockOrderList = [
  {
    id: 1,
    orderNo: 'RO202506200001',
    gearName: 'Osprey Stratos 36L 徒步背包',
    renterName: '张三',
    renterPhone: '13800138001',
    rentStartDate: '2025-06-18 09:00',
    returnDate: '2025-06-21 18:00',
    dailyRent: 45.0,
    depositStatus: '已缴纳',
    orderStatus: 1
  },
  {
    id: 2,
    orderNo: 'RO202506190002',
    gearName: 'MSR Hubba Hubba NX 2人帐篷',
    renterName: '李四',
    renterPhone: '13900139002',
    rentStartDate: '2025-06-17 14:00',
    returnDate: '2025-06-19 18:00',
    dailyRent: 70.0,
    depositStatus: '已缴纳',
    orderStatus: 4
  },
  {
    id: 3,
    orderNo: 'RO202506180003',
    gearName: 'Salomon X Ultra 4 GTX 徒步鞋',
    renterName: '王五',
    renterPhone: '13700137003',
    rentStartDate: '2025-06-16 08:30',
    returnDate: '2025-06-18 18:00',
    dailyRent: 38.0,
    depositStatus: '已退还',
    orderStatus: 3
  },
  {
    id: 4,
    orderNo: 'RO202506170004',
    gearName: 'Osprey Hikelite 26 轻量化背包',
    renterName: '赵六',
    renterPhone: '13600136004',
    rentStartDate: '—',
    returnDate: '—',
    dailyRent: 35.0,
    depositStatus: '未缴纳',
    orderStatus: 0
  },
  {
    id: 5,
    orderNo: 'RO202506160005',
    gearName: 'Mystery Ranch Terraframe 65',
    renterName: '孙七',
    renterPhone: '13500135005',
    rentStartDate: '2025-06-10 10:00',
    returnDate: '2025-06-14 18:00',
    dailyRent: 80.0,
    depositStatus: '已缴纳',
    orderStatus: 2
  },
  {
    id: 6,
    orderNo: 'RO202506150006',
    gearName: 'Kelty Mistral 20 羽绒睡袋',
    renterName: '周八',
    renterPhone: '13400134006',
    rentStartDate: '2025-06-12 11:00',
    returnDate: '2025-06-15 18:00',
    dailyRent: 22.0,
    depositStatus: '已退还',
    orderStatus: 3
  },
  {
    id: 7,
    orderNo: 'RO202506140007',
    gearName: 'MSR PocketRocket 2 炉具',
    renterName: '吴九',
    renterPhone: '13300133007',
    rentStartDate: '2025-06-13 07:30',
    returnDate: '2025-06-14 18:00',
    dailyRent: 18.0,
    depositStatus: '已缴纳',
    orderStatus: 4
  },
  {
    id: 8,
    orderNo: 'RO202506130008',
    gearName: 'Patagonia Torrentshell 3L',
    renterName: '郑十',
    renterPhone: '13200132008',
    rentStartDate: '2025-06-11 09:00',
    returnDate: '2025-06-13 18:00',
    dailyRent: 28.0,
    depositStatus: '已缴纳',
    orderStatus: 5
  }
]

const statusPieRef = ref(null)
const trendLineRef = ref(null)
const topBarRef = ref(null)
let statusPieChart = null
let trendLineChart = null
let topBarChart = null

const queryParams = reactive({
  orderNo: '',
  phone: '',
  orderStatus: '',
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待支付', value: 0 },
  { label: '租赁中', value: 1 },
  { label: '已逾期', value: 2 },
  { label: '已完成', value: 3 },
  { label: '待质检', value: 4 },
  { label: '异常完结', value: 5 }
]

const statusConfig = {
  0: { label: '待支付', type: 'info' },
  1: { label: '租赁中', type: 'warning' },
  2: { label: '已逾期', type: 'danger' },
  3: { label: '已完成', type: 'success' },
  4: { label: '待质检', type: 'warning' },
  5: { label: '异常完结', type: 'danger' }
}

const depositTagType = {
  已缴纳: 'success',
  未缴纳: 'info',
  已退还: ''
}

const detailVisible = ref(false)
const inspectVisible = ref(false)
const currentOrder = ref(null)
const inspectForm = reactive({
  isPassed: true,
  remark: ''
})

const filteredOrders = computed(() => {
  let list = [...mockOrderList]

  if (queryParams.orderNo.trim()) {
    const kw = queryParams.orderNo.trim().toLowerCase()
    list = list.filter((o) => o.orderNo.toLowerCase().includes(kw))
  }
  if (queryParams.phone.trim()) {
    list = list.filter((o) => o.renterPhone.includes(queryParams.phone.trim()))
  }
  if (queryParams.orderStatus !== '' && queryParams.orderStatus !== null) {
    list = list.filter((o) => o.orderStatus === queryParams.orderStatus)
  }
  if (queryParams.dateRange?.length === 2) {
    const [start, end] = queryParams.dateRange
    list = list.filter((o) => {
      if (!o.rentStartDate || o.rentStartDate === '—') return false
      const day = o.rentStartDate.slice(0, 10)
      return day >= start && day <= end
    })
  }

  return list
})

const paginatedOrders = computed(() => {
  const start = (pagination.pageNum - 1) * pagination.pageSize
  return filteredOrders.value.slice(start, start + pagination.pageSize)
})

const filteredTotal = computed(() => filteredOrders.value.length)

function getStatusLabel(status) {
  return statusConfig[status]?.label || '未知'
}

function getStatusType(status) {
  return statusConfig[status]?.type || 'info'
}

function formatMoney(value) {
  if (value === null || value === undefined) return '-'
  return `¥${Number(value).toFixed(2)}`
}

function renderStatusPie() {
  if (!statusPieRef.value) return
  if (!statusPieChart) statusPieChart = echarts.init(statusPieRef.value)

  statusPieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 单 ({d}%)' },
    legend: {
      orient: 'vertical',
      right: 4,
      top: 'center',
      textStyle: { color: '#6b6b6b', fontSize: 11 }
    },
    series: [
      {
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 12, fontWeight: 600, color: '#222' }
        },
        data: mockStatusDistribution
      }
    ]
  })
}

function renderTrendLine() {
  if (!trendLineRef.value) return
  if (!trendLineChart) trendLineChart = echarts.init(trendLineRef.value)

  trendLineChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(34,34,34,0.92)',
      borderColor: 'transparent',
      textStyle: { color: '#f0f0ee', fontSize: 12 }
    },
    grid: { left: 12, right: 16, top: 24, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: mockWeeklyRevenue.map((d) => d.date),
      axisLine: { lineStyle: { color: GRID_BORDER } },
      axisTick: { show: false },
      axisLabel: { color: AXIS_COLOR, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { show: false },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: AXIS_COLOR, fontSize: 11 }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: mockWeeklyRevenue.map((d) => d.amount),
        lineStyle: { width: 2, color: LINE_COLOR },
        itemStyle: { color: LINE_COLOR, borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(91, 124, 153, 0.28)' },
            { offset: 1, color: 'rgba(91, 124, 153, 0)' }
          ])
        }
      }
    ]
  })
}

function renderTopBar() {
  if (!topBarRef.value) return
  if (!topBarChart) topBarChart = echarts.init(topBarRef.value)

  const reversed = [...mockTopGears].reverse()
  const names = reversed.map((g) => g.name)
  const barData = reversed.map((g, i) => ({
    value: g.count,
    itemStyle: {
      color: BAR_COLORS[i % BAR_COLORS.length],
      borderRadius: [0, 3, 3, 0]
    }
  }))

  topBarChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(34,34,34,0.92)',
      borderColor: 'transparent',
      textStyle: { color: '#f0f0ee', fontSize: 12 }
    },
    grid: { left: 8, right: 24, top: 8, bottom: 8, containLabel: true },
    xAxis: {
      type: 'value',
      splitLine: { show: false },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: AXIS_COLOR, fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: '#6b6b6b',
        fontSize: 11,
        width: 120,
        overflow: 'truncate'
      }
    },
    series: [
      {
        type: 'bar',
        data: barData,
        barWidth: 14
      }
    ]
  })
}

function renderAllCharts() {
  renderStatusPie()
  renderTrendLine()
  renderTopBar()
}

function handleResize() {
  statusPieChart?.resize()
  trendLineChart?.resize()
  topBarChart?.resize()
}

function handleSearch() {
  pagination.pageNum = 1
}

function handleReset() {
  queryParams.orderNo = ''
  queryParams.phone = ''
  queryParams.orderStatus = ''
  queryParams.dateRange = []
  pagination.pageNum = 1
}

function handlePageChange(page) {
  pagination.pageNum = page
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
}

function openDetail(row) {
  currentOrder.value = row
  detailVisible.value = true
}

async function handleShip(row) {
  try {
    await ElMessageBox.confirm(
      `确认对订单 ${row.orderNo} 执行发货（借出）操作？`,
      '确认发货',
      { confirmButtonText: '确认', cancelButtonText: '取消' }
    )
    ElMessage.success('发货成功（Mock）')
  } catch {
    /* 用户取消 */
  }
}

function openInspect(row) {
  currentOrder.value = row
  inspectForm.isPassed = true
  inspectForm.remark = ''
  inspectVisible.value = true
}

function submitInspect() {
  ElMessage.success(inspectForm.isPassed ? '质检通过，装备已入库（Mock）' : '已标记异常（Mock）')
  inspectVisible.value = false
}

onMounted(async () => {
  await nextTick()
  renderAllCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  statusPieChart?.dispose()
  trendLineChart?.dispose()
  topBarChart?.dispose()
})
</script>

<template>
  <div class="order-manage-page">
    <!-- 顶部可视化大盘 ~30vh -->
    <section class="chart-dashboard">
      <el-row :gutter="16" class="chart-row">
        <el-col :xs="24" :lg="8">
          <div class="chart-panel">
            <p class="chart-panel__title">订单状态分布</p>
            <div ref="statusPieRef" class="chart-panel__body" />
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <div class="chart-panel">
            <p class="chart-panel__title">近七日流水趋势</p>
            <div ref="trendLineRef" class="chart-panel__body" />
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <div class="chart-panel">
            <p class="chart-panel__title">热门租赁装备 TOP 5</p>
            <div ref="topBarRef" class="chart-panel__body" />
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 底部表格区 -->
    <section class="table-section">
      <div class="table-section__head">
        <div>
          <h2 class="page-title">订单管理</h2>
          <p class="page-subtitle">全站租赁订单查询与履约操作 · Mock 数据演示</p>
        </div>
      </div>

      <el-form :inline="true" class="search-form" @submit.prevent="handleSearch">
        <el-form-item label="订单号">
          <el-input
            v-model="queryParams.orderNo"
            placeholder="模糊搜索"
            clearable
            style="width: 170px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户手机号">
          <el-input
            v-model="queryParams.phone"
            placeholder="精确匹配"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.orderStatus" placeholder="全部" style="width: 130px">
            <el-option
              v-for="item in statusOptions"
              :key="String(item.value)"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="起租日期">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="paginatedOrders"
        stripe
        border
        style="width: 100%"
        empty-text="暂无匹配的订单"
        class="order-table"
      >
        <el-table-column prop="orderNo" label="订单号" min-width="160" fixed="left" />
        <el-table-column prop="gearName" label="装备名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="租赁人" min-width="120">
          <template #default="{ row }">
            <div class="renter-cell">
              <span>{{ row.renterName }}</span>
              <span class="renter-phone">{{ row.renterPhone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="rentStartDate" label="起租日期" min-width="150" />
        <el-table-column prop="returnDate" label="归还日期" min-width="150" />
        <el-table-column label="日租金" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(row.dailyRent) }}</template>
        </el-table-column>
        <el-table-column label="押金状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="depositTagType[row.depositStatus] || 'info'"
              effect="plain"
              size="small"
            >
              {{ row.depositStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.orderStatus)" effect="light" size="small">
              {{ getStatusLabel(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">查看详情</el-button>
            <el-button
              v-if="row.orderStatus === 0"
              type="primary"
              link
              @click="handleShip(row)"
            >
              确认发货
            </el-button>
            <el-button
              v-if="row.orderStatus === 4"
              type="warning"
              link
              @click="openInspect(row)"
            >
              收货质检
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredTotal > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredTotal"
          layout="total, sizes, prev, pager, next"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </section>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="520px" destroy-on-close>
      <template v-if="currentOrder">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="装备">{{ currentOrder.gearName }}</el-descriptions-item>
          <el-descriptions-item label="租赁人">
            {{ currentOrder.renterName }} / {{ currentOrder.renterPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="起租日期">{{ currentOrder.rentStartDate }}</el-descriptions-item>
          <el-descriptions-item label="归还日期">{{ currentOrder.returnDate }}</el-descriptions-item>
          <el-descriptions-item label="日租金">{{ formatMoney(currentOrder.dailyRent) }}</el-descriptions-item>
          <el-descriptions-item label="押金状态">{{ currentOrder.depositStatus }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(currentOrder.orderStatus)" size="small">
              {{ getStatusLabel(currentOrder.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- 质检弹窗 -->
    <el-dialog
      v-model="inspectVisible"
      :title="`收货质检 - ${currentOrder?.orderNo || ''}`"
      width="480px"
      destroy-on-close
    >
      <el-form label-width="90px">
        <el-form-item label="质检结果">
          <el-radio-group v-model="inspectForm.isPassed">
            <el-radio :value="true">通过并入库</el-radio>
            <el-radio :value="false">异常转维修</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="inspectForm.remark"
            type="textarea"
            :rows="3"
            placeholder="装备状况说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectVisible = false">取消</el-button>
        <el-button type="primary" @click="submitInspect">确认质检</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-manage-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: calc(100vh - 140px);
}

/* 顶部大盘 ~30% */
.chart-dashboard {
  flex: 0 0 30vh;
  min-height: 260px;
}

.chart-row {
  height: 100%;
}

.chart-row :deep(.el-col) {
  height: 100%;
  margin-bottom: 12px;
}

.chart-panel {
  height: 100%;
  min-height: 240px;
  padding: 16px 16px 8px;
  background: #fff;
  border: 1px solid var(--color-border, #e8e8e6);
  border-radius: 4px;
  display: flex;
  flex-direction: column;
}

.chart-panel__title {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-subtle, #999);
}

.chart-panel__body {
  flex: 1;
  min-height: 0;
  width: 100%;
}

/* 底部表格 */
.table-section {
  flex: 1;
  padding: 24px;
  background: #fff;
  border: 1px solid var(--color-border, #e8e8e6);
  border-radius: 4px;
}

.table-section__head {
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--color-text, #222);
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-subtle, #999);
}

.search-form {
  margin-bottom: 16px;
}

.renter-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.3;
}

.renter-phone {
  font-size: 12px;
  color: var(--color-text-subtle, #999);
}

.order-table {
  --el-table-border-color: var(--color-border, #e8e8e6);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 992px) {
  .chart-dashboard {
    flex: none;
    min-height: auto;
  }

  .chart-panel {
    min-height: 220px;
  }
}
</style>
