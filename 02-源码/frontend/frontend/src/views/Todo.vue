<template>
  <div>
    <div class="page-h"><div><h2>我的待办</h2><p>待本人处理优先、更新时间倒序</p></div><div class="sp"></div>
      <el-button @click="load">刷新</el-button></div>
    <div class="kpis" style="grid-template-columns: repeat(4, 1fr)">
      <div class="kpi" v-for="c in counts" :key="c.code"><span>{{ label('order_status', c.code) }}</span><b>{{ c.cnt }}</b></div>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="当前没有待处理任务">
        <el-table-column prop="dispatch_no" label="调度单号" min-width="150" />
        <el-table-column label="运单编号" min-width="150">
          <template #default="{ row }"><router-link class="mono" :to="'/orders/' + row.id">—</router-link>
            <router-link class="mono" :to="'/dispatch'">{{ row.order_no }}</router-link></template>
        </el-table-column>
        <el-table-column prop="customer_name" label="客户" min-width="180" show-overflow-tooltip />
        <el-table-column label="运单状态" min-width="110"><template #default="{ row }"><StatusTag :code="row.order_status" type="order_status" /></template></el-table-column>
        <el-table-column label="调度状态" min-width="100"><template #default="{ row }"><StatusTag :code="row.status" type="dispatch_status" /></template></el-table-column>
        <el-table-column prop="remark" label="进度说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="计划装货" min-width="150"><template #default="{ row }"><span class="num">{{ fmtTime(row.plan_load_time) }}</span></template></el-table-column>
        <el-table-column label="处理" width="110" fixed="right">
          <template #default="{ row }"><router-link :to="'/dispatch'"><el-button link type="primary">去处理</el-button></router-link></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { label } from '../dict'
import { fmtTime } from '../utils/format'

const rows = ref([])
const counts = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await http.get('/dashboard/todo')
    rows.value = data.todo || []
    counts.value = data.counts || []
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>
