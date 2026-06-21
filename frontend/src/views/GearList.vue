<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import GearCard from '@/components/GearCard.vue'
import { getGearPage } from '@/api/gear'
import { createRentalOrder } from '@/api/order'
import { getGearImages, getFallbackGearImage } from '@/utils/gearImages'

const router = useRouter()
const route = useRoute()

const user = computed(() => {
  const token = localStorage.getItem('token')
  if (!token) return null
  const roleRaw = localStorage.getItem('role')
  return {
    userId: localStorage.getItem('userId'),
    username: localStorage.getItem('username') || '',
    role: roleRaw === null || roleRaw === '' ? 1 : Number(roleRaw)
  }
})

const isAdmin = computed(() => user.value?.role === 0)

const loading = ref(false)
const listReady = ref(false)
const tableData = ref([])
const gearSectionRef = ref(null)

const SKELETON_COUNT = 12

const sidebarCategoryOptions = [
  { label: '背包', value: 'backpack', match: (category) => /背包/.test(category || '') },
  { label: '帐篷', value: 'tent', match: (category) => /帐篷/.test(category || '') },
  { label: '鞋服', value: 'footwear', match: (category) => /鞋/.test(category || '') },
  { label: '睡袋', value: 'sleeping', match: (category) => /睡袋/.test(category || '') },
  { label: '炉具 / 配件', value: 'accessories', match: (category) => /炉具|登山杖|配件|电子设备/.test(category || '') }
]

const conditionOptions = ['全新', '9成新', '轻微使用痕迹']

const selectedCategories = ref([])
const selectedBrands = ref([])
const selectedConditions = ref([])
const sortBy = ref('default')
const collapseActive = ref(['category', 'brand', 'condition'])

const queryParams = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 12
})

