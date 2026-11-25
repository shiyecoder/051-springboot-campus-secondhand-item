import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from './router'

/** 统一 HTTP 客户端：自动带 JWT，按 {code,message,data} 解包；code=0 为成功 */
const http = axios.create({ baseURL: '/api', timeout: 30000 })

/**
 * 后端 Jackson 统一开启 SNAKE_CASE 输出与入参解析，
 * 所以前端可以全用驼峰写代码，请求发出前在这里统一转成下划线，
 * 避免出现「表单填了 dealPrice 后端收不到」这类字段名对不上的问题。
 */
const toSnakeKey = (k) => k.replace(/[A-Z]/g, (c) => '_' + c.toLowerCase())

function toSnakeDeep(v) {
  if (Array.isArray(v)) return v.map(toSnakeDeep)
  if (v && typeof v === 'object' && !(v instanceof FormData) && !(v instanceof Date) && !(v instanceof Blob)) {
    const out = {}
    Object.keys(v).forEach((key) => { out[toSnakeKey(key)] = toSnakeDeep(v[key]) })
    return out
  }
  return v
}

http.interceptors.request.use((cfg) => {
  const t = localStorage.getItem('xy_token')
  if (t) cfg.headers.Authorization = 'Bearer ' + t
  if (cfg.data && typeof cfg.data === 'object') cfg.data = toSnakeDeep(cfg.data)
  return cfg
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) return body.data
      if (body.code === 4010) {
        localStorage.removeItem('xy_token')
        localStorage.removeItem('xy_user')
        localStorage.removeItem('xy_menus')
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
