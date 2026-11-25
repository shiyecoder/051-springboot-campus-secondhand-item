<template>
  <div>
    <div class="page-h"><div><h2>我的运单</h2><p>仅展示本单位运单</p></div><div class="sp"></div>
      <el-button type="primary" @click="$router.push('/portal/demand')">提交需求</el-button></div>
    <div class="filter">
      <div class="fi"><label>运单编号</label><el-input v-model="q.keyword" placeholder="运单编号/货物" style="width: 190px" clearable /></div>
      <div class="fi"><label>状态</label>
        <el-select v-model="q.status" clearable placeholder="全部" style="width: 180px">
          <el-option v-for="(v, k) in DICT.order_status" :key="k" :value="k" :label="v[0]" />
        </el-select></div>
      <div class="sp"></div><el-button type="primary" @click="load">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无运单">
        <el-table-column prop="order_no" label="运单编号" min-width="150" />
        <el-table-column label="线路" min-width="130"><template #default="{ row }">{{ row.from_city }} → {{ row.to_city }}</template></el-table-column>
        <el-table-column prop="goods_name" label="货物" min-width="110" />
        <el-table-column prop="weight" label="重量(t)" min-width="90" />
        <el-table-column label="发货时间" min-width="150"><template #default="{ row }"><span class="num">{{ fmtTime(row.plan_load_time) }}</span></template></el-table-column>
        <el-table-column label="状态" min-width="110"><template #default="{ row }"><StatusTag :code="row.status" type="order_status" /></template></el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-if="row.status === 'ARRIVED'" link type="primary" @click="$router.push('/portal/sign')">确认签收</el-button>
            <el-button v-else-if="row.status === 'DONE'" link @click="$router.push('/portal/review')">评价</el-button>
            <span v-else class="mut">—</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="(p) => { page = p; load() }" /></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import { DICT } from '../../dict'
import { fmtTime } from '../../utils/format'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const q = reactive({ keyword: '', status: '' })

async function load() {
  loading.value = true
  try {
    const data = await http.get('/order/page', { params: { page: page.value, size: 10, ...q } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>
