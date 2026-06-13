<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
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

const SKELETON_COUNT = 12

const categoryOptions = [
  { label: '全部分类', value: '' },
  { label: '背包', value: '背包' },
  { label: '帐篷', value: '帐篷' },
  { label: '鞋服', value: '鞋服' },
  { label: '配件', value: '配件' }
]

const brandOptions = [
  { label: '全部品牌', value: '' },
  { label: 'Osprey', value: 'Osprey' },
  { label: 'Mystery Ranch', value: 'Mystery Ranch' },
  { label: 'Gregory', value: 'Gregory' },
  { label: 'MSR', value: 'MSR' },
  { label: 'Naturehike', value: 'Naturehike' },
  { label: 'Big Agnes', value: 'Big Agnes' },
  { label: 'Salomon', value: 'Salomon' },
  { label: 'Merrell', value: 'Merrell' },
  { label: 'Lowa', value: 'Lowa' },
  { label: 'Hoka', value: 'Hoka' },
  { label: 'Kelty', value: 'Kelty' },
  { label: 'Black Diamond', value: 'Black Diamond' }
]

const queryParams = reactive({
  keyword: '',
  category: '',
  brand: '',
  pageNum: 1,
  pageSize: 12
})

const showPagination = computed(() => total.value > queryParams.pageSize)

const rentDialogVisible = ref(false)
const rentLoading = ref(false)
const currentGear = ref(null)
const rentForm = reactive({
  rentalDays: 1,
  remark: '',
  hasDamageWaiver: false
})

const WAIVER_RATE = 0.1

const baseRentAmount = computed(() => {
  if (!currentGear.value) return 0
  return Number(currentGear.value.dailyRent) * rentForm.rentalDays
})

const waiverAmount = computed(() => {
  if (!rentForm.hasDamageWaiver) return 0
  return baseRentAmount.value * WAIVER_RATE
})

const totalOrderAmount = computed(() => baseRentAmount.value + waiverAmount.value)

let keywordTimer = null

