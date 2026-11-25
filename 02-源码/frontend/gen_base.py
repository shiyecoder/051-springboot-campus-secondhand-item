#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""NEW-051 前端生成器：Vue3 + Vite + Element Plus。

统一封装 http / dict / store / 通用 CrudPage / 上传组件，页面只写配置或用例，
避免每个页面重复实现分页与表单（需求 12 节「前端公共能力统一封装」）。
"""
import os

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(BASE, "src")
for d in ("views", "views/portal", "layout", "components", "utils"):
    os.makedirs(os.path.join(SRC, d), exist_ok=True)

F = {}


def add(path, content):
    F[path] = content.strip() + "\n"


add("main.js", r"""
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles.css'

createApp(App).use(router).use(ElementPlus, { locale: zhCn }).mount('#app')
""")

add("App.vue", r"""
<template>
  <router-view />
</template>
""")

add("http.js", r"""
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from './router'

/** 统一 HTTP 客户端：自动带 JWT，按 {code,message,data} 解包；code=0 为成功 */
const http = axios.create({ baseURL: '/api', timeout: 30000 })

/**
 * 后端 Jackson 统一开启 SNAKE_CASE 输出与入参解析，
 * 所以前端可以全用驼峰写代码，请求发出前在这里统一转成下划线，
 * 避免出现「表单填了 dealPrice 后端收不到」这类字段名对不上的问题。
 */
const toSnakeKey = (k) => k.replace(/[A-Z]/g, (c) => '_' + c.toLowerCase())

function toSnakeDeep(v) {
  if (Array.isArray(v)) return v.map(toSnakeDeep)
  if (v && typeof v === 'object' && !(v instanceof FormData) && !(v instanceof Date) && !(v instanceof Blob)) {
    const out = {}
    Object.keys(v).forEach((key) => { out[toSnakeKey(key)] = toSnakeDeep(v[key]) })
    return out
  }
  return v
}

http.interceptors.request.use((cfg) => {
  const t = localStorage.getItem('xy_token')
  if (t) cfg.headers.Authorization = 'Bearer ' + t
  if (cfg.data && typeof cfg.data === 'object') cfg.data = toSnakeDeep(cfg.data)
  return cfg
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) return body.data
      if (body.code === 4010) {
        localStorage.removeItem('xy_token')
        localStorage.removeItem('xy_user')
        localStorage.removeItem('xy_menus')
        router.push('/login')
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message))
    }
    return body
  },
  (err) => {
    ElMessage.error(err?.response?.data?.message || '网络异常，请稍后重试')
    return Promise.reject(err)
  }
)

export default http
""")

add("store.js", r"""
import { reactive } from 'vue'

/** 轻量全局状态：登录用户、菜单与 token（刷新后从 localStorage 恢复） */
export const store = reactive({
  token: localStorage.getItem('xy_token') || '',
  user: JSON.parse(localStorage.getItem('xy_user') || 'null'),
  menus: JSON.parse(localStorage.getItem('xy_menus') || '[]'),
  setAuth(data) {
    this.token = data.token || this.token
    this.user = data.user || this.user
    this.menus = data.menus || this.menus
    if (data.token) localStorage.setItem('xy_token', data.token)
    if (data.user) localStorage.setItem('xy_user', JSON.stringify(data.user))
    if (data.menus) localStorage.setItem('xy_menus', JSON.stringify(data.menus))
  },
  setUser(u) {
    this.user = u
    if (u) localStorage.setItem('xy_user', JSON.stringify(u))
  },
  clear() {
    this.token = ''
    this.user = null
    this.menus = []
    localStorage.removeItem('xy_token')
    localStorage.removeItem('xy_user')
    localStorage.removeItem('xy_menus')
  }
})

/** 角色落点：学生进门户，运营与管理员进后台 */
export const roleHome = (role) => {
  if (role === 'ROLE_SELLER' || role === 'ROLE_BUYER') return '/portal'
  return '/dashboard/index'
}
""")

add("dict.js", r"""
/**
 * 状态字典：颜色语义与设计系统一致（ok/run/warn/bad/gray）。
 * 状态不能只靠颜色表达，必须同时显示中文标签（需求 10.2）。
 */
