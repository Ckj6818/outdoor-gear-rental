<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createRentalOrder, getOccupiedDates } from '@/api/order'
import { zhCn } from '@/locale'

const locale = zhCn

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  gear: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const WAIVER_RATE = 0.1

const submitting = ref(false)
const occupiedLoading = ref(false)
const occupiedRanges = ref([])
const dateRange = ref(null)
/** 区间选择过程中已点的起租日（用于 disabled-date 第二段校验） */
const pickingStart = ref(null)

const form = reactive({
  remark: '',
  hasDamageWaiver: false
})

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const dailyRent = computed(() => Number(props.gear?.dailyRent ?? 0))
const deposit = computed(() => Number(props.gear?.deposit ?? 0))

const rentalDays = computed(() => {
  const range = dateRange.value
  if (!range || !range[0] || !range[1]) return 0
  const start = startOfDay(range[0])
  const end = startOfDay(range[1])
  const diffMs = end.getTime() - start.getTime()
  return Math.floor(diffMs / 86400000) + 1
})

const baseRentAmount = computed(() => dailyRent.value * rentalDays.value)

const waiverAmount = computed(() => {
  if (!form.hasDamageWaiver) return 0
  return baseRentAmount.value * WAIVER_RATE
})

/** 预计总租金 = 天数 × 日租金 + 押金（豁免金另计） */
const rentSubtotal = computed(() => baseRentAmount.value + deposit.value)

const totalOrderAmount = computed(() => rentSubtotal.value + waiverAmount.value)

const canSubmit = computed(() => rentalDays.value > 0 && !occupiedLoading.value)

function startOfDay(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

function toDateKey(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function normalizeOccupiedRanges(list) {
  return (list || [])
    .map((item) => ({
      start: item.startDate || item.start,
      end: item.endDate || item.end
    }))
    .filter((item) => item.start && item.end)
}

function isDateInOccupied(date, ranges) {
  const key = toDateKey(startOfDay(date))
  return ranges.some((range) => key >= range.start && key <= range.end)
}

/** 闭区间重叠：与后端 checkDateAvailable 规则一致 */
function rangeOverlapsOccupied(startDate, endDate, ranges) {
  const startKey = toDateKey(startOfDay(startDate))
  const endKey = toDateKey(startOfDay(endDate))
  return ranges.some((range) => !(startKey > range.end || endKey < range.start))
}

function disabledDate(date) {
  const day = startOfDay(date)
  const today = startOfDay(new Date())

  if (day < today) return true
  if (isDateInOccupied(day, occupiedRanges.value)) return true

  const anchorStart = pickingStart.value || dateRange.value?.[0]
  const hasEnd = dateRange.value?.[1]

  if (anchorStart && !hasEnd) {
    const start = startOfDay(anchorStart)
    if (day < start) return true
    if (rangeOverlapsOccupied(start, day, occupiedRanges.value)) return true
  }

  return false
}

/** 为日历单元格附加语义 class，配合 popper 样式区分占用/过期（disabled-date 仍负责禁用逻辑） */
function cellClassName(date) {
  const day = startOfDay(date)
  const today = startOfDay(new Date())

  if (day < today) {
    return 'booking-cell--past'
  }
  if (isDateInOccupied(day, occupiedRanges.value)) {
    return 'booking-cell--occupied'
  }
  if (disabledDate(date)) {
    return 'booking-cell--blocked'
  }
  return ''
}

function resetForm() {
  dateRange.value = null
  pickingStart.value = null
  form.remark = ''
  form.hasDamageWaiver = false
}

async function fetchOccupiedDates(gearId) {
  occupiedLoading.value = true
  occupiedRanges.value = []
  try {
    const res = await getOccupiedDates(gearId)
    occupiedRanges.value = normalizeOccupiedRanges(res.data)
  } catch {
    occupiedRanges.value = []
    ElMessage.warning('档期数据加载失败，请刷新后重试')
  } finally {
    occupiedLoading.value = false
  }
}

function handleCalendarChange(val) {
  if (Array.isArray(val)) {
    pickingStart.value = val[0] && !val[1] ? val[0] : null
    return
  }
  if (val instanceof Date) {
    pickingStart.value = val
  }
}

function handleDateRangeChange(val) {
  if (!val || !val[0] || !val[1]) {
    pickingStart.value = val?.[0] ?? null
    return
  }
  if (rangeOverlapsOccupied(val[0], val[1], occupiedRanges.value)) {
    ElMessage.warning('所选区间与已有租赁档期冲突，请重新选择')
    dateRange.value = null
    pickingStart.value = null
    return
  }
  pickingStart.value = null
}

async function handleSubmit() {
  if (!props.gear?.id) return
  if (!canSubmit.value) {
    ElMessage.warning('请选择合法的租赁档期')
    return
  }

  submitting.value = true
  try {
    await createRentalOrder({
      gearId: props.gear.id,
      rentalDays: rentalDays.value,
      hasDamageWaiver: form.hasDamageWaiver,
      remark: form.remark
    })
    ElMessage.success('下单成功！')
    visible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open && props.gear?.id) {
      resetForm()
      fetchOccupiedDates(props.gear.id)
    }
  }
)
</script>

<template>
  <el-dialog
    v-model="visible"
    title="确认租赁"
    width="480px"
    destroy-on-close
    class="gear-booking-modal"
  >
    <el-config-provider :locale="locale">
    <template v-if="gear">
      <div class="booking-summary">
        <p class="booking-summary__brand">{{ gear.brand }}</p>
        <h3 class="booking-summary__name">{{ gear.gearName }}</h3>
        <p v-if="gear.conditionGrade" class="booking-summary__meta">
          成色 · {{ gear.conditionGrade }}
        </p>
        <p class="booking-summary__price">
          ¥{{ Number(gear.dailyRent).toFixed(2) }} / 天
          <span v-if="deposit > 0" class="booking-summary__deposit">
            · 押金 ¥{{ deposit.toFixed(2) }}
          </span>
        </p>
      </div>

      <el-form label-width="90px" class="booking-form">
        <el-form-item label="租赁档期" required>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="起租日"
            end-placeholder="归还日"
            format="YYYY-MM-DD"
            popper-class="booking-date-picker-popper"
            class="booking-date-picker"
            :disabled-date="disabledDate"
            :cell-class-name="cellClassName"
            :disabled="occupiedLoading"
            style="width: 100%"
            @calendar-change="handleCalendarChange"
            @change="handleDateRangeChange"
          />
          <p v-if="occupiedLoading" class="booking-hint">正在加载可租档期…</p>
          <p v-else class="booking-hint">
            <span class="booking-hint__legend booking-hint__legend--blocked" aria-hidden="true">12</span>
            置灰并带删除线的日期不可选（已过期、已被占用或会与占用区间冲突）
          </p>
        </el-form-item>

        <el-form-item v-if="rentalDays > 0" label="租期">
          <span class="booking-days">{{ rentalDays }} 天</span>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>

        <el-form-item label-width="0" class="waiver-item">
          <el-checkbox v-model="form.hasDamageWaiver" class="waiver-checkbox">
            购买意外损坏豁免金（租金的 10%），免除非人为因素的轻微战损赔偿
          </el-checkbox>
        </el-form-item>

        <el-form-item label="费用明细" class="fee-breakdown">
          <div class="fee-lines">
            <p class="fee-line">
              <span>基础租金（{{ rentalDays || '—' }} 天）</span>
              <span>¥{{ baseRentAmount.toFixed(2) }}</span>
            </p>
            <p v-if="deposit > 0" class="fee-line">
              <span>押金</span>
              <span>¥{{ deposit.toFixed(2) }}</span>
            </p>
            <p v-if="form.hasDamageWaiver" class="fee-line">
              <span>意外损坏豁免金</span>
              <span>¥{{ waiverAmount.toFixed(2) }}</span>
            </p>
            <p class="fee-line fee-line--subtotal">
              <span>预计总租金</span>
              <span>¥{{ rentSubtotal.toFixed(2) }}</span>
            </p>
            <p class="fee-line fee-line--total">
              <span>订单应付</span>
              <span class="estimate-fee">¥{{ totalOrderAmount.toFixed(2) }}</span>
            </p>
          </div>
        </el-form-item>
      </el-form>
    </template>
    </el-config-provider>

    <template #footer>
      <el-button link @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="handleSubmit">
        确认下单
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.booking-summary {
  margin-bottom: var(--space-md);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--color-border);
}

