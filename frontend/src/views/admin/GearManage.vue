<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addGear,
  changeGearStatus,
  deleteGear,
  getGearList,
  updateGear
} from '@/api/sysGear'
import { getFallbackGearImage } from '@/utils/gearImages'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const statusLoadingId = ref(null)

const queryParams = reactive({
  name: '',
  category: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const categoryOptions = [
  { label: '全部分类', value: '' },
  { label: '重装背包', value: '重装背包' },
  { label: '轻量化背包', value: '轻量化背包' },
  { label: '帐篷', value: '帐篷' },
  { label: '徒步鞋', value: '徒步鞋' },
  { label: '睡袋', value: '睡袋' },
  { label: '炉具', value: '炉具' },
  { label: '登山杖', value: '登山杖' }
]

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '上架', value: 1 },
  { label: '下架', value: 0 }
]

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const editingId = ref(null)

const formData = reactive({
  name: '',
  brand: '',
  category: '',
  dailyRent: 0,
  deposit: 0,
  totalStock: 0,
  availableStock: 0,
  imageUrl: ''
})

const formRules = {
  name: [{ required: true, message: '请输入装备名称', trigger: 'blur' }],
  brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  dailyRent: [{ required: true, message: '请输入日租金', trigger: 'blur' }],
  deposit: [{ required: true, message: '请输入押金', trigger: 'blur' }],
  totalStock: [{ required: true, message: '请输入总库存', trigger: 'blur' }]
}

function formatMoney(value) {
  if (value === null || value === undefined) return '-'
  return `¥${Number(value).toFixed(2)}`
}

function resolveImage(url) {
  return url || getFallbackGearImage()
}

function resetFormData() {
  formData.name = ''
  formData.brand = ''
  formData.category = ''
  formData.dailyRent = 0
  formData.deposit = 0
  formData.totalStock = 0
  formData.availableStock = 0
  formData.imageUrl = ''
}

async function fetchGearList() {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (queryParams.name.trim()) params.name = queryParams.name.trim()
    if (queryParams.category) params.category = queryParams.category
    if (queryParams.status !== '' && queryParams.status !== null) {
      params.status = queryParams.status
    }

    const res = await getGearList(params)
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
  queryParams.name = ''
  queryParams.category = ''
  queryParams.status = ''
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

function openCreateDialog() {
  dialogMode.value = 'create'
  editingId.value = null
  resetFormData()
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  formData.name = row.name || ''
  formData.brand = row.brand || ''
  formData.category = row.category || ''
  formData.dailyRent = Number(row.dailyRent ?? 0)
  formData.deposit = Number(row.deposit ?? 0)
  formData.totalStock = Number(row.totalStock ?? 0)
  formData.availableStock = Number(row.availableStock ?? 0)
  formData.imageUrl = row.imageUrl || ''
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function closeDialog() {
  dialogVisible.value = false
  editingId.value = null
  resetFormData()
}

function buildPayload() {
  return {
    name: formData.name.trim(),
    brand: formData.brand.trim(),
    category: formData.category,
    dailyRent: Number(formData.dailyRent),
    deposit: Number(formData.deposit),
    totalStock: Number(formData.totalStock),
    availableStock: Number(formData.availableStock),
    imageUrl: formData.imageUrl.trim() || undefined
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (formData.availableStock > formData.totalStock) {
    ElMessage.warning('可用库存不能大于总库存')
    return
  }

  submitLoading.value = true
  try {
    const payload = buildPayload()
    if (dialogMode.value === 'create') {
      await addGear(payload)
      ElMessage.success('新增成功')
    } else {
      await updateGear(editingId.value, payload)
      ElMessage.success('更新成功')
    }
    closeDialog()
    await fetchGearList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await deleteGear(row.id)
  ElMessage.success('删除成功')
  if (tableData.value.length === 1 && queryParams.pageNum > 1) {
    queryParams.pageNum -= 1
  }
  await fetchGearList()
}

async function handleStatusChange(row, nextStatus) {
  const prevStatus = row.status
  statusLoadingId.value = row.id
  try {
    await changeGearStatus(row.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '已上架' : '已下架')
  } catch {
    row.status = prevStatus
  } finally {
    statusLoadingId.value = null
  }
}

onMounted(fetchGearList)
</script>

<template>
  <div class="gear-manage-page">
    <el-card shadow="never" class="header-card">
      <div class="page-header">
        <div>
          <h2>户外装备管理</h2>
          <p>管理装备 SPU 信息、库存与上下架状态</p>
        </div>
      </div>

      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="装备名称">
          <el-input
            v-model="queryParams.name"
            placeholder="模糊搜索"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.category" placeholder="全部分类" style="width: 150px">
            <el-option
              v-for="item in categoryOptions"
              :key="String(item.value)"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" style="width: 130px">
            <el-option
              v-for="item in statusOptions"
              :key="String(item.value)"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openCreateDialog">新增装备</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
        empty-text="暂无装备数据"
      >
        <el-table-column label="图片" width="90" align="center">
          <template #default="{ row }">
            <el-image
              class="gear-thumb"
              :src="resolveImage(row.imageUrl)"
              :preview-src-list="[resolveImage(row.imageUrl)]"
              fit="cover"
              preview-teleported
            >
              <template #error>
                <div class="gear-thumb gear-thumb--error">无图</div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="装备名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="日租金" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(row.dailyRent) }}</template>
        </el-table-column>
        <el-table-column label="押金" width="100" align="right">
          <template #default="{ row }">{{ formatMoney(row.deposit) }}</template>
        </el-table-column>
        <el-table-column prop="totalStock" label="总库存" width="90" align="center" />
        <el-table-column prop="availableStock" label="可用库存" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="statusLoadingId === row.id"
              inline-prompt
              active-text="上架"
              inactive-text="下架"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确认删除该装备？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
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
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增装备' : '编辑装备'"
      width="560px"
      destroy-on-close
      @close="closeDialog"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="装备名称" prop="name">
          <el-input v-model="formData.name" placeholder="如 Osprey Stratos 36L" maxlength="100" />
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="formData.brand" placeholder="如 Osprey" maxlength="50" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="formData.category" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categoryOptions.filter((o) => o.value)"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日租金" prop="dailyRent">
          <el-input-number
            v-model="formData.dailyRent"
            :min="0"
            :precision="2"
            :step="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="押金" prop="deposit">
          <el-input-number
            v-model="formData.deposit"
            :min="0"
            :precision="2"
            :step="10"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="总库存" prop="totalStock">
          <el-input-number
            v-model="formData.totalStock"
            :min="0"
            :step="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="可用库存">
          <el-input-number
            v-model="formData.availableStock"
            :min="0"
            :step="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="图片链接">
          <el-input
            v-model="formData.imageUrl"
            placeholder="/images/gears/1-main.jpg"
            maxlength="512"
          />
        </el-form-item>
        <el-form-item v-if="formData.imageUrl" label="预览">
          <el-image
            class="form-preview"
            :src="resolveImage(formData.imageUrl)"
            fit="cover"
            preview-teleported
            :preview-src-list="[resolveImage(formData.imageUrl)]"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ dialogMode === 'create' ? '确认新增' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.gear-manage-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.header-card,
.table-card {
  border-radius: 12px;
}

.page-header {
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

.gear-thumb {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.gear-thumb--error {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #c0c4cc;
  background: #f5f7fa;
}

.form-preview {
  width: 96px;
  height: 96px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
