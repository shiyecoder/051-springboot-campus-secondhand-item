<template>
  <div>
    <div class="page-h">
      <div><h2>容量与时间窗联合配车</h2><p>输入依据 → 明细分 → 等级说明 → 人工修正，全流程留痕可追溯</p></div>
      <div class="sp"></div>
      <el-select v-model="orderId" filterable placeholder="选择待配车运单" style="width: 300px" @change="calc">
        <el-option v-for="o in orders" :key="o.id" :value="o.id" :label="`${o.order_no} · ${o.customer_name} · ${o.from_city}→${o.to_city}`" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="calc">重新计算</el-button>
    </div>

    <div class="grid2" style="margin-bottom: 14px">
      <div class="card">
        <div class="card-h"><h3>计算输入（快照）</h3><div class="sp"></div>
          <span class="mut" style="font-size: 11.5px">规则版本 {{ result.rule_version || '-' }}</span></div>
        <el-descriptions :column="2" border size="small" v-if="order">
          <el-descriptions-item label="运单编号">{{ order.order_no }}</el-descriptions-item>
          <el-descriptions-item label="计算时间">{{ fmtTime(result.gen_time || nowStr()) }}</el-descriptions-item>
          <el-descriptions-item label="货物重量">{{ order.weight }} t</el-descriptions-item>
          <el-descriptions-item label="货物体积">{{ order.volume }} m³</el-descriptions-item>
          <el-descriptions-item label="要求车型">{{ order.vehicle_type_req }}</el-descriptions-item>
          <el-descriptions-item label="要求送达">{{ fmtTime(order.require_arrive_time) }}</el-descriptions-item>
          <el-descriptions-item label="装货地址" :span="2">{{ order.from_address }}</el-descriptions-item>
          <el-descriptions-item label="候选车辆">{{ result.candidates || 0 }} 辆</el-descriptions-item>
          <el-descriptions-item label="评分维度">6 项（权重合计 100%）</el-descriptions-item>
        </el-descriptions>
        <div class="note" style="margin-top: 10px">缺失数据不会静默按 0 计分：例如车辆历史准时率缺失时，该维度不计入并提示补录。</div>
      </div>
      <div class="card">
        <div class="card-h"><h3>推荐结果</h3></div>
        <div v-if="result.total_score !== undefined" class="result">
          <div class="ring" :style="ringStyle"><div class="inner"><b class="num">{{ result.total_score }}</b><span>总分</span></div></div>
          <div>
            <div class="plate">{{ result.plate_no }} · {{ result.vehicle_type }}</div>
            <div class="mut" style="margin-top: 4px">规则 {{ result.rule_version }} · {{ result.candidates }} 辆候选中排名第 1</div>
            <div style="margin-top: 10px"><StatusTag :code="result.result_level" type="result_level" />
              <span class="tag gray" style="margin-left: 6px">{{ result.unmet_conditions === '无硬性条件未满足' ? '无硬性条件未满足' : '存在未满足条件' }}</span></div>
          </div>
        </div>
        <el-empty v-else description="请选择运单后执行计算" :image-size="70" />
        <div class="note" style="margin-top: 12px">{{ result.main_factors || '—' }}<br />可改进项：{{ result.improvements || '—' }}</div>
      </div>
    </div>

    <div class="card" style="margin-bottom: 14px">
      <div class="card-h"><h3>计算明细（可追溯到规则版本）</h3><div class="sp"></div>
        <el-button size="small" @click="$router.push('/rules')">调整规则权重</el-button></div>
      <el-table :data="result.items || []" size="small">
        <el-table-column prop="dimension" label="评分维度" min-width="110" />
        <el-table-column prop="basis" label="计算依据" min-width="300" />
        <el-table-column label="单项得分" min-width="120">
          <template #default="{ row }">
            <div class="bar"><i :style="{ width: Math.min(100, row.score) + '%' }"></i></div>
            <span class="num">{{ row.score }}<span class="mut"> / {{ row.full_score }}</span></span>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" min-width="80" />
        <el-table-column label="加权得分" min-width="140">
          <template #default="{ row }">
            <div class="bar"><i :style="{ width: Math.min(100, row.weighted_score * 4) + '%' }"></i></div>
            <span class="num">{{ row.weighted_score }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="grid3">
      <div class="card"><div class="card-h"><h3>未满足条件</h3></div>
        <div style="font-size: 12.5px">{{ result.unmet_conditions || '—' }}</div></div>
      <div class="card"><div class="card-h"><h3>人工修正</h3></div>
        <el-form label-width="72px" size="small">
          <el-form-item label="改为"><el-input v-model="manualPlate" placeholder="车牌号" /></el-form-item>
          <el-form-item label="原因"><el-input v-model="manualReason" type="textarea" :rows="2" placeholder="不少于 5 个字" /></el-form-item>
        </el-form>
        <el-button size="small" type="primary" @click="manual">提交修正</el-button>
        <div class="mut" style="font-size: 11.5px; margin-top: 8px">原始计算结果不会被覆盖，修正记入操作日志。</div>
      </div>
      <div class="card"><div class="card-h"><h3>候选车辆</h3></div>
        <el-table :data="result.top || []" size="small" empty-text="暂无候选">
          <el-table-column prop="plate_no" label="车牌" min-width="100" />
          <el-table-column prop="total_score" label="得分" min-width="70" />
          <el-table-column label="等级" min-width="90"><template #default="{ row }"><StatusTag :code="row.result_level" type="result_level" /></template></el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { fmtTime, nowStr } from '../utils/format'

/** 创新点：规则配置 + 计算留痕 + 结果解释 + 人工修正 */
const orders = ref([])
const orderId = ref(null)
const order = ref(null)
const result = ref({})
const loading = ref(false)
const manualPlate = ref('')
const manualReason = ref('')

const ringStyle = computed(() => {
  const s = Number(result.value.total_score || 0)
  const color = s >= 85 ? '#2D765C' : s >= 70 ? '#B4761E' : '#A61B29'
  return { background: `conic-gradient(${color} 0 ${s}%, #F1E9E6 ${s}% 100%)` }
})

async function loadOrders() {
  const data = await http.get('/order/page', { params: { page: 1, size: 50, status: 'CREATED' } })
  orders.value = data.records || []
  if (!orderId.value && orders.value.length) {
    orderId.value = orders.value[0].id
    calc()
  } else if (!orders.value.length) {
    const all = await http.get('/order/page', { params: { page: 1, size: 20 } })
    orders.value = all.records || []
    if (orders.value.length) { orderId.value = orders.value[0].id; calc() }
  }
}

async function calc() {
  if (!orderId.value) return
  loading.value = true
  try {
    const detail = await http.get(`/order/${orderId.value}/detail`)
    order.value = detail.order
    result.value = await http.post(`/order/${orderId.value}/evaluate`)
  } finally {
    loading.value = false
  }
}

async function manual() {
  if (!result.value.score_id) {
    ElMessage.warning('请先执行计算')
    return
  }
  if ((manualReason.value || '').trim().length < 5) {
    ElMessage.warning('人工修正必须填写原因，且不少于 5 个字')
    return
  }
  await http.post(`/order/score/${result.value.score_id}/manual`, { plateNo: manualPlate.value, reason: manualReason.value })
  ElMessage.success('修正已记录，原始结果保留')
}

onMounted(loadOrders)
</script>

<style scoped>
.result { display: flex; align-items: center; gap: 20px; }
.ring { width: 96px; height: 96px; border-radius: 50%; display: grid; place-items: center; }
.ring .inner { width: 74px; height: 74px; border-radius: 50%; background: #fff; display: grid; place-items: center; text-align: center; }
.ring b { font-size: 24px; color: var(--p); }
.ring span { font-size: 10.5px; color: var(--ink2); display: block; }
.plate { font-size: 15px; font-weight: 600; }
.bar { height: 7px; background: #f1e9e6; border-radius: 4px; overflow: hidden; margin-bottom: 3px; }
.bar i { display: block; height: 7px; background: #2f6f6a; border-radius: 4px; }
</style>
