<template>
  <div v-if="d.order">
    <div class="page-h">
      <div><h2>运单 {{ d.order.order_no }}</h2>
        <p>{{ d.order.customer_name }} → {{ d.order.receiver_name }} · {{ d.order.goods_name }} · {{ d.order.weight }} 吨</p></div>
      <div class="sp"></div>
      <el-button @click="$router.back()">返回</el-button>
      <el-button v-if="canDispatch" @click="$router.push('/dispatch')">调度配车</el-button>
      <el-button type="primary" @click="evaluate" :loading="computing">执行配车计算</el-button>
    </div>

    <div class="card statusbar">
      <div><label>运单编号</label><b class="mono">{{ d.order.order_no }}</b></div>
      <div><label>当前状态</label><StatusTag :code="d.order.status" type="order_status" /></div>
      <div><label>责任人</label><span>{{ d.order.driver_name || d.order.create_by || '-' }}</span></div>
      <div><label>创建时间</label><span class="num">{{ fmtTime(d.order.create_time) }}</span></div>
      <div><label>最近更新</label><span class="num">{{ fmtTime(d.order.update_time) }}</span></div>
      <div><label>应收</label><span class="num">¥ {{ fmtMoney(d.order.receivable) }}</span></div>
    </div>

    <div class="grid2" style="margin-bottom: 14px">
      <div class="card">
        <div class="card-h"><h3>基础信息</h3></div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="发货方">{{ d.order.customer_name }}</el-descriptions-item>
          <el-descriptions-item label="收货方">{{ d.order.receiver_name }}</el-descriptions-item>
          <el-descriptions-item label="货物类型">{{ d.order.goods_type }}</el-descriptions-item>
          <el-descriptions-item label="货物名称">{{ d.order.goods_name }}</el-descriptions-item>
          <el-descriptions-item label="重量 / 体积">{{ d.order.weight }} t / {{ d.order.volume }} m³</el-descriptions-item>
          <el-descriptions-item label="计划里程">{{ d.order.plan_distance }} km</el-descriptions-item>
          <el-descriptions-item label="装货地址" :span="2">{{ d.order.from_address }}</el-descriptions-item>
          <el-descriptions-item label="卸货地址" :span="2">{{ d.order.to_address }}</el-descriptions-item>
          <el-descriptions-item label="要求送达">{{ fmtTime(d.order.require_arrive_time) }}</el-descriptions-item>
          <el-descriptions-item label="特殊要求">{{ d.order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="card">
        <div class="card-h"><h3>调度与车辆</h3><div class="sp"></div>
          <span class="mut" style="font-size: 11.5px">{{ d.score ? '规则 ' + d.score.score.rule_version : '未计算' }}</span></div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="车牌">{{ d.order.plate_no || '待配车' }}</el-descriptions-item>
          <el-descriptions-item label="驾驶员">{{ d.order.driver_name || '待配车' }}</el-descriptions-item>
          <el-descriptions-item label="要求车型">{{ d.order.vehicle_type_req }}</el-descriptions-item>
          <el-descriptions-item label="计划装货">{{ fmtTime(d.order.plan_load_time) }}</el-descriptions-item>
          <el-descriptions-item label="预计到达">{{ fmtTime(d.order.plan_arrive_time) }}</el-descriptions-item>
          <el-descriptions-item label="实际到达">{{ fmtTime(d.order.actual_arrive_time) }}</el-descriptions-item>
        </el-descriptions>
        <div class="note" style="margin-top: 10px" v-if="lastChange">改派记录：原车 {{ lastChange.origin_vehicle }}，{{ lastChange.change_reason }}（{{ fmtTime(lastChange.change_time) }}）</div>
        <div style="margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap">
          <el-button size="small" v-for="t in nextActions" :key="t.code" :type="t.primary ? 'primary' : 'default'" @click="transition(t.code)">
            {{ t.label }}
          </el-button>
        </div>
      </div>
    </div>

    <div class="grid2">
      <div class="card">
        <div class="card-h"><h3>运输节点时间轴</h3><div class="sp"></div>
          <router-link to="/track" style="font-size: 12px">全部节点</router-link></div>
        <el-timeline v-if="d.nodes && d.nodes.length">
          <el-timeline-item v-for="(n, i) in d.nodes" :key="n.id" :timestamp="fmtTime(n.node_time)" placement="top"
            :color="i === 0 ? '#5B2533' : '#2D765C'">
            <b>{{ n.node_name }}</b>
            <div class="mut" style="font-size: 12px">{{ n.place }} · {{ n.description || '—' }} · 上报人 {{ n.reporter }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无节点记录，装车后可上报" :image-size="70" />
      </div>
      <div class="card">
        <div class="card-h"><h3>费用 / 回单 / 异常</h3></div>
        <el-tabs>
          <el-tab-pane :label="'费用 ' + (d.fees || []).length">
            <el-table :data="d.fees || []" size="small" empty-text="暂无费用">
              <el-table-column prop="fee_type" label="类型" min-width="86" />
              <el-table-column label="收/付" min-width="70">
                <template #default="{ row }">
                  <span :class="row.direction === 'RECV' ? 'dir-in' : 'dir-out'">{{ row.direction === 'RECV' ? '应收' : '应付' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="金额" min-width="100"><template #default="{ row }"><span class="num">{{ fmtMoney(row.amount) }}</span></template></el-table-column>
              <el-table-column label="状态" min-width="90"><template #default="{ row }"><StatusTag :code="row.status" type="fee_status" /></template></el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="'回单 ' + (d.receipts || []).length">
            <el-table :data="d.receipts || []" size="small" empty-text="暂无回单">
              <el-table-column prop="receipt_no" label="回单编号" min-width="130" />
              <el-table-column label="状态" min-width="100"><template #default="{ row }"><StatusTag :code="row.status" type="receipt_status" /></template></el-table-column>
              <el-table-column prop="reject_reason" label="退回原因" min-width="140" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="'异常 ' + (d.exceptions || []).length">
            <el-table :data="d.exceptions || []" size="small" empty-text="无异常">
              <el-table-column prop="exception_no" label="异常编号" min-width="130" />
              <el-table-column prop="type" label="类型" min-width="100" />
              <el-table-column label="状态" min-width="100"><template #default="{ row }"><StatusTag :code="row.status" type="exception_status" /></template></el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <div class="card" style="margin-top: 14px" v-if="d.score">
      <div class="card-h"><h3>容量与时间窗联合配车 · 计算依据</h3><div class="sp"></div>
        <StatusTag :code="d.score.score.result_level" type="result_level" />
        <b class="num" style="margin-left: 10px; font-size: 18px; color: #5B2533">{{ d.score.score.total_score }}</b>
        <span class="mut" style="margin-left: 4px">分</span></div>
      <el-table :data="d.score.items || []" size="small">
        <el-table-column prop="dimension" label="评分维度" min-width="110" />
        <el-table-column prop="basis" label="计算依据" min-width="260" />
        <el-table-column prop="full_score" label="满分" min-width="70" />
        <el-table-column prop="weight" label="权重%" min-width="80" />
        <el-table-column prop="weighted_score" label="加权得分" min-width="100" />
      </el-table>
      <div class="note" style="margin-top: 10px">
        {{ d.score.score.main_factors }} · 未满足条件：{{ d.score.score.unmet_conditions }} · 可改进项：{{ d.score.score.improvements }}
        <span v-if="d.score.score.manual_flag === 1"> · 人工修正：{{ d.score.score.manual_vehicle }}（{{ d.score.score.manual_reason }}）</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { fmtMoney, fmtTime } from '../utils/format'
import { store } from '../store'

/** 运单详情：顶部状态条 + 基础信息 + 调度 + 节点时间轴 + 费用/回单/异常 + 配车计算依据 */
const route = useRoute()
const router = useRouter()
const d = ref({})
const computing = ref(false)

const FLOW = ['CREATED', 'DISPATCHED', 'LOADED', 'SHIPPING', 'ARRIVED', 'SIGNED', 'FEE_CHECK', 'DONE']
const LABELS = { CREATED: '运单创建', DISPATCHED: '调度配车', LOADED: '装货发车', SHIPPING: '在途节点', ARRIVED: '到达签收', SIGNED: '签收回单', FEE_CHECK: '费用核对', DONE: '已完成' }

const canDispatch = computed(() => ['ROLE_MANAGER', 'ROLE_DISPATCHER'].includes(store.user?.roleCode))
const lastChange = computed(() => (d.value.dispatches || []).find((x) => x.change_reason))

const nextActions = computed(() => {
  const cur = d.value.order?.status
  const i = FLOW.indexOf(cur)
  const list = []
  if (i >= 0 && i < FLOW.length - 1) list.push({ code: FLOW[i + 1], label: '推进到「' + LABELS[FLOW[i + 1]] + '」', primary: true })
  if (i > 0) list.push({ code: FLOW[i - 1], label: '退回「' + LABELS[FLOW[i - 1]] + '」' })
  if (cur !== 'CANCELED' && cur !== 'DONE') list.push({ code: 'EXCEPTION', label: '标记异常' })
  return list
})

async function load() {
  d.value = await http.get(`/order/${route.params.id}/detail`)
}

async function transition(target) {
  let reason = ''
  if (target === 'EXCEPTION' || FLOW.indexOf(target) < FLOW.indexOf(d.value.order.status)) {
    const r = await ElMessageBox.prompt('请填写原因（不少于 5 个字），将记入操作日志', '状态流转', {
      inputPlaceholder: '例如：车辆前序任务延误，需退回重新调度'
    }).catch(() => null)
    if (!r) return
    reason = r.value || ''
  }
  await http.post(`/order/${route.params.id}/transition`, { target, reason })
  ElMessage.success('状态已更新')
  load()
}

async function evaluate() {
  computing.value = true
  try {
    const r = await http.post(`/order/${route.params.id}/evaluate`)
    ElMessage.success(`计算完成：总分 ${r.total_score}（${r.result_level}）`)
    load()
  } finally {
    computing.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.statusbar { display: flex; gap: 34px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
.statusbar label { display: block; font-size: 11.5px; color: var(--ink2); margin-bottom: 3px; }
.statusbar b { font-size: 15px; }
.dir-in { color: var(--ok); font-size: 12px; }
.dir-out { color: #a8603f; font-size: 12px; }
</style>
