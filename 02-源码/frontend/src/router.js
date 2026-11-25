import { createRouter, createWebHashHistory } from 'vue-router'
import { roleHome, store } from './store'

/**
 * 路由表：后台（运营/管理员）与门户（学生买家/卖家）两套骨架。
 * 侧边栏内容来自登录接口下发的菜单，这里只负责组件映射与标题。
 */
const routes = [
  { path: '/login', component: () => import('./views/Login.vue'), meta: { title: '登录', public: true } },
  {
    path: '/',
    component: () => import('./layout/AdminLayout.vue'),
    redirect: '/dashboard/index',
    children: [
      { path: 'dashboard/index', component: () => import('./views/Dashboard.vue'), meta: { title: '运营首页' } },
      { path: 'admin/audit', component: () => import('./views/GoodsAudit.vue'), meta: { title: '商品审核台' } },
      { path: 'admin/goods', component: () => import('./views/Goods.vue'), meta: { title: '商品管理' } },
      { path: 'admin/orders', component: () => import('./views/Orders.vue'), meta: { title: '订单管理' } },
      { path: 'admin/orders/:id', component: () => import('./views/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: 'admin/reviews', component: () => import('./views/Reviews.vue'), meta: { title: '评价管理' } },
      { path: 'admin/reports', component: () => import('./views/Reports.vue'), meta: { title: '举报与纠纷' } },
      { path: 'admin/credit', component: () => import('./views/CreditLogs.vue'), meta: { title: '信用分流水' } },
      { path: 'admin/price', component: () => import('./views/PriceCalcs.vue'), meta: { title: '定价评分记录' } },
      { path: 'admin/analysis', component: () => import('./views/Analysis.vue'), meta: { title: '运营统计' } },
      { path: 'admin/categories', component: () => import('./views/Categories.vue'), meta: { title: '分类管理' } },
      { path: 'admin/campus', component: () => import('./views/Campus.vue'), meta: { title: '校区面交点' } },
      { path: 'admin/rules', component: () => import('./views/Rules.vue'), meta: { title: '定价规则配置' } },
      { path: 'admin/notices', component: () => import('./views/Notices.vue'), meta: { title: '公告管理' } },
      { path: 'admin/users', component: () => import('./views/Users.vue'), meta: { title: '用户管理' } },
      { path: 'admin/roles', component: () => import('./views/Roles.vue'), meta: { title: '角色管理' } },
      { path: 'admin/menus', component: () => import('./views/Menus.vue'), meta: { title: '菜单管理' } },
      { path: 'admin/dicts', component: () => import('./views/Dicts.vue'), meta: { title: '字典管理' } },
      { path: 'admin/logs', component: () => import('./views/Logs.vue'), meta: { title: '操作日志' } },
      { path: 'admin/messages', component: () => import('./views/Messages.vue'), meta: { title: '消息中心' } },
      { path: 'admin/profile', component: () => import('./views/Profile.vue'), meta: { title: '个人信息' } }
    ]
  },
  {
    path: '/portal',
    component: () => import('./layout/PortalLayout.vue'),
    children: [
      { path: '', component: () => import('./views/portal/Home.vue'), meta: { title: '首页' } },
      { path: 'search', component: () => import('./views/portal/Search.vue'), meta: { title: '逛闲置' } },
      { path: 'goods/:id', component: () => import('./views/portal/GoodsDetail.vue'), meta: { title: '商品详情' } },
      { path: 'publish', component: () => import('./views/portal/Publish.vue'), meta: { title: '发布闲置' } },
      { path: 'mygoods', component: () => import('./views/portal/MyGoods.vue'), meta: { title: '我的闲置' } },
      { path: 'orders', component: () => import('./views/portal/MyOrders.vue'), meta: { title: '我的订单' } },
      { path: 'orders/:id', component: () => import('./views/portal/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: 'credit', component: () => import('./views/portal/MyCredit.vue'), meta: { title: '我的信用' } },
      { path: 'reviews', component: () => import('./views/portal/MyReviews.vue'), meta: { title: '我的评价' } },
      { path: 'messages', component: () => import('./views/portal/Messages.vue'), meta: { title: '消息' } },
      { path: 'profile', component: () => import('./views/portal/Profile.vue'), meta: { title: '个人资料' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard/index' }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to) => {
  document.title = (to.meta.title ? to.meta.title + ' · ' : '') + '拾光集'
  if (to.meta.public) return true
  if (!store.token) return { path: '/login', query: { redirect: to.fullPath } }
  const role = store.user?.roleCode
  const inPortal = to.path === '/portal' || to.path.startsWith('/portal/')
  const isStudent = role === 'ROLE_SELLER' || role === 'ROLE_BUYER'
  // 学生只能进门户，运营与管理员只能进后台，避免越权访问
  if (isStudent && !inPortal) return roleHome(role)
  if (!isStudent && inPortal) return roleHome(role)
  return true
})

export default router
