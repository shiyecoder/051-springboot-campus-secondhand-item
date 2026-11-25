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
