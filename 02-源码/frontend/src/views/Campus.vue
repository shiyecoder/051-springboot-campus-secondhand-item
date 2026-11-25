<template>
  <div>
    <div class="page-h"><div><h2>校区面交点</h2><p>停用的面交点不能在新订单中选择；联系人电话仅运营可见，前台脱敏</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">新建面交点</el-button></div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无面交点">
        <el-table-column prop="campus_name" label="校区" min-width="100" />
        <el-table-column prop="place_name" label="面交点" min-width="160" />
        <el-table-column prop="address_desc" label="位置说明" min-width="240" show-overflow-tooltip />
        <el-table-column label="开放时段" min-width="150">
          <template #default="{ row }"><span class="num">{{ row.open_start }}–{{ row.open_end }}</span></template>
        </el-table-column>
        <el-table-column prop="manager_name" label="联系人" width="100" />
        <el-table-column prop="manager_phone" label="电话" min-width="130" />
        <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :code="row.status" type="enable_status" /></template></el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
      </div>
    </div>
    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '新建') + '面交点'" size="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="校区">
          <el-select v-model="form.campus_id" style="width: 100%" @change="onCampus">
            <el-option label="东校区" :value="1" /><el-option label="西校区" :value="2" /><el-option label="北校区" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="面交点名称"><el-input v-model="form.place_name" /></el-form-item>
        <el-form-item label="位置说明"><el-input v-model="form.address_desc" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="开放时段">
          <el-time-select v-model="form.open_start" start="06:00" step="00:30" end="23:00" style="width: 130px" />
          <span style="margin: 0 8px">–</span>
          <el-time-select v-model="form.open_end" start="06:00" step="00:30" end="23:30" style="width: 130px" />
        </el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.manager_name" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.manager_phone" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const form = reactive({ campus_id: 1, campus_name: '东校区', place_name: '', address_desc: '', open_start: '08:00', open_end: '21:00', manager_name: '', manager_phone: '', status: 1 })

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/campus-place/page', { params: { page: page.value, size: 10 } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onCampus = (v) => { form.campus_name = { 1: '东校区', 2: '西校区', 3: '北校区' }[v] }

function open(row) {
  Object.assign(form, row || { campus_id: 1, campus_name: '东校区', place_name: '', address_desc: '', open_start: '08:00', open_end: '21:00', manager_name: '', manager_phone: '', status: 1 })
  drawer.value = true
}

async function submit() {
  await http.post('/campus-place', { ...form, open_start: form.open_start + ':00', open_end: form.open_end + ':00' })
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除面交点「${row.place_name}」？`, '删除确认', { type: 'warning' })
  await http.delete(`/campus-place/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(() => load())
</script>
