<template>
  <div>
    <div class="page-h"><div><h2>签收确认</h2><p>确认实收数量并上传签收凭证，之后进入回单复核</p></div></div>
    <div class="grid2">
      <div class="card">
        <div class="card-h"><h3>待签收运单</h3></div>
        <el-select v-model="orderId" placeholder="选择待签收运单" style="width: 100%; margin-bottom: 14px">
          <el-option v-for="o in orders" :key="o.id" :value="o.id" :label="`${o.order_no} · ${o.from_city}→${o.to_city}`" />
        </el-select>
        <el-form :model="form" label-width="96px">
          <el-form-item label="应收数量"><span class="num">{{ cur?.quantity || 0 }} 件</span></el-form-item>
          <el-form-item label="实收数量" required><el-input-number v-model="form.real_qty" :min="0" style="width: 100%" /></el-form-item>
          <el-form-item label="差异说明"><el-input v-model="form.diff_note" placeholder="无差异可留空" /></el-form-item>
          <el-form-item label="签收人" required><el-input v-model="form.signer" /></el-form-item>
          <el-form-item label="签收时间"><el-date-picker v-model="form.sign_time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
          <el-form-item label="签收凭证"><UploadBox v-model="form.images" text="上传签收单" /></el-form-item>
        </el-form>
        <div style="text-align: right"><el-button type="primary" @click="submit">确认签收</el-button></div>
      </div>
      <div class="card">
        <div class="card-h"><h3>签收后流程</h3></div>
        <el-timeline>
          <el-timeline-item color="#2D765C">到达卸货：驾驶员上报到达</el-timeline-item>
          <el-timeline-item color="#5B2533">客户确认签收（当前）</el-timeline-item>
          <el-timeline-item>回单复核：客服复核凭证清晰度与数量差异</el-timeline-item>
          <el-timeline-item>费用核对：运费与代垫费用汇总进客户账单</el-timeline-item>
        </el-timeline>
        <div class="note">未完成签收复核的运单不能进入最终对账完成状态。</div>
      </div>
    </div>

    <div class="card" style="margin-top: 14px">
      <div class="card-h"><h3>历史签收记录</h3><div class="sp"></div>
        <span class="mut" style="font-size: 11.5px">已签收 {{ history.length }} 单，可核对实际到达时间与应收金额</span></div>
      <el-table :data="history" size="small" empty-text="暂无已签收记录">
        <el-table-column prop="order_no" label="运单编号" min-width="150" />
        <el-table-column label="线路" min-width="140">
          <template #default="{ row }">{{ row.from_city }} → {{ row.to_city }}</template>
        </el-table-column>
        <el-table-column prop="goods_name" label="货物" min-width="110" />
        <el-table-column label="实际到达" min-width="150">
          <template #default="{ row }"><span class="num">{{ fmtTime(row.actual_arrive_time) }}</span></template>
        </el-table-column>
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }"><StatusTag :code="row.status" type="order_status" /></template>
        </el-table-column>
        <el-table-column label="应收(元)" min-width="110">
          <template #default="{ row }"><span class="num">{{ fmtMoney(row.receivable) }}</span></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../http'
import UploadBox from '../../components/UploadBox.vue'
import StatusTag from '../../components/StatusTag.vue'
import { fmtMoney, fmtTime, nowStr } from '../../utils/format'
import { store } from '../../store'

const orders = ref([])
const history = ref([])
const orderId = ref(null)
const form = reactive({ real_qty: 0, diff_note: '', signer: store.user?.realName, sign_time: nowStr(), images: '' })
const cur = computed(() => orders.value.find((o) => o.id === orderId.value))

async function load() {
  const data = await http.get('/order/page', { params: { page: 1, size: 30, status: 'ARRIVED' } })
  orders.value = data.records || []
  if (orders.value.length) {
    orderId.value = orders.value[0].id
    form.real_qty = orders.value[0].quantity
  }
  // 签收历史：一次取本客户全部运单再按状态筛，只用于回看，不参与提交
  const all = await http.get('/order/page', { params: { page: 1, size: 100 } })
  history.value = (all.records || []).filter((o) => ['SIGNED', 'FEE_CHECK', 'DONE'].includes(o.status))
}

async function submit() {
  if (!orderId.value) {
    ElMessage.warning('请选择待签收运单')
    return
  }
  await http.post('/sign', {
    order_id: orderId.value, order_no: cur.value.order_no, plan_qty: cur.value.quantity,
    real_qty: form.real_qty, diff_note: form.diff_note, signer: form.signer,
    sign_time: form.sign_time, images: form.images, status: form.real_qty === cur.value.quantity ? 'SIGNED' : 'DISPUTE',
    create_time: nowStr()
  })
  await http.post(`/order/${orderId.value}/transition`, { target: 'SIGNED', reason: '', description: '客户确认签收' })
  ElMessage.success('签收已确认，等待客服复核回单')
  load()
}

onMounted(load)
</script>
