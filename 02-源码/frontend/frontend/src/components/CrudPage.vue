<template>
  <div>
    <div class="page-h">
      <div>
        <h2>{{ title }}</h2>
        <p>{{ desc }}</p>
      </div>
      <div class="sp"></div>
      <el-button v-if="exportable" @click="exportCsv">导出当前筛选</el-button>
      <el-button v-if="canCreate" type="primary" @click="openForm()">新建{{ entity || '' }}</el-button>
    </div>

    <div class="filter">
      <div class="fi" v-for="f in filters" :key="f.key">
        <label>{{ f.label }}</label>
        <el-select v-if="f.type === 'select'" v-model="query[f.key]" clearable placeholder="全部" style="width: 180px" @change="reload(1)">
          <el-option v-for="o in optionsOf(f)" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
        <el-date-picker v-else-if="f.type === 'date'" v-model="query[f.key]" type="date" value-format="YYYY-MM-DD" style="width: 180px" @change="reload(1)" />
        <el-input v-else v-model="query[f.key]" :placeholder="f.placeholder || '请输入'" clearable style="width: 190px" @keyup.enter="reload(1)" />
      </div>
      <div class="sp"></div>
      <el-button type="primary" @click="reload(1)">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading" style="width: 100%" empty-text="当前筛选条件下无记录，清空筛选或新建数据">
        <el-table-column v-for="c in columns" :key="c.key" :prop="c.key" :label="c.label" :min-width="c.min || 110" show-overflow-tooltip>
          <template #default="{ row }">
            <StatusTag v-if="c.dict" :code="row[c.key]" :type="c.dict" />
            <img v-else-if="c.type === 'image'" class="thumb" :src="row[c.key]" alt="" />
            <span v-else-if="c.type === 'money'" class="num">{{ fmtMoney(row[c.key]) }}</span>
            <span v-else-if="c.type === 'time'" class="num">{{ fmtTime(row[c.key]) }}</span>
            <span v-else-if="c.type === 'day'" class="num">{{ fmtDay(row[c.key]) }}</span>
            <router-link v-else-if="c.type === 'link'" class="mono" :to="linkOf(row)">{{ row[c.key] }}</router-link>
            <span v-else-if="c.type === 'num'" class="num">{{ row[c.key] ?? '-' }}</span>
            <span v-else>{{ row[c.key] ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
            <slot name="row-actions" :row="row" />
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="reload" />
        <el-select v-model="size" style="width: 110px" @change="reload(1)">
          <el-option :value="10" label="10 条/页" />
          <el-option :value="20" label="20 条/页" />
          <el-option :value="50" label="50 条/页" />
        </el-select>
      </div>
    </div>

    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '新建') + (entity || '记录')" size="520px" :before-close="beforeClose">
      <el-form :model="form" label-width="112px">
        <el-form-item v-for="f in formFields" :key="f.key" :label="f.label" :required="f.required">
          <el-select v-if="f.type === 'select'" v-model="form[f.key]" style="width: 100%" clearable>
            <el-option v-for="o in optionsOf(f)" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
          <el-date-picker v-else-if="f.type === 'date'" v-model="form[f.key]" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
          <el-input v-else-if="f.type === 'textarea'" v-model="form[f.key]" type="textarea" :rows="3" />
          <el-input-number v-else-if="f.type === 'number'" v-model="form[f.key]" :min="0" :precision="2" style="width: 100%" />
          <UploadBox v-else-if="f.type === 'image'" v-model="form[f.key]" />
          <el-input v-else v-model="form[f.key]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from './StatusTag.vue'
import UploadBox from './UploadBox.vue'
import { fmtDay, fmtMoney, fmtTime } from '../utils/format'
import { DICT } from '../dict'

/**
 * 通用列表页：服务端分页 + 关键字/条件筛选 + 抽屉表单 + 删除确认。
 * 各业务页面只传配置，避免每个页面重复实现分页与表单。
 */
const props = defineProps({
  title: { type: String, required: true },
  desc: { type: String, default: '' },
  api: { type: String, required: true },
  entity: { type: String, default: '' },
  columns: { type: Array, required: true },
  filters: { type: Array, default: () => [] },
  formFields: { type: Array, default: () => [] },
  canCreate: { type: Boolean, default: true },
  exportable: { type: Boolean, default: true },
  linkBase: { type: String, default: '' },
  linkKey: { type: String, default: 'order_no' },
  scope: { type: Object, default: () => ({}) }
})

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const drawer = ref(false)
const query = reactive({})
const form = ref({})
let dirty = false

const dictOptions = (dictKey) =>
  Object.entries(DICT[dictKey] || {}).map(([value, v]) => ({ value, label: v[0] }))

const optionsOf = (f) => (f.dict ? dictOptions(f.dict) : f.options || [])

function linkOf(row) {
  const id = row.id
  return `${props.linkBase}/${id}`
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, ...props.scope }
    Object.keys(query).forEach((k) => {
      if (query[k] !== '' && query[k] !== null && query[k] !== undefined) params[k] = query[k]
    })
    const data = await http.get(`/${props.api}/page`, { params })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function reload(p) {
  if (p) page.value = p
  load()
}

function reset() {
  Object.keys(query).forEach((k) => (query[k] = ''))
  reload(1)
}

function openForm(row) {
  form.value = row ? { ...row } : {}
  dirty = false
  drawer.value = true
}

async function submit() {
  if (form.value.id) {
    await http.put(`/${props.api}/${form.value.id}`, form.value)
  } else {
    await http.post(`/${props.api}`, form.value)
  }
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

function remove(row) {
  ElMessageBox.confirm('删除后不可恢复，确认删除该记录？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    confirmButtonClass: 'el-button--danger'
  })
    .then(async () => {
      await http.delete(`/${props.api}/${row.id}`)
      ElMessage.success('已删除')
      load()
    })
    .catch(() => {})
}

function beforeClose(done) {
  if (!dirty) return done()
  ElMessageBox.confirm('有未保存的修改，确认离开？', '提示').then(() => done()).catch(() => {})
}

/** 导出当前筛选结果（前端生成 CSV，记录筛选条件） */
function exportCsv() {
  const head = props.columns.map((c) => c.label).join(',')
  const body = rows.value
    .map((r) => props.columns.map((c) => `"${r[c.key] ?? ''}"`).join(','))
    .join('\n')
  const blob = new Blob(['\ufeff' + head + '\n' + body], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${props.title}-${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  ElMessage.success('已导出当前筛选结果')
}

onMounted(load)

defineExpose({ reload, openForm })
</script>
