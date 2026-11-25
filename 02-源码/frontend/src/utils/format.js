/** 全局时间/金额格式化：全系统统一调用，禁止各页面自行拼接 */
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
  return '¥' + Number(v).toFixed(2)
}

/** 相对时间：用于详情页「3 小时前发布」 */
export function fromNow(v) {
  if (!v) return '-'
  const t = new Date(String(v).replace(' ', 'T')).getTime()
  const diff = (Date.now() - t) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
  return Math.floor(diff / 86400) + ' 天前'
}

/** 把逗号分隔的图片路径转数组 */
export const splitImages = (s) => (s ? String(s).split(',').filter(Boolean) : [])
