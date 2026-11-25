<template>
  <div>
    <div class="page-h"><div><h2>发布闲置</h2><p>最多 6 张实拍图 ｜ 提交后进入待审核，审核期间不可被下单 ｜ 24 小时内最多发布 10 件</p></div></div>
    <div class="split">
      <div class="card">
        <h3>实拍图（置顶）</h3>
        <UploadBox v-model="form.imageUrls" :max="6" biz-type="goods" />
        <el-form :model="form" label-width="110px" style="margin-top: 16px">
          <el-form-item label="标题" required><el-input v-model="form.title" maxlength="120" show-word-limit /></el-form-item>
          <el-form-item label="分类" required>
            <el-cascader v-model="cascade" :options="tree" :props="{ value: 'id', label: 'name', children: 'children' }" style="width: 320px" @change="onCascade" />
          </el-form-item>
          <el-form-item label="成色" required>
            <el-select v-model="form.conditionLevel" style="width: 200px">
              <el-option v-for="(v, k) in DICT.condition_level" :key="k" :label="v[0]" :value="k" />
            </el-select>
          </el-form-item>
          <el-form-item label="售价 / 入手价" required>
            <el-input-number v-model="form.price" :min="0.01" :precision="2" :controls="false" style="width: 160px" />
            <span style="margin: 0 10px; color: var(--ink2); font-size: 12.5px">入手价</span>
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :controls="false" style="width: 160px" />
          </el-form-item>
          <el-form-item label="校区 / 面交点">
            <el-select v-model="form.campusId" style="width: 160px" @change="loadPlaces">
              <el-option label="东校区" :value="1" /><el-option label="西校区" :value="2" /><el-option label="北校区" :value="3" />
            </el-select>
            <el-select v-model="form.tradePlaceId" clearable placeholder="可协商时留空" style="width: 220px; margin-left: 8px">
              <el-option v-for="p in places" :key="p.id" :label="p.place_name" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="交易方式">
            <el-select v-model="form.tradeType" style="width: 200px">
              <el-option label="面交" value="面交" /><el-option label="自提" value="自提" /><el-option label="可送" value="可送" />
            </el-select>
          </el-form-item>
          <el-form-item label="商品描述" required>
            <el-input v-model="form.description" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="不少于 10 字，写清使用时长、瑕疵位置与配件" />
          </el-form-item>
        </el-form>
        <div class="notice-bar">提交前请确认：不发布违禁品；图片为本人实拍；描述属实。命中敏感词将进入人工重点审核。</div>
        <el-button type="primary" :loading="loading" @click="submit">提交审核</el-button>
      </div>

      <div>
        <div class="card">
          <h3>定价参考</h3>
          <el-button size="small" @click="calc" :disabled="!form.price">计算成交概率</el-button>
          <div v-if="ref_" style="margin-top: 12px">
            <div class="score">{{ ref_.total_score }} 分 · {{ ref_.grade }} 级</div>
            <p class="muted">建议价 ¥{{ ref_.suggest_low ?? '-' }} ~ ¥{{ ref_.suggest_high ?? '-' }}（规则 {{ ref_.rule_version }}）</p>
            <p class="muted">{{ ref_.conclusion }}</p>
            <div class="bar"><i :style="{ width: Math.min(100, ref_.total_score) + '%' }"></i></div>
            <p class="muted" v-for="f in ref_.factors" :key="f.name">{{ f.name }}：{{ f.score }} 分（权重 {{ f.weight }}%）</p>
            <p class="muted warn" v-if="ref_.missing && ref_.missing.length">未满足条件：{{ ref_.missing.join('；') }}</p>
            <p class="muted" v-for="i in ref_.improvements" :key="i">可改进：{{ i }}</p>
          </div>
          <p class="muted" v-else>发布前先算一次，判断价格是否偏离市场。</p>
        </div>
        <div class="card">
          <h3>发布规则</h3>
          <p class="muted">同一卖家 24 小时内最多发布 10 件；标题与描述重复度超过 80% 会提示重复发布并转人工复核。</p>
          <p class="muted">信用分低于 60 时同时在线商品限制为 3 件；低于 40 时禁止发布新商品 30 天。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import http from '../../http'
import UploadBox from '../../components/UploadBox.vue'
import { DICT } from '../../dict'
import { store } from '../../store'

/** 发布闲置：图片置顶、价格与入手价并排、提交前展示审核规则确认 */
const router = useRouter()
const tree = ref([])
const places = ref([])
const cascade = ref([])
const ref_ = ref(null)
const loading = ref(false)
const form = reactive({
  title: '', categoryId: null, conditionLevel: '九成新', price: null, originalPrice: null,
  campusId: store.user?.campusId || 1, tradePlaceId: null, tradeType: '面交', description: '', imageUrls: []
})

const onCascade = (v) => { form.categoryId = v && v.length ? v[v.length - 1] : null }

async function loadPlaces() {
  places.value = await http.get('/campus-place/list', { params: { campus_id: form.campusId } })
}

async function calc() {
  // 先用一件同类商品的价格作为占位预览不可行，这里直接提示：计算需先发布
  ElMessage.info('定价参考基于同分类历史成交，发布后可在商品详情查看完整评分')
}

async function submit() {
  if (!form.imageUrls.length) {
    ElMessage.warning('至少上传 1 张实拍图')
    return
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择二级分类')
    return
  }
  loading.value = true
  try {
    const data = await http.post('/goods', form)
    ElMessage.success('已提交审核，商品编号 ' + data.goods_no + (data.duplicate_hint ? '（' + data.duplicate_hint + '）' : ''))
    router.push('/portal/mygoods')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  tree.value = await http.get('/category/tree')
  loadPlaces()
})
</script>

<style scoped>
.score { font-size: 20px; font-weight: 700; color: var(--p); }
.bar { height: 8px; background: #EDF1F0; border-radius: 999px; overflow: hidden; margin: 8px 0 12px; }
.bar i { display: block; height: 100%; background: var(--p2); }
.muted { color: var(--ink2); font-size: 12.5px; margin: 6px 0; }
.warn { color: #8A6118; }
</style>
