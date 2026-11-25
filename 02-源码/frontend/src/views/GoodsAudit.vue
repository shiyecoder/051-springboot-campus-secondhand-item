<template>
  <div>
    <div class="page-h">
      <div><h2>商品审核台</h2><p>待审核 {{ rows.length }} 件 ｜ 按违禁品、虚假描述、图片合规三项检查；驳回需选原因并填写不少于 5 字说明</p></div>
    </div>
    <div class="split">
      <div class="card">
        <h3>待审核队列</h3>
        <el-table :data="rows" v-loading="loading" empty-text="暂无待审核商品" @row-click="pick" highlight-current-row>
          <el-table-column label="商品" min-width="240">
            <template #default="{ row }">
              <div class="prod">
                <img class="thumb" :src="row.cover_url" alt="" />
                <div><b>{{ row.title }}</b><small>{{ row.goods_no }} · {{ row.campus_name }}</small></div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="售价" width="100"><template #default="{ row }"><span class="num">¥{{ row.price }}</span></template></el-table-column>
          <el-table-column label="成色" width="90"><template #default="{ row }"><StatusTag :code="row.condition_level" type="condition_level" /></template></el-table-column>
        </el-table>
        <div class="pager">
          <span>共 {{ total }} 条</span>
          <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="reload" />
        </div>
      </div>

      <div class="card" v-if="current.id">
        <h3>图文对照</h3>
        <div class="gallery">
          <img v-for="(p, i) in images" :key="i" :src="p" alt="" />
        </div>
        <p class="line"><b>{{ current.title }}</b> ｜ ¥{{ current.price }}
          <span v-if="current.original_price"> ｜ 入手价 ¥{{ current.original_price }}</span></p>
        <p class="line muted">{{ current.description }}</p>
        <p class="line muted">实拍图 {{ images.length }} 张 ｜ 卖家 {{ current.seller_id }} ｜ 发布时间 {{ fmtTime(current.create_time) }}</p>
        <p class="line muted" v-if="current.audit_remark">系统提示：{{ current.audit_remark }}</p>
        <el-input v-model="reason" type="textarea" :rows="2" placeholder="审核意见（驳回必填，不少于 5 字）" style="margin-top: 12px" />
        <div class="acts">
          <el-button type="primary" @click="submit(true)">通过并上架</el-button>
          <el-button type="danger" plain @click="submit(false)">驳回</el-button>
          <el-button plain @click="submit(null, true)">冻结商品</el-button>
        </div>
      </div>
      <div class="card" v-else><h3>选择左侧一件商品查看图文对照</h3><p class="muted">审核通过后立即在门户上架，驳回会通知卖家并展示审核意见。</p></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { fmtTime, splitImages } from '../utils/format'

/** 运营审核台：左侧队列 + 右侧大图与字段对照，底部通过/驳回/冻结 */
const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const current = ref({})
const reason = ref('')
const images = computed(() => splitImages(current.value.image_urls || current.value.cover_url))

async function load() {
  loading.value = true
  try {
    const data = await http.get('/goods/page', { params: { page: page.value, size: 10, status: '待审核' } })
    rows.value = data.records || []
    total.value = data.total || 0
    if (!current.value.id && rows.value.length) pick(rows.value[0])
  } finally {
    loading.value = false
  }
}
const reload = (p) => { page.value = p; load() }
const pick = (row) => { current.value = row; reason.value = '' }

async function submit(pass, freeze = false) {
  if (pass === false && reason.value.trim().length < 5) {
    ElMessage.warning('驳回必须选择原因并填写不少于 5 字的说明')
    return
  }
  await http.post(`/goods/${current.value.id}/audit`, { pass, freeze, reason: reason.value || null })
  ElMessage.success(freeze ? '已冻结' : pass ? '已通过并上架' : '已驳回')
  current.value = {}
  load()
}

onMounted(load)
</script>

<style scoped>
.prod { display: flex; gap: 10px; align-items: center; }
.prod b { font-weight: 600; font-size: 13px; display: block; }
.prod small { color: var(--ink2); font-size: 11.5px; }
.gallery { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.gallery img { width: 100%; aspect-ratio: 4/3; object-fit: cover; border-radius: 8px; background: #EDF1F0; }
.line { font-size: 13px; margin: 10px 0 0; }
.muted { color: var(--ink2); font-size: 12.5px; }
.acts { display: flex; gap: 8px; margin-top: 14px; }
</style>
