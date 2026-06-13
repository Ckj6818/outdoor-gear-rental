import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    let msg = data?.msg || error.message || '网络异常'
    if (status === 404) {
      msg = data?.msg || '接口不存在或后端未启动，请确认 Spring Boot 已在 8081 端口运行'
    } else if (status === 401) {
      msg = data?.msg || '未登录或 Token 已失效，请重新登录'
    } else if (status === 500) {
      msg = data?.msg || '服务器内部错误，请查看后端控制台日志'
    } else if (!error.response) {
      msg = '无法连接后端服务，请确认后端已启动且 Vite 代理配置正确'
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
