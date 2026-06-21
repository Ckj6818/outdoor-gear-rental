import request from '@/utils/request'

/** 订单分页查询 */
export function getOrderPage(params) {
  return request.get('/orders', { params })
}

/** 创建租赁订单 */
export function createRentalOrder(data) {
  return request.post('/orders', data)
}

/** 模拟支付 */
export function payOrder(orderId) {
  return request.put(`/orders/${orderId}/pay`)
}

/** 归还装备 */
export function returnGear(orderId) {
  return request.put(`/orders/${orderId}/return`)
}

/** 取消/删除订单（待支付） */
export function cancelOrder(orderId) {
  return request.delete(`/orders/${orderId}`)
}

/** 查询装备已被占用的租赁档期 */
export function getOccupiedDates(gearId) {
  return request.get(`/orders/occupied-dates/${gearId}`)
}

/** 管理员全量订单分页查询 */
export function getAdminOrderPage(params) {
  return request.get('/admin/orders', { params })
}

/** 管理员质检 */
export const inspectOrder = (data) => request.post('/admin/orders/inspect', data)