const availableBrands = computed(() => {
  const brands = new Set()
  tableData.value.forEach((gear) => {
    if (gear.brand) brands.add(gear.brand)
  })
  return [...brands].sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

function gearMatchesCategory(gear, categoryValue) {
  const option = sidebarCategoryOptions.find((item) => item.value === categoryValue)
  return option ? option.match(gear.category) : false
}

const filteredGears = computed(() => {
  let list = [...tableData.value]
  const keyword = queryParams.keyword.trim().toLowerCase()

  if (keyword) {
    list = list.filter((gear) => {
      const name = (gear.gearName || '').toLowerCase()
      const brand = (gear.brand || '').toLowerCase()
      const desc = (gear.description || '').toLowerCase()
      return name.includes(keyword) || brand.includes(keyword) || desc.includes(keyword)
    })
  }

  if (selectedCategories.value.length) {
    list = list.filter((gear) =>
      selectedCategories.value.some((value) => gearMatchesCategory(gear, value))
    )
  }

  if (selectedBrands.value.length) {
    list = list.filter((gear) => selectedBrands.value.includes(gear.brand))
  }

  if (selectedConditions.value.length) {
    list = list.filter((gear) => selectedConditions.value.includes(gear.conditionGrade))
  }

  return list
})

const sortedGears = computed(() => {
  const list = [...filteredGears.value]

  if (sortBy.value === 'price-asc') {
    return list.sort((a, b) => Number(a.dailyRent) - Number(b.dailyRent))
  }
  if (sortBy.value === 'price-desc') {
    return list.sort((a, b) => Number(b.dailyRent) - Number(a.dailyRent))
  }
  return list.sort((a, b) => a.id - b.id)
})

const filteredTotal = computed(() => sortedGears.value.length)

const paginatedGears = computed(() => {
  const start = (queryParams.pageNum - 1) * queryParams.pageSize
  return sortedGears.value.slice(start, start + queryParams.pageSize)
})

const showPagination = computed(() => filteredTotal.value > queryParams.pageSize)

const hasActiveFilters = computed(
  () =>
    !!(
      queryParams.keyword.trim() ||
      selectedCategories.value.length ||
      selectedBrands.value.length ||
      selectedConditions.value.length
    )
)

const rentDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const detailGear = ref(null)
const detailImageSrc = ref('')
const detailImageFallback = ref(false)
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

function scrollToResults() {
  gearSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function fetchGearList({ scroll = false } = {}) {
  loading.value = true
  listReady.value = false
  try {
    const res = await getGearPage({
      status: 1,
      pageNum: 1,
      pageSize: 100
    })
    tableData.value = res.data.records || []
    await nextTick()
    listReady.value = true
    if (scroll) scrollToResults()
  } finally {
    loading.value = false
  }
}

function resetPage() {
  queryParams.pageNum = 1
}

function onSidebarFilterChange() {
  resetPage()
}

function onKeywordInput() {
  clearTimeout(keywordTimer)
  keywordTimer = setTimeout(() => {
    resetPage()
    if (queryParams.keyword.trim()) scrollToResults()
  }, 380)
}

function onKeywordEnter() {
  clearTimeout(keywordTimer)
  resetPage()
  scrollToResults()
}

function onSortChange() {
  resetPage()
}

function clearAllFilters() {
  queryParams.keyword = ''
  selectedCategories.value = []
  selectedBrands.value = []
  selectedConditions.value = []
  sortBy.value = 'default'
  resetPage()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  scrollToResults()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  scrollToResults()
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

function openDetailDialog(gear) {
  detailGear.value = gear
  detailImageFallback.value = false
  detailImageSrc.value = getGearImages(gear).main
  detailDialogVisible.value = true
}

function handleDetailImageError() {
  if (!detailImageFallback.value) {
    detailImageFallback.value = true
    detailImageSrc.value = getFallbackGearImage()
  }
}

function handleEditGear() {
  if (!detailGear.value) return
  ElMessage.info(`装备编辑功能开发中：${detailGear.value.gearName}`)
}

function parseSpecifications(specs) {
  if (!specs) return []
  return specs
    .split(';')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => {
      const colonIndex = item.indexOf(':')
      if (colonIndex === -1) {
        return { label: item, value: '' }
      }
      return {
        label: item.slice(0, colonIndex).trim(),
        value: item.slice(colonIndex + 1).trim()
      }
    })
}

function rentFromDetail() {
  if (!detailGear.value) return
  detailDialogVisible.value = false
  openRentDialog(detailGear.value)
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
  syncKeywordFromRoute()
  fetchGearList({ scroll: !!queryParams.keyword.trim() })
})

watch(filteredTotal, (count) => {
  const maxPage = Math.max(1, Math.ceil(count / queryParams.pageSize) || 1)
  if (queryParams.pageNum > maxPage) {
    queryParams.pageNum = maxPage
  }
})

watch(
  () => route.query.keyword,
  (keyword) => {
    const nextKeyword = typeof keyword === 'string' ? keyword.trim() : ''
    if (nextKeyword !== queryParams.keyword.trim()) {
      queryParams.keyword = nextKeyword
      resetPage()
      if (nextKeyword) scrollToResults()
    }
  }
)

function syncKeywordFromRoute() {
  const keyword = route.query.keyword
  if (typeof keyword === 'string' && keyword.trim()) {
    queryParams.keyword = keyword.trim()
  }
}

onUnmounted(() => {
  clearTimeout(keywordTimer)
})
</script>

<template>
  <div class="gear-list-page">
    <header class="page-header">
      <div class="page-heading">
        <div class="page-heading__row">
          <h1 class="page-title">装备大厅</h1>
          <el-tag v-if="isAdmin" class="admin-mode-tag" effect="plain">管理模式</el-tag>
        </div>
        <p class="page-subtitle">精选户外装备，随时租赁出发</p>
      </div>

      <div class="page-search-wrap">
        <input
          v-model="queryParams.keyword"
          class="filter-search"
          type="search"
          placeholder="搜索装备名称、品牌..."
          aria-label="搜索装备"
          @input="onKeywordInput"
          @keydown.enter.prevent="onKeywordEnter"
        />
      </div>
    </header>

    <div class="catalog-layout">
      <aside class="filter-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">筛选</span>
          <button
            v-if="hasActiveFilters"
            type="button"
            class="sidebar-clear"
            @click="clearAllFilters"
          >
            Clear all
          </button>
        </div>

        <el-collapse v-model="collapseActive" class="filter-collapse">
          <el-collapse-item title="装备分类" name="category">
            <el-checkbox-group
              v-model="selectedCategories"
              class="sidebar-checkbox-group"
              @change="onSidebarFilterChange"
            >
              <el-checkbox
                v-for="item in sidebarCategoryOptions"
                :key="item.value"
                :label="item.value"
                class="sidebar-checkbox"
              >
                {{ item.label }}
              </el-checkbox>
            </el-checkbox-group>
          </el-collapse-item>

          <el-collapse-item title="品牌" name="brand">
            <el-checkbox-group
              v-model="selectedBrands"
              class="sidebar-checkbox-group"
              @change="onSidebarFilterChange"
            >
              <el-checkbox
                v-for="brand in availableBrands"
                :key="brand"
                :label="brand"
                class="sidebar-checkbox"
              >
                {{ brand }}
              </el-checkbox>
            </el-checkbox-group>
            <p v-if="!availableBrands.length" class="sidebar-empty">暂无品牌数据</p>
          </el-collapse-item>

          <el-collapse-item title="成色" name="condition">
            <el-checkbox-group
              v-model="selectedConditions"
              class="sidebar-checkbox-group"
              @change="onSidebarFilterChange"
            >
              <el-checkbox
                v-for="condition in conditionOptions"
                :key="condition"
                :label="condition"
                class="sidebar-checkbox"
              >
                {{ condition }}
              </el-checkbox>
            </el-checkbox-group>
          </el-collapse-item>
        </el-collapse>
      </aside>

      <main class="product-grid-area">
        <div class="grid-action-bar">
          <span class="result-count">共找到 {{ filteredTotal }} 件装备</span>
          <el-select
            v-model="sortBy"
            class="sort-select"
            placeholder="排序"
            :teleported="false"
            @change="onSortChange"
          >
            <el-option label="默认排序" value="default" />
            <el-option label="日租金从低到高" value="price-asc" />
            <el-option label="日租金从高到低" value="price-desc" />
          </el-select>
        </div>

        <section ref="gearSectionRef" class="gear-section">
          <div v-if="loading" class="skeleton-grid" aria-busy="true" aria-label="加载中">
            <div v-for="n in SKELETON_COUNT" :key="n" class="skeleton-item">
              <el-skeleton animated>
                <template #template>
                  <el-skeleton-item variant="image" class="skeleton-image" />
                  <el-skeleton-item variant="text" class="skeleton-line skeleton-line--brand" />
                  <el-skeleton-item variant="text" class="skeleton-line skeleton-line--name" />
                  <el-skeleton-item variant="text" class="skeleton-line skeleton-line--price" />
                </template>
              </el-skeleton>
            </div>
          </div>

          <p v-else-if="paginatedGears.length === 0" class="empty-state">暂无符合条件的装备</p>

          <div v-else class="gear-grid">
            <div
              v-for="(gear, index) in paginatedGears"
              :key="gear.id"
              class="grid-item"
              :class="{ 'is-visible': listReady }"
              :style="{ animationDelay: `${index * 0.06}s` }"
            >
              <GearCard
                :gear="gear"
                :main-image="getGearImages(gear).main"
                :hover-image="getGearImages(gear).hover"
                @click="openDetailDialog"
              />
            </div>
          </div>
        </section>

        <footer v-if="showPagination && !loading" class="pagination-wrap">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[12, 16, 24]"
            :total="filteredTotal"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </footer>
      </main>
    </div>

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

    <el-dialog
      v-model="detailDialogVisible"
      title="装备详情"
      width="650px"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-if="detailGear" class="detail-body">
        <div class="detail-media">
          <img
            class="detail-image"
            :src="detailImageSrc"
            :alt="detailGear.gearName"
            @error="handleDetailImageError"
          />
        </div>
        <div class="detail-info">
          <p class="detail-brand">{{ detailGear.brand }}</p>
          <div class="detail-name-row">
            <h3 class="detail-name">{{ detailGear.gearName }}</h3>
            <el-tag v-if="isAdmin" class="admin-mode-tag admin-mode-tag--detail" effect="plain">
              管理模式
            </el-tag>
          </div>
          <div class="detail-tags">
            <el-tag v-if="detailGear.category" type="info" effect="plain">
              {{ detailGear.category }}
            </el-tag>
            <el-tag v-if="detailGear.conditionGrade" effect="plain">
              {{ detailGear.conditionGrade }}
            </el-tag>
            <el-tag
              :type="detailGear.availableStock > 0 ? 'success' : 'danger'"
              effect="plain"
            >
              {{ detailGear.availableStock > 0 ? `可租 ${detailGear.availableStock} 件` : '暂无库存' }}
            </el-tag>
          </div>
          <p class="detail-desc">
            {{ detailGear.description || '暂无装备描述，欢迎咨询客服了解详情。' }}
          </p>

          <section v-if="detailGear.specifications" class="detail-specs">
            <h4 class="detail-section-title">技术参数</h4>
            <el-descriptions :column="2" size="small" border class="detail-specs__grid">
              <el-descriptions-item
                v-for="(item, index) in parseSpecifications(detailGear.specifications)"
                :key="index"
                :label="item.label"
              >
                {{ item.value }}
              </el-descriptions-item>
            </el-descriptions>
          </section>

          <aside v-if="detailGear.usageInstructions" class="detail-usage">
            <div class="detail-usage__header">
              <el-icon class="detail-usage__icon"><InfoFilled /></el-icon>
              <span class="detail-usage__title">使用须知</span>
            </div>
            <p class="detail-usage__text">{{ detailGear.usageInstructions }}</p>
          </aside>

          <div class="detail-price">
            <span class="detail-price__value">¥ {{ Number(detailGear.dailyRent).toFixed(2) }}</span>
            <span class="detail-price__unit">/ 天</span>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="isAdmin" type="info" plain @click="handleEditGear">编辑装备</el-button>
        <el-button
          type="primary"
          :disabled="!detailGear || detailGear.availableStock <= 0"
          @click="rentFromDetail"
        >
          立即租赁
        </el-button>
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

.filter-bar,
.page-header {
  margin-bottom: var(--space-lg);
}

.catalog-layout {
  display: flex;
  align-items: flex-start;
  gap: 48px;
}

.filter-sidebar {
  flex: 0 0 260px;
  width: 260px;
  padding-right: 32px;
  border-right: 1px solid var(--color-border);
  box-sizing: border-box;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.sidebar-title {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text);
}

.sidebar-clear {
  margin: 0;
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-decoration: underline;
  text-underline-offset: 3px;
  color: var(--color-text-subtle);
  cursor: pointer;
  transition: color 0.2s ease;
}

.sidebar-clear:hover {
  color: var(--color-text);
}

.filter-collapse {
  border: none;
}

.filter-collapse :deep(.el-collapse-item__header) {
  height: auto;
  min-height: 40px;
  padding: 8px 0;
  border: none;
  border-bottom: 1px solid var(--color-border-light);
  background: transparent;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--color-text);
}

.filter-collapse :deep(.el-collapse-item__wrap) {
  border: none;
  background: transparent;
}

.filter-collapse :deep(.el-collapse-item__content) {
  padding: 12px 0 20px;
}

.sidebar-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-checkbox {
  height: auto;
  margin-right: 0;
}

.sidebar-checkbox :deep(.el-checkbox__label) {
  font-size: 13px;
  color: var(--color-text-muted);
  letter-spacing: 0.02em;
  line-height: 1.4;
}

.sidebar-checkbox :deep(.el-checkbox__inner) {
  width: 16px;
  height: 16px;
  border-radius: 2px;
  border-color: var(--color-border);
  background: transparent;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.sidebar-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: var(--color-text, #222);
  border-color: var(--color-text, #222);
}

.sidebar-checkbox :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: var(--color-text);
}

.sidebar-checkbox :deep(.el-checkbox__inner::after) {
  border-color: #fff;
}

.sidebar-empty {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-subtle);
}

.product-grid-area {
  flex: 1;
  min-width: 0;
}

.grid-action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: calc(var(--space-lg) + 4px);
}