export const DICT = {
  goods_status: {
    待审核: ['待审核', 'warn'],
    在售: ['在售', 'ok'],
    已下架: ['已下架', 'gray'],
    已售出: ['已售出', 'run'],
    审核驳回: ['审核驳回', 'bad'],
    冻结: ['冻结', 'bad']
  },
  order_status: {
    待卖家确认: ['待卖家确认', 'warn'],
    待面交: ['待面交', 'run'],
    已完成: ['已完成', 'ok'],
    已取消: ['已取消', 'gray'],
    纠纷中: ['纠纷中', 'bad']
  },
  condition_level: {
    全新: ['全新', 'ok'],
    九成新: ['九成新', 'ok'],
    七成新: ['七成新', 'run'],
    五成新: ['五成新', 'warn'],
    有瑕疵: ['有瑕疵', 'bad']
  },
  trade_type: { 面交: ['面交', 'ok'], 自提: ['自提', 'run'], 可送: ['可送', 'warn'] },
  pay_type: { 现金: ['现金', 'ok'], 转账: ['转账', 'run'], 其他: ['其他', 'gray'] },
  report_reason: {
    违禁品: ['违禁品', 'bad'], 虚假描述: ['虚假描述', 'warn'], 诈骗: ['诈骗', 'bad'],
    骚扰: ['骚扰', 'warn'], 校外人员: ['校外人员', 'bad']
  },
  report_status: {
    待受理: ['待受理', 'warn'], 处理中: ['处理中', 'run'], 已处理: ['已处理', 'ok'],
    已驳回: ['已驳回', 'gray'], 申诉中: ['申诉中', 'bad']
  },
  review_status: { 待审核: ['待审核', 'warn'], 已展示: ['已展示', 'ok'], 已隐藏: ['已隐藏', 'gray'] },
  credit_reason: {
    交易好评: ['交易好评', 'ok'], 中评: ['中评', 'run'], 差评: ['差评', 'bad'],
    放鸽子: ['放鸽子', 'bad'], 违规下架: ['违规下架', 'bad'], 人工调整: ['人工调整', 'warn']
  },
  price_grade: { A: ['A 定价合理', 'ok'], B: ['B 略偏高', 'warn'], C: ['C 偏离市场', 'bad'] },
  appeal_status: { 无: ['无', 'gray'], 申诉中: ['申诉中', 'warn'], 申诉通过: ['申诉通过', 'ok'], 申诉驳回: ['申诉驳回', 'bad'] },
  rule_status: { 启用: ['启用', 'ok'], 停用: ['停用', 'gray'], 草稿: ['草稿', 'warn'] },
  notice_status: { 已发布: ['已发布', 'ok'], 草稿: ['草稿', 'warn'], 已下线: ['已下线', 'gray'] },
  enable_status: { 1: ['启用', 'ok'], 0: ['停用', 'gray'] }
}

export const label = (type, code) => (DICT[type] && DICT[type][code] ? DICT[type][code][0] : code ?? '-')
export const tone = (type, code) => (DICT[type] && DICT[type][code] ? DICT[type][code][1] : 'gray')
""")

add("utils/format.js", r"""
/** 全局时间/金额格式化：全系统统一调用，禁止各页面自行拼接 */
export function fmtTime(v) {
  if (!v) return '-'
  const s = String(v).replace('T', ' ')
  return s.length >= 19 ? s.slice(0, 19) : s
}

export function fmtDay(v) {
  if (!v) return '-'
  return String(v).slice(0, 10)
}

export function fmtMoney(v) {
  if (v === null || v === undefined || v === '') return '-'
  return '¥' + Number(v).toFixed(2)
}

/** 相对时间：用于详情页「3 小时前发布」 */
export function fromNow(v) {
  if (!v) return '-'
  const t = new Date(String(v).replace(' ', 'T')).getTime()
  const diff = (Date.now() - t) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
  return Math.floor(diff / 86400) + ' 天前'
}

