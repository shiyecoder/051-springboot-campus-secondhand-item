<template>
  <div>
    <div class="page-h"><div><h2>费用管理</h2><p>运费、装卸费、过路费、油费、停车费与异常赔付统一台账；修改保留历史与操作人</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">登记费用</el-button></div>
    <div class="filter">
      <div class="fi"><label>关键字</label><el-input v-model="q.keyword" placeholder="费用单号/运单编号" style="width: 190px" clearable /></div>
      <div class="fi"><label>费用类型</label>
        <el-select v-model="q.fee_type" clearable placeholder="全部" style="width: 180px">
          <el-option v-for="t in feeTypes" :key="t" :value="t" :label="t" />
        </el-select></div>
      <div class="fi"><label>审核状态</label>
        <el-select v-model="q.status" clearable placeholder="全部" style="width: 180px">
          <el-option v-for="(v, k) in DICT.fee_status" :key="k" :value="k" :label="v[0]" />
        </el-select></div>
      <div class="sp"></div><el-button type="primary" @click="load">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无费用记录">
        <el-table-column prop="fee_no" label="费用单号" min-width="150" />
        <el-table-column label="运单编号" min-width="150">
          <template #default="{ row }"><router-link class="mono" :to="'/orders/' + row.order_id">{{ row.order_no }}</router-link></template>
        </el-table-column>
        <el-table-column prop="fee_type" label="类型" min-width="110" />
        <el-table-column prop="direction" label="方向" min-width="80" />
        <el-table-column label="金额(元)" min-width="120"><template #default="{ row }"><span class="num">{{ fmtMoney(row.amount) }}</span></template></el-table-column>
        <el-table-column label="状态" min-width="100"><template #default="{ row }"><StatusTag :code="row.status" type="fee_status" /></template></el-table-column>
        <el-table-column prop="submitter" label="提交人" min-width="100" />
        <el-table-column label="提交时间" min-width="150"><template #default="{ row }"><span class="num">{{ fmtTime(row.submit_time) }}</span></template></el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" link type="primary" @click="audit(row)">审核</el-button>
            <el-button v-if="row.voucher" link @click="view(row)">凭证</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="(p) => { page = p; load() }" /></div>
    </div>

    <el-drawer v-model="drawer" title="登记费用" size="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="运单编号" required><el-input v-model="form.order_no" /></el-form-item>
        <el-form-item label="费用类型" required>
          <el-select v-model="form.fee_type" style="width: 100%">
            <el-option v-for="t in feeTypes" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="方向">
          <el-select v-model="form.direction" style="width: 100%">
            <el-option value="RECV" label="应收" /><el-option value="PAY" label="应付" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" required><el-input-number v-model="form.amount" :precision="2" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="提交时间"><el-date-picker v-model="form.submit_time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="凭证"><UploadBox v-model="form.voucher" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-drawer>

    <el-dialog v-model="viewer" title="费用凭证" width="520px">
      <img v-if="voucher" :src="voucher" style="width: 100%; border-radius: 6px" alt="" />
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import UploadBox from '../components/UploadBox.vue'
import { DICT } from '../dict'
import { fmtMoney, fmtTime, nowStr } from '../utils/format'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const viewer = ref(false)
const voucher = ref('')
const q = reactive({ keyword: '', fee_type: '', status: '' })
const feeTypes = ['运费', '装卸费', '过路费', '油费', '停车费', '异常赔付']
const form = reactive({ order_no: '', fee_type: '运费', direction: 'RECV', amount: 0, submit_time: nowStr(), voucher: '', remark: '' })

async function load() {
  loading.value = true
  try {
    const data = await http.get('/fee/page', { params: { page: page.value, size: 10, ...q } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open() {
  Object.assign(form, { order_no: '', fee_type: '运费', direction: 'RECV', amount: 0, submit_time: nowStr(), voucher: '', remark: '' })
  drawer.value = true
}

async function save() {
  if (!form.order_no) {
    ElMessage.warning('请填写运单编号')
    return
  }
  await http.post('/fee', { ...form, fee_no: 'FY' + Date.now(), status: 'PENDING', submitter: '当前用户' })
  ElMessage.success('已提交待审核')
  drawer.value = false
  load()
}

function view(row) {
  voucher.value = row.voucher
  viewer.value = true
}

async function audit(row) {
  const r = await ElMessageBox.prompt('请填写审核意见（不少于 5 个字）', '费用审核', { inputPlaceholder: '凭证清晰，同意支付' }).catch(() => null)
  if (!r) return
  await http.put(`/fee/${row.id}`, { status: 'CHECKED', audit_note: r.value, audit_by: '财务审核人', audit_time: nowStr() })
  ElMessage.success('审核通过')
  load()
}

onMounted(load)
</script>
