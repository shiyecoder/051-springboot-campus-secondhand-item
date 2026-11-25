<template>
  <div>
    <div class="page-h"><div><h2>回单复核</h2><p>客服复核签收数量、差异与凭证清晰度，不合格可退回补充</p></div><div class="sp"></div>
      <el-button @click="load">刷新</el-button></div>
    <div class="filter">
      <div class="fi"><label>关键字</label><el-input v-model="q.keyword" placeholder="回单编号/运单编号" style="width: 190px" clearable /></div>
      <div class="fi"><label>状态</label>
        <el-select v-model="q.status" clearable placeholder="全部" style="width: 180px">
          <el-option v-for="(v, k) in DICT.receipt_status" :key="k" :value="k" :label="v[0]" />
        </el-select></div>
      <div class="sp"></div>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无回单">
        <el-table-column prop="receipt_no" label="回单编号" min-width="140" />
        <el-table-column label="运单编号" min-width="140">
          <template #default="{ row }"><router-link class="mono" :to="'/orders/' + row.order_id">{{ row.order_no }}</router-link></template>
        </el-table-column>
        <el-table-column prop="uploader" label="上传人" min-width="88" />
        <el-table-column prop="file_count" label="凭证数" min-width="78" />
        <el-table-column label="状态" min-width="96"><template #default="{ row }"><StatusTag :code="row.status" type="receipt_status" /></template></el-table-column>
        <el-table-column label="纸质回单" min-width="100"><template #default="{ row }"><StatusTag :code="row.paper_status" type="paper_status" /></template></el-table-column>
        <el-table-column label="上传时间" min-width="140"><template #default="{ row }"><span class="num">{{ fmtTime(row.upload_time) }}</span></template></el-table-column>
        <el-table-column prop="reject_reason" label="退回原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openCheck(row)">复核</el-button>
            <el-button v-if="row.status !== 'REJECTED'" link type="danger" @click="reject(row)">退回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="(p) => { page = p; load() }" /></div>
    </div>

    <el-drawer v-model="drawer" :title="'回单复核 · ' + (cur.receipt_no || '')" size="640px">
      <div class="note" style="margin-bottom: 12px">运单 {{ cur.order_no }} · 上传人 {{ cur.uploader }} · {{ cur.file_count }} 张凭证</div>
      <div style="display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 14px">
        <img v-for="(img, i) in imgs" :key="i" :src="img" style="width: 150px; height: 108px; object-fit: cover; border-radius: 6px; border: 1px solid #e7dfdc" alt="" />
      </div>
      <el-form label-width="96px">
        <el-form-item label="复核结论">
          <el-radio-group v-model="checkOk">
            <el-radio :label="true">凭证清晰、数量无差异</el-radio>
            <el-radio :label="false">凭证模糊 / 数量有差异</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="纸质回单">
          <el-select v-model="paper" style="width: 240px">
            <el-option v-for="(v, k) in DICT.paper_status" :key="k" :value="k" :label="v[0]" />
          </el-select>
        </el-form-item>
        <el-form-item label="存放位置"><el-input v-model="location" placeholder="如 A-2026-11" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" @click="submit">提交复核</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { DICT } from '../dict'
import { fmtTime } from '../utils/format'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const cur = ref({})
const checkOk = ref(true)
const paper = ref('NONE')
const location = ref('')
const q = reactive({ keyword: '', status: '' })

const imgs = computed(() => (cur.value.images || '').split(',').filter(Boolean))

async function load() {
  loading.value = true
  try {
    const data = await http.get('/receipt/page', { params: { page: page.value, size: 10, ...q } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openCheck(row) {
  cur.value = row
  checkOk.value = true
  paper.value = row.paper_status || 'NONE'
  location.value = row.archive_location || ''
  drawer.value = true
}

async function submit() {
  await http.put(`/receipt/${cur.value.id}`, {
    status: checkOk.value ? 'CHECKED' : 'REJECTED',
    reject_reason: checkOk.value ? null : '凭证模糊或数量有差异，需补充',
    paper_status: paper.value,
    archive_location: location.value,
    check_by: '客服复核人'
  })
  if (!checkOk.value) {
    // 退回后运单回到「到达签收」，客户重新上传
    await http.post(`/order/${cur.value.order_id}/transition`, { target: 'ARRIVED', reason: '回单凭证不合格，退回补充' }).catch(() => {})
  }
  ElMessage.success(checkOk.value ? '复核通过' : '已退回补充')
  drawer.value = false
  load()
}

async function reject(row) {
  const r = await ElMessageBox.prompt('请填写退回原因（不少于 5 个字）', '退回补充', { inputPlaceholder: '照片模糊，需重拍' }).catch(() => null)
  if (!r) return
  await http.put(`/receipt/${row.id}`, { status: 'REJECTED', reject_reason: r.value })
  ElMessage.success('已退回')
  load()
}

onMounted(load)
</script>
