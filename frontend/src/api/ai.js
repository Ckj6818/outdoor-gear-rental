import request from '@/utils/request'

/**
 * AI 户外装备导购
 * @param {string} question 用户提问
 * @param {Array<{role: 'user'|'assistant', content: string}>} [history] 对话历史（不含当前提问）
 * @returns {Promise<{ data: string }>} data 为大模型返回的 JSON 字符串
 */
export function consultAi(question, history = []) {
  return request.post('/ai/consult', { question, history }, { timeout: 90000 })
}
