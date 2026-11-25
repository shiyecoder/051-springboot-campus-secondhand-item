<template>
  <div>
    <div class="page-h"><div><h2>分类管理</h2><p>两级分类；删除前校验是否被商品引用，停用分类不影响历史商品展示</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">新建分类</el-button></div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" row-key="id" empty-text="暂无分类">
        <el-table-column label="分类名称" min-width="220">
          <template #default="{ row }"><span :class="{ child: row.parent_id !== 0 }">{{ row.parent_id === 0 ? '' : '└ ' }}{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="category_code" label="编码" min-width="150" />
        <el-table-column label="层级" width="90"><template #default="{ row }">{{ row.parent_id === 0 ? '一级' : '二级' }}</template></el-table-column>
        <el-table-column prop="sort_no" label="排序" width="80" />
        <el-table-column label="热门" width="90"><template #default="{ row }"><span v-if="row.is_hot" class="tag warn">热门</span><span v-else class="muted">—</span></template></el-table-column>
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
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="20" :current-page="page" @current-change="load" />
      </div>
    </div>
    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '新建') + '分类'" size="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="上级分类">
          <el-select v-model="form.parent_id" style="width: 100%">
            <el-option label="一级分类" :value="0" />
            <el-option v-for="c in roots" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类编码"><el-input v-model="form.category_code" placeholder="大写字母与下划线，如 ELEC_PHONE" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sort_no" :min="0" /></el-form-item>
        <el-form-item label="首页热门"><el-switch v-model="form.is_hot" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const form = reactive({ parent_id: 0, name: '', category_code: '', sort_no: 10, is_hot: 0, status: 1, remark: '' })
const roots = computed(() => rows.value.filter((r) => r.parent_id === 0))

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/category/page', { params: { page: page.value, size: 50 } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open(row) {
  Object.assign(form, row || { parent_id: 0, name: '', category_code: '', sort_no: 10, is_hot: 0, status: 1, remark: '' })
  drawer.value = true
}

async function submit() {
  await http.post('/category', form)
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？被商品引用的分类不允许删除`, '删除确认', { type: 'warning' })
  await http.delete(`/category/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.child { color: var(--ink2); padding-left: 10px; }
.muted { color: var(--ink2); }
</style>
