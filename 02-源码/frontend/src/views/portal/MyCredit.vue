<template>
  <div>
    <div class="page-h"><div><h2>我的信用</h2><p>信用分决定可同时在线商品数与发布权限：低于 60 限 3 件，低于 40 禁止发布 30 天</p></div></div>
    <div class="grid g4">
      <div class="kpi"><small>当前信用分</small><b>{{ store.user?.creditScore ?? '-' }}</b><i>{{ level }}</i></div>
      <div class="kpi"><small>近 30 天变动</small><b>{{ recent }}</b><i>来自流水统计</i></div>
      <div class="kpi"><small>在线商品上限</small><b>{{ (store.user?.creditScore ?? 100) < 60 ? '3 件' : '不限' }}</b><i>按当前信用分</i></div>
      <div class="kpi"><small>发布权限</small><b>{{ (store.user?.creditScore ?? 100) < 40 ? '受限' : '正常' }}</b><i>低于 40 分受限 30 天</i></div>
    </div>
    <div class="card" style="margin-top: 16px">
      <h3>信用分变动明细</h3>
      <el-table :data="rows" empty-text="暂无信用变动记录">
        <el-table-column prop="create_time" label="时间" min-width="170" />
        <el-table-column prop="reason_type" label="原因" min-width="130" />
        <el-table-column label="变动" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.change_value >= 0 ? 'var(--ok)' : 'var(--bad)' }">{{ row.change_value > 0 ? '+' : '' }}{{ row.change_value }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="after_score" label="变动后" width="100" />
        <el-table-column prop="biz_no" label="关联单号" min-width="170" />
        <el-table-column prop="remark" label="说明" min-width="220" />
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" /></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../../http'
import { store } from '../../store'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const level = computed(() => {
  const s = store.user?.creditScore ?? 100
  return s >= 120 ? '等级：优秀' : s >= 100 ? '等级：良好' : s >= 60 ? '等级：正常' : '等级：受限'
})
const recent = computed(() => rows.value.slice(0, 10).reduce((s, r) => s + (r.change_value || 0), 0))

async function load(p) {
  if (p) page.value = p
  const data = await http.get('/credit/page', { params: { page: page.value, size: 10 } })
  rows.value = data.records || []
  total.value = data.total || 0
}

onMounted(() => load())
</script>
