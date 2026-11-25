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