.booking-summary__brand {
  margin: 0 0 8px;
  font-size: 10px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-subtle);
}

.booking-summary__name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 400;
  color: var(--color-text);
}

.booking-summary__meta {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.06em;
  color: var(--color-text-subtle);
}

.booking-summary__price {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.booking-summary__deposit {
  color: var(--color-text-subtle);
}

.booking-form {
  margin-top: var(--space-md);
}

.booking-hint {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--color-text-subtle);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.booking-hint__legend {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 4px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  flex-shrink: 0;
}

.booking-hint__legend--blocked {
  color: #b8b8b5;
  text-decoration: line-through;
  text-decoration-color: rgba(0, 0, 0, 0.2);
  background: rgba(0, 0, 0, 0.03);
  cursor: not-allowed;
}

.booking-days {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
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

.fee-line--subtotal {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed var(--color-border);
  color: var(--color-text);
}

.fee-line--total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
  color: var(--color-text);
}

.estimate-fee {
  font-size: 18px;
  font-weight: 500;
  color: var(--color-text);
}

.gear-booking-modal :deep(.el-dialog__body) {
  padding-top: 8px;
}
</style>

<!-- 日历面板 teleport 到 body，需非 scoped 样式配合 popper-class -->
<style>
.booking-date-picker-popper .el-date-table td.disabled {
  pointer-events: none;
  cursor: not-allowed !important;
}

.booking-date-picker-popper .el-date-table td.disabled .el-date-table-cell {
  pointer-events: none;
  cursor: not-allowed !important;
  background-color: transparent !important;
}

.booking-date-picker-popper .el-date-table td.disabled:hover .el-date-table-cell {
  background-color: transparent !important;
}

.booking-date-picker-popper .el-date-table td.disabled .el-date-table-cell__text {
  color: #b8b8b5;
  text-decoration: line-through;
  text-decoration-color: rgba(0, 0, 0, 0.22);
  text-decoration-thickness: 1px;
  opacity: 0.78;
  cursor: not-allowed !important;
}

/* 已被他人占用的档期：略深灰，强调不可租 */
.booking-date-picker-popper .el-date-table td.booking-cell--occupied.disabled .el-date-table-cell__text {
  color: #a8a8a4;
  text-decoration-color: rgba(0, 0, 0, 0.28);
}

/* 已过期：删除线稍浅，与占用区分 */
.booking-date-picker-popper .el-date-table td.booking-cell--past.disabled .el-date-table-cell__text {
  color: #c4c4c0;
  text-decoration-color: rgba(0, 0, 0, 0.14);
  opacity: 0.62;
}
</style>
