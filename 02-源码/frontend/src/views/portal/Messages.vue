<template>
  <div>
    <div class="page-h"><div><h2>消息</h2><p>审核结果、订单提醒、纠纷处理、信用变动都会在这里通知</p></div>
      <div class="sp"></div><el-button @click="readAll">全部标为已读</el-button></div>
    <div class="card">
      <el-table :data="rows" empty-text="暂无消息">
        <el-table-column prop="msg_type" label="类型" width="100" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="360" show-overflow-tooltip />
        <el-table-column prop="create_time" label="时间" min-width="170" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><span :class="row.read_flag === 0 ? 'tag bad' : 'tag gray'">{{ row.read_flag === 0 ? '未读' : '已读' }}</span></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../../http'

const rows = ref([])
const load = async () => { rows.value = await http.get('/system/messages') }
const readAll = async () => { await http.put('/system/messages/read-all'); load() }

onMounted(load)
</script>
