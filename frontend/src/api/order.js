import request from '@/utils/request'

/** 订单分页查询 */
export function getOrderPage(params) {
  return request.get('/orders', { params })
}

/** 创建租赁订单 */
export function createRentalOrder(data) {
  return request.post('/orders', data)
}
