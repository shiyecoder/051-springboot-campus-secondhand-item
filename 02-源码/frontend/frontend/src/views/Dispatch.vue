<template>
  <div>
    <div class="page-h">
      <div><h2>调度配车</h2><p>待本人处理优先、更新时间倒序；配车前置冲突校验，改派必须填写原因</p></div>
      <div class="sp"></div>
      <el-radio-group v-model="status" @change="load">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="TODO">待办</el-radio-button>
        <el-radio-button label="PROCESSING">处理中</el-radio-button>
        <el-radio-button label="EXCEPTION">异常</el-radio-button>
        <el-radio-button label="DONE">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="当前筛选条件下无调度任务">
        <!-- 调度单号与运单编号合成一列两行，10 列挤在 1200px 里会被右侧固定列盖住状态 -->
        <el-table-column label="调度单 / 运单" min-width="150">
          <template #default="{ row }">
            <div class="mono" style="font-size: 12px">{{ row.dispatch_no }}</div>
            <router-link class="mono" style="font-size: 12px" :to="'/orders/' + row.order_id">{{ row.order_no }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="customer_name" label="客户" min-width="150" show-overflow-tooltip />
        <el-table-column label="线路" min-width="110">
          <template #default="{ row }">{{ row.from_city }} → {{ row.to_city }}</template>
        </el-table-column>
        <el-table-column prop="goods_desc" label="车型/载重" min-width="130" />
        <el-table-column prop="plate_no" label="车牌" min-width="100" />
        <el-table-column prop="driver_name" label="驾驶员" min-width="90" />
        <el-table-column label="状态" min-width="96">
          <template #default="{ row }"><StatusTag :code="row.status" type="dispatch_status" /></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAssign(row)">配车</el-button>
            <el-button link @click="openChange(row)">改派</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="(p) => { page = p; load() }" />
      </div>
    </div>

    <el-drawer v-model="drawer" :title="mode === 'assign' ? '配车 · ' + current.order_no : '改派 · ' + current.dispatch_no" size="680px">
      <div class="card" style="margin-bottom: 12px; background: #fbf7f5">
        <div class="mut" style="font-size: 12px">运单 {{ current.order_no }} · {{ current.customer_name }} · {{ current.from_city }}→{{ current.to_city }} · {{ current.goods_desc }}</div>
        <div class="mut" style="font-size: 12px; margin-top: 4px">进度说明：{{ current.remark || '—' }}</div>
      </div>
      <el-form label-width="96px">
        <el-form-item label="候选车辆">
          <el-select v-model="vehicleId" style="width: 100%" placeholder="按推荐分排序" @change="onVehicle">
            <el-option v-for="c in candidates" :key="c.vehicle_id" :value="c.vehicle_id"
              :label="`${c.plate_no} · ${c.vehicle_type} · ${c.total_score} 分 · ${levelOf(c.result_level)}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="驾驶员">
          <el-select v-model="driverId" style="width: 100%" placeholder="选择驾驶员">
            <el-option v-for="d in drivers" :key="d.id" :value="d.id" :label="`${d.name} · ${d.license_type} · 有效至 ${d.license_expire}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="改派原因" v-if="mode === 'change'">
          <el-input v-model="reason" type="textarea" :rows="3" placeholder="必须填写原因（不少于 5 个字），将保留原车辆原驾驶员" />
        </el-form-item>
      </el-form>

      <div class="card" v-if="selected">
        <div class="card-h"><h3>冲突校验与计算依据</h3><div class="sp"></div>
          <StatusTag :code="selected.result_level" type="result_level" />
          <b class="num" style="margin-left: 8px; color: #5B2533">{{ selected.total_score }}</b></div>
        <el-table :data="selected.items" size="small">
          <el-table-column prop="dimension" label="维度" min-width="90" />
          <el-table-column prop="basis" label="计算依据" min-width="240" />
          <el-table-column prop="weight" label="权重%" min-width="70" />
          <el-table-column prop="weighted_score" label="加权得分" min-width="90" />
        </el-table>
        <div class="note" style="margin-top: 10px">
          未满足条件：{{ selected.unmet_conditions }}<br />可改进项：{{ selected.improvements }}
        </div>
      </div>

      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" @click="submit">确认{{ mode === 'assign' ? '配车' : '改派' }}</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

/** 调度配车：候选项由后端按规则版本打分，配车/改派都走后端校验 */
const rows = ref([])
const total = ref(0)
const page = ref(1)
const status = ref('')
const loading = ref(false)
const drawer = ref(false)
const mode = ref('assign')
const current = ref({})
const candidates = ref([])
const drivers = ref([])
const vehicleId = ref(null)
const driverId = ref(null)
const reason = ref('')
const selected = ref(null)

const levelOf = (l) => ({ RECOMMEND: '推荐', BACKUP: '备选', REJECT: '不推荐' }[l] || l)

async function load() {
  loading.value = true
  try {
    const data = await http.get('/dispatch/page', { params: { page: page.value, size: 10, status: status.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function openAssign(row) {
  mode.value = 'assign'
  current.value = row
  reason.value = ''
  vehicleId.value = row.vehicle_id
  driverId.value = row.driver_id
  drawer.value = true
  const opt = await http.get('/order/options')
  drivers.value = opt.drivers || []
  candidates.value = await http.get('/dispatch/candidates', { params: { orderId: row.order_id } })
  selected.value = candidates.value.find((c) => c.vehicle_id === vehicleId.value) || candidates.value[0] || null
  if (selected.value) vehicleId.value = selected.value.vehicle_id
}

async function openChange(row) {
  mode.value = 'change'
  current.value = row
  reason.value = ''
  vehicleId.value = row.vehicle_id
  driverId.value = row.driver_id
  drawer.value = true
  const opt = await http.get('/order/options')
  drivers.value = opt.drivers || []
  candidates.value = await http.get('/dispatch/candidates', { params: { orderId: row.order_id } })
  selected.value = candidates.value.find((c) => c.vehicle_id === vehicleId.value) || null
}

function onVehicle(v) {
  selected.value = candidates.value.find((c) => c.vehicle_id === v) || null
}

async function submit() {
  if (mode.value === 'assign') {
    const r = await http.post('/dispatch/assign', { orderId: current.value.order_id, vehicleId: vehicleId.value, driverId: driverId.value })
    ElMessage.success('配车成功，评分 ' + (r.score?.total_score ?? ''))
  } else {
    if ((reason.value || '').trim().length < 5) {
      ElMessage.warning('改派必须填写原因，且不少于 5 个字')
      return
    }
    await http.post(`/dispatch/${current.value.id}/change`, { orderId: current.value.order_id, vehicleId: vehicleId.value, driverId: driverId.value, reason: reason.value })
    ElMessage.success('改派成功，已保留原车辆与原因')
  }
  drawer.value = false
  load()
}

onMounted(load)
</script>
