<template>
  <div>
    <div class="page-h">
      <div><h2>统计报表</h2><p>运单趋势、客户运量、车辆利用率、准时率、线路排行与费用构成</p></div>
      <div class="sp"></div>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="grid2" style="margin-bottom: 14px">
      <div class="card"><div class="card-h"><h3>月度运单量与费用</h3></div><Chart :option="monthlyOpt" :height="240" /></div>
      <div class="card"><div class="card-h"><h3>客户运量 TOP8</h3></div><Chart :option="customerOpt" :height="240" /></div>
    </div>
    <div class="grid2" style="margin-bottom: 14px">
      <div class="card"><div class="card-h"><h3>月度准时率</h3></div><Chart :option="ontimeOpt" :height="220" /></div>
      <div class="card"><div class="card-h"><h3>费用构成</h3></div><Chart :option="feeOpt" :height="220" /></div>
    </div>
    <div class="grid2" style="margin-bottom: 14px">
      <div class="card">
        <div class="card-h"><h3>创新点效果：推荐采纳率</h3><div class="sp"></div>
          <router-link to="/rules" style="font-size: 12px">规则配置</router-link></div>
        <div class="effect">
          <div><b class="num">{{ data.effect?.adopt_rate ?? 0 }}%</b><span>采纳率</span></div>
          <div><b class="num">{{ data.effect?.manual_rate ?? 0 }}%</b><span>人工调整率</span></div>
          <div><b class="num">{{ data.effect?.total ?? 0 }}</b><span>计算次数</span></div>
        </div>
        <el-table :data="data.effect?.manual_reasons || []" size="small" empty-text="暂无人工修正记录">
          <el-table-column prop="reason" label="人工修正原因" min-width="240" show-overflow-tooltip />
          <el-table-column prop="operator" label="操作人" min-width="90" />
          <el-table-column prop="time" label="时间" min-width="150" />
        </el-table>
      </div>
      <div class="card">
        <div class="card-h"><h3>创新结果分布</h3></div>
        <Chart :option="scoreOpt" :height="220" />
      </div>
    </div>
    <div class="card">
      <div class="card-h"><h3>车辆利用率明细</h3></div>
      <el-table :data="data.vehicle_usage || []" size="small">
        <el-table-column prop="plate_no" label="车牌" min-width="110" />
        <el-table-column prop="vehicle_type" label="车型" min-width="110" />
        <el-table-column prop="trips" label="本月任务" min-width="90" />
        <el-table-column prop="mileage" label="行驶里程(km)" min-width="120" />
        <el-table-column prop="usage_rate" label="利用率%" min-width="90" />
        <el-table-column prop="ontime_rate" label="准时率%" min-width="90" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../http'
import Chart from '../components/Chart.vue'
import { label } from '../dict'

const data = ref({})
const PALETTE = ['#5B2533', '#2F6F6A', '#7A3947', '#B4761E', '#9C8A86', '#C3B2AE']

const scoreOpt = computed(() => {
  const items = (data.value.score_dist || []).map((d) => ({ name: label('result_level', d.code) || d.code, value: d.cnt }))
  return {
    color: PALETTE,
    tooltip: { trigger: 'item', formatter: '{b}：{c} 次（{d}%）' },
    legend: { bottom: 0, itemWidth: 9, itemHeight: 9, textStyle: { fontSize: 11 } },
    series: [{ type: 'pie', radius: ['40%', '62%'], center: ['50%', '42%'], label: { show: false }, labelLine: { show: false }, data: items }]
  }
})

const monthlyOpt = computed(() => ({
  color: PALETTE, tooltip: { trigger: 'axis' },
  legend: { right: 0, itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 11 } },
  grid: { left: 40, right: 14, top: 30, bottom: 26 },
  xAxis: { type: 'category', data: (data.value.monthly || []).map((d) => d.month), axisLabel: { fontSize: 11 } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F1E9E6' } }, axisLabel: { fontSize: 11 } },
  series: [
    { name: '运单量', type: 'bar', barWidth: 18, data: (data.value.monthly || []).map((d) => d.orders), itemStyle: { color: '#5B2533' } },
    { name: '费用(万元)', type: 'line', smooth: true, data: (data.value.monthly || []).map((d) => d.fee), itemStyle: { color: '#2F6F6A' } }
  ]
}))

const customerOpt = computed(() => ({
  color: ['#5B2533'], grid: { left: 150, right: 24, top: 10, bottom: 20 },
  xAxis: { type: 'value', splitLine: { lineStyle: { color: '#F1E9E6' } }, axisLabel: { fontSize: 11 } },
  yAxis: { type: 'category', data: (data.value.customer_volume || []).map((d) => d.name).reverse(), axisLabel: { fontSize: 11 } },
  series: [{ type: 'bar', barWidth: 12, data: (data.value.customer_volume || []).map((d) => d.cnt).reverse(), itemStyle: { color: '#7A3947', borderRadius: [0, 3, 3, 0] } }]
}))

const ontimeOpt = computed(() => ({
  color: ['#2F6F6A'], grid: { left: 44, right: 18, top: 18, bottom: 24 },
  xAxis: { type: 'category', data: (data.value.ontime_by_month || []).map((d) => d.month), axisLabel: { fontSize: 11 } },
  yAxis: { type: 'value', max: 100, splitLine: { lineStyle: { color: '#F1E9E6' } }, axisLabel: { fontSize: 11, formatter: '{value}%' } },
  series: [{ type: 'line', smooth: true, areaStyle: { opacity: 0.12 }, data: (data.value.ontime_by_month || []).map((d) => d.rate) }]
}))

const feeOpt = computed(() => {
  const items = (data.value.fee_compose || []).map((d) => ({ name: d.name, value: d.amount }))
  return {
    color: PALETTE,
    tooltip: { trigger: 'item', valueFormatter: (v) => '¥ ' + Number(v).toLocaleString() },
    legend: { bottom: 0, itemWidth: 9, itemHeight: 9, textStyle: { fontSize: 11 } },
    series: [{ type: 'pie', radius: ['38%', '62%'], center: ['50%', '42%'], label: { show: false }, labelLine: { show: false }, data: items }]
  }
})

async function load() {
  data.value = await http.get('/dashboard/analysis')
}
onMounted(load)
</script>

<style scoped>
.effect { display: flex; gap: 28px; padding: 6px 0 14px; }
.effect b { font-size: 22px; color: var(--p); display: block; }
.effect span { font-size: 11.5px; color: var(--ink2); }
</style>
