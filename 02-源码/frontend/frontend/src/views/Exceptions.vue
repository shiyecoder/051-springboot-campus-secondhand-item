<template>
  <div>
    <div class="page-h"><div><h2>异常事件</h2><p>严重货损或延误需车队经理审核处理结果后关闭</p></div><div class="sp"></div>
      <el-button @click="load">刷新</el-button></div>
    <div class="filter">
      <div class="fi"><label>关键字</label><el-input v-model="q.keyword" placeholder="异常编号/运单编号" style="width: 190px" clearable /></div>
      <div class="fi"><label>类型</label>
        <el-select v-model="q.type" clearable placeholder="全部" style="width: 180px">
          <el-option v-for="t in types" :key="t" :value="t" :label="t" />
        </el-select></div>
      <div class="fi"><label>状态</label>
        <el-select v-model="q.status" clearable placeholder="全部" style="width: 180px">
          <el-option v-for="(v, k) in DICT.exception_status" :key="k" :value="k" :label="v[0]" />
        </el-select></div>
      <div class="sp"></div>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无异常记录">
        <el-table-column prop="exception_no" label="异常编号" min-width="140" />
        <el-table-column label="运单编号" min-width="140">
          <template #default="{ row }"><router-link class="mono" :to="'/orders/' + row.order_id">{{ row.order_no }}</router-link></template>
        </el-table-column>
        <el-table-column prop="type" label="类型" min-width="110" />
        <el-table-column label="程度" min-width="84"><template #default="{ row }"><StatusTag :code="row.level" type="exception_level" /></template></el-table-column>
        <el-table-column prop="description" label="简要描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" min-width="96"><template #default="{ row }"><StatusTag :code="row.status" type="exception_status" /></template></el-table-column>
        
        <el-table-column label="登记时间" min-width="140"><template #default="{ row }"><span class="num">{{ fmtTime(row.create_time) }}</span></template></el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openFollow(row)">跟进</el-button>
            <el-button v-if="row.status === 'PENDING_AUDIT'" link type="danger" @click="audit(row)">审核关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="(p) => { page = p; load() }" /></div>
    </div>

    <el-drawer v-model="drawer" :title="'异常跟进 · ' + (cur.exception_no || '')" size="520px">
      <el-form label-width="96px">
        <el-form-item label="处理过程"><el-input v-model="form.process_note" type="textarea" :rows="4" placeholder="记录处理过程与结果" /></el-form-item>
        <el-form-item label="客户沟通"><el-input v-model="form.customer_note" type="textarea" :rows="3" placeholder="记录与客户的沟通内容" /></el-form-item>
        <el-form-item label="沟通方式">
          <el-select v-model="form.way" style="width: 100%">
            <el-option v-for="w in ['电话', '微信', '现场', '邮件']" :key="w" :value="w" :label="w" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="mut" style="font-size: 12px">严重货损或延误需车队经理审核后关闭，财务/客服可跟进但不能直接关闭。</div>
      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" @click="saveFollow">保存跟进记录</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
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
const form = reactive({ process_note: '', customer_note: '', way: '电话' })
const types = ['堵车/交通管制', '车辆故障', '货损货差', '拒收', '地址变更', '延误']
const q = reactive({ keyword: '', type: '', status: '' })

async function load() {
  loading.value = true
  try {
    const data = await http.get('/exception/page', { params: { page: page.value, size: 10, ...q } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openFollow(row) {
  cur.value = row
  form.process_note = row.process_note || ''
  form.customer_note = row.customer_note || ''
  drawer.value = true
}

async function saveFollow() {
  await http.put(`/exception/${cur.value.id}`, {
    process_note: form.process_note,
    customer_note: form.customer_note,
    status: cur.value.status === 'OPEN' ? 'PROCESSING' : cur.value.status
  })
  await http.post(`/comm`, { exception_id: cur.value.id, exception_no: cur.value.exception_no, way: form.way, content: form.customer_note || '跟进处理', operator: '当前用户', comm_time: new Date().toISOString().slice(0, 19).replace('T', ' ') })
  ElMessage.success('跟进记录已保存')
  drawer.value = false
  load()
}

async function audit(row) {
  const r = await ElMessageBox.prompt('请填写审核意见（不少于 5 个字）', '经理审核关闭', { inputPlaceholder: '处理结果属实，同意关闭' }).catch(() => null)
  if (!r) return
  await http.put(`/exception/${row.id}`, { status: 'CLOSED', audit_note: r.value, audit_by: '车队经理' })
  ElMessage.success('已关闭并留痕')
  load()
}

onMounted(load)
</script>
