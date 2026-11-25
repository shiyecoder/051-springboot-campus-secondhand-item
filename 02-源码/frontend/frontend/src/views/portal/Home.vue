<template>
  <div>
    <div class="hero">
      <div>
        <h1>{{ store.user?.orgName }}</h1>
        <p>在途 {{ stat.transit }} 单 · 待签收 {{ stat.arrived }} 单 · 累计运费 ¥ {{ fmtMoney(stat.fee) }}</p>
      </div>
      <div class="sp"></div>
      <router-link to="/portal/demand"><el-button>提交运输需求</el-button></router-link>
      <router-link to="/portal/orders"><el-button>查看全部运单</el-button></router-link>
    </div>

    <div class="kpis" style="grid-template-columns: repeat(4, 1fr); margin-top: 18px">
      <div class="kpi"><span>在途运单</span><b>{{ stat.transit }}</b><em>实时节点可查</em></div>
      <div class="kpi"><span>待签收</span><b>{{ stat.arrived }}</b><em>请确认数量与凭证</em></div>
      <div class="kpi"><span>已完成</span><b>{{ stat.done }}</b><em>可评价服务</em></div>
      <div class="kpi"><span>累计运费</span><b>¥ {{ fmtMoney(stat.fee) }}</b><em>按运单应收汇总</em></div>
    </div>

    <div class="card" style="margin-top: 16px">
      <div class="card-h"><h3>最近运单进度</h3><div class="sp"></div>
        <router-link to="/portal/orders" style="font-size: 12px">全部运单</router-link></div>
      <div v-for="o in latest" :key="o.id" class="order-card">
        <div class="oc-head">
          <b class="mono">{{ o.order_no }}</b>
          <StatusTag :code="o.status" type="order_status" />
          <span class="mut">{{ o.from_city }} → {{ o.to_city }} · {{ o.goods_name }} {{ o.weight }} 吨</span>
          <div class="sp"></div>
          <span class="mut" style="font-size: 12px">要求送达 {{ fmtTime(o.require_arrive_time) }}</span>
        </div>
        <div class="steps">
          <div v-for="(s, i) in STEPS" :key="s" class="s" :class="stepClass(o.status, i)">{{ s }}</div>
        </div>
      </div>
      <el-empty v-if="!latest.length" description="暂无运单，先去提交运输需求" :image-size="70" />
    </div>

    <div class="grid2" style="margin-top: 16px">
      <div class="card">
        <div class="card-h"><h3>接下来可以做什么</h3></div>
        <router-link v-for="a in actions" :key="a.to + a.text" class="act" :to="a.to">
          <span class="dot" :class="a.type"></span>
          <span class="txt">{{ a.text }}</span>
          <span class="cnt num">{{ a.count }}</span>
          <span class="arw">→</span>
        </router-link>
      </div>
      <div class="card">
        <div class="card-h"><h3>运输服务标准</h3><div class="sp"></div>
          <span class="mut" style="font-size: 11.5px">合同约定条款摘要</span></div>
        <div class="promise" v-for="p in PROMISE" :key="p.t">
          <b>{{ p.t }}</b><span>{{ p.d }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import { store } from '../../store'
import { fmtMoney, fmtTime } from '../../utils/format'

const STEPS = ['已受理', '调度配车', '装货发车', '在途运输', '到达签收', '费用核对']
// 在途运输单独成一步，否则 SHIPPING 会显示成“装货发车”，与状态标签对不上
const MAP = { CREATED: 0, DISPATCHED: 1, LOADED: 2, SHIPPING: 3, ARRIVED: 4, SIGNED: 4, FEE_CHECK: 5, DONE: 5 }
const latest = ref([])
const stat = reactive({ transit: 0, arrived: 0, done: 0, fee: 0 })
const PROMISE = [
  { t: '准点承诺', d: '按约定时间窗送达，超时未达 2 小时内主动反馈原因与预计到达时间' },
  { t: '全程可视', d: '装货、发车、在途、到达四个节点实时上报，异常事件单独登记闭环' },
  { t: '回单管理', d: '签收后 3 个工作日内上传纸质回单照片并完成复核归档' },
  { t: '结算口径', d: '按运单费用明细对账，月结客户次月 5 日出账，账期以合同为准' }
]

const stepClass = (status, i) => {
  const cur = MAP[status] ?? 0
  return i < cur ? 'done' : i === cur ? 'cur' : ''
}

const actions = computed(() => [
  { text: '待签收运单（确认数量与凭证）', count: stat.arrived, to: '/portal/sign', type: stat.arrived ? 'hot' : '' },
  { text: '待评价运单（服务完成后打分）', count: stat.done, to: '/portal/review', type: '' },
  { text: '提交新的运输需求', count: '', to: '/portal/demand', type: '' },
  { text: '查看全部运单与在途节点', count: stat.transit, to: '/portal/orders', type: '' }
])

onMounted(async () => {
  const data = await http.get('/order/page', { params: { page: 1, size: 5 } })
  latest.value = data.records || []
  const all = await http.get('/order/page', { params: { page: 1, size: 100 } })
  const rows = all.records || []
  stat.transit = rows.filter((r) => ['LOADED', 'SHIPPING'].includes(r.status)).length
  stat.arrived = rows.filter((r) => r.status === 'ARRIVED').length
  stat.done = rows.filter((r) => r.status === 'DONE').length
  stat.fee = rows.reduce((a, b) => a + Number(b.receivable || 0), 0)
})
</script>

<style scoped>
.hero { background: linear-gradient(120deg, #5b2533, #7a3947 65%, #8c4a56); color: #fff; border-radius: 10px; padding: 26px 30px; display: flex; align-items: center; gap: 16px; }
.hero h1 { font-size: 21px; margin: 0; font-weight: 600; letter-spacing: .5px; }
.hero p { opacity: .85; font-size: 12.5px; margin: 6px 0 0; }
.hero .sp { flex: 1; }
.hero :deep(.el-button) { background: rgba(255, 255, 255, .14); border-color: rgba(255, 255, 255, .4); color: #fff; }
.order-card { border: 1px solid var(--line); border-radius: 8px; padding: 16px; margin-bottom: 12px; }
.oc-head { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.oc-head .sp { flex: 1; }
.steps { display: flex; margin-top: 14px; }
.steps .s { flex: 1; text-align: center; font-size: 11.5px; color: var(--ink2); position: relative; }
.steps .s:before { content: ''; width: 9px; height: 9px; border-radius: 50%; background: #e2d6d3; display: block; margin: 0 auto 6px; }
.steps .s.done { color: var(--ok); }
.steps .s.done:before { background: var(--ok); }
.steps .s.cur { color: var(--p); font-weight: 600; }
.steps .s.cur:before { background: var(--p); box-shadow: 0 0 0 3px #efdde0; }
.act { display: flex; align-items: center; gap: 10px; padding: 11px 4px; border-bottom: 1px dashed var(--line); color: var(--ink); font-size: 13px; }
.act:last-child { border-bottom: 0; }
.act .dot { width: 6px; height: 6px; border-radius: 50%; background: #cbb9b6; }
.act .dot.hot { background: var(--p); }
.act .txt { flex: 1; }
.act .cnt { color: var(--p); font-weight: 600; }
.act .arw { color: #b9a7a4; }
.promise { padding: 9px 0; border-bottom: 1px dashed var(--line); font-size: 12.5px; }
.promise:last-child { border-bottom: 0; }
.promise b { display: block; font-weight: 600; margin-bottom: 3px; }
.promise span { color: var(--ink2); line-height: 1.7; }
</style>
