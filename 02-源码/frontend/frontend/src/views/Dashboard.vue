<template>
  <div>
    <div class="page-h">
      <div><h2>物流运营看板</h2><p>数据实时统计 · 6 个核心指标 + 6 个图表一屏可读，逐单明细见「统计报表」</p></div>
      <div class="sp"></div>
      <el-button @click="load">刷新</el-button>
      <el-button type="primary" @click="$router.push('/orders')">运单中心</el-button>
    </div>

    <div class="kpis">
      <div class="kpi" v-for="k in data.kpis" :key="k.name">
        <span>{{ k.name }}</span><b>{{ k.value }}</b><em :class="k.trend">{{ k.sub }}</em>
      </div>
    </div>

    <div class="grid2" style="margin-bottom: 14px">
      <div class="card">
        <div class="card-h"><h3>运单量与运费趋势（近 6 个月）</h3><div class="sp"></div>
          <span class="mut" style="font-size: 11.5px">点击图形回到运单列表</span></div>
        <Chart :option="trendOpt" :height="192" />
      </div>
      <div class="card">
        <div class="card-h"><h3>运单状态分布</h3></div>
        <Chart :option="statusOpt" :height="192" />
      </div>
    </div>

    <div class="grid3">
      <div class="card">
        <div class="card-h"><h3>线路运量排行</h3></div>
        <Chart :option="routeOpt" :height="172" />
      </div>
      <div class="card">
        <div class="card-h"><h3>异常类型构成</h3></div>
        <Chart :option="excOpt" :height="172" />
      </div>
      <div class="card">
        <div class="card-h"><h3>费用构成</h3></div>
        <Chart :option="feeOpt" :height="172" />
      </div>
    </div>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../http'
import Chart from '../components/Chart.vue'
import { label } from '../dict'

/** 运营看板：所有聚合由后端 /api/dashboard/overview 一次返回，前端不做全表二次聚合 */
const data = ref({ kpis: [], trend: [], status_dist: [], route_rank: [], exception_type: [], fee_compose: [], vehicle_usage: [], effect: {} })

const PALETTE = ['#5B2533', '#2F6F6A', '#7A3947', '#B4761E', '#9C8A86', '#C3B2AE']

const trendOpt = computed(() => ({
  color: PALETTE,
  tooltip: { trigger: 'axis' },
  legend: { right: 0, top: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 11 } },
  grid: { left: 38, right: 14, top: 32, bottom: 30 },
  xAxis: { type: 'category', data: (data.value.trend || []).map((d) => d.month), axisLine: { lineStyle: { color: '#E7DFDC' } }, axisLabel: { fontSize: 11 } },
  yAxis: [{ type: 'value', splitLine: { lineStyle: { color: '#F1E9E6' } }, axisLabel: { fontSize: 11 } }],
  series: [
    { name: '运单量', type: 'bar', barWidth: 16, data: (data.value.trend || []).map((d) => d.orders), itemStyle: { color: '#5B2533' } },
    { name: '运费(万元)', type: 'line', smooth: true, symbolSize: 6, data: (data.value.trend || []).map((d) => d.fee), lineStyle: { color: '#2F6F6A' }, itemStyle: { color: '#2F6F6A' } }
  ]
}))

const statusOpt = computed(() => {
  const items = (data.value.status_dist || []).map((d) => ({ name: label('order_status', d.code), value: d.cnt }))
  return {
    color: PALETTE,
    tooltip: { trigger: 'item', formatter: '{b}：{c} 单（{d}%）' },
    // 9 个状态在环上放标签必然互相压字，改成“环图 + 带数字的图例”，一眼可读
    legend: {
      bottom: 0, itemWidth: 9, itemHeight: 9, itemGap: 10, textStyle: { fontSize: 11 },
      formatter: (name) => {
        const hit = items.find((x) => x.name === name)
        return hit ? `${name} ${hit.value}` : name
      }
    },
    series: [{
      type: 'pie', radius: ['44%', '64%'], center: ['50%', '40%'],
      label: { show: false }, labelLine: { show: false },
      data: items
    }]
  }
})

const routeOpt = computed(() => ({
  color: ['#5B2533'],
  grid: { left: 96, right: 26, top: 12, bottom: 20 },
  xAxis: { type: 'value', splitLine: { lineStyle: { color: '#F1E9E6' } }, axisLabel: { fontSize: 11 } },
  // 线路名较长，给左边留够 96px 再截断，避免“杭州→上…”被裁掉半个字
  yAxis: {
    type: 'category', data: (data.value.route_rank || []).map((d) => d.name).reverse(),
    axisLabel: { fontSize: 11, color: '#4A3A3E', width: 90, overflow: 'truncate' }, axisLine: { lineStyle: { color: '#E7DFDC' } }
  },
  series: [{ type: 'bar', barWidth: 14, data: (data.value.route_rank || []).map((d) => d.cnt).reverse(), itemStyle: { color: '#5B2533', borderRadius: [0, 3, 3, 0] } }]
}))

const excOpt = computed(() => {
  const items = (data.value.exception_type || []).map((d) => ({ name: d.name, value: d.cnt }))
  return {
    color: PALETTE,
    tooltip: { trigger: 'item', formatter: '{b}：{c} 起（{d}%）' },
    // 扇区标签在窄卡片里必然互相压字：标签关掉，图例带数量
    legend: {
      bottom: 0, itemWidth: 9, itemHeight: 9, textStyle: { fontSize: 11 },
      formatter: (name) => {
        const hit = items.find((x) => x.name === name)
        return hit ? `${name} ${hit.value}` : name
      }
    },
    series: [{ type: 'pie', radius: '58%', center: ['50%', '40%'], label: { show: false }, labelLine: { show: false }, data: items }]
  }
})

const feeOpt = computed(() => {
  const items = (data.value.fee_compose || []).map((d) => ({ name: d.name, value: d.amount }))
  return {
    color: PALETTE,
    tooltip: { trigger: 'item', valueFormatter: (v) => '¥ ' + Number(v).toLocaleString() },
    legend: { bottom: 0, itemWidth: 9, itemHeight: 9, textStyle: { fontSize: 11 } },
    series: [{ type: 'pie', radius: ['38%', '62%'], center: ['50%', '40%'], label: { show: false }, labelLine: { show: false }, data: items }]
  }
})

async function load() {
  data.value = await http.get('/dashboard/overview')
}

onMounted(load)
</script>

<style scoped>
.bar { height: 7px; background: #f1e9e6; border-radius: 4px; overflow: hidden; margin-bottom: 3px; }
.bar i { display: block; height: 7px; background: #2f6f6a; border-radius: 4px; }
.effect { display: flex; gap: 28px; padding: 4px 0 12px; }
.effect b { font-size: 22px; color: var(--p); display: block; }
.effect span { font-size: 11.5px; color: var(--ink2); }
</style>
