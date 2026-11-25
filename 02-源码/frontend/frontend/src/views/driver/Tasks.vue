<template>
  <div>
    <div class="page-h"><div><h2>我的任务</h2><p>仅显示指派给本人的运单任务</p></div><div class="sp"></div>
      <el-button @click="load">刷新</el-button></div>
    <div class="card" v-for="o in rows" :key="o.id" style="margin-bottom: 12px">
      <div class="oc-head">
        <b class="mono">{{ o.order_no }}</b>
        <StatusTag :code="o.status" type="order_status" />
        <span class="mut">{{ o.from_city }} → {{ o.to_city }} · {{ o.goods_name }} {{ o.weight }} 吨</span>
        <div class="sp"></div>
        <span class="mut" style="font-size: 12px">要求送达 {{ fmtTime(o.require_arrive_time) }}</span>
      </div>
      <div class="d-grid">
        <div><label>装货地址</label><p>{{ o.from_address }}</p></div>
        <div><label>卸货地址</label><p>{{ o.to_address }}</p></div>
        <div><label>车牌</label><p>{{ o.plate_no || '-' }}</p></div>
        <div><label>计划装货</label><p class="num">{{ fmtTime(o.plan_load_time) }}</p></div>
      </div>
      <div class="note" v-if="o.remark">注意事项：{{ o.remark }}</div>
      <div class="btns">
        <el-button size="large" type="primary" @click="$router.push('/driver/node')">上报节点</el-button>
        <el-button size="large" @click="$router.push('/driver/exception')">异常上报</el-button>
        <el-button size="large" @click="$router.push('/driver/fee')">费用提交</el-button>
      </div>
    </div>
    <el-empty v-if="!rows.length" description="暂无指派给本人的任务" :image-size="70" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import { fmtTime } from '../../utils/format'

const rows = ref([])
async function load() {
  const data = await http.get('/order/page', { params: { page: 1, size: 20 } })
  rows.value = data.records || []
}
onMounted(load)
</script>

<style scoped>
.oc-head { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; margin-bottom: 12px; }
.oc-head .sp { flex: 1; }
.d-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px 20px; }
.d-grid label { font-size: 11.5px; color: var(--ink2); }
.d-grid p { margin: 2px 0 0; font-size: 13px; }
.btns { display: flex; gap: 10px; margin-top: 14px; flex-wrap: wrap; }
.btns :deep(.el-button) { min-width: 120px; }
</style>
