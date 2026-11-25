import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from './router'

/** 统一 HTTP 客户端：自动带 JWT，统一解包 {code,message,data} */
const http = axios.create({ baseURL: '/api', timeout: 30000 })

http.interceptors.request.use((cfg) => {
  const t = localStorage.getItem('tms_token')
  if (t) cfg.headers.Authorization = 'Bearer ' + t
  return cfg
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) return body.data
      if (body.code === 401) {
        localStorage.removeItem('tms_token')
        router.push('/login')
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message))
    }
    return body
  },
  (err) => {
    ElMessage.error(err?.response?.data?.message || '网络异常，请稍后重试')
    return Promise.reject(err)
  }
)

export default http
