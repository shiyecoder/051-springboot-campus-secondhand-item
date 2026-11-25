<template>
  <div>
    <div class="page-h"><div><h2>消息中心</h2><p>审核、订单、纠纷、信用四类提醒；未读 {{ rows.filter((r) => r.read_flag === 0).length }} 条</p></div>
      <div class="sp"></div><el-button @click="readAll">全部标为已读</el-button></div>
    <div class="card">
      <el-table :data="rows" empty-text="暂无消息">
        <el-table-column prop="msg_type" label="类型" width="110" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="360" show-overflow-tooltip />
        <el-table-column prop="create_time" label="时间" min-width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><span :class="row.read_flag === 0 ? 'tag bad' : 'tag gray'">{{ row.read_flag === 0 ? '未读' : '已读' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }"><el-button link type="primary" :disabled="row.read_flag === 1" @click="read(row)">标为已读</el-button></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../http'

const rows = ref([])

async function load() {
  rows.value = await http.get('/system/messages')
}
async function read(row) {
  await http.put(`/system/messages/${row.id}/read`)
  load()
}
async function readAll() {
  await http.put('/system/messages/read-all')
  load()
}

onMounted(load)
</script>
