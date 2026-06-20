import request from '@/utils/request'

/** 运营大屏统计数据 */
export function getDashboardStats() {
  return request.get('/admin/dashboard/stats')
}
