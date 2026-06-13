<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGearPage } from '@/api/gear'
import { createRentalOrder } from '@/api/order'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const categoryOptions = [
  { label: '全部分类', value: '' },
  { label: '重装背包', value: '重装背包' },
  { label: '轻量化背包', value: '轻量化背包' },
  { label: '帐篷', value: '帐篷' },
  { label: '徒步鞋', value: '徒步鞋' }
]

const queryParams = reactive({
  gearName: '',
  category: '',
  pageNum: 1,
  pageSize: 8
})

const rentDialogVisible = ref(false)
const rentLoading = ref(false)
const currentGear = ref(null)
const rentForm = reactive({
  rentalDays: 1,
  remark: ''
})

async function fetchGearList() {
  loading.value = true
  try {
    const res = await getGearPage({
      gearName: queryParams.gearName || undefined,
      category: queryParams.category || undefined,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchGearList()
}

function handleReset() {
  queryParams.gearName = ''
  queryParams.category = ''
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

function getCategoryTagType(category) {
  const map = {
    '重装背包': 'danger',
    '轻量化背包': 'success',
    '帐篷': 'warning',
    '徒步鞋': 'primary'
  }
  return map[category] || 'info'
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
    <el-card shadow="never" class="search-card">
      <div class="page-title">
        <h2>装备大厅</h2>
        <p>精选户外背包、帐篷、徒步鞋，随时租赁出发</p>
      </div>

      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="装备名称">
          <el-input
            v-model="queryParams.gearName"
            placeholder="输入装备名称搜索"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="装备分类">
          <el-select
            v-model="queryParams.category"
            placeholder="请选择分类"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="'Search'" @click="handleSearch">搜索</el-button>
          <el-button :icon="'Refresh'" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-loading="loading" class="gear-content">
      <el-empty v-if="!loading && tableData.length === 0" description="暂无装备数据" />

      <el-row v-else :gutter="20">
        <el-col
          v-for="gear in tableData"
          :key="gear.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card shadow="hover" class="gear-card">
            <div class="gear-card-header">
              <el-tag :type="getCategoryTagType(gear.category)" size="small">
                {{ gear.category }}
              </el-tag>
              <el-tag v-if="gear.availableStock > 0" type="success" size="small" effect="plain">
                可租 {{ gear.availableStock }}
              </el-tag>
              <el-tag v-else type="info" size="small" effect="plain">已租罄</el-tag>
            </div>

            <h3 class="gear-name" :title="gear.gearName">{{ gear.gearName }}</h3>
            <p class="gear-brand">{{ gear.brand }}</p>
            <p class="gear-desc">{{ gear.description || '暂无描述' }}</p>

            <div class="gear-meta">
              <span class="price">¥{{ gear.dailyRent }}<small>/天</small></span>
              <span class="stock">库存 {{ gear.availableStock }}/{{ gear.totalStock }}</span>
            </div>

            <el-button
              type="primary"
              :disabled="gear.availableStock <= 0 || gear.status !== 1"
              style="width: 100%"
              @click="openRentDialog(gear)"
            >
              {{ gear.availableStock > 0 ? '立即租赁' : '暂无库存' }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[8, 12, 16, 24]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="rentDialogVisible" title="确认租赁" width="460px" destroy-on-close>
      <template v-if="currentGear">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="装备名称">{{ currentGear.gearName }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ currentGear.brand }}</el-descriptions-item>
          <el-descriptions-item label="日租金">¥{{ currentGear.dailyRent }}</el-descriptions-item>
        </el-descriptions>

        <el-form label-width="90px" style="margin-top: 20px">
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
        <el-button @click="rentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="rentLoading" @click="submitRent">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gear-list-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-card {
  border-radius: 12px;
}

.page-title h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
}

.page-title p {
  margin: 0 0 20px;
  color: #909399;
  font-size: 14px;
}

.gear-content {
  min-height: 320px;
}

.gear-card {
  margin-bottom: 20px;
  border-radius: 12px;
  min-height: 280px;
  display: flex;
  flex-direction: column;
}

.gear-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.gear-name {
  margin: 0 0 6px;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gear-brand {
  margin: 0 0 10px;
  color: #606266;
  font-size: 13px;
}

.gear-desc {
  margin: 0 0 16px;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.gear-meta {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 16px;
}

.price {
  color: #f56c6c;
  font-size: 22px;
  font-weight: 700;
}

.price small {
  font-size: 13px;
  font-weight: 400;
}

.stock {
  color: #909399;
  font-size: 13px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 8px 0 16px;
}

.estimate-fee {
  color: #f56c6c;
  font-size: 20px;
  font-weight: 700;
}
</style>
