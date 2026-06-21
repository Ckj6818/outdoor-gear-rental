import request from '@/utils/request'

/** 分页查询管理员列表 */
export function getAdminList(params) {
  return request.get('/admin/system/user', {
    params: { ...params, role: 0 }
  })
}

/** 新增管理员 */
export function addAdmin(data) {
  return request.post('/admin/system/user', data)
}

/** 修改管理员 */
export function updateAdmin(id, data) {
  return request.put(`/admin/system/user/${id}`, data)
}

/** 删除管理员 */
export function deleteAdmin(id) {
  return request.delete(`/admin/system/user/${id}`)
}

/** 切换管理员状态 */
export function changeStatus(id, status) {
  return request.put(`/admin/system/user/${id}/status`, { status })
}
