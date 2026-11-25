/**
 * 全局时间与数值格式化：全系统统一调用，禁止各页面自行拼接
 * 时间统一 yyyy-MM-dd HH:mm:ss（需求文档 测试数据与验收用例）
 */
export function fmtTime(v) {
  if (!v) return '-'
  const s = String(v).replace('T', ' ')
  return s.length >= 19 ? s.slice(0, 19) : s
}

export function fmtDay(v) {
  if (!v) return '-'
  return String(v).slice(0, 10)
}

export function fmtMoney(v) {
  if (v === null || v === undefined || v === '') return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return v
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function fmtNum(v, d = 1) {
  if (v === null || v === undefined || v === '') return '-'
  const n = Number(v)
  return Number.isNaN(n) ? v : n.toFixed(d)
}

/** 差异率显示：正负号 + 百分比 */
export function fmtRate(v) {
  const n = Number(v || 0)
  return (n > 0 ? '+' : '') + n.toFixed(1) + '%'
}

export const nowStr = () => {
  const d = new Date()
  const p = (x) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
