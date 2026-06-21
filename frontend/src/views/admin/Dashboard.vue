<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/dashboard'

const loading = ref(false)
const stats = ref(null)

const lineChartRef = ref(null)
const pieChartRef = ref(null)
let lineChart = null
let pieChart = null

const summaryCards = computed(() => {
  const data = stats.value || {}
  return [
    {
      title: '今日营收',
      value: formatMoney(data.todayRevenue),
      icon: 'Money',
      color: '#409eff',
      bg: 'rgba(64, 158, 255, 0.1)'
    },
    {
      title: '出借中',
      value: data.rentingCount ?? '-',
      icon: 'Box',
      color: '#e6a23c',
      bg: 'rgba(230, 162, 60, 0.12)'
    },
    {
      title: '待质检',
      value: data.pendingInspectionCount ?? '-',
      icon: 'DocumentChecked',
      color: '#f56c6c',
      bg: 'rgba(245, 108, 108, 0.12)'
    },
    {
      title: '总用户数',
      value: data.totalPlayers ?? '-',
      icon: 'User',
      color: '#67c23a',
      bg: 'rgba(103, 194, 58, 0.12)'
    }
  ]
})

function formatMoney(value) {
  if (value === null || value === undefined) return '¥0.00'
  return `¥${Number(value).toFixed(2)}`
}

function renderLineChart() {
  if (!lineChartRef.value || !stats.value?.weeklyTrend?.length) return

  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }

  const dates = stats.value.weeklyTrend.map((item) => item.date)
  const revenues = stats.value.weeklyTrend.map((item) => Number(item.revenue))
  const orders = stats.value.weeklyTrend.map((item) => item.orderCount)

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['营收(元)', '订单量'], top: 0 },
    grid: { left: 48, right: 48, top: 40, bottom: 28 },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: [
      { type: 'value', name: '营收' },
      { type: 'value', name: '订单量', splitLine: { show: false } }
    ],
    series: [
      {
        name: '营收(元)',
        type: 'line',
        smooth: true,
        data: revenues,
        areaStyle: { color: 'rgba(64, 158, 255, 0.15)' },
        itemStyle: { color: '#409eff' }
      },
      {
        name: '订单量',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: orders,
        itemStyle: { color: '#67c23a' }
      }
    ]
  })
}

function renderPieChart() {
  if (!pieChartRef.value || !stats.value?.categoryShare?.length) return

  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [
      {
        name: '出借占比',
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        data: stats.value.categoryShare.map((item) => ({
          name: item.category,
          value: item.count
        }))
      }
    ]
  })
}

function handleResize() {
  lineChart?.resize()
  pieChart?.resize()
}

async function fetchStats() {
  loading.value = true
  try {
    const res = await getDashboardStats()
    stats.value = res.data
    await nextTick()
    renderLineChart()
    renderPieChart()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <div v-loading="loading" class="dashboard-page">
    <div class="dashboard-header">
      <div>
        <h2>运营数据大屏</h2>
        <p>Outdoor Gear Rental · 实时运营概览（Mock 数据演示）</p>
      </div>
      <el-button type="primary" plain @click="fetchStats">刷新数据</el-button>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col v-for="card in summaryCards" :key="card.title" :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-card__body">
            <div class="summary-card__icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div>
              <div class="summary-card__title">{{ card.title }}</div>
              <div class="summary-card__value" :style="{ color: card.color }">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>近 7 日营收与订单量趋势</span>
          </template>
          <div ref="lineChartRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>各分类装备出借占比</span>
          </template>
          <div ref="pieChartRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.dashboard-header h2 {
  margin: 0 0 8px;
  font-size: 26px;
  color: #1f2d3d;
}

.dashboard-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.summary-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.summary-card__body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.summary-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-card__title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.summary-card__value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.chart-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.chart-box {
  width: 100%;
  height: 360px;
}
</style>
