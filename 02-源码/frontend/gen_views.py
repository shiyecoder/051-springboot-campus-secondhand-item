#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""NEW-051 视图生成器：后台 22 个页面 + 门户 11 个页面。

列表页尽量复用 CrudPage（分页/筛选/抽屉表单/导出），
业务动作页（审核台、订单详情、规则配置、举报处理、定价评分）单独实现。
"""
import os

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(BASE, "src")
V = {}

def add(p, c):
    V[p] = c.strip() + "\n"

# ================================================================ 登录
add("views/Login.vue", r"""
<template>
  <div class="login">
    <div class="lleft">
      <div class="lbrand">拾光集 <span>校园二手</span></div>
      <h1>让闲置<br />在校园里流转起来</h1>
      <p>发布审核 · 分类检索 · 面交下单 · 双向评价 · 纠纷调解，只服务本校学生。</p>
      <div class="lstat">
        <div><b>{{ stats.goods ?? '-' }}</b><small>在售闲置</small></div>
        <div><b>{{ stats.deals ?? '-' }}</b><small>累计成交</small></div>
        <div><b>{{ stats.users ?? '-' }}</b><small>认证同学</small></div>
      </div>
      <div class="lbg" aria-hidden="true"></div>
    </div>
    <div class="lright">
      <div class="lform">
        <h2>登录</h2>
        <p class="sub">使用校园账号登录，学生账号由学号绑定校区</p>
        <el-form :model="form" @keyup.enter="submit">
          <el-form-item>
            <el-input v-model="form.username" size="large" placeholder="登录名" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" />
          </el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">登录</el-button>
        </el-form>
        <div class="quick">
          <span>演示账号</span>
          <a @click="fill('admin')">管理员 admin</a>
          <a @click="fill('operator')">运营 operator</a>
          <a @click="fill('seller01')">卖家 seller01</a>
          <a @click="fill('buyer01')">买家 buyer01</a>
        </div>
        <p class="tip">默认密码 123456 ｜ 连续失败 5 次锁定 10 分钟</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../http'
import { roleHome, store } from '../store'

/** 登录页：左侧品牌与背景图（本地静态资源），右侧表单；不使用默认表单样式 */
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const stats = reactive({})

const fill = (u) => {
  form.username = u
  form.password = '123456'
}

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入登录名与密码')
    return
  }
  loading.value = true
  try {
    const data = await http.post('/auth/login', form)
    store.setAuth(data)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || roleHome(data.user.roleCode))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    Object.assign(stats, await http.get('/auth/site-stats'))
  } catch (e) { /* 统计失败不影响登录 */ }
})
</script>

<style scoped>
.login { display: grid; grid-template-columns: 1.15fr 1fr; min-height: 100vh; background: #fff; }
.lleft { position: relative; padding: 56px 64px; background: linear-gradient(155deg, #146B5B, #0E4C41 62%, #0B3B33); color: #EAF3F1; overflow: hidden; display: flex; flex-direction: column; }
.lbrand { font-size: 17px; font-weight: 700; letter-spacing: 1px; }
.lbrand span { font-size: 11px; font-weight: 400; opacity: .7; margin-left: 8px; letter-spacing: 2px; }
.lleft h1 { font-size: 38px; line-height: 1.35; margin: 56px 0 16px; font-weight: 700; letter-spacing: 1px; }
.lleft p { font-size: 14px; line-height: 1.9; opacity: .92; max-width: 430px; }
.lstat { display: flex; gap: 44px; margin-top: auto; padding-top: 40px; position: relative; z-index: 2; }
.lstat b { font-size: 30px; font-variant-numeric: tabular-nums; }
.lstat small { display: block; font-size: 12px; opacity: .78; margin-top: 2px; }
.lbg { position: absolute; right: -80px; bottom: -60px; width: 420px; height: 420px; border-radius: 50%; background: radial-gradient(circle at 30% 30%, rgba(217,164,65,.45), rgba(217,164,65,0) 68%); }
.lright { display: grid; place-items: center; padding: 40px; background: #FBFCFC; }
.lform { width: 100%; max-width: 360px; }
.lform h2 { font-size: 24px; margin: 0 0 6px; }
.sub { font-size: 12.5px; color: var(--ink2); margin: 0 0 24px; }
.quick { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 18px; font-size: 12.5px; color: var(--ink2); }
.quick a { color: var(--p2); cursor: pointer; }
.quick a:hover { color: var(--p); text-decoration: underline; }
.tip { margin-top: 22px; font-size: 12px; color: var(--ink2); }
@media (max-width: 900px) {
  .login { grid-template-columns: 1fr; }
  .lleft { padding: 20px 22px; }
  .lleft h1 { font-size: 22px; margin: 12px 0 8px; }
  .lleft p { display: none; }
  .lstat { display: none; }
  .lbg { display: none; }
  .lright { padding: 24px 18px 40px; place-items: start center; }
}
</style>
""")

# ================================================================ 运营首页
add("views/Dashboard.vue", r"""
<template>
  <div>
    <div class="page-h">
      <div><h2>运营首页</h2><p>{{ updated }} 更新 ｜ 数据范围：全部校区</p></div>
      <div class="sp"></div>
      <el-radio-group v-model="days" size="small" @change="load">
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
        <el-radio-button :value="90">本学期</el-radio-button>
      </el-radio-group>
    </div>

    <div class="grid g4">
      <div class="kpi" v-for="k in kpis" :key="k.label">
        <small>{{ k.label }}</small><b>{{ k.value }}</b><i>{{ k.hint }}</i>
      </div>
    </div>

    <div class="grid g2" style="margin-top: 16px">
      <div class="card">
        <h3>近 {{ days }} 天发布量与成交量</h3>
        <Chart :option="trendOption" :height="240" />
      </div>
      <div class="card">
        <h3>分类供给分布（在售）</h3>
        <Chart :option="pieOption" :height="240" />
      </div>
    </div>

    <div class="grid g2" style="margin-top: 16px">
      <div class="card">
        <h3>发布 → 审核 → 下单 → 成交 漏斗</h3>
        <Chart :option="funnelOption" :height="220" />
      </div>
      <div class="card">
        <h3>待办与供给预警</h3>
        <div class="todo">
          <router-link to="/admin/audit" class="todo-i"><b>{{ todo.pendingGoods ?? 0 }}</b><span>待审核商品</span></router-link>
          <router-link to="/admin/reports" class="todo-i"><b>{{ todo.pendingReports ?? 0 }}</b><span>待受理举报</span></router-link>
          <router-link to="/admin/reports" class="todo-i"><b>{{ todo.disputes ?? 0 }}</b><span>处理中纠纷</span></router-link>
          <router-link to="/admin/reports" class="todo-i"><b>{{ todo.appeals ?? 0 }}</b><span>待复核申诉</span></router-link>
        </div>
        <p v-for="w in warn" :key="w.name" class="warn-line">{{ w.name }}：{{ w.tip }}</p>
        <p v-if="!warn.length" class="warn-line">暂无供大于求的分类</p>
      </div>
    </div>

    <div class="grid g2" style="margin-top: 16px">
      <div class="card"><h3>各校区成交量</h3><Chart :option="campusOption" :height="220" /></div>
      <div class="card"><h3>用户信用分分布</h3><Chart :option="creditOption" :height="220" /></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../http'
import Chart from '../components/Chart.vue'

/** 运营看板：8 个核心指标 + 6 组图表，整页控制在 1440×900 一屏内 */
const days = ref(30)
const data = ref({ kpi: {}, priceStats: {}, trend: [], categorySupply: [], campusDeal: [], funnel: [], creditDistribution: [], supplyWarning: [] })
const todo = ref({})
const updated = ref('')
const warn = computed(() => data.value.supplyWarning || [])

const kpis = computed(() => [
  { label: '今日新增商品', value: data.value.kpi.todayNew ?? 0, hint: '按发布时间统计' },
  { label: '待审核商品', value: data.value.kpi.pendingAudit ?? 0, hint: '48 小时内需受理' },
  { label: '在售商品', value: data.value.kpi.onSale ?? 0, hint: '覆盖 3 个校区' },
  { label: '本周成交', value: data.value.kpi.weekDeal ?? 0, hint: '已完成订单' },
  { label: '发布成交转化率', value: (data.value.kpi.conversionRate ?? 0) + '%', hint: '完成订单/商品总数' },
  { label: '平均成交价', value: '¥' + (data.value.kpi.avgDealPrice ?? 0), hint: '仅统计已完成' },
  { label: '纠纷率', value: (data.value.kpi.disputeRate ?? 0) + '%', hint: '纠纷中/全部订单' },
  { label: '定价建议采纳率', value: (data.value.priceStats.adoptRate ?? 0) + '%', hint: '人工修正 ' + (data.value.priceStats.correctRate ?? 0) + '%' }
])

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['发布量', '成交量'], right: 0, top: 0, textStyle: { fontSize: 11 } },
  grid: { left: 32, right: 8, top: 28, bottom: 24 },
  xAxis: { type: 'category', data: (data.value.trend || []).map((x) => x.day), axisLabel: { fontSize: 10 } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#EEF1F2' } } },
  series: [
    { name: '发布量', type: 'line', smooth: true, data: (data.value.trend || []).map((x) => x.publish) },
    { name: '成交量', type: 'line', smooth: true, data: (data.value.trend || []).map((x) => x.deal) }
  ]
}))

const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { fontSize: 11 } },
  series: [{
    type: 'pie', radius: ['42%', '66%'], center: ['50%', '44%'],
    itemStyle: { borderColor: '#fff', borderWidth: 2 },
    label: { formatter: '{b}\n{c}', fontSize: 11 },
    data: data.value.categorySupply || []
  }]
}))

const funnelOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'funnel', left: '8%', right: '8%', top: 10, bottom: 10,
    label: { formatter: '{b} {c}', fontSize: 11 },
    data: data.value.funnel || []
  }]
}))

const campusOption = computed(() => ({
  tooltip: {},
  grid: { left: 60, right: 16, top: 12, bottom: 24 },
  xAxis: { type: 'value', splitLine: { lineStyle: { color: '#EEF1F2' } } },
  yAxis: { type: 'category', data: (data.value.campusDeal || []).map((x) => x.name) },
  series: [{ type: 'bar', barWidth: 16, data: (data.value.campusDeal || []).map((x) => x.value) }]
}))

const creditOption = computed(() => ({
  tooltip: {},
  grid: { left: 32, right: 8, top: 12, bottom: 24 },
  xAxis: { type: 'category', data: (data.value.creditDistribution || []).map((x) => x.name), axisLabel: { fontSize: 10 } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#EEF1F2' } } },
  series: [{ type: 'bar', barWidth: 18, data: (data.value.creditDistribution || []).map((x) => x.value) }]
}))

async function load() {
  data.value = await http.get('/dashboard/overview', { params: { days: days.value } })
  todo.value = await http.get('/dashboard/todo')
  updated.value = new Date().toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>

<style scoped>
.todo { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
.todo-i { border: 1px solid var(--line); border-radius: 8px; padding: 10px; text-align: center; }
.todo-i b { display: block; font-size: 20px; color: var(--p); }
.todo-i span { font-size: 11.5px; color: var(--ink2); }
.todo-i:hover { border-color: var(--p2); }
.warn-line { font-size: 12.5px; color: var(--ink2); margin: 8px 0 0; }
</style>
""")

# ================================================================ 审核台
add("views/GoodsAudit.vue", r"""
<template>
  <div>
    <div class="page-h">
      <div><h2>商品审核台</h2><p>待审核 {{ rows.length }} 件 ｜ 按违禁品、虚假描述、图片合规三项检查；驳回需选原因并填写不少于 5 字说明</p></div>
    </div>
    <div class="split">
      <div class="card">
        <h3>待审核队列</h3>
        <el-table :data="rows" v-loading="loading" empty-text="暂无待审核商品" @row-click="pick" highlight-current-row>
          <el-table-column label="商品" min-width="240">
            <template #default="{ row }">
              <div class="prod">
                <img class="thumb" :src="row.cover_url" alt="" />
                <div><b>{{ row.title }}</b><small>{{ row.goods_no }} · {{ row.campus_name }}</small></div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="售价" width="100"><template #default="{ row }"><span class="num">¥{{ row.price }}</span></template></el-table-column>
          <el-table-column label="成色" width="90"><template #default="{ row }"><StatusTag :code="row.condition_level" type="condition_level" /></template></el-table-column>
        </el-table>
        <div class="pager">
          <span>共 {{ total }} 条</span>
          <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="reload" />
        </div>
      </div>

      <div class="card" v-if="current.id">
        <h3>图文对照</h3>
        <div class="gallery">
          <img v-for="(p, i) in images" :key="i" :src="p" alt="" />
        </div>
        <p class="line"><b>{{ current.title }}</b> ｜ ¥{{ current.price }}
          <span v-if="current.original_price"> ｜ 入手价 ¥{{ current.original_price }}</span></p>
        <p class="line muted">{{ current.description }}</p>
        <p class="line muted">实拍图 {{ images.length }} 张 ｜ 卖家 {{ current.seller_id }} ｜ 发布时间 {{ fmtTime(current.create_time) }}</p>
        <p class="line muted" v-if="current.audit_remark">系统提示：{{ current.audit_remark }}</p>
        <el-input v-model="reason" type="textarea" :rows="2" placeholder="审核意见（驳回必填，不少于 5 字）" style="margin-top: 12px" />
        <div class="acts">
          <el-button type="primary" @click="submit(true)">通过并上架</el-button>
          <el-button type="danger" plain @click="submit(false)">驳回</el-button>
          <el-button plain @click="submit(null, true)">冻结商品</el-button>
        </div>
      </div>
      <div class="card" v-else><h3>选择左侧一件商品查看图文对照</h3><p class="muted">审核通过后立即在门户上架，驳回会通知卖家并展示审核意见。</p></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { fmtTime, splitImages } from '../utils/format'

/** 运营审核台：左侧队列 + 右侧大图与字段对照，底部通过/驳回/冻结 */
const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const current = ref({})
const reason = ref('')
const images = computed(() => splitImages(current.value.image_urls || current.value.cover_url))

async function load() {
  loading.value = true
  try {
    const data = await http.get('/goods/page', { params: { page: page.value, size: 10, status: '待审核' } })
    rows.value = data.records || []
    total.value = data.total || 0
    if (!current.value.id && rows.value.length) pick(rows.value[0])
  } finally {
    loading.value = false
  }
}
const reload = (p) => { page.value = p; load() }
const pick = (row) => { current.value = row; reason.value = '' }

async function submit(pass, freeze = false) {
  if (pass === false && reason.value.trim().length < 5) {
    ElMessage.warning('驳回必须选择原因并填写不少于 5 字的说明')
    return
  }
  await http.post(`/goods/${current.value.id}/audit`, { pass, freeze, reason: reason.value || null })
  ElMessage.success(freeze ? '已冻结' : pass ? '已通过并上架' : '已驳回')
  current.value = {}
  load()
}

onMounted(load)
</script>

<style scoped>
.prod { display: flex; gap: 10px; align-items: center; }
.prod b { font-weight: 600; font-size: 13px; display: block; }
.prod small { color: var(--ink2); font-size: 11.5px; }
.gallery { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.gallery img { width: 100%; aspect-ratio: 4/3; object-fit: cover; border-radius: 8px; background: #EDF1F0; }
.line { font-size: 13px; margin: 10px 0 0; }
.muted { color: var(--ink2); font-size: 12.5px; }
.acts { display: flex; gap: 8px; margin-top: 14px; }
</style>
""")

# ================================================================ 商品管理
add("views/Goods.vue", r"""
<template>
  <CrudPage
    title="商品管理"
    desc="覆盖待审核/在售/已下架/已售出/审核驳回/冻结六种状态；状态只能通过审核与流转变更"
    api="goods"
    entity="商品"
    :columns="columns"
    :filters="filters"
    :form-fields="[]"
    :can-create="false"
    link-base="/portal/goods"
  >
    <template #row-actions="{ row }">
      <el-button link type="warning" @click="offShelf(row)" v-if="row.status === '在售'">下架</el-button>
      <el-button link type="primary" @click="priceRef(row)">定价参考</el-button>
    </template>
  </CrudPage>
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'

/** 商品管理：后台全量列表，行内提供下架与定价参考 */
const columns = [
  { key: 'cover_url', label: '封面', type: 'image', min: 70 },
  { key: 'goods_no', label: '商品编号', min: 140 },
  { key: 'title', label: '标题', min: 220 },
  { key: 'campus_name', label: '校区', min: 90 },
  { key: 'condition_level', label: '成色', dict: 'condition_level', min: 95 },
  { key: 'price', label: '售价', type: 'money', min: 100 },
  { key: 'original_price', label: '入手价', type: 'money', min: 100 },
  { key: 'status', label: '状态', dict: 'goods_status', min: 100 },
  { key: 'view_count', label: '浏览', type: 'num', min: 80 },
  { key: 'create_time', label: '发布时间', type: 'time', min: 160 }
]
const filters = [
  { key: 'keyword', label: '关键词', placeholder: '标题或商品编号' },
  { key: 'status', label: '状态', type: 'select', dict: 'goods_status' },
  { key: 'condition_level', label: '成色', type: 'select', dict: 'condition_level' },
  { key: 'campus_id', label: '校区', type: 'select', options: [{ value: 1, label: '东校区' }, { value: 2, label: '西校区' }, { value: 3, label: '北校区' }] }
]

async function offShelf(row) {
  const { value } = await ElMessageBox.prompt('请填写下架原因（不少于 5 字）', '下架确认', { inputPlaceholder: '例如：与买家协商后暂停出售' })
  await http.post(`/goods/${row.id}/off-shelf`, { reason: value })
  ElMessage.success('已下架')
  location.reload()
}

async function priceRef(row) {
  const d = await http.get(`/goods/${row.id}/price-reference`)
  ElMessageBox.alert(
    `建议价区间 ¥${d.suggest_low ?? '-'} ~ ¥${d.suggest_high ?? '-'}<br/>总分 ${d.total_score}（${d.grade} 级）<br/>${d.conclusion}` +
    (d.missing && d.missing.length ? `<br/><br/>未满足条件：${d.missing.join('；')}` : ''),
    '定价参考（规则 ' + d.rule_version + '）',
    { dangerouslyUseHTMLString: true }
  )
}
</script>
""")

# ================================================================ 订单管理 / 详情
add("views/Orders.vue", r"""
<template>
  <CrudPage
    title="订单管理"
    desc="状态机：待卖家确认 → 待面交 → 已完成；取消、纠纷分支均写操作日志并通知双方"
    api="trade-order"
    entity="订单"
    :columns="columns"
    :filters="filters"
    :form-fields="[]"
    :can-create="false"
    link-base="/admin/orders"
    link-key="order_no"
  />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  { key: 'order_no', label: '订单号', type: 'link', min: 170 },
  { key: 'goods_title', label: '商品', min: 220 },
  { key: 'buyer_id', label: '买家', type: 'num', min: 80 },
  { key: 'seller_id', label: '卖家', type: 'num', min: 80 },
  { key: 'deal_price', label: '成交价', type: 'money', min: 100 },
  { key: 'pay_type', label: '支付', min: 80 },
  { key: 'appoint_time', label: '面交时间', type: 'time', min: 160 },
  { key: 'status', label: '状态', dict: 'order_status', min: 110 },
  { key: 'create_time', label: '下单时间', type: 'time', min: 160 }
]
const filters = [
  { key: 'keyword', label: '订单号' },
  { key: 'status', label: '状态', type: 'select', dict: 'order_status' }
]
</script>
""")

add("views/OrderDetail.vue", r"""
<template>
  <div v-if="order.id">
    <div class="page-h">
      <div><h2>订单 {{ order.order_no }}</h2><p>{{ order.status }} ｜ 下单 {{ fmtTime(order.create_time) }} ｜ 商品锁定至 {{ fmtTime(order.lock_expire_time) }}</p></div>
      <div class="sp"></div>
      <StatusTag :code="order.status" type="order_status" />
    </div>
    <div class="steps">
      <b>下单</b><span>→</span><b :class="{ dim: !order.seller_confirm_time }">卖家接单</b><span>→</span>
      <b :class="{ dim: !order.buyer_confirm_time }">买家确认收货</b><span>→</span><b :class="{ dim: order.status !== '已完成' }">双方评价</b>
    </div>
    <div class="split">
      <div class="card">
        <h3>商品快照</h3>
        <div class="prod">
          <img :src="order.goods_cover" class="cover" alt="" />
          <div><b>{{ order.goods_title }}</b><small>成交价 ¥{{ order.deal_price }} ｜ {{ order.pay_type }}</small></div>
        </div>
        <el-descriptions :column="2" border style="margin-top: 14px">
          <el-descriptions-item label="面交时间">{{ fmtTime(order.appoint_time) }}</el-descriptions-item>
          <el-descriptions-item label="面交点">{{ order.appoint_place }}</el-descriptions-item>
          <el-descriptions-item label="买家 ID">{{ order.buyer_id }}</el-descriptions-item>
          <el-descriptions-item label="卖家 ID">{{ order.seller_id }}</el-descriptions-item>
          <el-descriptions-item label="卖家接单">{{ fmtTime(order.seller_confirm_time) }}</el-descriptions-item>
          <el-descriptions-item label="买家确认">{{ fmtTime(order.buyer_confirm_time) }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{ order.cancel_reason || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="card">
        <h3>可执行操作</h3>
        <p class="muted">操作按钮由后端按当前状态与操作人计算，前端只做展示。</p>
        <div class="acts">
          <el-button v-if="actions.includes('confirm')" type="primary" @click="confirm">确认接单</el-button>
          <el-button v-if="actions.includes('complete')" type="primary" @click="complete">确认收货</el-button>
          <el-button v-if="actions.includes('cancel')" plain @click="cancel">取消订单</el-button>
          <el-button v-if="actions.includes('dispute')" type="danger" plain @click="dispute">发起纠纷</el-button>
          <el-button v-if="actions.includes('review')" @click="$router.push('/portal/reviews')">去评价</el-button>
          <span v-if="actions.includes('wait')" class="muted">纠纷处理中，等待运营调解</span>
        </div>
        <h3 style="margin-top: 18px">评价（双盲）</h3>
        <p class="muted" v-if="!reviews.length">双方均提交后才互相可见，当前暂无可展示评价</p>
        <div v-for="r in reviews" :key="r.id" class="review">
          <b>{{ r.anonymous ? '匿名用户' : '用户 ' + r.from_user_id }}</b>
          <span class="stars">{{ '★'.repeat(r.score) }}</span>
          <p>{{ r.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { fmtTime } from '../utils/format'

const route = useRoute()
const order = ref({})
const actions = ref([])
const reviews = ref([])
const id = computed(() => route.params.id)

async function load() {
  const data = await http.get(`/trade-order/${id.value}`)
  order.value = data.order
  actions.value = data.actions || []
  reviews.value = data.reviews || []
}

async function confirm() {
  const { value } = await ElMessageBox.prompt('填写实际成交价（允许低于标价）', '确认接单', { inputValue: String(order.value.deal_price) })
  await http.post(`/trade-order/${id.value}/confirm`, { dealPrice: Number(value), payType: order.value.pay_type })
  ElMessage.success('已确认接单')
  load()
}
async function complete() {
  await http.post(`/trade-order/${id.value}/complete`)
  ElMessage.success('已确认收货')
  load()
}
async function cancel() {
  const { value } = await ElMessageBox.prompt('请填写取消原因（不少于 5 字）', '取消订单')
  await http.post(`/trade-order/${id.value}/cancel`, { reason: value })
  ElMessage.success('订单已取消')
  load()
}
async function dispute() {
  const { value } = await ElMessageBox.prompt('请描述问题（不少于 5 字），运营将在 48 小时内受理', '发起纠纷')
  await http.post(`/trade-order/${id.value}/dispute`, { reason: value, targetStatus: '纠纷中', attachments: [] })
  ElMessage.success('已发起纠纷，商品已冻结')
  load()
}

onMounted(load)
</script>

<style scoped>
.steps { display: flex; gap: 10px; align-items: center; font-size: 12.5px; color: var(--ink2); margin-bottom: 14px; }
.steps b { color: var(--p); }
.steps .dim { color: var(--ink2); font-weight: 400; }
.prod { display: flex; gap: 12px; align-items: center; }
.cover { width: 96px; height: 72px; object-fit: cover; border-radius: 8px; }
.prod b { display: block; font-size: 14px; }
.prod small { color: var(--ink2); font-size: 12px; }
.acts { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; }
.muted { color: var(--ink2); font-size: 12.5px; }
.review { border-top: 1px solid var(--line); padding-top: 10px; margin-top: 10px; font-size: 13px; }
.stars { color: var(--a); margin-left: 8px; font-size: 12px; }
</style>
""")

# ================================================================ 举报 / 评价
add("views/Reports.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>举报与纠纷</h2><p>48 小时内受理；处理结果通过站内消息通知双方；被举报人可申诉一次，由另一名运营复核</p></div></div>
    <div class="filter">
      <div class="fi"><label>状态</label>
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option v-for="(v, k) in DICT.report_status" :key="k" :label="v[0]" :value="k" />
        </el-select>
      </div>
      <div class="fi"><label>类型</label>
        <el-select v-model="query.reason_type" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option v-for="(v, k) in DICT.report_reason" :key="k" :label="v[0]" :value="k" />
        </el-select>
      </div>
      <div class="sp"></div>
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="当前筛选条件下无举报记录">
        <el-table-column prop="report_no" label="举报编号" min-width="150" />
        <el-table-column prop="biz_title" label="关联对象" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="110"><template #default="{ row }"><StatusTag :code="row.reason_type" type="report_reason" /></template></el-table-column>
        <el-table-column prop="description" label="问题描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :code="row.status" type="report_status" /></template></el-table-column>
        <el-table-column label="申诉" width="110"><template #default="{ row }"><StatusTag :code="row.appeal_status" type="appeal_status" /></template></el-table-column>
        <el-table-column label="证据" width="80">
          <template #default="{ row }"><el-button link type="primary" @click="peek(row)">查看</el-button></template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="['已处理','已驳回'].includes(row.status)" @click="open(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
      </div>
    </div>

    <el-drawer v-model="drawer" title="举报处理" size="520px">
      <el-descriptions :column="1" border v-if="current.id">
        <el-descriptions-item label="举报编号">{{ current.report_no }}</el-descriptions-item>
        <el-descriptions-item label="关联对象">{{ current.biz_title || current.biz_type + ' #' + current.biz_id }}</el-descriptions-item>
        <el-descriptions-item label="举报类型">{{ current.reason_type }}</el-descriptions-item>
        <el-descriptions-item label="问题描述">{{ current.description }}</el-descriptions-item>
        <el-descriptions-item label="证据图片">
          <div class="ev"><img v-for="(p, i) in splitImages(current.evidence_urls)" :key="i" :src="p" alt="" /></div>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="96px" style="margin-top: 16px">
        <el-form-item label="处理结果" required>
          <el-input v-model="form.result" type="textarea" :rows="3" placeholder="不少于 5 字，例如：核实后下架商品并对卖家扣 10 分" />
        </el-form-item>
        <el-form-item label="处置">
          <el-checkbox v-model="form.freezeGoods">冻结商品</el-checkbox>
          <el-checkbox v-model="form.limitUser">限制账号</el-checkbox>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="form.targetStatus" style="width: 180px">
            <el-option label="处理中" value="处理中" />
            <el-option label="已处理" value="已处理" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" @click="submit">提交处理</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { splitImages } from '../utils/format'
import { DICT } from '../dict'

/** 举报受理与处理：处理结果必填，可同时冻结商品或限制账号 */
const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const current = ref({})
const query = reactive({ status: '', reason_type: '' })
const form = reactive({ result: '', targetStatus: '已处理', freezeGoods: false, limitUser: false })

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    Object.keys(query).forEach((k) => query[k] && (params[k] = query[k]))
    const data = await http.get('/report/page', { params })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open(row) {
  current.value = row
  form.result = ''
  form.targetStatus = '已处理'
  form.freezeGoods = false
  form.limitUser = false
  drawer.value = true
}

const peek = (row) => {
  current.value = row
  drawer.value = true
}

async function submit() {
  if (form.result.trim().length < 5) {
    ElMessage.warning('处理结果需填写不少于 5 字')
    return
  }
  await http.post(`/report/${current.value.id}/handle`, form)
  ElMessage.success('处理完成')
  drawer.value = false
  load()
}

onMounted(() => load())
</script>

<style scoped>
.ev { display: flex; gap: 6px; flex-wrap: wrap; }
.ev img { width: 72px; height: 54px; object-fit: cover; border-radius: 6px; }
</style>
""")

add("views/Reviews.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>评价管理</h2><p>双盲机制：双方都提交后才互相可见；违规评价可隐藏</p></div></div>
    <div class="filter">
      <div class="fi"><label>状态</label>
        <el-select v-model="status" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option v-for="(v, k) in DICT.review_status" :key="k" :label="v[0]" :value="k" />
        </el-select>
      </div>
      <div class="sp"></div><el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无评价记录">
        <el-table-column prop="order_id" label="订单ID" width="100" />
        <el-table-column prop="from_user_id" label="评价人" width="100" />
        <el-table-column prop="to_user_id" label="被评人" width="100" />
        <el-table-column label="评分" width="120">
          <template #default="{ row }"><span class="stars">{{ '★'.repeat(row.score) }}</span></template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="140" />
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :code="row.status" type="review_status" /></template></el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="setStatus(row, '已展示')">展示</el-button>
            <el-button link type="danger" @click="setStatus(row, '已隐藏')">隐藏</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { DICT } from '../dict'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const status = ref('')
const loading = ref(false)

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/report/review/page', { params: { page: page.value, size: 10, status: status.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function setStatus(row, s) {
  await http.put(`/report/review/${row.id}/status`, null, { params: { status: s } })
  ElMessage.success('已更新')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.stars { color: var(--a); font-size: 12px; }
</style>
""")

# ================================================================ 信用 / 定价记录
add("views/CreditLogs.vue", r"""
<template>
  <CrudPage
    title="信用分流水"
    desc="初始 100，好评 +2、差评 -5、放鸽子 -10、违规下架 -15，上下限 0–150；人工调整需填写原因"
    api="credit"
    entity="信用流水"
    :columns="columns"
    :filters="filters"
    :form-fields="[]"
    :can-create="false"
    :exportable="true"
  >
    <template #row-actions>
      <el-button link type="primary" @click="adjust">调整信用分</el-button>
    </template>
  </CrudPage>
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'

const columns = [
  { key: 'user_id', label: '用户ID', type: 'num', min: 90 },
  { key: 'change_value', label: '变动', type: 'num', min: 80 },
  { key: 'before_score', label: '变动前', type: 'num', min: 90 },
  { key: 'after_score', label: '变动后', type: 'num', min: 90 },
  { key: 'reason_type', label: '原因', dict: 'credit_reason', min: 120 },
  { key: 'biz_no', label: '关联单号', min: 160 },
  { key: 'remark', label: '说明', min: 220 },
  { key: 'create_time', label: '时间', type: 'time', min: 160 }
]
const filters = [
  { key: 'user_id', label: '用户ID' },
  { key: 'reason_type', label: '原因', type: 'select', dict: 'credit_reason' }
]

async function adjust() {
  const { value } = await ElMessageBox.prompt('按格式填写：用户ID,变动值,原因（如 7,-10,多次放鸽子核实）', '人工调整信用分')
  const parts = String(value || '').split(',')
  if (parts.length < 3) {
    ElMessage.warning('格式不正确')
    return
  }
  await http.post('/credit/adjust', { userId: Number(parts[0]), changeValue: Number(parts[1]), remark: parts.slice(2).join(',').trim() })
  ElMessage.success('已调整并写入流水')
}
</script>
""")

add("views/PriceCalcs.vue", r"""
<template>
  <div>
    <div class="page-h">
      <div><h2>定价评分记录</h2><p>共 {{ total }} 次计算 ｜ 每次保留输入快照、六项得分、规则版本与人工修正痕迹</p></div>
      <div class="sp"></div>
      <el-button @click="$router.push('/admin/rules')">规则配置</el-button>
    </div>
    <div class="filter">
      <div class="fi"><label>等级</label>
        <el-select v-model="grade" clearable placeholder="全部" style="width: 160px" @change="load(1)">
          <el-option label="A 定价合理" value="A" /><el-option label="B 略偏高" value="B" /><el-option label="C 偏离市场" value="C" />
        </el-select>
      </div>
      <div class="sp"></div><el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无评分记录">
        <el-table-column prop="goods_id" label="商品ID" width="90" />
        <el-table-column prop="rule_version" label="规则版本" width="100" />
        <el-table-column label="总分" width="90"><template #default="{ row }"><b class="num">{{ row.total_score }}</b></template></el-table-column>
        <el-table-column label="等级" width="90"><template #default="{ row }"><StatusTag :code="row.grade" type="price_grade" /></template></el-table-column>
        <el-table-column label="建议价区间" min-width="150">
          <template #default="{ row }"><span class="num">¥{{ row.suggest_low }} ~ ¥{{ row.suggest_high }}</span></template>
        </el-table-column>
        <el-table-column prop="factors" label="影响因素" min-width="240" show-overflow-tooltip />
        <el-table-column label="人工修正" width="110">
          <template #default="{ row }"><span v-if="row.corrected_grade">→ {{ row.corrected_grade }}</span><span v-else class="muted">—</span></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="detail(row)">详情</el-button>
            <el-button link type="warning" @click="correct(row)">修正</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
      </div>
    </div>

    <el-drawer v-model="drawer" title="评分详情（原始结果保留）" size="560px">
      <el-descriptions :column="2" border v-if="current.id">
        <el-descriptions-item label="商品ID">{{ current.goods_id }}</el-descriptions-item>
        <el-descriptions-item label="规则版本">{{ current.rule_version }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ current.total_score }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ current.grade }}</el-descriptions-item>
        <el-descriptions-item label="建议价区间" :span="2">¥{{ current.suggest_low }} ~ ¥{{ current.suggest_high }}</el-descriptions-item>
        <el-descriptions-item label="结论" :span="2">{{ current.conclusion }}</el-descriptions-item>
        <el-descriptions-item label="未满足条件" :span="2">{{ current.missing || '无' }}</el-descriptions-item>
        <el-descriptions-item label="人工修正" :span="2">
          <span v-if="current.corrected_grade">{{ current.grade }} → {{ current.corrected_grade }}，原因：{{ current.correct_reason }}</span>
          <span v-else>未修正</span>
        </el-descriptions-item>
      </el-descriptions>
      <h3 style="margin: 18px 0 8px">六项得分</h3>
      <el-table :data="scoreRows" size="small">
        <el-table-column prop="name" label="因素" />
        <el-table-column prop="score" label="得分" width="90" />
      </el-table>
      <h3 style="margin: 18px 0 8px">输入快照</h3>
      <pre class="snap">{{ pretty(current.snapshot) }}</pre>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

/** 定价评分记录：展示原始结果与人工修正留痕，可查看输入快照 */
const rows = ref([])
const total = ref(0)
const page = ref(1)
const grade = ref('')
const loading = ref(false)
const drawer = ref(false)
const current = ref({})

const scoreRows = computed(() => {
  const c = current.value
  return [
    { name: '同分类成交价偏离度', score: c.score_category },
    { name: '商品成色', score: c.score_condition },
    { name: '同校同类在售数量', score: c.score_supply },
    { name: '发布时长与浏览转化', score: c.score_heat },
    { name: '卖家信用分', score: c.score_credit },
    { name: '图文完整度', score: c.score_info }
  ]
})

const pretty = (s) => {
  try { return JSON.stringify(JSON.parse(s || '{}'), null, 2) } catch (e) { return s || '{}' }
}

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/price/calc/page', { params: { page: page.value, size: 10, grade: grade.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const detail = (row) => { current.value = row; drawer.value = true }

async function correct(row) {
  const { value } = await ElMessageBox.prompt('修正等级（A/B/C），原因不少于 5 字，格式如：A,开学季刚需，实际成交高于模型', '人工修正', { inputValue: 'B,人工复核后调整' })
  const [g, ...rest] = String(value || '').split(',')
  await http.post(`/price/calc/${row.id}/correct`, { grade: g.trim(), reason: rest.join(',').trim() })
  ElMessage.success('已记录修正，原始结果保留')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.stars { color: var(--a); }
.muted { color: var(--ink2); }
.snap { background: #FAFBFB; border: 1px solid var(--line); border-radius: 8px; padding: 10px; font-size: 12px; max-height: 220px; overflow: auto; }
</style>
""")

# ================================================================ 分析 / 分类 / 校区
add("views/Analysis.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>运营统计</h2><p>按分类、校区、成色对比供给与成交，识别供大于求的分类</p></div></div>
    <div class="grid g2">
      <div class="card"><h3>各校区成交量</h3><Chart :option="campusOption" :height="220" /></div>
      <div class="card"><h3>举报类型占比</h3><Chart :option="reportOption" :height="220" /></div>
    </div>
    <div class="card" style="margin-top: 16px">
      <h3>分类供给与成交</h3>
      <el-table :data="byCategory" empty-text="暂无数据">
        <el-table-column prop="name" label="一级分类" min-width="140" />
        <el-table-column prop="onSale" label="在售" width="100" />
        <el-table-column prop="sold" label="已成交" width="100" />
        <el-table-column label="成交率" width="120"><template #default="{ row }">{{ row.rate }}%</template></el-table-column>
        <el-table-column label="判断" min-width="200">
          <template #default="{ row }">
            <span v-if="row.onSale >= 3 && row.onSale > row.sold" class="tag warn">供大于求，建议专题清理</span>
            <span v-else class="tag ok">供需正常</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card">
      <h3>成色与成交价</h3>
      <el-table :data="byCondition" empty-text="暂无数据">
        <el-table-column prop="name" label="成色" min-width="120" />
        <el-table-column prop="onSale" label="在售" width="100" />
        <el-table-column prop="sold" label="已成交" width="100" />
        <el-table-column label="成交均价" width="140"><template #default="{ row }">{{ fmtMoney(row.avgPrice) }}</template></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../http'
import Chart from '../components/Chart.vue'
import { fmtMoney } from '../utils/format'

const data = ref({})
const byCategory = computed(() => data.value.byCategory || [])
const byCondition = computed(() => data.value.byCondition || [])

const campusOption = computed(() => ({
  tooltip: {},
  grid: { left: 60, right: 16, top: 12, bottom: 24 },
  xAxis: { type: 'value', splitLine: { lineStyle: { color: '#EEF1F2' } } },
  yAxis: { type: 'category', data: (data.value.byCampus || []).map((x) => x.name) },
  series: [{ type: 'bar', barWidth: 16, data: (data.value.byCampus || []).map((x) => x.value) }]
}))
const reportOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { fontSize: 11 } },
  series: [{ type: 'pie', radius: ['40%', '64%'], center: ['50%', '44%'], label: { fontSize: 11 }, data: data.value.reportTypes || [] }]
}))

onMounted(async () => {
  data.value = await http.get('/dashboard/analysis')
})
</script>
""")

add("views/Categories.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>分类管理</h2><p>两级分类；删除前校验是否被商品引用，停用分类不影响历史商品展示</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">新建分类</el-button></div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" row-key="id" empty-text="暂无分类">
        <el-table-column label="分类名称" min-width="220">
          <template #default="{ row }"><span :class="{ child: row.parent_id !== 0 }">{{ row.parent_id === 0 ? '' : '└ ' }}{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="category_code" label="编码" min-width="150" />
        <el-table-column label="层级" width="90"><template #default="{ row }">{{ row.parent_id === 0 ? '一级' : '二级' }}</template></el-table-column>
        <el-table-column prop="sort_no" label="排序" width="80" />
        <el-table-column label="热门" width="90"><template #default="{ row }"><span v-if="row.is_hot" class="tag warn">热门</span><span v-else class="muted">—</span></template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :code="row.status" type="enable_status" /></template></el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" :current-page="page" @current-change="load" />
      </div>
    </div>
    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '新建') + '分类'" size="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="上级分类">
          <el-select v-model="form.parent_id" style="width: 100%">
            <el-option label="一级分类" :value="0" />
            <el-option v-for="c in roots" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类编码"><el-input v-model="form.category_code" placeholder="大写字母与下划线，如 ELEC_PHONE" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sort_no" :min="0" /></el-form-item>
        <el-form-item label="首页热门"><el-switch v-model="form.is_hot" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const form = reactive({ parent_id: 0, name: '', category_code: '', sort_no: 10, is_hot: 0, status: 1, remark: '' })
const roots = computed(() => rows.value.filter((r) => r.parent_id === 0))

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/category/page', { params: { page: page.value, size: 50 } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open(row) {
  Object.assign(form, row || { parent_id: 0, name: '', category_code: '', sort_no: 10, is_hot: 0, status: 1, remark: '' })
  drawer.value = true
}

async function submit() {
  await http.post('/category', form)
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？被商品引用的分类不允许删除`, '删除确认', { type: 'warning' })
  await http.delete(`/category/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.child { color: var(--ink2); padding-left: 10px; }
.muted { color: var(--ink2); }
</style>
""")

add("views/Campus.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>校区面交点</h2><p>停用的面交点不能在新订单中选择；联系人电话仅运营可见，前台脱敏</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">新建面交点</el-button></div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无面交点">
        <el-table-column prop="campus_name" label="校区" min-width="100" />
        <el-table-column prop="place_name" label="面交点" min-width="160" />
        <el-table-column prop="address_desc" label="位置说明" min-width="240" show-overflow-tooltip />
        <el-table-column label="开放时段" min-width="150">
          <template #default="{ row }"><span class="num">{{ row.open_start }}–{{ row.open_end }}</span></template>
        </el-table-column>
        <el-table-column prop="manager_name" label="联系人" width="100" />
        <el-table-column prop="manager_phone" label="电话" min-width="130" />
        <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :code="row.status" type="enable_status" /></template></el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
      </div>
    </div>
    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '新建') + '面交点'" size="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="校区">
          <el-select v-model="form.campus_id" style="width: 100%" @change="onCampus">
            <el-option label="东校区" :value="1" /><el-option label="西校区" :value="2" /><el-option label="北校区" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="面交点名称"><el-input v-model="form.place_name" /></el-form-item>
        <el-form-item label="位置说明"><el-input v-model="form.address_desc" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="开放时段">
          <el-time-select v-model="form.open_start" start="06:00" step="00:30" end="23:00" style="width: 130px" />
          <span style="margin: 0 8px">–</span>
          <el-time-select v-model="form.open_end" start="06:00" step="00:30" end="23:30" style="width: 130px" />
        </el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.manager_name" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.manager_phone" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const form = reactive({ campus_id: 1, campus_name: '东校区', place_name: '', address_desc: '', open_start: '08:00', open_end: '21:00', manager_name: '', manager_phone: '', status: 1 })

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/campus-place/page', { params: { page: page.value, size: 10 } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onCampus = (v) => { form.campus_name = { 1: '东校区', 2: '西校区', 3: '北校区' }[v] }

function open(row) {
  Object.assign(form, row || { campus_id: 1, campus_name: '东校区', place_name: '', address_desc: '', open_start: '08:00', open_end: '21:00', manager_name: '', manager_phone: '', status: 1 })
  drawer.value = true
}

async function submit() {
  await http.post('/campus-place', { ...form, open_start: form.open_start + ':00', open_end: form.open_end + ':00' })
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除面交点「${row.place_name}」？`, '删除确认', { type: 'warning' })
  await http.delete(`/campus-place/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(() => load())
</script>
""")

# ================================================================ 规则配置
add("views/Rules.vue", r"""
<template>
  <div>
    <div class="page-h">
      <div><h2>定价规则配置</h2><p>六项权重合计必须等于 100%；保存即生成新版本并立即生效，历史计算不受影响</p></div>
      <div class="sp"></div><el-button type="primary" @click="save">保存并生成新版本</el-button>
    </div>
    <div class="split">
      <div class="card">
        <h3>当前权重（合计 {{ sum }}%）</h3>
        <el-form :model="form" label-width="160px">
          <el-form-item v-for="f in fields" :key="f.key" :label="f.label">
            <el-slider v-model="form[f.key]" :min="0" :max="60" :step="5" show-input style="max-width: 420px" />
          </el-form-item>
          <el-form-item label="变更说明">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="说明本次调整原因，便于版本追溯" />
          </el-form-item>
        </el-form>
        <div class="sum" :class="{ bad: sum !== 100 }">权重合计 {{ sum }}% {{ sum === 100 ? '（校验通过）' : '（必须等于 100%）' }}</div>
      </div>
      <div class="card">
        <h3>版本历史</h3>
        <el-table :data="rules" empty-text="暂无规则版本">
          <el-table-column prop="rule_version" label="版本" width="80" />
          <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :code="row.status" type="rule_status" /></template></el-table-column>
          <el-table-column prop="effect_time" label="生效时间" min-width="160" />
          <el-table-column prop="remark" label="说明" min-width="180" show-overflow-tooltip />
        </el-table>
        <p class="muted" style="margin-top: 12px">等级阈值：A ≥ 80 ｜ B 60–79 ｜ C &lt; 60。修改权重只影响新计算或主动重算的数据。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

/** 规则配置页（创新点要求 1）：权重合计校验 + 版本号与生效时间留痕 */
const fields = [
  { key: 'weightCategory', label: '同分类成交价偏离度' },
  { key: 'weightCondition', label: '商品成色' },
  { key: 'weightSupply', label: '同校同类在售数量' },
  { key: 'weightHeat', label: '发布时长与浏览转化' },
  { key: 'weightCredit', label: '卖家信用分' },
  { key: 'weightInfo', label: '图文完整度' }
]
const form = reactive({ ruleName: '商品定价成交概率评分规则', weightCategory: 30, weightCondition: 20, weightSupply: 15, weightHeat: 15, weightCredit: 10, weightInfo: 10, remark: '' })
const rules = ref([])
const sum = computed(() => fields.reduce((s, f) => s + Number(form[f.key] || 0), 0))

async function load() {
  rules.value = await http.get('/price/rules')
  const active = rules.value.find((r) => r.status === '启用')
  if (active) {
    Object.assign(form, {
      weightCategory: active.weight_category, weightCondition: active.weight_condition,
      weightSupply: active.weight_supply, weightHeat: active.weight_heat,
      weightCredit: active.weight_credit, weightInfo: active.weight_info
    })
  }
}

async function save() {
  if (sum.value !== 100) {
    ElMessage.warning('六项权重合计必须等于 100%')
    return
  }
  const r = await http.post('/price/rules', form)
  ElMessage.success('已生成新版本 ' + r.version + ' 并立即生效')
  load()
}

onMounted(load)
</script>

<style scoped>
.sum { font-size: 13px; color: var(--ok); font-weight: 600; }
.sum.bad { color: var(--bad); }
.muted { color: var(--ink2); font-size: 12.5px; }
</style>
""")

# ================================================================ 系统管理
add("views/Notices.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>公告管理</h2><p>后台发布、门户展示；支持草稿与下线</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">发布公告</el-button></div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无公告">
        <el-table-column prop="title" label="标题" min-width="300" />
        <el-table-column prop="notice_type" label="类型" width="110" />
        <el-table-column prop="publisher" label="发布人" width="110" />
        <el-table-column prop="publish_time" label="发布时间" min-width="160" />
        <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :code="row.status" type="notice_status" /></template></el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="open(row)">编辑</el-button></template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" /></div>
    </div>
    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '发布') + '公告'" size="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.notice_type" style="width: 100%">
            <el-option label="平台公告" value="平台公告" /><el-option label="交易提醒" value="交易提醒" /><el-option label="规则变更" value="规则变更" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="已发布" value="已发布" /><el-option label="草稿" value="草稿" /><el-option label="已下线" value="已下线" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const form = reactive({ title: '', notice_type: '平台公告', content: '', status: '已发布' })

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/system/notices', { params: { page: page.value, size: 10 } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open(row) {
  Object.assign(form, row || { title: '', notice_type: '平台公告', content: '', status: '已发布' })
  drawer.value = true
}

async function submit() {
  if (form.id) {
    await http.put(`/system/notices/${form.id}`, form)
  } else {
    await http.post('/system/notices', form)
  }
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

onMounted(() => load())
</script>
""")

add("views/Users.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>用户管理</h2><p>学生账号通过学号导入并绑定校区；密码字段不下发前端</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">新增用户</el-button></div>
    <div class="filter">
      <div class="fi"><label>关键字</label><el-input v-model="keyword" placeholder="登录名/姓名/学号" clearable style="width: 190px" @keyup.enter="load(1)" /></div>
      <div class="fi"><label>角色</label>
        <el-select v-model="roleCode" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option label="平台管理员" value="ROLE_ADMIN" /><el-option label="校园运营人员" value="ROLE_OPERATOR" />
          <el-option label="学生卖家" value="ROLE_SELLER" /><el-option label="学生买家" value="ROLE_BUYER" />
        </el-select>
      </div>
      <div class="sp"></div><el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无用户">
        <el-table-column prop="username" label="登录名" min-width="120" />
        <el-table-column prop="real_name" label="姓名" min-width="110" />
        <el-table-column prop="student_no" label="学号" min-width="130" />
        <el-table-column prop="campus_name" label="校区" width="100" />
        <el-table-column prop="credit_score" label="信用分" width="90" />
        <el-table-column prop="role_name" label="角色" min-width="120" />
        <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :code="row.status" type="enable_status" /></template></el-table-column>
        <el-table-column prop="last_login" label="最近登录" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">编辑</el-button>
            <el-button link type="warning" @click="reset(row)">重置密码</el-button>
            <el-button link type="danger" @click="toggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" /></div>
    </div>
    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '新增') + '用户'" size="520px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="登录名" required><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名" required><el-input v-model="form.real_name" /></el-form-item>
        <el-form-item label="学号"><el-input v-model="form.student_no" /></el-form-item>
        <el-form-item label="校区">
          <el-select v-model="form.campus_name" style="width: 100%" @change="(v) => (form.campus_id = { 东校区: 1, 西校区: 2, 北校区: 3 }[v])">
            <el-option label="东校区" value="东校区" /><el-option label="西校区" value="西校区" /><el-option label="北校区" value="北校区" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role_code" style="width: 100%">
            <el-option label="平台管理员" value="ROLE_ADMIN" /><el-option label="校园运营人员" value="ROLE_OPERATOR" />
            <el-option label="学生卖家" value="ROLE_SELLER" /><el-option label="学生买家" value="ROLE_BUYER" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id"><el-input v-model="form.password" placeholder="默认 123456" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const keyword = ref('')
const roleCode = ref('')
const form = reactive({ username: '', real_name: '', student_no: '', campus_id: 1, campus_name: '东校区', phone: '', role_code: 'ROLE_BUYER', password: '123456', status: 1 })

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/system/users', { params: { page: page.value, size: 10, keyword: keyword.value || undefined, role_code: roleCode.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open(row) {
  Object.assign(form, row || { username: '', real_name: '', student_no: '', campus_id: 1, campus_name: '东校区', phone: '', role_code: 'ROLE_BUYER', password: '123456', status: 1 })
  drawer.value = true
}

async function submit() {
  await http.post('/system/users', form)
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

async function reset(row) {
  await ElMessageBox.confirm(`将「${row.real_name}」密码重置为 123456？`, '重置密码', { type: 'warning' })
  await http.put(`/system/users/${row.id}/reset`)
  ElMessage.success('已重置')
}

async function toggle(row) {
  await http.put(`/system/users/${row.id}/status`, null, { params: { status: row.status === 1 ? 0 : 1 } })
  ElMessage.success('状态已更新')
  load()
}

onMounted(() => load())
</script>
""")

add("views/Roles.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>角色管理</h2><p>菜单权限 + 按钮权限 + 数据范围三层控制；分配菜单时「菜单管理」项默认隐藏</p></div></div>
    <div class="card">
      <el-table :data="rows" empty-text="暂无角色">
        <el-table-column prop="role_name" label="角色名称" min-width="140" />
        <el-table-column prop="role_code" label="编码" min-width="150" />
        <el-table-column label="数据范围" width="120">
          <template #default="{ row }">{{ { all: '全部', dept: '本部门', self: '本人' }[row.data_scope] || row.data_scope }}</template>
        </el-table-column>
        <el-table-column prop="menu_count" label="菜单数" width="100" />
        <el-table-column prop="user_count" label="用户数" width="100" />
        <el-table-column prop="remark" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="assign(row)">分配菜单</el-button></template>
        </el-table-column>
      </el-table>
    </div>
    <el-drawer v-model="drawer" :title="'为「' + (current.role_name || '') + '」分配菜单'" size="480px">
      <el-tree ref="treeRef" :data="tree" show-checkbox node-key="id" :props="{ label: 'menu_name', children: 'children' }" default-expand-all />
      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" @click="save">保存授权</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'

const rows = ref([])
const tree = ref([])
const drawer = ref(false)
const current = ref({})
const treeRef = ref(null)

async function load() {
  rows.value = await http.get('/system/roles')
  tree.value = await http.get('/system/menus')
}

async function assign(row) {
  current.value = row
  drawer.value = true
  const ids = await http.get(`/system/roles/${row.id}/menus`)
  setTimeout(() => treeRef.value && treeRef.value.setCheckedKeys(ids), 60)
}

async function save() {
  const checked = treeRef.value.getCheckedKeys()
  const half = treeRef.value.getHalfCheckedKeys()
  await http.put(`/system/roles/${current.value.id}/menus`, [...checked, ...half])
  ElMessage.success('授权已保存，重新登录后生效')
  drawer.value = false
  load()
}

onMounted(load)
</script>
""")

add("views/Menus.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>菜单管理</h2><p>目录 / 菜单 / 按钮三级；按钮权限用于控制行内操作</p></div></div>
    <div class="card">
      <el-table :data="rows" row-key="id" default-expand-all empty-text="暂无菜单">
        <el-table-column prop="menu_name" label="菜单名称" min-width="240" />
        <el-table-column prop="menu_type" label="类型" width="100" />
        <el-table-column prop="path" label="路由" min-width="180" />
        <el-table-column prop="perm" label="权限标识" min-width="180" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="可见" width="80"><template #default="{ row }">{{ row.visible ? '显示' : '隐藏' }}</template></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../http'

const rows = ref([])
onMounted(async () => {
  rows.value = await http.get('/system/menus')
})
</script>
""")

add("views/Dicts.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>字典管理</h2><p>状态与类型枚举统一维护，前端下拉统一从字典接口取值</p></div></div>
    <div class="split">
      <div class="card">
        <h3>字典类型</h3>
        <el-table :data="types" empty-text="暂无字典类型" highlight-current-row @row-click="pick">
          <el-table-column prop="dict_name" label="字典名称" min-width="140" />
          <el-table-column prop="dict_type" label="类型编码" min-width="160" />
          <el-table-column prop="remark" label="说明" min-width="200" show-overflow-tooltip />
        </el-table>
      </div>
      <div class="card">
        <h3>字典项：{{ current.dict_type || '请选择左侧类型' }}</h3>
        <el-table :data="data" empty-text="暂无字典项">
          <el-table-column prop="dict_label" label="标签" min-width="140" />
          <el-table-column prop="dict_value" label="值" min-width="120" />
          <el-table-column prop="sort" label="排序" width="80" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../http'

const types = ref([])
const data = ref([])
const current = ref({})

async function pick(row) {
  current.value = row
  data.value = await http.get('/system/dicts', { params: { type: row.dict_type } })
}

onMounted(async () => {
  types.value = await http.get('/system/dicts/types')
  if (types.value.length) pick(types.value[0])
})
</script>
""")

add("views/Logs.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>操作日志</h2><p>审核、冻结、信用调整、人工改价等关键写操作留痕；日志不可由普通管理员删除</p></div></div>
    <div class="filter">
      <div class="fi"><label>模块</label><el-input v-model="module" placeholder="如 商品审核" clearable style="width: 180px" @keyup.enter="load(1)" /></div>
      <div class="fi"><label>操作人</label><el-input v-model="operName" placeholder="操作人姓名" clearable style="width: 180px" @keyup.enter="load(1)" /></div>
      <div class="sp"></div><el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无日志">
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="oper_type" label="操作" width="120" />
        <el-table-column prop="biz_no" label="业务编号" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip />
        <el-table-column prop="oper_name" label="操作人" width="110" />
        <el-table-column prop="oper_time" label="时间" min-width="160" />
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" /></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../http'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const module = ref('')
const operName = ref('')

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/system/logs', { params: { page: page.value, size: 10, module: module.value || undefined, oper_name: operName.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => load())
</script>
""")

add("views/Messages.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>消息中心</h2><p>审核、订单、纠纷、信用四类提醒；未读 {{ rows.filter((r) => r.read_flag === 0).length }} 条</p></div>
      <div class="sp"></div><el-button @click="readAll">全部标为已读</el-button></div>
    <div class="card">
      <el-table :data="rows" empty-text="暂无消息">
        <el-table-column prop="msg_type" label="类型" width="110" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="360" show-overflow-tooltip />
        <el-table-column prop="create_time" label="时间" min-width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><span :class="row.read_flag === 0 ? 'tag bad' : 'tag gray'">{{ row.read_flag === 0 ? '未读' : '已读' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button link type="primary" :disabled="row.read_flag === 1" @click="read(row)">标为已读</el-button></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../http'

const rows = ref([])

async function load() {
  rows.value = await http.get('/system/messages')
}
async function read(row) {
  await http.put(`/system/messages/${row.id}/read`)
  load()
}
async function readAll() {
  await http.put('/system/messages/read-all')
  load()
}

onMounted(load)
</script>
""")

add("views/Profile.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>个人信息</h2><p>改资料、改密码、上传头像，头像上传后右上角同步显示</p></div></div>
    <div class="split">
      <div class="card">
        <h3>基本资料</h3>
        <el-form :model="form" label-width="110px">
          <el-form-item label="头像"><UploadBox v-model="form.avatar" biz-type="avatar" :multiple="false" :max="1" /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.realName" style="max-width: 320px" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" style="max-width: 320px" /></el-form-item>
          <el-form-item label="所属校区"><el-input :model-value="store.user?.campusName" disabled style="max-width: 320px" /></el-form-item>
          <el-form-item label="角色">{{ store.user?.roleName }} · 数据范围 {{ { all: '全部', dept: '本部门', self: '本人' }[store.user?.dataScope] }}</el-form-item>
          <el-button type="primary" @click="saveProfile">保存修改</el-button>
        </el-form>
      </div>
      <div class="card">
        <h3>修改密码</h3>
        <el-form :model="pwd" label-width="110px">
          <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password style="max-width: 320px" /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password style="max-width: 320px" /></el-form-item>
          <el-form-item label="确认新密码"><el-input v-model="pwd.confirm" type="password" show-password style="max-width: 320px" /></el-form-item>
          <el-button type="primary" @click="savePassword">更新密码</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import { store } from '../store'
import UploadBox from '../components/UploadBox.vue'

const form = reactive({ realName: '', phone: '', avatar: '' })
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

async function load() {
  const data = await http.get('/auth/me')
  store.setAuth({ user: data.user, menus: data.menus })
  form.realName = data.user.realName
  form.phone = data.user.phone
  form.avatar = data.user.avatar
}

async function saveProfile() {
  await http.put('/auth/profile', form)
  ElMessage.success('资料已更新')
  load()
}

async function savePassword() {
  if (pwd.newPassword !== pwd.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  await http.put('/auth/password', { oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
  ElMessage.success('密码已更新')
  pwd.oldPassword = pwd.newPassword = pwd.confirm = ''
}

onMounted(load)
</script>
""")

for name, content in V.items():
    path = os.path.join(SRC, name.replace("/", os.sep))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

print("admin views:", len(V))
