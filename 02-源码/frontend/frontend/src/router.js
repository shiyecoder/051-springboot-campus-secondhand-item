import { createRouter, createWebHashHistory } from 'vue-router'
import { roleHome, store } from './store'

/**
 * 路由表：后台（管理端）、货主门户、司机端三套骨架。
 * 侧边栏内容来自登录接口下发的菜单，这里只负责组件映射与标题。
 */
const routes = [
  { path: '/login', component: () => import('./views/Login.vue'), meta: { title: '登录', public: true } },
  {
    path: '/',
    component: () => import('./layout/AdminLayout.vue'),
    redirect: '/dashboard/index',
    children: [
      { path: 'dashboard/index', component: () => import('./views/Dashboard.vue'), meta: { title: '运营看板' } },
      { path: 'todo', component: () => import('./views/Todo.vue'), meta: { title: '我的待办' } },
      { path: 'demands', component: () => import('./views/Demands.vue'), meta: { title: '运输需求' } },
      { path: 'orders', component: () => import('./views/Orders.vue'), meta: { title: '运单中心' } },
      { path: 'orders/:id', component: () => import('./views/OrderDetail.vue'), meta: { title: '运单详情' } },
      { path: 'dispatch', component: () => import('./views/Dispatch.vue'), meta: { title: '调度配车' } },
      { path: 'recommend', component: () => import('./views/Recommend.vue'), meta: { title: '联合配车推荐' } },
      { path: 'loading', component: () => import('./views/Loading.vue'), meta: { title: '装车发运' } },
      { path: 'track', component: () => import('./views/Track.vue'), meta: { title: '在途节点' } },
      { path: 'exceptions', component: () => import('./views/Exceptions.vue'), meta: { title: '异常事件' } },
      { path: 'receipts', component: () => import('./views/Receipts.vue'), meta: { title: '回单复核' } },
      { path: 'fees', component: () => import('./views/Fees.vue'), meta: { title: '费用管理' } },
      { path: 'bills', component: () => import('./views/Bills.vue'), meta: { title: '客户账单' } },
      { path: 'payments', component: () => import('./views/Payments.vue'), meta: { title: '收付款' } },
      { path: 'reviews', component: () => import('./views/Reviews.vue'), meta: { title: '服务评价' } },
      { path: 'customers', component: () => import('./views/Customers.vue'), meta: { title: '客户档案' } },
      { path: 'addresses', component: () => import('./views/Addresses.vue'), meta: { title: '客户地址' } },
      { path: 'vehicles', component: () => import('./views/Vehicles.vue'), meta: { title: '车辆档案' } },
      { path: 'vcerts', component: () => import('./views/Vcerts.vue'), meta: { title: '车辆证件' } },
      { path: 'drivers', component: () => import('./views/Drivers.vue'), meta: { title: '驾驶员' } },
      { path: 'dcerts', component: () => import('./views/Dcerts.vue'), meta: { title: '驾驶员证件' } },
      { path: 'analysis', component: () => import('./views/Analysis.vue'), meta: { title: '统计报表' } },
      { path: 'rules', component: () => import('./views/Rules.vue'), meta: { title: '配车规则配置' } },
      { path: 'users', component: () => import('./views/Users.vue'), meta: { title: '用户管理' } },
      { path: 'roles', component: () => import('./views/Roles.vue'), meta: { title: '角色管理' } },
      { path: 'menus', component: () => import('./views/Menus.vue'), meta: { title: '菜单管理' } },
      { path: 'messages', component: () => import('./views/Messages.vue'), meta: { title: '消息中心' } },
      { path: 'profile', component: () => import('./views/Profile.vue'), meta: { title: '个人信息' } }
    ]
  },
  {
    path: '/portal',
    component: () => import('./layout/PortalLayout.vue'),
    children: [
      { path: '', component: () => import('./views/portal/Home.vue'), meta: { title: '门户首页' } },
      { path: 'orders', component: () => import('./views/portal/MyOrders.vue'), meta: { title: '我的运单' } },
      { path: 'demand', component: () => import('./views/portal/Demand.vue'), meta: { title: '提交需求' } },
      { path: 'sign', component: () => import('./views/portal/Sign.vue'), meta: { title: '签收确认' } },
      { path: 'review', component: () => import('./views/portal/Review.vue'), meta: { title: '服务评价' } }
    ]
  },
  {
    path: '/driver',
    component: () => import('./layout/DriverLayout.vue'),
    children: [
      { path: 'tasks', component: () => import('./views/driver/Tasks.vue'), meta: { title: '我的任务' } },
      { path: 'node', component: () => import('./views/driver/NodeReport.vue'), meta: { title: '节点上报' } },
      { path: 'fee', component: () => import('./views/driver/FeeSubmit.vue'), meta: { title: '费用提交' } },
      { path: 'exception', component: () => import('./views/driver/ExceptionReport.vue'), meta: { title: '异常上报' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard/index' }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to) => {
  document.title = (to.meta.title ? to.meta.title + ' · ' : '') + '运联通 TMS'
  if (to.meta.public) return true
  if (!store.token) return { path: '/login', query: { redirect: to.fullPath } }
  const role = store.user?.roleCode
  // 角色与骨架必须匹配：客户只能进门户，司机只能进司机端，内部员工不进门户
  // 用「等于或带斜杠」判断骨架前缀：'/drivers'（驾驶员档案）不能被 '/driver' 前缀误伤
  const inPortal = to.path === '/portal' || to.path.startsWith('/portal/')
  const inDriver = to.path === '/driver' || to.path.startsWith('/driver/')
  if (inPortal && role !== 'ROLE_CUSTOMER') return roleHome(role)
  if (inDriver && role !== 'ROLE_DRIVER') return roleHome(role)
  if (!inPortal && !inDriver && (role === 'ROLE_CUSTOMER' || role === 'ROLE_DRIVER')) {
    return roleHome(role)
  }
  return true
})

export default router
