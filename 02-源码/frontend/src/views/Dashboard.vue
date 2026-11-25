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
        <Chart :option="funnelOption" :height="252" />
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

// 漏斗：每层单独配色并留 3px 间隙，右侧标注「数量 + 占首层百分比」，
// 否则四层同色叠在一起会看成一个梯形，读不出逐层流失
const funnelOption = computed(() => {
  const arr = data.value.funnel || []
  const base = arr[0]?.value || 1
  return {
    tooltip: { trigger: 'item', formatter: '{b}：{c}' },
    color: ['#146B5B', '#2E8574', '#4C9E8C', '#8FC0B2'],
    series: [{
      type: 'funnel', left: '4%', right: '32%', top: 4, bottom: 4,
      minSize: '30%', gap: 5,
      label: {
        position: 'right',
        fontSize: 11,
        color: '#33403C',
        formatter: (p) => `${p.name} ${p.value}  ${Math.round((p.value / base) * 100)}%`
      },
      labelLine: { length: 6, lineStyle: { color: '#C9D3D0' } },
      itemStyle: { borderWidth: 0 },
      data: arr
    }]
  }
})

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
