<template>
  <div>
    <div class="page-h"><div><h2>字典管理</h2><p>状态与类型枚举统一维护，前端下拉统一从字典接口取值</p></div></div>
    <div class="split">
      <div class="card">
        <h3>字典类型</h3>
        <el-table :data="types" empty-text="暂无字典类型" highlight-current-row @row-click="pick">
          <el-table-column prop="dict_name" label="字典名称" min-width="140" />
          <el-table-column prop="dict_type" label="类型编码" min-width="160" />
          <el-table-column prop="remark" label="说明" min-width="200" show-overflow-tooltip />
        </el-table>
      </div>
      <div class="card">
        <h3>字典项：{{ current.dict_type || '请选择左侧类型' }}</h3>
        <el-table :data="data" empty-text="暂无字典项">
          <el-table-column prop="dict_label" label="标签" min-width="140" />
          <el-table-column prop="dict_value" label="值" min-width="120" />
          <el-table-column prop="sort" label="排序" width="80" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../http'

const types = ref([])
const data = ref([])
const current = ref({})

async function pick(row) {
  current.value = row
  data.value = await http.get('/system/dicts', { params: { type: row.dict_type } })
}

onMounted(async () => {
  types.value = await http.get('/system/dicts/types')
  if (types.value.length) pick(types.value[0])
})
</script>