/** 把逗号分隔的图片路径转数组 */
export const splitImages = (s) => (s ? String(s).split(',').filter(Boolean) : [])
""")

add("components/StatusTag.vue", r"""
<template>
  <span class="tag" :class="toneOf">{{ text }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { label, tone } from '../dict'

const props = defineProps({ code: [String, Number], type: String })
const text = computed(() => label(props.type, props.code))
const toneOf = computed(() => tone(props.type, props.code))
</script>
""")

add("components/Chart.vue", r"""
<template>
  <div ref="el" :style="{ width: '100%', height: height + 'px' }"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

/** ECharts 轻封装：统一主色序列，随窗口自适应，卸载时销毁避免内存泄漏 */
const props = defineProps({
  option: { type: Object, required: true },
  height: { type: Number, default: 220 }
})
const el = ref(null)
let chart = null

const paint = () => {
  if (chart) chart.setOption({ color: ['#146B5B', '#3E8E7E', '#D9A441', '#B9C2C6'], ...props.option }, true)
}
const resize = () => chart && chart.resize()

onMounted(() => {
  chart = echarts.init(el.value)
  paint()
  window.addEventListener('resize', resize)
})
watch(() => props.option, paint, { deep: true })
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart && chart.dispose()
})
</script>
""")

add("components/UploadBox.vue", r"""
<template>
  <div class="up">
    <div v-for="(p, i) in list" :key="p + i" class="up-item">
      <img :src="p" alt="" />
      <i class="up-del" @click="remove(i)">×</i>
    </div>
    <label class="up-add" v-if="list.length < max">
      <input type="file" accept="image/jpeg,image/png,image/webp" hidden @change="pick" />
      <span v-if="!busy">＋ 上传</span>
      <span v-else>上传中…</span>
    </label>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'

/** 图片上传组件：只回传后端相对路径，不提供手填 URL 入口（禁止外链） */
const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  max: { type: Number, default: 6 },
  multiple: { type: Boolean, default: true },
  bizType: { type: String, default: 'goods' }
})
const emit = defineEmits(['update:modelValue'])
const busy = ref(false)

const list = computed(() =>
  Array.isArray(props.modelValue)
    ? props.modelValue.filter(Boolean)
    : String(props.modelValue || '').split(',').filter(Boolean)
)

async function pick(e) {
  const file = e.target.files[0]
  if (!file) return
  busy.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('bizType', props.bizType)
    const data = await http.post('/file/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    const next = props.multiple ? [...list.value, data.file_path] : [data.file_path]
    emit('update:modelValue', props.multiple || Array.isArray(props.modelValue) ? next : data.file_path)
    ElMessage.success('上传成功')
  } finally {
    busy.value = false
    e.target.value = ''
  }
}

function remove(i) {
  const next = list.value.filter((_, idx) => idx !== i)
  emit('update:modelValue', Array.isArray(props.modelValue) ? next : next.join(','))
}
</script>

