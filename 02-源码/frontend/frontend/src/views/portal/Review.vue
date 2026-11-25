<template>
  <div>
    <div class="page-h"><div><h2>服务评价</h2><p>对已完成的运单评价时效、货损与服务态度</p></div></div>
    <div class="card" style="max-width: 760px">
      <el-form label-width="96px">
        <el-form-item label="运单">
          <el-select v-model="orderId" placeholder="选择已完成运单" style="width: 100%">
            <el-option v-for="o in orders" :key="o.id" :value="o.id" :label="`${o.order_no} · ${o.from_city}→${o.to_city}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="整体评分"><el-rate v-model="form.score" show-text :texts="['很差','较差','一般','满意','非常满意']" /></el-form-item>
        <el-form-item label="时效"><el-radio-group v-model="form.timeliness"><el-radio label="准时" /><el-radio label="略有延误" /><el-radio label="严重延误" /></el-radio-group></el-form-item>
        <el-form-item label="货损"><el-radio-group v-model="form.damage"><el-radio label="完好" /><el-radio label="轻微破损" /><el-radio label="严重破损" /></el-radio-group></el-form-item>
        <el-form-item label="评价内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div style="text-align: right"><el-button type="primary" @click="submit">提交评价</el-button></div>
    </div>

    <div class="card" style="margin-top: 14px">
      <div class="card-h"><h3>我的评价记录</h3><div class="sp"></div>
        <span class="mut" style="font-size: 11.5px">共 {{ mine.length }} 条，评分 ≤3 分自动转异常复核</span></div>
      <el-table :data="mine" size="small" empty-text="暂无评价记录，可对已完成运单打分">
        <el-table-column prop="order_no" label="运单编号" min-width="150" />
        <el-table-column label="评分" min-width="120">
          <template #default="{ row }"><el-rate :model-value="row.score" disabled size="small" /></template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="tags" label="标签" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }"><StatusTag :code="row.status" type="review_status" /></template>
        </el-table-column>
        <el-table-column label="评价时间" min-width="150">
          <template #default="{ row }"><span class="num">{{ fmtTime(row.review_time) }}</span></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import { store } from '../../store'
import { fmtTime, nowStr } from '../../utils/format'

const orders = ref([])
const mine = ref([])
const orderId = ref(null)
const form = reactive({ score: 5, timeliness: '准时', damage: '完好', content: '' })

async function load() {
  const data = await http.get('/order/page', { params: { page: 1, size: 30, status: 'DONE' } })
  orders.value = data.records || []
  if (orders.value.length) orderId.value = orders.value[0].id
  // 只回看本企业的评价，避免看到别家客户的评价内容
  const rv = await http.get('/review/page', { params: { page: 1, size: 100 } })
  mine.value = (rv.records || []).filter((r) => r.customer_name === store.user?.orgName)
}

async function submit() {
  if (!orderId.value) {
    ElMessage.warning('暂无可评价的已完成运单')
    return
  }
  const o = orders.value.find((x) => x.id === orderId.value)
  await http.post('/review', {
    review_no: 'PJ' + Date.now(), order_id: o.id, order_no: o.order_no, customer_name: store.user?.orgName,
    score: form.score, content: form.content || `${form.timeliness}，${form.damage}，整体${form.score} 分`,
    tags: `${form.timeliness},${form.damage}`, status: form.score >= 4 ? 'NORMAL' : 'ABNORMAL',
    review_time: nowStr(), create_time: nowStr()
  })
  ElMessage.success('评价已提交，感谢反馈')
}
onMounted(load)
</script>
