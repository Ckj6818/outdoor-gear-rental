<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addAdmin,
  changeStatus,
  deleteAdmin,
  getAdminList,
  updateAdmin
} from '@/api/adminUser'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const statusLoadingId = ref(null)

const queryParams = reactive({
  username: '',
  phone: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
]

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const editingId = ref(null)

const formData = reactive({
  username: '',
  phone: '',
  email: ''
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度为 2-50 个字符', trigger: 'blur' }
  ],
  phone: [
    {
      validator: (_rule, value, callback) => {
        if (!value || !String(value).trim()) {
          callback()
          return
        }
        if (/^1[3-9]\d{9}$/.test(String(value).trim())) {
          callback()
        } else {
          callback(new Error('请输入正确的手机号'))
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const currentUserId = localStorage.getItem('userId')

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function resetFormData() {
  formData.username = ''
  formData.phone = ''
  formData.email = ''
}

async function fetchAdminList() {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    if (queryParams.username.trim()) {
      params.username = queryParams.username.trim()
    }
    if (queryParams.phone.trim()) {
      params.phone = queryParams.phone.trim()
    }
    if (queryParams.status !== '' && queryParams.status !== null) {
      params.status = queryParams.status
    }

    const res = await getAdminList(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchAdminList()
}

function handleReset() {
  queryParams.username = ''
  queryParams.phone = ''
  queryParams.status = ''
  queryParams.pageNum = 1
  fetchAdminList()
}

function handlePageChange(page) {
  queryParams.pageNum = page
  fetchAdminList()
}

function handleSizeChange(size) {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchAdminList()
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
  formData.username = row.username || ''
  formData.phone = row.phone || ''
  formData.email = row.email || ''
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function closeDialog() {
  dialogVisible.value = false
  editingId.value = null
  resetFormData()
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    username: formData.username.trim(),
    phone: formData.phone.trim() || undefined,
    email: formData.email.trim() || undefined
  }

  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await addAdmin(payload)
      ElMessage.success('新增成功，初始密码为 123456')
    } else {
      await updateAdmin(editingId.value, payload)
      ElMessage.success('更新成功')
    }
    closeDialog()
    await fetchAdminList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  if (String(row.id) === String(currentUserId)) {
    ElMessage.warning('不能删除当前登录账号')
    return
  }
  await deleteAdmin(row.id)
  ElMessage.success('删除成功')
  if (tableData.value.length === 1 && queryParams.pageNum > 1) {
    queryParams.pageNum -= 1
  }
  await fetchAdminList()
}

async function handleStatusChange(row, nextStatus) {
  if (String(row.id) === String(currentUserId) && nextStatus === 0) {
    ElMessage.warning('不能禁用当前登录账号')
    row.status = 1
    return
  }

  const prevStatus = row.status
  statusLoadingId.value = row.id
  try {
    await changeStatus(row.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
  } catch {
    row.status = prevStatus
  } finally {
    statusLoadingId.value = null
  }
}

onMounted(fetchAdminList)
</script>

<template>
  <div class="admin-manage-page">
    <el-card shadow="never" class="header-card">
      <div class="page-header">
        <div>
          <h2>管理员账号管理</h2>
          <p>管理系统管理员账户，支持新增、编辑、启用/禁用与删除</p>
        </div>
      </div>

      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.username"
            placeholder="模糊搜索用户名"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="queryParams.phone"
            placeholder="精确匹配手机号"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" style="width: 140px">
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
          <el-button type="success" @click="openCreateDialog">新增管理员</el-button>
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
        empty-text="暂无管理员数据"
      >
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="130">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="statusLoadingId === row.id"
              inline-prompt
              active-text="正常"
              inactive-text="禁用"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确认删除该管理员？"
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
      :title="dialogMode === 'create' ? '新增管理员' : '编辑管理员'"
      width="480px"
      destroy-on-close
      @close="closeDialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入登录用户名"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" maxlength="100" />
        </el-form-item>
        <p v-if="dialogMode === 'create'" class="form-tip">
          新增后默认密码为 <strong>123456</strong>，请提醒管理员首次登录后修改。
        </p>
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
.admin-manage-page {
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

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.form-tip {
  margin: 0 0 0 80px;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.form-tip strong {
  color: #303133;
}
</style>