<style scoped>
.up { display: flex; flex-wrap: wrap; gap: 8px; }
.up-item { position: relative; width: 88px; height: 66px; border-radius: 8px; overflow: hidden; border: 1px solid var(--line); }
.up-item img { width: 100%; height: 100%; object-fit: cover; display: block; }
.up-del { position: absolute; top: 2px; right: 4px; color: #fff; background: rgba(31, 41, 51, .6); width: 16px; height: 16px; line-height: 14px; text-align: center; border-radius: 50%; font-style: normal; cursor: pointer; font-size: 12px; }
.up-add { width: 88px; height: 66px; border: 1px dashed var(--line); border-radius: 8px; display: grid; place-items: center; color: var(--ink2); font-size: 12px; cursor: pointer; }
</style>
""")

# ---------------------------------------------------------------- 通用列表页
add("components/CrudPage.vue", r"""
<template>
  <div>
    <div class="page-h">
      <div><h2>{{ title }}</h2><p>{{ desc }}</p></div>
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
            <span v-else-if="c.type === 'num'" class="num">{{ row[c.key] ?? '-' }}</span>
            <router-link v-else-if="c.type === 'link'" class="mono" :to="`${linkBase}/${row.id}`">{{ row[c.key] }}</router-link>
            <span v-else>{{ row[c.key] ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="actionWidth" fixed="right">
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

    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '新建') + (entity || '记录')" size="520px">
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
import { fmtMoney, fmtTime } from '../utils/format'
import { DICT } from '../dict'

/** 通用列表页：服务端分页 + 条件筛选 + 抽屉表单 + 删除确认 + 当前筛选导出 */
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
  actionWidth: { type: Number, default: 150 },
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

const dictOptions = (k) => Object.entries(DICT[k] || {}).map(([value, v]) => ({ value, label: v[0] }))
const optionsOf = (f) => (f.dict ? dictOptions(f.dict) : f.options || [])

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

const reload = (p) => {
  if (p) page.value = p
  load()
}
const reset = () => {
  Object.keys(query).forEach((k) => (query[k] = ''))
  reload(1)
}
const openForm = (row) => {
  form.value = row ? { ...row } : {}
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
  ElMessageBox.confirm('删除后不可恢复，确认删除该记录？', '删除确认', { type: 'warning', confirmButtonText: '确认删除' })
    .then(async () => {
      await http.delete(`/${props.api}/${row.id}`)
      ElMessage.success('已删除')
      load()
    })
    .catch(() => {})
}

/** 导出当前筛选（前端生成 CSV，无需后端导出接口） */
function exportCsv() {
  const head = props.columns.map((c) => c.label).join(',')
  const body = rows.value.map((r) => props.columns.map((c) => `"${r[c.key] ?? ''}"`).join(',')).join('\n')
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
""")

add("styles.css", r"""
/* NEW-051 校园清新绿 设计系统（与 design/design-system.md 一致） */
:root {
  --p: #146B5B;
  --p2: #3E8E7E;
  --a: #D9A441;
  --bg: #F5F6F7;
  --ink: #1F2933;
  --ink2: #5B6770;
  --ok: #2B7955;
  --bad: #B02A37;
  --line: #E4E7EA;
  --r: 10px;
  --shadow: 0 1px 2px rgba(31, 41, 51, .06), 0 8px 24px rgba(31, 41, 51, .06);
}
* { box-sizing: border-box; }
body { margin: 0; font: 14px/1.6 -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif; color: var(--ink); background: var(--bg); }
a { color: inherit; text-decoration: none; }
.num { font-variant-numeric: tabular-nums; }
.mono { font-family: ui-monospace, Menlo, monospace; font-size: 12.5px; color: var(--p2); }
.sp { flex: 1; }
.page-h { display: flex; align-items: flex-end; gap: 14px; margin-bottom: 16px; }
.page-h h2 { font-size: 18px; margin: 0; letter-spacing: .2px; }
.page-h p { font-size: 12.5px; color: var(--ink2); margin: 3px 0 0; }
.card { background: #fff; border: 1px solid var(--line); border-radius: var(--r); padding: 16px; box-shadow: var(--shadow); }
.card + .card { margin-top: 16px; }
.card h3 { font-size: 14px; margin: 0 0 12px; }
.filter { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; background: #fff; border: 1px solid var(--line); border-radius: var(--r); padding: 12px 14px; margin-bottom: 14px; }
.filter label { font-size: 12.5px; color: var(--ink2); margin-right: 6px; }
.filter .fi { display: flex; align-items: center; }
.thumb { width: 48px; height: 36px; object-fit: cover; border-radius: 6px; background: #EEF1F2; display: block; }
.pager { display: flex; align-items: center; gap: 10px; justify-content: flex-end; padding-top: 12px; font-size: 12.5px; color: var(--ink2); }
.tag { display: inline-flex; align-items: center; height: 22px; padding: 0 9px; border-radius: 999px; font-size: 12px; background: #EEF4F2; color: var(--p); }
.tag.ok { background: #E8F3ED; color: var(--ok); }
.tag.run { background: #E7F1EF; color: var(--p2); }
.tag.warn { background: #FBF2DF; color: #8A6118; }
.tag.bad { background: #F9E8EA; color: var(--bad); }
.tag.gray { background: #F0F2F3; color: var(--ink2); }
.grid { display: grid; gap: 14px; }
.g4 { grid-template-columns: repeat(4, 1fr); }
.g3 { grid-template-columns: repeat(3, 1fr); }
.g2 { grid-template-columns: repeat(2, 1fr); }
.kpi { background: #fff; border: 1px solid var(--line); border-radius: var(--r); padding: 14px 16px; box-shadow: var(--shadow); }
.kpi small { font-size: 12px; color: var(--ink2); }
.kpi b { display: block; font-size: 24px; margin-top: 4px; font-variant-numeric: tabular-nums; color: var(--p); }
.kpi i { font-style: normal; font-size: 11.5px; color: var(--ink2); }
.notice-bar { display: flex; gap: 10px; align-items: center; background: #FBF7EC; border: 1px solid #EFE3C6; color: #6B5218; border-radius: var(--r); padding: 10px 14px; font-size: 12.5px; margin-bottom: 16px; }
.split { display: grid; grid-template-columns: 1.6fr 1fr; gap: 16px; }
.el-button--primary { --el-button-bg-color: #146B5B; --el-button-border-color: #146B5B; --el-button-hover-bg-color: #3E8E7E; --el-button-hover-border-color: #3E8E7E; }
.el-table th.el-table__cell { background: #FAFBFB; color: var(--ink2); font-weight: 600; font-size: 12.5px; }
@media (max-width: 768px) {
  .g4 { grid-template-columns: repeat(2, 1fr); }
  .split { grid-template-columns: 1fr; }
  .page-h { flex-wrap: wrap; }
}
""")

add("router.js", r"""
import { createRouter, createWebHashHistory } from 'vue-router'
import { roleHome, store } from './store'

/**
 * 路由表：后台（运营/管理员）与门户（学生买家/卖家）两套骨架。
 * 侧边栏内容来自登录接口下发的菜单，这里只负责组件映射与标题。
 */
const routes = [
  { path: '/login', component: () => import('./views/Login.vue'), meta: { title: '登录', public: true } },
  {
    path: '/',
    component: () => import('./layout/AdminLayout.vue'),
    redirect: '/dashboard/index',
    children: [
      { path: 'dashboard/index', component: () => import('./views/Dashboard.vue'), meta: { title: '运营首页' } },
      { path: 'admin/audit', component: () => import('./views/GoodsAudit.vue'), meta: { title: '商品审核台' } },
      { path: 'admin/goods', component: () => import('./views/Goods.vue'), meta: { title: '商品管理' } },
      { path: 'admin/orders', component: () => import('./views/Orders.vue'), meta: { title: '订单管理' } },
      { path: 'admin/orders/:id', component: () => import('./views/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: 'admin/reviews', component: () => import('./views/Reviews.vue'), meta: { title: '评价管理' } },
      { path: 'admin/reports', component: () => import('./views/Reports.vue'), meta: { title: '举报与纠纷' } },
      { path: 'admin/credit', component: () => import('./views/CreditLogs.vue'), meta: { title: '信用分流水' } },
      { path: 'admin/price', component: () => import('./views/PriceCalcs.vue'), meta: { title: '定价评分记录' } },
      { path: 'admin/analysis', component: () => import('./views/Analysis.vue'), meta: { title: '运营统计' } },
      { path: 'admin/categories', component: () => import('./views/Categories.vue'), meta: { title: '分类管理' } },
      { path: 'admin/campus', component: () => import('./views/Campus.vue'), meta: { title: '校区面交点' } },
      { path: 'admin/rules', component: () => import('./views/Rules.vue'), meta: { title: '定价规则配置' } },
      { path: 'admin/notices', component: () => import('./views/Notices.vue'), meta: { title: '公告管理' } },
      { path: 'admin/users', component: () => import('./views/Users.vue'), meta: { title: '用户管理' } },
      { path: 'admin/roles', component: () => import('./views/Roles.vue'), meta: { title: '角色管理' } },
      { path: 'admin/menus', component: () => import('./views/Menus.vue'), meta: { title: '菜单管理' } },
      { path: 'admin/dicts', component: () => import('./views/Dicts.vue'), meta: { title: '字典管理' } },
      { path: 'admin/logs', component: () => import('./views/Logs.vue'), meta: { title: '操作日志' } },
      { path: 'admin/messages', component: () => import('./views/Messages.vue'), meta: { title: '消息中心' } },
      { path: 'admin/profile', component: () => import('./views/Profile.vue'), meta: { title: '个人信息' } }
    ]
  },
  {
    path: '/portal',
    component: () => import('./layout/PortalLayout.vue'),
    children: [
      { path: '', component: () => import('./views/portal/Home.vue'), meta: { title: '首页' } },
      { path: 'search', component: () => import('./views/portal/Search.vue'), meta: { title: '逛闲置' } },
      { path: 'goods/:id', component: () => import('./views/portal/GoodsDetail.vue'), meta: { title: '商品详情' } },
      { path: 'publish', component: () => import('./views/portal/Publish.vue'), meta: { title: '发布闲置' } },
      { path: 'mygoods', component: () => import('./views/portal/MyGoods.vue'), meta: { title: '我的闲置' } },
      { path: 'orders', component: () => import('./views/portal/MyOrders.vue'), meta: { title: '我的订单' } },
      { path: 'orders/:id', component: () => import('./views/portal/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: 'credit', component: () => import('./views/portal/MyCredit.vue'), meta: { title: '我的信用' } },
      { path: 'reviews', component: () => import('./views/portal/MyReviews.vue'), meta: { title: '我的评价' } },
      { path: 'messages', component: () => import('./views/portal/Messages.vue'), meta: { title: '消息' } },
      { path: 'profile', component: () => import('./views/portal/Profile.vue'), meta: { title: '个人资料' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard/index' }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to) => {
  document.title = (to.meta.title ? to.meta.title + ' · ' : '') + '拾光集'
  if (to.meta.public) return true
  if (!store.token) return { path: '/login', query: { redirect: to.fullPath } }
  const role = store.user?.roleCode
  const inPortal = to.path === '/portal' || to.path.startsWith('/portal/')
  const isStudent = role === 'ROLE_SELLER' || role === 'ROLE_BUYER'
  // 学生只能进门户，运营与管理员只能进后台，避免越权访问
  if (isStudent && !inPortal) return roleHome(role)
  if (!isStudent && inPortal) return roleHome(role)
  return true
})

export default router
""")

# ---------------------------------------------------------------- 布局
add("layout/AdminLayout.vue", r"""
<template>
  <div class="shell">
    <aside class="side">
      <div class="brand">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#E8F0EE" stroke-width="1.8">
          <path d="M4 8h16v12H4z" /><path d="M8 8V5h8v3" /><path d="M9 13h6" />
        </svg>
        <b>拾光集</b><span>SECOND HAND</span>
      </div>
      <nav class="nav">
        <template v-for="g in menus" :key="g.id">
          <div class="grp">{{ g.name }}</div>
          <router-link v-for="m in g.children || []" :key="m.id" :to="m.path" :class="{ on: isOn(m.path) }">
            <svg viewBox="0 0 24 24" fill="currentColor" v-html="iconOf(m.icon)"></svg>{{ m.name }}
          </router-link>
        </template>
      </nav>
    </aside>
    <div class="main">
      <header class="top">
        <div class="crumb">校园二手 / <b>{{ route.meta.title || '' }}</b></div>
        <div class="sp"></div>
        <router-link class="iconbtn" to="/admin/messages" title="消息中心">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M6 16V10a6 6 0 1 1 12 0v6l2 3H4z" /></svg>
          <i class="dot" v-if="unread"></i>
        </router-link>
        <el-dropdown @command="onCommand">
          <div class="who">
            <img v-if="store.user?.avatar" class="ava-img" :src="store.user.avatar" alt="" />
            <div v-else class="ava">{{ (store.user?.realName || 'U').slice(0, 1) }}</div>
            <div><small>{{ store.user?.campusName || '' }}</small><i>{{ store.user?.realName }} · {{ store.user?.roleName }}</i></div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="messages">消息中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>
      <section class="content"><router-view /></section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { store } from '../store'
import http from '../http'

/** 后台骨架：220px 侧栏 + 56px 顶栏 + 自适应内容区（设计系统第 5 节） */
const route = useRoute()
const router = useRouter()
const unread = ref(0)

const ICONS = {
  dash: '<path d="M3 13h8V3H3v10zm10 8h8V11h-8v10zM3 21h8v-6H3v6zM13 9h8V3h-8v6z"/>',
  check: '<path d="M4 12l5 5L20 6" fill="none" stroke="currentColor" stroke-width="2"/>',
  box: '<path d="M3 7l9-4 9 4v10l-9 4-9-4z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  order: '<path d="M6 2h9l5 5v15H6z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  star: '<path d="M12 3l2.8 5.9 6.2.8-4.6 4.3 1.2 6.2L12 17.4 6.4 20.2l1.2-6.2L3 9.7l6.2-.8z" fill="none" stroke="currentColor" stroke-width="1.5"/>',
  alert: '<path d="M12 3l9 17H3z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  medal: '<circle cx="12" cy="14" r="6" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M8 3l3 6M16 3l-3 6" stroke="currentColor" stroke-width="1.6"/>',
  chart: '<path d="M4 20V9M10 20V4M16 20v-7M22 20H2" fill="none" stroke="currentColor" stroke-width="1.7"/>',
  gear: '<circle cx="12" cy="12" r="3.2" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  tag: '<path d="M3 12l9-9 9 9-9 9z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  pin: '<path d="M12 22s7-6.3 7-12A7 7 0 1 0 5 10c0 5.7 7 12 7 12z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  rule: '<path d="M4 6h16M4 12h10M4 18h13" stroke="currentColor" stroke-width="1.7"/>',
  bell: '<path d="M6 16V10a6 6 0 1 1 12 0v6l2 3H4z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  user: '<circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M4 21c0-4.4 3.6-7 8-7s8 2.6 8 7" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  shield: '<path d="M12 3l8 3v6c0 5-3.4 8.2-8 9-4.6-.8-8-4-8-9V6z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  list: '<path d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" stroke-width="1.7"/>',
  book: '<path d="M4 4h7v16H4zM13 4h7v16h-7z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  doc: '<path d="M5 3h10l4 4v14H5z" fill="none" stroke="currentColor" stroke-width="1.6"/>'
}
const iconOf = (k) => ICONS[k] || ICONS.doc
const isOn = (p) => p && (route.path === p || route.path.startsWith(p + '/'))
const menus = computed(() => store.menus.filter((m) => m.type === 'catalog'))

function onCommand(c) {
  if (c === 'logout') {
    store.clear()
    router.push('/login')
  } else if (c === 'profile') {
    router.push('/admin/profile')
  } else if (c === 'messages') {
    router.push('/admin/messages')
  }
}

onMounted(async () => {
  try {
    unread.value = await http.get('/auth/unread')
  } catch (e) {
    unread.value = 0
  }
})
</script>

<style scoped>
.shell { display: flex; min-height: 100vh; }
.side { width: 220px; flex: 0 0 220px; background: var(--p); color: #EAF3F1; display: flex; flex-direction: column; position: sticky; top: 0; height: 100vh; }
.brand { height: 56px; display: flex; align-items: center; gap: 9px; padding: 0 18px; border-bottom: 1px solid rgba(255,255,255,.12); }
.brand b { font-size: 14px; letter-spacing: .5px; }
.brand span { font-size: 10px; opacity: .6; letter-spacing: 1.4px; }
.nav { padding: 10px 0; overflow: auto; flex: 1; }
.nav .grp { padding: 12px 18px 6px; font-size: 11px; opacity: .55; letter-spacing: 1px; }
.nav a { display: flex; align-items: center; gap: 9px; padding: 9px 18px; color: #DCEBE7; font-size: 13px; border-left: 3px solid transparent; }
.nav a:hover { background: rgba(255,255,255,.08); color: #fff; }
.nav a.on { background: rgba(255,255,255,.14); color: #fff; border-left-color: var(--a); font-weight: 600; }
.nav a svg { width: 15px; height: 15px; opacity: .85; flex: 0 0 15px; }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.top { height: 56px; background: #fff; border-bottom: 1px solid var(--line); display: flex; align-items: center; gap: 16px; padding: 0 24px; position: sticky; top: 0; z-index: 9; }
.crumb { font-size: 12px; color: var(--ink2); }
.crumb b { color: var(--ink); font-weight: 600; }
.iconbtn { width: 32px; height: 32px; border-radius: 6px; display: grid; place-items: center; color: var(--ink2); position: relative; }
.iconbtn:hover { background: #EAF3F1; color: var(--p); }
.dot { position: absolute; top: 5px; right: 5px; width: 6px; height: 6px; background: var(--bad); border-radius: 50%; }
.who { display: flex; align-items: center; gap: 8px; padding-left: 14px; border-left: 1px solid var(--line); cursor: pointer; }
.ava { width: 28px; height: 28px; border-radius: 50%; background: var(--p2); color: #fff; display: grid; place-items: center; font-size: 11px; }
.ava-img { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
.who small { display: block; color: var(--ink2); font-size: 11px; line-height: 1.2; }
.who i { font-style: normal; font-size: 12px; font-weight: 600; }
.content { padding: 24px; flex: 1; }
</style>
""")

add("layout/PortalLayout.vue", r"""
<template>
  <div class="pshell">
    <header class="pnav">
      <router-link class="logo" to="/portal">拾光集<span>校园闲置</span></router-link>
      <router-link v-for="n in nav" :key="n.path" class="lk" :class="{ on: isOn(n.path) }" :to="n.path">{{ n.name }}</router-link>
      <div class="sp"></div>
      <div class="search" @click="$router.push('/portal/search')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><path d="M20 20l-4-4" /></svg>
        搜索教材、数码、代步工具…
      </div>
      <el-dropdown @command="onCommand">
        <div class="who">
          <img v-if="store.user?.avatar" class="ava-img" :src="store.user.avatar" alt="" />
          <div v-else class="ava">{{ (store.user?.realName || 'U').slice(0, 1) }}</div>
          <div><small>{{ store.user?.campusName }}</small><i>信用 {{ store.user?.creditScore ?? '-' }}</i></div>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人资料</el-dropdown-item>
            <el-dropdown-item command="credit">我的信用</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </header>
    <div class="pbody"><router-view /></div>
    <footer class="foot">拾光集 · 校园二手物品交易平台 ｜ 仅限本校学生，面交验货，不接入在线支付与快递</footer>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { store } from '../store'

/** 门户骨架：64px 顶部导航 + 1280px 居中内容区，学生买家与卖家共用 */
const route = useRoute()
const router = useRouter()

const nav = [
  { name: '首页', path: '/portal' },
  { name: '逛闲置', path: '/portal/search' },
  { name: '发布闲置', path: '/portal/publish' },
  { name: '我的闲置', path: '/portal/mygoods' },
  { name: '我的订单', path: '/portal/orders' },
  { name: '我的信用', path: '/portal/credit' },
  { name: '我的评价', path: '/portal/reviews' }
]
const isOn = (p) => (p === '/portal' ? route.path === '/portal' : route.path.startsWith(p))

function onCommand(c) {
  if (c === 'logout') {
    store.clear()
    router.push('/login')
  } else if (c === 'profile') {
    router.push('/portal/profile')
  } else if (c === 'credit') {
    router.push('/portal/credit')
  }
}
</script>

<style scoped>
.pshell { min-height: 100vh; display: flex; flex-direction: column; }
.pnav { height: 64px; background: #fff; border-bottom: 1px solid var(--line); display: flex; align-items: center; gap: 20px; padding: 0 32px; position: sticky; top: 0; z-index: 9; }
.logo { font-weight: 700; color: var(--p); font-size: 16px; display: flex; align-items: center; gap: 8px; }
.logo span { font-weight: 400; font-size: 11px; color: var(--ink2); }
.lk { font-size: 14px; color: var(--ink2); padding: 6px 2px; }
.lk.on { color: var(--p); font-weight: 600; border-bottom: 2px solid var(--p); }
.search { display: flex; align-items: center; gap: 8px; background: var(--bg); border: 1px solid var(--line); border-radius: 999px; padding: 7px 14px; width: 300px; color: var(--ink2); font-size: 13px; cursor: pointer; }
.who { display: flex; align-items: center; gap: 8px; padding-left: 14px; border-left: 1px solid var(--line); cursor: pointer; }
.ava { width: 28px; height: 28px; border-radius: 50%; background: var(--p2); color: #fff; display: grid; place-items: center; font-size: 11px; }
.ava-img { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
.who small { display: block; color: var(--ink2); font-size: 11px; line-height: 1.2; }
.who i { font-style: normal; font-size: 12px; font-weight: 600; }
.pbody { padding: 24px 32px; max-width: 1280px; margin: 0 auto; width: 100%; flex: 1; }
.foot { padding: 22px 32px; color: var(--ink2); font-size: 12px; text-align: center; border-top: 1px solid var(--line); background: #fff; }
@media (max-width: 900px) {
  .pnav { height: auto; flex-wrap: wrap; gap: 10px; padding: 10px 14px; }
  .search { display: none; }
  .pbody { padding: 14px; }
}
</style>
""")

for name, content in F.items():
    path = os.path.join(SRC, name.replace("/", os.sep))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

print("base frontend files:", len(F))
