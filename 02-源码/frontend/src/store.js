import { reactive } from 'vue'

/** 轻量全局状态：登录用户、菜单与 token（刷新后从 localStorage 恢复） */
export const store = reactive({
  token: localStorage.getItem('xy_token') || '',
  user: JSON.parse(localStorage.getItem('xy_user') || 'null'),
  menus: JSON.parse(localStorage.getItem('xy_menus') || '[]'),
  setAuth(data) {
    this.token = data.token || this.token
    this.user = data.user || this.user
    this.menus = data.menus || this.menus
    if (data.token) localStorage.setItem('xy_token', data.token)
    if (data.user) localStorage.setItem('xy_user', JSON.stringify(data.user))
    if (data.menus) localStorage.setItem('xy_menus', JSON.stringify(data.menus))
  },
  setUser(u) {
    this.user = u
    if (u) localStorage.setItem('xy_user', JSON.stringify(u))
  },
  clear() {
    this.token = ''
    this.user = null
    this.menus = []
    localStorage.removeItem('xy_token')
    localStorage.removeItem('xy_user')
    localStorage.removeItem('xy_menus')
  }
})

/** 角色落点：学生进门户，运营与管理员进后台 */
export const roleHome = (role) => {
  if (role === 'ROLE_SELLER' || role === 'ROLE_BUYER') return '/portal'
  return '/dashboard/index'
}
