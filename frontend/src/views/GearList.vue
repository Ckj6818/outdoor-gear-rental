<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import GearCard from '@/components/GearCard.vue'
import { getGearPage } from '@/api/gear'
import { createRentalOrder } from '@/api/order'
import { getGearImages } from '@/utils/gearImages'

const router = useRouter()

const loading = ref(false)
const listReady = ref(false)
const tableData = ref([])
const total = ref(0)

const categoryOptions = [
  { label: '全部', value: '' },
  { label: '重装背包', value: '重装背包' },
  { label: '轻量化背包', value: '轻量化背包' },
  { label: '帐篷', value: '帐篷' },
  { label: '徒步鞋', value: '徒步鞋' }
]

const queryParams = reactive({
  gearName: '',
  category: '',
  pageNum: 1,
  pageSize: 12
})

const showPagination = computed(() => total.value > queryParams.pageSize)

const rentDialogVisible = ref(false)
const rentLoading = ref(false)
const currentGear = ref(null)
const rentForm = reactive({
  rentalDays: 1,
  remark: ''
})

async function fetchGearList() {
  loading.value = true
  listReady.value = false
  try {
    const res = await getGearPage({
      gearName: queryParams.gearName || undefined,
      category: queryParams.category || undefined,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    await nextTick()
    listReady.value = true
  } finally {
    loading.value = false
  }
}

function selectCategory(value) {
  if (queryParams.category === value) return
  queryParams.category = value
  queryParams.pageNum = 1
  fetchGearList()
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchGearList()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  fetchGearList()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchGearList()
}

function openRentDialog(gear) {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再租赁装备')
    router.push('/login')
    return
  }
  if (gear.availableStock <= 0) {
    ElMessage.warning('该装备暂无库存')
    return
  }
  currentGear.value = gear
  rentForm.rentalDays = 1
  rentForm.remark = ''
  rentDialogVisible.value = true
}

async function submitRent() {
  if (!currentGear.value) return
  rentLoading.value = true
  try {
    await createRentalOrder({
      gearId: currentGear.value.id,
      rentalDays: rentForm.rentalDays,
      remark: rentForm.remark
    })
    ElMessage.success('下单成功！')
    rentDialogVisible.value = false
    fetchGearList()
  } finally {
    rentLoading.value = false
  }
}

onMounted(() => {
  fetchGearList()
})
</script>

<template>
  <div class="gear-list-page">
    <header class="filter-bar">
      <div class="page-heading">
        <h1 class="page-title">装备大厅</h1>
        <p class="page-subtitle">精选户外装备，随时租赁出发</p>
      </div>

      <div class="search-row">
        <input
          v-model="queryParams.gearName"
          class="search-input"
          type="search"
          placeholder="搜索装备名称"
          @keyup.enter="handleSearch"
        />
      </div>

      <nav class="category-nav" aria-label="装备分类">
        <button
          v-for="item in categoryOptions"
          :key="item.value"
          type="button"
          class="category-tab"
          :class="{ active: queryParams.category === item.value }"
          @click="selectCategory(item.value)"
        >
          {{ item.label }}
        </button>
      </nav>
    </header>

    <section class="gear-section">
      <div v-if="loading" class="loading-state">
        <span class="loading-text">Loading</span>
      </div>

      <p v-else-if="tableData.length === 0" class="empty-state">暂无装备数据</p>

      <div v-else class="gear-grid">
        <div
          v-for="(gear, index) in tableData"
          :key="gear.id"
          class="grid-item"
          :class="{ 'is-visible': listReady }"
          :style="{ animationDelay: `${index * 0.08}s` }"
        >
          <GearCard
            :gear="gear"
            :main-image="getGearImages(gear).main"
            :hover-image="getGearImages(gear).hover"
            @click="openRentDialog"
          />
        </div>
      </div>
    </section>

    <footer v-if="showPagination" class="pagination-wrap">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[12, 16, 24]"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </footer>

    <el-dialog
      v-model="rentDialogVisible"
      title="确认租赁"
      width="460px"
      destroy-on-close
      class="rent-dialog"
    >
      <template v-if="currentGear">
        <div class="rent-summary">
          <p class="rent-brand">{{ currentGear.brand }}</p>
          <h3 class="rent-name">{{ currentGear.gearName }}</h3>
          <p class="rent-price">¥{{ currentGear.dailyRent }} / 天</p>
        </div>

        <el-form label-width="90px" class="rent-form">
          <el-form-item label="租赁天数">
            <el-input-number v-model="rentForm.rentalDays" :min="1" :max="30" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="rentForm.remark" type="textarea" :rows="3" placeholder="选填" />
          </el-form-item>
          <el-form-item label="预估费用">
            <span class="estimate-fee">
              ¥{{ (Number(currentGear.dailyRent) * rentForm.rentalDays).toFixed(2) }}
            </span>
          </el-form-item>
        </el-form>
      </template>

      <template #footer>
        <el-button link @click="rentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="rentLoading" @click="submitRent">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gear-list-page {
  width: 100vw;
  position: relative;
  left: 50%;
  margin-left: -50vw;
  padding: var(--space-xl) clamp(24px, 5vw, 80px) calc(var(--space-xl) + 24px);
  box-sizing: border-box;
}

