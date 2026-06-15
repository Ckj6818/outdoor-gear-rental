import request from '@/utils/request'

/** 装备分页查询 */
export function getGearPage(params) {
  return request.get('/gears', { params })
}

/** 装备详情 */
export function getGearById(id) {
  return request.get(`/gears/${id}`)
}