async function fetchGearList() {
  loading.value = true
  listReady.value = false
  try {
    const res = await getGearPage({
      keyword: queryParams.keyword.trim() || undefined,
      category: queryParams.category || undefined,
      brand: queryParams.brand || undefined,
      status: 1,
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

function resetAndFetch() {
  queryParams.pageNum = 1
  fetchGearList()
}

function onKeywordInput() {
  clearTimeout(keywordTimer)
  keywordTimer = setTimeout(resetAndFetch, 380)
}

function onFilterChange() {
  resetAndFetch()
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
  rentForm.hasDamageWaiver = false
  rentDialogVisible.value = true
}

async function submitRent() {
  if (!currentGear.value) return
  rentLoading.value = true
  try {
    await createRentalOrder({
      gearId: currentGear.value.id,
      rentalDays: rentForm.rentalDays,
      hasDamageWaiver: rentForm.hasDamageWaiver,
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

onUnmounted(() => {
  clearTimeout(keywordTimer)
})
</script>

<template>
  <div class="gear-list-page">
    <header class="filter-bar">
      <div class="page-heading">
        <h1 class="page-title">装备大厅</h1>
        <p class="page-subtitle">精选户外装备，随时租赁出发</p>
      </div>

      <div class="filter-toolbar">
        <input
          v-model="queryParams.keyword"
          class="filter-search"
          type="search"
          placeholder="搜索装备名称"
          aria-label="搜索装备名称"
          @input="onKeywordInput"
        />

        <el-select
          v-model="queryParams.category"
          class="filter-select"
          placeholder="分类"
          :teleported="false"
          @change="onFilterChange"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.value || 'all-category'"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-select
          v-model="queryParams.brand"
          class="filter-select"
          placeholder="品牌"
          :teleported="false"
          @change="onFilterChange"
        >
          <el-option
            v-for="item in brandOptions"
            :key="item.value || 'all-brand'"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>
    </header>

    <section class="gear-section">
      <div v-if="loading" class="skeleton-grid" aria-busy="true" aria-label="加载中">
        <div v-for="n in SKELETON_COUNT" :key="n" class="skeleton-item">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" class="skeleton-image" />
              <el-skeleton-item variant="text" class="skeleton-line skeleton-line--brand" />
              <el-skeleton-item variant="text" class="skeleton-line skeleton-line--name" />
            </template>
          </el-skeleton>
        </div>
      </div>

      <p v-else-if="tableData.length === 0" class="empty-state">暂无符合条件的装备</p>

      <div v-else class="gear-grid">
        <div
          v-for="(gear, index) in tableData"
          :key="gear.id"
          class="grid-item"
          :class="{ 'is-visible': listReady }"
          :style="{ animationDelay: `${index * 0.06}s` }"
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

    <footer v-if="showPagination && !loading" class="pagination-wrap">
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
          <p v-if="currentGear.conditionGrade" class="rent-condition">
            成色 · {{ currentGear.conditionGrade }}
          </p>
          <p class="rent-price">¥{{ currentGear.dailyRent }} / 天</p>
        </div>

        <el-form label-width="90px" class="rent-form">
          <el-form-item label="租赁天数">
            <el-input-number v-model="rentForm.rentalDays" :min="1" :max="30" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="rentForm.remark" type="textarea" :rows="3" placeholder="选填" />
          </el-form-item>
          <el-form-item label-width="0" class="waiver-item">
            <el-checkbox v-model="rentForm.hasDamageWaiver" class="waiver-checkbox">
              购买意外损坏豁免金（租金的 10%），免除非人为因素的轻微战损赔偿
            </el-checkbox>
          </el-form-item>
          <el-form-item label="费用明细" class="fee-breakdown">
            <div class="fee-lines">
              <p class="fee-line">
                <span>基础租金</span>
                <span>¥{{ baseRentAmount.toFixed(2) }}</span>
              </p>
              <p v-if="rentForm.hasDamageWaiver" class="fee-line">
                <span>意外损坏豁免金</span>
                <span>¥{{ waiverAmount.toFixed(2) }}</span>
              </p>
              <p class="fee-line fee-line--total">
                <span>订单总价</span>
                <span class="estimate-fee">¥{{ totalOrderAmount.toFixed(2) }}</span>
              </p>
            </div>
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
  margin-bottom: calc(var(--space-xl) + 8px);
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

.filter-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 32px 48px;
  max-width: 920px;
}

.filter-search {
  flex: 1 1 240px;
  min-width: 200px;
  max-width: 360px;
  padding: 10px 0;
  border: none;
  border-bottom: 1px solid var(--color-border);
  border-radius: 0;
  background: transparent;
  font-family: inherit;
  font-size: 14px;
  letter-spacing: var(--letter-spacing-base);
  color: var(--color-text);
  outline: none;
  transition: border-color 0.3s ease;
}

.filter-search::placeholder {
  color: var(--color-text-subtle);
}

.filter-search:focus {
  border-bottom-color: var(--color-text);
}

.filter-select {
  width: 148px;
}

.filter-select :deep(.el-select__wrapper) {
  padding: 8px 0;
  border: none;
  border-bottom: 1px solid var(--color-border);
  background: transparent;
  box-shadow: none;
  transition: border-color 0.3s ease;
}

.filter-select :deep(.el-select__wrapper:hover),
.filter-select :deep(.el-select__wrapper.is-focused) {
  border-bottom-color: var(--color-text-muted);
  box-shadow: none;
}

.filter-select :deep(.el-select__placeholder),
.filter-select :deep(.el-select__selected-item) {
  font-size: 13px;
  letter-spacing: 0.06em;
  color: var(--color-text);
}

.gear-section {
  min-height: 420px;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 60px;
}

.skeleton-item :deep(.el-skeleton) {
  --el-skeleton-color: var(--color-border-light);
  --el-skeleton-to-color: var(--color-border);
}

.skeleton-image {
  width: 100%;
  aspect-ratio: 4 / 5;
  height: auto;
  border-radius: 0;
}

.skeleton-line {
  margin-top: 20px;
  height: 12px;
}

.skeleton-line--brand {
  width: 36%;
}

.skeleton-line--name {
  width: 72%;
  margin-top: 12px;
}

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

.rent-condition {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.06em;
  color: var(--color-text-subtle);
}

.rent-form {
  margin-top: var(--space-md);
}

.waiver-item {
  margin-bottom: 8px;
}

.waiver-item :deep(.el-form-item__content) {
  line-height: 1.6;
}

.waiver-checkbox {
  align-items: flex-start;
  white-space: normal;
  height: auto;
}

.waiver-checkbox :deep(.el-checkbox__label) {
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-muted);
  letter-spacing: var(--letter-spacing-base);
}

.fee-breakdown :deep(.el-form-item__label) {
  color: var(--color-text-subtle);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.fee-lines {
  width: 100%;
}

.fee-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.fee-line--total {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
  color: var(--color-text);
}

.estimate-fee {
  font-size: 18px;
  font-weight: 400;
  letter-spacing: var(--letter-spacing-base);
  color: var(--color-text);
}

@media (max-width: 1200px) {
  .gear-grid,
  .skeleton-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 4rem;
  }
}

@media (max-width: 900px) {
  .gear-grid,
  .skeleton-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 3.5rem;
  }

  .filter-toolbar {
    gap: 24px 32px;
  }
}

@media (max-width: 560px) {
  .gear-list-page {
    padding-top: var(--space-lg);
  }

  .gear-grid,
  .skeleton-grid {
    grid-template-columns: 1fr;
    gap: 3rem;
  }

  .filter-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 20px;
  }

  .filter-search {
    max-width: none;
  }

  .filter-select {
    width: 100%;
  }
}
</style>