.filter-bar {
  margin-bottom: calc(var(--space-xl) + 16px);
}

.page-heading {
  margin-bottom: var(--space-lg);
}

.page-title {
  margin: 0 0 12px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--color-text);
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-subtle);
  letter-spacing: var(--letter-spacing-base);
}

.search-row {
  margin-bottom: var(--space-lg);
  max-width: 320px;
}

.search-input {
  width: 100%;
  padding: 10px 0;
  border: none;
  border-bottom: 1px solid #ccc;
  border-radius: 0;
  background: transparent;
  font-family: inherit;
  font-size: 14px;
  letter-spacing: var(--letter-spacing-base);
  color: var(--color-text);
  outline: none;
  transition: border-color 0.3s ease;
}

.search-input::placeholder {
  color: var(--color-text-subtle);
}

.search-input:focus {
  border-bottom-color: var(--color-text);
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
}

.category-tab {
  padding: 0 0 6px;
  border: none;
  background: none;
  cursor: pointer;
  font-family: inherit;
  font-size: 13px;
  font-weight: 400;
  letter-spacing: 0.08em;
  color: var(--color-text-subtle);
  position: relative;
  transition: color 0.3s ease;
}

.category-tab::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 1px;
  background: var(--color-text);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.category-tab:hover {
  color: var(--color-text-muted);
}

.category-tab.active {
  color: var(--color-text);
  font-weight: 500;
}

.category-tab.active::after {
  transform: scaleX(1);
}

.gear-section {
  min-height: 420px;
}

.loading-state,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--color-text-subtle);
}

.gear-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 60px;
}

.grid-item {
  opacity: 0;
  transform: translateY(28px);
}

.grid-item.is-visible {
  animation: staggerFadeIn 0.75s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

@keyframes staggerFadeIn {
  from {
    opacity: 0;
    transform: translateY(28px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: calc(var(--space-xl) + 24px);
  padding-bottom: var(--space-md);
}

.pagination-wrap :deep(.el-pagination) {
  --el-pagination-button-bg-color: transparent;
  --el-pagination-hover-color: var(--color-text);
}

.pagination-wrap :deep(.el-pagination.is-background .el-pager li) {
  background: transparent;
  box-shadow: none;
}

.rent-summary {
  margin-bottom: var(--space-md);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--color-border);
}

.rent-brand {
  margin: 0 0 8px;
  font-size: 10px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-subtle);
}

.rent-name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 400;
  color: var(--color-text);
}

.rent-price {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.rent-form {
  margin-top: var(--space-md);
}

.estimate-fee {
  font-size: 18px;
  font-weight: 400;
  letter-spacing: var(--letter-spacing-base);
  color: var(--color-text);
}

@media (max-width: 1200px) {
  .gear-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 4rem;
  }
}

@media (max-width: 900px) {
  .gear-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 3.5rem;
  }
}

@media (max-width: 560px) {
  .gear-list-page {
    padding-top: var(--space-lg);
  }

  .gear-grid {
    grid-template-columns: 1fr;
    gap: 3rem;
  }

  .category-nav {
    gap: 20px;
  }
}
</style>
