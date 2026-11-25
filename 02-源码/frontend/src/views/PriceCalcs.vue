<template>
  <div>
    <div class="page-h">
      <div><h2>定价评分记录</h2><p>共 {{ total }} 次计算 ｜ 每次保留输入快照、六项得分、规则版本与人工修正痕迹</p></div>
      <div class="sp"></div>
      <el-button @click="$router.push('/admin/rules')">规则配置</el-button>
    </div>
    <div class="filter">
      <div class="fi"><label>等级</label>
        <el-select v-model="grade" clearable placeholder="全部" style="width: 160px" @change="load(1)">
          <el-option label="A 定价合理" value="A" /><el-option label="B 略偏高" value="B" /><el-option label="C 偏离市场" value="C" />
        </el-select>
      </div>
      <div class="sp"></div><el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无评分记录">
        <el-table-column prop="goods_id" label="商品ID" width="90" />
        <el-table-column prop="rule_version" label="规则版本" width="100" />
        <el-table-column label="总分" width="90"><template #default="{ row }"><b class="num">{{ row.total_score }}</b></template></el-table-column>
        <el-table-column label="等级" width="90"><template #default="{ row }"><StatusTag :code="row.grade" type="price_grade" /></template></el-table-column>
        <el-table-column label="建议价区间" min-width="150">
          <template #default="{ row }"><span class="num">¥{{ row.suggest_low }} ~ ¥{{ row.suggest_high }}</span></template>
        </el-table-column>
        <el-table-column prop="factors" label="影响因素" min-width="240" show-overflow-tooltip />
        <el-table-column label="人工修正" width="110">
          <template #default="{ row }"><span v-if="row.corrected_grade">→ {{ row.corrected_grade }}</span><span v-else class="muted">—</span></template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="detail(row)">详情</el-button>
            <el-button link type="warning" @click="correct(row)">修正</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
      </div>
    </div>

    <el-drawer v-model="drawer" title="评分详情（原始结果保留）" size="560px">
      <el-descriptions :column="2" border v-if="current.id">
        <el-descriptions-item label="商品ID">{{ current.goods_id }}</el-descriptions-item>
        <el-descriptions-item label="规则版本">{{ current.rule_version }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ current.total_score }}</el-descriptions-item>
        <el-descriptions-item label="等级">{{ current.grade }}</el-descriptions-item>
        <el-descriptions-item label="建议价区间" :span="2">¥{{ current.suggest_low }} ~ ¥{{ current.suggest_high }}</el-descriptions-item>
        <el-descriptions-item label="结论" :span="2">{{ current.conclusion }}</el-descriptions-item>
        <el-descriptions-item label="未满足条件" :span="2">{{ current.missing || '无' }}</el-descriptions-item>
        <el-descriptions-item label="人工修正" :span="2">
          <span v-if="current.corrected_grade">{{ current.grade }} → {{ current.corrected_grade }}，原因：{{ current.correct_reason }}</span>
          <span v-else>未修正</span>
        </el-descriptions-item>
      </el-descriptions>
      <h3 style="margin: 18px 0 8px">六项得分</h3>
      <el-table :data="scoreRows" size="small">
        <el-table-column prop="name" label="因素" />
        <el-table-column prop="score" label="得分" width="90" />
      </el-table>
      <h3 style="margin: 18px 0 8px">输入快照</h3>
      <pre class="snap">{{ pretty(current.snapshot) }}</pre>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

/** 定价评分记录：展示原始结果与人工修正留痕，可查看输入快照 */
const rows = ref([])
const total = ref(0)
const page = ref(1)
const grade = ref('')
const loading = ref(false)
const drawer = ref(false)
const current = ref({})

const scoreRows = computed(() => {
  const c = current.value
  return [
    { name: '同分类成交价偏离度', score: c.score_category },
    { name: '商品成色', score: c.score_condition },
    { name: '同校同类在售数量', score: c.score_supply },
    { name: '发布时长与浏览转化', score: c.score_heat },
    { name: '卖家信用分', score: c.score_credit },
    { name: '图文完整度', score: c.score_info }
  ]
})

const pretty = (s) => {
  try { return JSON.stringify(JSON.parse(s || '{}'), null, 2) } catch (e) { return s || '{}' }
}

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/price/calc/page', { params: { page: page.value, size: 10, grade: grade.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const detail = (row) => { current.value = row; drawer.value = true }

async function correct(row) {
  const { value } = await ElMessageBox.prompt('修正等级（A/B/C），原因不少于 5 字，格式如：A,开学季刚需，实际成交高于模型', '人工修正', { inputValue: 'B,人工复核后调整' })
  const [g, ...rest] = String(value || '').split(',')
  await http.post(`/price/calc/${row.id}/correct`, { grade: g.trim(), reason: rest.join(',').trim() })
  ElMessage.success('已记录修正，原始结果保留')
  load()
}

onMounted(() => load())
</script>

<style scoped>
.stars { color: var(--a); }
.muted { color: var(--ink2); }
.snap { background: #FAFBFB; border: 1px solid var(--line); border-radius: 8px; padding: 10px; font-size: 12px; max-height: 220px; overflow: auto; }
</style>
