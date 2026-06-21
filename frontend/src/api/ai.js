import request from '@/utils/request'

/**
 * AI 户外装备导购
 * @param {string} question 用户提问
 * @returns {Promise<{ data: string }>} data 为大模型返回的 JSON 字符串
 */
export function consultAi(question) {
  return request.post('/ai/consult', { question }, { timeout: 90000 })
}
