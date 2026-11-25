<template>
  <div>
    <div class="page-h"><div><h2>消息中心</h2><p>待审批、即将超时、已退回、待验收消息，含业务编号与详情跳转</p></div></div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无消息">
        <el-table-column label="类型" min-width="110">
          <template #default="{ row }"><span class="tag" :class="toneOf(row.msg_type)">{{ row.msg_type }}</span></template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="320" show-overflow-tooltip />
        <el-table-column label="业务编号" min-width="150">
          <template #default="{ row }"><router-link class="mono" :to="row.biz_path || '/'">{{ row.biz_no }}</router-link></template>
        </el-table-column>
        <el-table-column label="时间" min-width="150"><template #default="{ row }"><span class="num">{{ fmtTime(row.create_time) }}</span></template></el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }"><span class="tag" :class="row.is_read ? 'gray' : 'warn'">{{ row.is_read ? '已读' : '未读' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }"><el-button link type="primary" @click="read(row)">标记已读</el-button></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import { fmtTime } from '../utils/format'

const rows = ref([])
const loading = ref(false)
const toneOf = (t) => (t === '即将超时' || t === '已退回' ? 'bad' : t === '待审批' ? 'warn' : 'run')

async function load() {
  loading.value = true
  try {
    rows.value = await http.get('/system/messages')
  } finally {
    loading.value = false
  }
}

async function read(row) {
  await http.put(`/system/messages/${row.id}/read`)
  ElMessage.success('已读')
  load()
}

onMounted(load)
</script>
