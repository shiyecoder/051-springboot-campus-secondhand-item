<template>
  <div>
    <div class="page-h"><div><h2>我的闲置</h2><p>在售 {{ counts['在售'] || 0 }} 件 ｜ 待审核 {{ counts['待审核'] || 0 }} 件 ｜ 已售出 {{ counts['已售出'] || 0 }} 件 ｜ 信用 {{ store.user?.creditScore }}</p></div>
      <div class="sp"></div><el-button type="primary" @click="$router.push('/portal/publish')">发布闲置</el-button></div>
    <div class="filter">
      <div class="fi"><label>状态</label>
        <el-select v-model="status" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option v-for="(v, k) in DICT.goods_status" :key="k" :label="v[0]" :value="k" />
        </el-select>
      </div>
      <div class="sp"></div><el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="还没有发布闲置，点击右上角发布第一件">
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="prod">
              <img class="thumb" :src="row.cover_url" alt="" />
              <div><b>{{ row.title }}</b><small>{{ row.goods_no }} · {{ row.campus_name }}</small></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="售价" width="110"><template #default="{ row }"><span class="num">¥{{ row.price }}</span></template></el-table-column>
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :code="row.status" type="goods_status" /></template></el-table-column>
        <el-table-column prop="view_count" label="浏览" width="90" />
        <el-table-column prop="create_time" label="发布时间" min-width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/portal/goods/${row.id}`)">查看</el-button>
            <el-button v-if="row.status === '在售'" link type="warning" @click="offShelf(row)">下架</el-button>
            <el-button v-if="row.status === '审核驳回'" link type="danger" @click="showRemark(row)">原因</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" /></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import { DICT } from '../../dict'
import { store } from '../../store'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const status = ref('')
const loading = ref(false)
const counts = ref({})

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/goods/page', { params: { page: page.value, size: 10, status: status.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
    const all = await http.get('/goods/page', { params: { page: 1, size: 100 } })
    const c = {}
    for (const r of all.records || []) c[r.status] = (c[r.status] || 0) + 1
    counts.value = c
  } finally {
    loading.value = false
  }
}

async function offShelf(row) {
  const { value } = await ElMessageBox.prompt('请填写下架原因', '下架确认', { inputValue: '暂时不想卖了' })
  await http.post(`/goods/${row.id}/off-shelf`, { reason: value })
  ElMessage.success('已下架')
  load()
}

const showRemark = (row) => ElMessageBox.alert(row.audit_remark || '未填写审核意见', '驳回原因')

onMounted(() => load())
</script>

<style scoped>
.prod { display: flex; gap: 10px; align-items: center; }
.prod b { font-weight: 600; font-size: 13px; display: block; }
.prod small { color: var(--ink2); font-size: 11.5px; }
</style>
