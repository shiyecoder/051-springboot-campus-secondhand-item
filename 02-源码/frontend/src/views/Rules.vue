<template>
  <div>
    <div class="page-h">
      <div><h2>定价规则配置</h2><p>六项权重合计必须等于 100%；保存即生成新版本并立即生效，历史计算不受影响</p></div>
      <div class="sp"></div><el-button type="primary" @click="save">保存并生成新版本</el-button>
    </div>
    <div class="split">
      <div class="card">
        <h3>当前权重（合计 {{ sum }}%）</h3>
        <el-form :model="form" label-width="160px">
          <el-form-item v-for="f in fields" :key="f.key" :label="f.label">
            <el-slider v-model="form[f.key]" :min="0" :max="60" :step="5" show-input style="max-width: 420px" />
          </el-form-item>
          <el-form-item label="变更说明">
            <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="说明本次调整原因，便于版本追溯" />
          </el-form-item>
        </el-form>
        <div class="sum" :class="{ bad: sum !== 100 }">权重合计 {{ sum }}% {{ sum === 100 ? '（校验通过）' : '（必须等于 100%）' }}</div>
      </div>
      <div class="card">
        <h3>版本历史</h3>
        <el-table :data="rules" empty-text="暂无规则版本">
          <el-table-column prop="rule_version" label="版本" width="80" />
          <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :code="row.status" type="rule_status" /></template></el-table-column>
          <el-table-column prop="effect_time" label="生效时间" min-width="160" />
          <el-table-column prop="remark" label="说明" min-width="180" show-overflow-tooltip />
        </el-table>
        <p class="muted" style="margin-top: 12px">等级阈值：A ≥ 80 ｜ B 60–79 ｜ C &lt; 60。修改权重只影响新计算或主动重算的数据。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

/** 规则配置页（创新点要求 1）：权重合计校验 + 版本号与生效时间留痕 */
const fields = [
  { key: 'weightCategory', label: '同分类成交价偏离度' },
  { key: 'weightCondition', label: '商品成色' },
  { key: 'weightSupply', label: '同校同类在售数量' },
  { key: 'weightHeat', label: '发布时长与浏览转化' },
  { key: 'weightCredit', label: '卖家信用分' },
  { key: 'weightInfo', label: '图文完整度' }
]
const form = reactive({ ruleName: '商品定价成交概率评分规则', weightCategory: 30, weightCondition: 20, weightSupply: 15, weightHeat: 15, weightCredit: 10, weightInfo: 10, remark: '' })
const rules = ref([])
const sum = computed(() => fields.reduce((s, f) => s + Number(form[f.key] || 0), 0))

async function load() {
  rules.value = await http.get('/price/rules')
  const active = rules.value.find((r) => r.status === '启用')
  if (active) {
    Object.assign(form, {
      weightCategory: active.weight_category, weightCondition: active.weight_condition,
      weightSupply: active.weight_supply, weightHeat: active.weight_heat,
      weightCredit: active.weight_credit, weightInfo: active.weight_info
    })
  }
}

async function save() {
  if (sum.value !== 100) {
    ElMessage.warning('六项权重合计必须等于 100%')
    return
  }
  const r = await http.post('/price/rules', form)
  ElMessage.success('已生成新版本 ' + r.version + ' 并立即生效')
  load()
}

onMounted(load)
</script>

<style scoped>
.sum { font-size: 13px; color: var(--ok); font-weight: 600; }
.sum.bad { color: var(--bad); }
.muted { color: var(--ink2); font-size: 12.5px; }
</style>
