import { reactive } from 'vue'

/** 轻量全局状态：登录用户、菜单与 token（刷新后从 localStorage 恢复） */
export const store = reactive({
  token: localStorage.getItem('tms_token') || '',
  user: JSON.parse(localStorage.getItem('tms_user') || 'null'),
  menus: JSON.parse(localStorage.getItem('tms_menus') || '[]'),
  setAuth(data) {
    this.token = data.token || this.token
    this.user = data.user || this.user
    this.menus = data.menus || this.menus
    if (data.token) localStorage.setItem('tms_token', data.token)
    if (data.user) localStorage.setItem('tms_user', JSON.stringify(data.user))
    if (data.menus) localStorage.setItem('tms_menus', JSON.stringify(data.menus))
  },
  clear() {
    this.token = ''
    this.user = null
    this.menus = []
    localStorage.removeItem('tms_token')
    localStorage.removeItem('tms_user')
    localStorage.removeItem('tms_menus')
  }
})

export const roleHome = (role) => {
  if (role === 'ROLE_CUSTOMER') return '/portal'
  if (role === 'ROLE_DRIVER') return '/driver/tasks'
  return '/dashboard/index'
}
