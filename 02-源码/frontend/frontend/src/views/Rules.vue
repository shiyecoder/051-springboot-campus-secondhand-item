<template>
  <div>
    <div class="page-h"><div><h2>配车规则配置</h2><p>权重合计必须等于 100%，修改后记录版本与生效时间；历史计算保留原版本</p></div>
      <div class="sp"></div>
      <el-button @click="load">刷新</el-button>
      <el-button type="primary" :loading="saving" @click="publish">保存并发布新版本</el-button></div>

    <div class="grid2">
      <div class="card">
        <div class="card-h"><h3>当前版本 {{ rule.version }} · {{ fmtTime(rule.effect_time) }}</h3><div class="sp"></div>
          <StatusTag code="RECOMMEND" type="result_level" /><span class="mut" style="margin-left: 6px; font-size: 11.5px">使用中</span></div>
        <el-table :data="dims" size="small">
          <el-table-column prop="name" label="评分维度" min-width="120" />
          <el-table-column label="权重" min-width="140">
            <template #default="{ row }">
              <el-input-number v-model="weights[row.key]" :min="0" :max="100" :step="5" size="small" style="width: 110px" />
            </template>
          </el-table-column>
          <el-table-column prop="desc" label="说明" min-width="240" />
        </el-table>
        <div style="margin-top: 10px; display: flex; align-items: center; gap: 10px">
          <span class="mut">权重合计</span>
          <b class="num" :style="{ color: sum === 100 ? '#2D765C' : '#A61B29', fontSize: '16px' }">{{ sum }}%</b>
          <span class="mut">· 推荐 ≥ {{ rule.threshold_recommend }}，备选 {{ rule.threshold_backup }}–{{ rule.threshold_recommend - 1 }}，不推荐 &lt; {{ rule.threshold_backup }}</span>
        </div>
      </div>
      <div class="card">
        <div class="card-h"><h3>硬性拦截规则</h3></div>
        <el-form label-width="120px" size="small">
          <el-form-item label="核载利用率上限"><el-input-number v-model="rule.load_limit_rate" :min="50" :max="100" /> %</el-form-item>
          <el-form-item label="容积利用率上限"><el-input-number v-model="rule.volume_limit_rate" :min="50" :max="100" /> %</el-form-item>
          <el-form-item label="空驶距离上限"><el-input-number v-model="rule.max_deadhead_km" :min="10" :max="300" /> km</el-form-item>
          <el-form-item label="任务最小间隔"><el-input-number v-model="rule.min_gap_minutes" :min="0" :max="180" /> 分钟</el-form-item>
          <el-form-item label="推荐阈值"><el-input-number v-model="rule.threshold_recommend" :min="50" :max="100" /></el-form-item>
          <el-form-item label="备选阈值"><el-input-number v-model="rule.threshold_backup" :min="30" :max="95" /></el-form-item>
        </el-form>
        <div class="note">修改权重后只影响新计算或主动重新计算的数据；证件有效性、维修停用等硬性条件不受权重影响。</div>
        <div class="card-h" style="margin-top: 14px"><h3>版本历史</h3></div>
        <el-table :data="history" size="small">
          <el-table-column prop="version" label="版本" min-width="70" />
          <el-table-column label="生效时间" min-width="150"><template #default="{ row }"><span class="num">{{ fmtTime(row.effect_time) }}</span></template></el-table-column>
          <el-table-column prop="create_by" label="修改人" min-width="90" />
          <el-table-column prop="remark" label="说明" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" min-width="80" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { fmtTime, nowStr } from '../utils/format'

/** 创新点配套：规则配置页。权重合计校验 + 版本留痕 */
const rule = ref({})
const history = ref([])
const weights = reactive({ load: 25, volume: 20, type: 15, deadhead: 15, timewin: 15, ontime: 10 })
const saving = ref(false)
const dims = [
  { key: 'load', name: '载重匹配', desc: '货物重量与车辆核载余量' },
  { key: 'volume', name: '容积匹配', desc: '货物体积与车厢容积余量' },
  { key: 'type', name: '车型要求', desc: '厢式/高栏/冷藏/平板硬性匹配' },
  { key: 'deadhead', name: '空驶距离', desc: '车辆当前位置到装货地距离' },
  { key: 'timewin', name: '送达时间窗', desc: '预计到达与要求送达时间的余量' },
  { key: 'ontime', name: '历史准时率', desc: '近 90 天车辆任务准时率' }
]
const sum = computed(() => Object.values(weights).reduce((a, b) => a + Number(b || 0), 0))

async function load() {
  const data = await http.get('/rule/page', { params: { page: 1, size: 20 } })
  history.value = (data.records || []).sort((a, b) => b.id - a.id)
  const active = history.value.find((r) => r.status === 'ACTIVE') || history.value[0] || {}
  rule.value = { ...active }
  try {
    const w = JSON.parse(active.weights || '{}')
    Object.keys(weights).forEach((k) => (weights[k] = w[k] ?? weights[k]))
  } catch (e) { /* 保持默认 */ }
}

async function publish() {
  if (sum.value !== 100) {
    ElMessage.error('权重合计必须等于 100%，当前 ' + sum.value + '%')
    return
  }
  saving.value = true
  try {
    const v = 'v' + (history.value.length + 1)
    await http.post('/rule', {
      version: v, weights: JSON.stringify(weights), threshold_recommend: rule.value.threshold_recommend,
      threshold_backup: rule.value.threshold_backup, max_deadhead_km: rule.value.max_deadhead_km,
      min_gap_minutes: rule.value.min_gap_minutes, load_limit_rate: rule.value.load_limit_rate,
      volume_limit_rate: rule.value.volume_limit_rate, status: 'ACTIVE', effect_time: nowStr(),
      create_by: '车队经理', remark: '权重调整并发布新版本'
    })
    if (rule.value.id) await http.put(`/rule/${rule.value.id}`, { status: 'ARCHIVED' })
    ElMessage.success('已发布 ' + v)
    load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
