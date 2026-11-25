<template>
  <div>
    <div class="page-h"><div><h2>我的订单</h2><p>作为买家 ｜ 作为卖家 ｜ 待面交请按约定时间到场，逾期可发起纠纷</p></div></div>
    <el-tabs v-model="side" @tab-change="load(1)">
      <el-tab-pane label="我买到的" name="buyer" />
      <el-tab-pane label="我卖出的" name="seller" />
    </el-tabs>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无订单，去逛逛闲置吧">
        <el-table-column prop="order_no" label="订单号" min-width="170" />
        <el-table-column label="商品" min-width="240">
          <template #default="{ row }">
            <div class="prod"><img class="thumb" :src="row.goods_cover" alt="" /><b>{{ row.goods_title }}</b></div>
          </template>
        </el-table-column>
        <el-table-column label="成交价" width="110"><template #default="{ row }"><span class="num">¥{{ row.deal_price }}</span></template></el-table-column>
        <el-table-column prop="appoint_time" label="面交时间" min-width="160" />
        <el-table-column label="状态" width="120"><template #default="{ row }"><StatusTag :code="row.status" type="order_status" /></template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/portal/orders/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === '待面交' && side === 'buyer'" link type="success" @click="complete(row)">确认收货</el-button>
            <el-button v-if="['待卖家确认'].includes(row.status) && side === 'seller'" link type="success" @click="confirm(row)">确认接单</el-button>
            <el-button v-if="['待卖家确认', '待面交'].includes(row.status)" link type="danger" @click="cancel(row)">取消</el-button>
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

const side = ref('buyer')
const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/trade-order/page', { params: { page: page.value, size: 10, side: side.value } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function complete(row) {
  await ElMessageBox.confirm('确认已当面验货并收到商品？', '确认收货', { type: 'warning' })
  await http.post(`/trade-order/${row.id}/complete`)
  ElMessage.success('已确认收货，去评价吧')
  load()
}

async function confirm(row) {
  const { value } = await ElMessageBox.prompt('填写实际成交价（允许低于标价）', '确认接单', { inputValue: String(row.deal_price) })
  await http.post(`/trade-order/${row.id}/confirm`, { dealPrice: Number(value), payType: row.pay_type })
  ElMessage.success('已确认接单')
  load()
}

async function cancel(row) {
  const { value } = await ElMessageBox.prompt('请填写取消原因（不少于 5 字）', '取消订单')
  await http.post(`/trade-order/${row.id}/cancel`, { reason: value })
  ElMessage.success('订单已取消')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.prod { display: flex; gap: 10px; align-items: center; }
.prod b { font-weight: 600; font-size: 13px; }
</style>
