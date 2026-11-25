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
