<template>
  <div>
    <div class="page-h"><div><h2>费用提交</h2><p>过路费、油费、停车费等代垫费用提交后由调度或财务审核</p></div></div>
    <div class="grid2">
      <div class="card">
        <div class="card-h"><h3>新增费用</h3></div>
        <el-form :model="form" label-width="96px">
          <el-form-item label="运单编号" required><el-input v-model="form.order_no" placeholder="如 WB20261101007" /></el-form-item>
          <el-form-item label="费用类型" required>
            <el-select v-model="form.fee_type" style="width: 100%">
              <el-option v-for="t in ['过路费', '油费', '停车费', '装卸费']" :key="t" :value="t" :label="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="金额(元)" required><el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
          <el-form-item label="发生时间"><el-date-picker v-model="form.submit_time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
          <el-form-item label="凭证" required><UploadBox v-model="form.voucher" text="上传凭证" /></el-form-item>
          <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        </el-form>
        <div style="text-align: right"><el-button type="primary" size="large" @click="submit">提交审核</el-button></div>
      </div>
      <div class="card">
        <div class="card-h"><h3>我的费用记录</h3></div>
        <el-table :data="rows" size="small" empty-text="暂无费用记录">
          <el-table-column prop="fee_no" label="费用单号" min-width="140" />
          <el-table-column prop="fee_type" label="类型" min-width="90" />
          <el-table-column label="金额" min-width="100"><template #default="{ row }"><span class="num">{{ fmtMoney(row.amount) }}</span></template></el-table-column>
          <el-table-column label="状态" min-width="100"><template #default="{ row }"><StatusTag :code="row.status" type="fee_status" /></template></el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import UploadBox from '../../components/UploadBox.vue'
import { fmtMoney, nowStr } from '../../utils/format'
import { store } from '../../store'

const rows = ref([])
const form = reactive({ order_no: '', fee_type: '过路费', amount: 0, submit_time: nowStr(), voucher: '', remark: '' })

async function load() {
  const data = await http.get('/fee/page', { params: { page: 1, size: 20, submitter: store.user?.realName } })
  rows.value = data.records || []
}

async function submit() {
  if (!form.order_no) {
    ElMessage.warning('请填写运单编号')
    return
  }
  if (!form.voucher) {
    ElMessage.warning('请上传费用凭证')
    return
  }
  await http.post('/fee', {
    ...form, fee_no: 'FY' + Date.now(), direction: 'PAY', status: 'PENDING',
    submitter: store.user?.realName, create_time: nowStr()
  })
  ElMessage.success('已提交，等待审核')
  form.amount = 0
  form.voucher = ''
  load()
}

onMounted(load)
</script>
