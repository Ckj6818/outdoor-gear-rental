import request from '@/utils/request'

/** 分页查询装备 */
export function getGearList(params) {
  return request.get('/admin/system/gear', { params })
}

/** 新增装备 */
export function addGear(data) {
  return request.post('/admin/system/gear', data)
}

/** 修改装备 */
export function updateGear(id, data) {
  return request.put(`/admin/system/gear/${id}`, data)
}

/** 删除装备 */
export function deleteGear(id) {
  return request.delete(`/admin/system/gear/${id}`)
}

/** 切换上下架状态 */
export function changeGearStatus(id, status) {
  return request.put(`/admin/system/gear/${id}/status`, { status })
}