.page-search-wrap {
  max-width: 420px;
}

.sort-select {
  width: 168px;
}

.sort-select :deep(.el-select__wrapper) {
  padding: 8px 0;
  min-height: auto;
  border: none;
  border-bottom: 1px solid var(--color-border);
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.sort-select :deep(.el-select__wrapper:hover),
.sort-select :deep(.el-select__wrapper.is-focused) {
  border-bottom-color: var(--color-text-muted);
  box-shadow: none;
}

.sort-select :deep(.el-select__placeholder),
.sort-select :deep(.el-select__selected-item) {
  font-size: 12px;
  letter-spacing: 0.04em;
  color: var(--color-text);
}

.page-heading {
  margin-bottom: calc(var(--space-lg) + 8px);
}

.page-heading__row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.page-heading__row .page-title {
  margin-bottom: 0;
}

.admin-mode-tag {
  --el-tag-bg-color: var(--color-border-light, #f0f0ee);
  --el-tag-border-color: var(--color-border, #e8e8e6);
  --el-tag-text-color: var(--color-text-muted, #6b6b6b);
  font-size: 11px;
  letter-spacing: 0.06em;
}

.admin-mode-tag--detail {
  flex-shrink: 0;
}

.detail-name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 4px;
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
  display: none;
}

.filter-search {
  width: 100%;
  max-width: 420px;
  padding: 12px 0;
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
  width: 140px;
}

.filter-select :deep(.el-select__wrapper) {
  padding: 10px 0;
  min-height: auto;
  border: none;
  border-bottom: 1px solid var(--color-border);
  border-radius: 0;
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

.filter-clear {
  padding: 10px 0;
  border: none;
  background: transparent;
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.12em;
  color: var(--color-text-subtle);
  cursor: pointer;
  transition: color 0.25s ease;
}

.filter-clear:hover {
  color: var(--color-text);
}

.result-meta {
  display: none;
}

.result-count {
  font-size: 12px;
  letter-spacing: 0.06em;
  color: var(--color-text-subtle);
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
  height: 11px;
}

.skeleton-line--brand {
  width: 32%;
}

.skeleton-line--name {
  width: 68%;
  margin-top: 14px;
}

.skeleton-line--price {
  width: 24%;
  margin-top: 18px;
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

.detail-body {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.detail-media {
  flex: 0 0 280px;
  width: 280px;
  aspect-ratio: 4 / 5;
  overflow: hidden;
  background: var(--color-border-light);
}

.detail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.detail-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-brand {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text-subtle);
}

.detail-name {
  margin: 0;
  font-size: 22px;
  font-weight: 500;
  line-height: 1.35;
  color: var(--color-text);
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-desc {
  margin: 4px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-text-muted);
}

.detail-section-title {
  margin: 0 0 10px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-subtle);
}

.detail-specs {
  margin-top: 20px;
}

.detail-specs__grid {
  --el-descriptions-item-bordered-label-background: var(--color-border-light, #f7f7f5);
}

.detail-specs__grid :deep(.el-descriptions__label) {
  width: 88px;
  font-size: 12px;
  color: var(--color-text-subtle);
}

.detail-specs__grid :deep(.el-descriptions__content) {
  font-size: 13px;
  color: var(--color-text);
}

.detail-usage {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--color-border-light, #f7f7f5);
}

.detail-usage__header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.detail-usage__icon {
  font-size: 14px;
  color: var(--color-text-subtle);
}

.detail-usage__title {
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.06em;
  color: var(--color-text-subtle);
}

.detail-usage__text {
  margin: 0;
  font-size: 12px;
  line-height: 1.65;
  color: var(--color-text-subtle);
}

.detail-price {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.detail-price__value {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: 0.02em;
}

.detail-price__unit {
  margin-left: 4px;
  font-size: 14px;
  color: var(--color-text-subtle);
}

.detail-dialog :deep(.el-dialog__body) {
  padding-top: 8px;
}

@media (max-width: 640px) {
  .detail-body {
    flex-direction: column;
  }

  .detail-media {
    width: 100%;
    flex: none;
  }
}

@media (max-width: 1200px) {
  .gear-grid,
  .skeleton-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 4rem;
  }
}

@media (max-width: 900px) {
  .catalog-layout {
    flex-direction: column;
    gap: 32px;
  }

  .filter-sidebar {
    flex: none;
    width: 100%;
    padding-right: 0;
    padding-bottom: 24px;
    border-right: none;
    border-bottom: 1px solid var(--color-border);
  }

  .gear-grid,
  .skeleton-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 3.5rem;
  }

  .grid-action-bar {
    flex-wrap: wrap;
  }

  .sort-select {
    width: 100%;
    max-width: 220px;
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

  .page-search-wrap,
  .filter-search {
    max-width: none;
  }

  .sort-select {
    max-width: none;
    width: 100%;
  }
}
</style>
