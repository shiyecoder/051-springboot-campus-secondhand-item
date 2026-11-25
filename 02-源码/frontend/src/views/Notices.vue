<template>
  <div>
    <div class="page-h"><div><h2>公告管理</h2><p>后台发布、门户展示；支持草稿与下线</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">发布公告</el-button></div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无公告">
        <el-table-column prop="title" label="标题" min-width="300" />
        <el-table-column prop="notice_type" label="类型" width="110" />
        <el-table-column prop="publisher" label="发布人" width="110" />
        <el-table-column prop="publish_time" label="发布时间" min-width="160" />
        <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :code="row.status" type="notice_status" /></template></el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="open(row)">编辑</el-button></template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" /></div>
    </div>
    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '发布') + '公告'" size="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.notice_type" style="width: 100%">
            <el-option label="平台公告" value="平台公告" /><el-option label="交易提醒" value="交易提醒" /><el-option label="规则变更" value="规则变更" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="已发布" value="已发布" /><el-option label="草稿" value="草稿" /><el-option label="已下线" value="已下线" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const form = reactive({ title: '', notice_type: '平台公告', content: '', status: '已发布' })

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/system/notices', { params: { page: page.value, size: 10 } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open(row) {
  Object.assign(form, row || { title: '', notice_type: '平台公告', content: '', status: '已发布' })
  drawer.value = true
}

async function submit() {
  if (form.id) {
    await http.put(`/system/notices/${form.id}`, form)
  } else {
    await http.post('/system/notices', form)
  }
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

onMounted(() => load())
</script>
