#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""NEW-051 门户视图生成器（学生买家 / 学生卖家共用门户骨架）。"""
import os

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(BASE, "src")
V = {}

def add(p, c):
    V[p] = c.strip() + "\n"

add("views/portal/Home.vue", r"""
<template>
  <div>
    <div class="notice-bar" v-if="notices.length">
      <b>公告</b>
      <span>{{ notices[0].title }} ｜ {{ notices[0].content }}</span>
    </div>

    <div class="banner">
      <div>
        <h2>毕业季闲置专场</h2>
        <p>教材、宿舍用品、代步工具集中展示，支持按校区筛选</p>
        <el-button type="warning" @click="$router.push('/portal/search')">去逛逛</el-button>
      </div>
      <img src="/uploads/seed/goods/03.jpg" alt="" />
    </div>

    <div class="cats">
      <router-link v-for="c in categories" :key="c.id" class="cat" :to="{ path: '/portal/search', query: { parent_id: c.id } }">
        <b>{{ c.name }}</b><small>{{ c.count }} 件在售</small>
      </router-link>
    </div>

    <div class="sect" v-for="block in blocks" :key="block.title">
      <h3>{{ block.title }}</h3>
      <span class="tag ok" v-if="block.count">{{ block.count }} 件</span>
      <router-link class="more" to="/portal/search">查看更多 ›</router-link>
      <div class="pgrid">
        <router-link v-for="g in block.items" :key="g.id" class="pcard" :to="`/portal/goods/${g.id}`">
          <img :src="g.cover_url" alt="" />
          <div class="pd">
            <div class="pt">{{ g.title }}</div>
            <div class="pp">¥{{ g.price }}<s v-if="g.original_price">¥{{ g.original_price }}</s></div>
            <div class="pm"><StatusTag :code="g.condition_level" type="condition_level" /> {{ g.campus_name }}</div>
          </div>
        </router-link>
      </div>
      <p v-if="!block.items || !block.items.length" class="muted">该区块暂无在售商品</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import { store } from '../../store'

/** 门户首页：公告 + 轮播位 + 热门分类 + 今日上新/低价专区/同校好物 */
const data = ref({})
const notices = computed(() => data.value.notices || [])
const categories = computed(() => (data.value.categories || []).filter((c) => c.count > 0))
const blocks = computed(() => [
  { title: '今日上新', items: data.value.todayNew || [], count: (data.value.todayNew || []).length },
  { title: '低价专区', items: data.value.lowPrice || [], count: (data.value.lowPrice || []).length },
  { title: '同校好物（' + (store.user?.campusName || '本校') + '）', items: data.value.sameCampus || [], count: (data.value.sameCampus || []).length }
])

onMounted(async () => {
  data.value = await http.get('/dashboard/portal', { params: { campusId: store.user?.campusId } })
})
</script>

<style scoped>
.banner { display: flex; align-items: center; gap: 20px; background: linear-gradient(100deg, #146B5B, #3E8E7E); color: #fff; border-radius: var(--r); padding: 24px 26px; }
.banner h2 { margin: 0 0 6px; font-size: 22px; }
.banner p { margin: 0 0 14px; font-size: 13px; opacity: .92; }
.banner img { width: 260px; height: 140px; object-fit: cover; border-radius: 8px; margin-left: auto; }
.cats { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin: 16px 0 4px; }
.cat { background: #fff; border: 1px solid var(--line); border-radius: var(--r); padding: 12px; text-align: center; }
.cat b { display: block; font-size: 13px; }
.cat small { color: var(--ink2); font-size: 11.5px; }
.cat:hover { border-color: var(--p2); }
.sect { margin: 22px 0 10px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.sect h3 { font-size: 15px; margin: 0; }
.sect .more { margin-left: auto; font-size: 12.5px; color: var(--p2); }
.pgrid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; width: 100%; }
.pcard { background: #fff; border: 1px solid var(--line); border-radius: var(--r); overflow: hidden; }
.pcard img { width: 100%; aspect-ratio: 4/3; object-fit: cover; display: block; background: #EDF1F0; }
.pd { padding: 10px 12px 12px; }
.pt { font-size: 13.5px; line-height: 1.45; height: 40px; overflow: hidden; }
.pp { color: var(--bad); font-size: 16px; font-weight: 700; margin-top: 6px; }
.pp s { color: var(--ink2); font-size: 11.5px; font-weight: 400; margin-left: 6px; }
.pm { display: flex; align-items: center; gap: 6px; margin-top: 8px; font-size: 11.5px; color: var(--ink2); }
.muted { color: var(--ink2); font-size: 12.5px; }
@media (max-width: 900px) {
  .cats { grid-template-columns: repeat(3, 1fr); }
  .pgrid { grid-template-columns: repeat(2, 1fr); }
  .banner img { display: none; }
}
</style>
""")

add("views/portal/Search.vue", r"""
<template>
  <div>
    <div class="filter">
      <div class="fi"><label>关键词</label><el-input v-model="query.keyword" placeholder="iPad / 教材 / 自行车" clearable style="width: 200px" @keyup.enter="load(1)" /></div>
      <div class="fi"><label>分类</label>
        <el-select v-model="query.parent_id" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option v-for="c in tree" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
      <div class="fi"><label>成色</label>
        <el-select v-model="query.condition_level" clearable placeholder="全部" style="width: 140px" @change="load(1)">
          <el-option v-for="(v, k) in DICT.condition_level" :key="k" :label="v[0]" :value="k" />
        </el-select>
      </div>
      <div class="fi"><label>校区</label>
        <el-select v-model="query.campus_id" clearable placeholder="全部" style="width: 140px" @change="load(1)">
          <el-option label="东校区" :value="1" /><el-option label="西校区" :value="2" /><el-option label="北校区" :value="3" />
        </el-select>
      </div>
      <div class="fi"><label>价格</label>
        <el-input v-model="query.min_price" placeholder="最低" style="width: 90px" />
        <span style="margin: 0 6px">–</span>
        <el-input v-model="query.max_price" placeholder="最高" style="width: 90px" />
      </div>
      <div class="sp"></div><el-button type="primary" @click="load(1)">查询</el-button>
    </div>

    <div class="page-h">
      <div><h2>搜索结果</h2><p>共 {{ total }} 件</p></div>
      <div class="sp"></div>
      <el-radio-group v-model="query.sort" size="small" @change="load(1)">
        <el-radio-button value="new">最新发布</el-radio-button>
        <el-radio-button value="price_asc">价格从低到高</el-radio-button>
        <el-radio-button value="credit">信用优先</el-radio-button>
      </el-radio-group>
    </div>

    <div class="pgrid" v-if="rows.length">
      <router-link v-for="g in rows" :key="g.id" class="pcard" :to="`/portal/goods/${g.id}`">
        <img :src="g.cover_url" alt="" />
        <div class="pd">
          <div class="pt" v-html="hl(g.title)"></div>
          <div class="pp">¥{{ g.price }}<s v-if="g.original_price">¥{{ g.original_price }}</s></div>
          <div class="pm"><StatusTag :code="g.condition_level" type="condition_level" /> {{ g.campus_name }}</div>
        </div>
      </router-link>
    </div>
    <el-empty v-else description="没有找到符合条件的闲置，试试放宽价格或换一个分类" />

    <div class="pager" v-if="total > size">
      <span>共 {{ total }} 条</span>
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import { DICT } from '../../dict'

/** 门户检索：关键词高亮、二级分类、价格区间、成色、校区与三种排序 */
const route = useRoute()
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const tree = ref([])
const query = reactive({ keyword: '', parent_id: route.query.parent_id ? Number(route.query.parent_id) : '', condition_level: '', campus_id: '', min_price: '', max_price: '', sort: 'new' })

const hl = (t) => {
  if (!query.keyword) return t
  return String(t).replace(new RegExp(query.keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), (m) => `<mark>${m}</mark>`)
}

async function load(p) {
  if (p) page.value = p
  const params = { page: page.value, size: size.value, sort: query.sort }
  Object.keys(query).forEach((k) => { if (query[k] !== '' && query[k] !== null && k !== 'sort') params[k] = query[k] })
  const data = await http.get('/goods/search', { params })
  rows.value = data.records || []
  total.value = data.total || 0
}

onMounted(async () => {
  tree.value = await http.get('/category/tree')
  load(1)
})
</script>

<style scoped>
.pgrid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.pcard { background: #fff; border: 1px solid var(--line); border-radius: var(--r); overflow: hidden; }
.pcard img { width: 100%; aspect-ratio: 4/3; object-fit: cover; display: block; background: #EDF1F0; }
.pd { padding: 10px 12px 12px; }
.pt { font-size: 13.5px; line-height: 1.45; height: 40px; overflow: hidden; }
.pp { color: var(--bad); font-size: 16px; font-weight: 700; margin-top: 6px; }
.pp s { color: var(--ink2); font-size: 11.5px; font-weight: 400; margin-left: 6px; }
.pm { display: flex; align-items: center; gap: 6px; margin-top: 8px; font-size: 11.5px; color: var(--ink2); }
:deep(mark) { background: #FBF2DF; color: #8A6118; }
@media (max-width: 900px) { .pgrid { grid-template-columns: repeat(2, 1fr); } }
</style>
""")

add("views/portal/GoodsDetail.vue", r"""
<template>
  <div v-if="goods.id" class="split">
    <div class="card">
      <div class="gallery">
        <img v-for="(p, i) in images" :key="i" :src="p" alt="" :class="{ main: i === active }" @click="active = i" />
      </div>
      <h3 class="ttl">{{ goods.title }}</h3>
      <p class="desc">{{ goods.description }}</p>
      <el-descriptions :column="2" border style="margin-top: 14px">
        <el-descriptions-item label="成色">{{ goods.condition_level }}</el-descriptions-item>
        <el-descriptions-item label="交易方式">{{ goods.trade_type }}</el-descriptions-item>
        <el-descriptions-item label="入手价">{{ goods.original_price ? '¥' + goods.original_price : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="折价率">{{ discount ?? '-' }}{{ discount ? '%' : '' }}</el-descriptions-item>
        <el-descriptions-item label="校区">{{ goods.campus_name }}</el-descriptions-item>
        <el-descriptions-item label="浏览">{{ goods.view_count }} 次</el-descriptions-item>
      </el-descriptions>
      <div class="notice-bar" style="margin: 14px 0 0">交易风险提示：平台不支持在线支付与快递，请当面验货后再付款，勿向陌生人转账。</div>
    </div>

    <div>
      <div class="card">
        <div class="price">¥{{ goods.price }} <s v-if="goods.original_price">¥{{ goods.original_price }}</s></div>
        <p class="muted" v-if="ref_">定价参考：{{ ref_.grade }} 级 · 总分 {{ ref_.total_score }} ｜ 建议 ¥{{ ref_.suggest_low }} ~ ¥{{ ref_.suggest_high }}</p>
        <div class="buy">
          <el-button type="primary" :disabled="isOwner || goods.status !== '在售'" @click="openOrder">
            {{ isOwner ? '这是你发布的商品' : goods.status === '在售' ? '立即下单' : '当前不可下单' }}
          </el-button>
        </div>
      </div>

      <div class="card">
        <h3>卖家信息</h3>
        <div class="seller">
          <img v-if="seller.avatar" :src="seller.avatar" class="ava-img" alt="" />
          <div v-else class="ava">{{ (seller.real_name || 'U').slice(0, 1) }}</div>
          <div><b>{{ seller.real_name }}</b><small>{{ seller.campus_name }} · 历史成交 {{ seller.deal_count }} 单</small></div>
        </div>
        <p class="muted" style="margin-top: 8px">信用分 <b style="color: var(--p)">{{ seller.credit_score }}</b>（{{ seller.credit_score >= 100 ? '优秀' : seller.credit_score >= 60 ? '正常' : '受限' }}）</p>
      </div>

      <div class="card">
        <h3>公告</h3>
        <p v-for="n in notices" :key="n.id" class="muted">{{ n.title }}</p>
      </div>
    </div>

    <el-dialog v-model="dialog" title="确认下单" width="460px">
      <el-form :model="order" label-width="90px">
        <el-form-item label="面交时间" required>
          <el-date-picker v-model="order.appointTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" placeholder="必须晚于当前时间且在 7 天内" />
        </el-form-item>
        <el-form-item label="面交点" required>
          <el-select v-model="order.appointPlace" style="width: 100%" placeholder="选择校区面交点">
            <el-option v-for="p in places" :key="p.id" :label="p.place_name + '（' + p.campus_name + '）'" :value="p.campus_name + '·' + p.place_name" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="order.payType" style="width: 100%">
            <el-option label="现金" value="现金" /><el-option label="转账" value="转账" /><el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <p class="muted">下单后商品锁定 24 小时，卖家需在 12 小时内确认，超时自动取消。平台不处理在线支付。</p>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../http'
import { splitImages } from '../../utils/format'
import { store } from '../../store'

/** 商品详情：六图轮播 + 卖家信用 + 面交点 + 定价参考，底部固定下单 */
const route = useRoute()
const router = useRouter()
const goods = ref({})
const seller = ref({})
const notices = ref([])
const discount = ref(null)
const places = ref([])
const ref_ = ref(null)
const active = ref(0)
const dialog = ref(false)
const order = reactive({ appointTime: '', appointPlace: '', payType: '现金' })
const images = computed(() => splitImages(goods.value.image_urls || goods.value.cover_url))
const isOwner = computed(() => store.user?.id === goods.value.seller_id)

async function load() {
  const data = await http.get(`/goods/${route.params.id}`)
  goods.value = data.goods
  seller.value = data.seller || {}
  notices.value = data.notices || []
  discount.value = data.discount_rate
  try {
    ref_.value = await http.get(`/goods/${route.params.id}/price-reference`)
  } catch (e) { /* 定价参考失败不阻塞详情展示 */ }
}

async function openOrder() {
  if (!order.appointTime) {
    const d = new Date(Date.now() + 86400000)
    order.appointTime = d.toISOString().slice(0, 10) + ' 15:00:00'
  }
  places.value = await http.get('/campus-place/list', { params: { campus_id: goods.value.campus_id } })
  dialog.value = true
}

async function submit() {
  if (!order.appointTime || !order.appointPlace) {
    ElMessage.warning('请选择面交时间与面交点')
    return
  }
  const data = await http.post('/trade-order', null, {
    params: { goodsId: goods.value.id, appointTime: order.appointTime, appointPlace: order.appointPlace, payType: order.payType }
  })
  ElMessage.success('下单成功，订单号 ' + data.order_no)
  dialog.value = false
  router.push(`/portal/orders/${data.id}`)
}

onMounted(load)
</script>

<style scoped>
.gallery { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.gallery img { width: 100%; aspect-ratio: 4/3; object-fit: cover; border-radius: 8px; background: #EDF1F0; cursor: pointer; border: 2px solid transparent; }
.gallery img.main { border-color: var(--p); }
.ttl { font-size: 17px; margin: 16px 0 6px; }
.desc { font-size: 13px; color: var(--ink2); margin: 0; }
.price { font-size: 26px; color: var(--bad); font-weight: 700; }
.price s { font-size: 13px; color: var(--ink2); font-weight: 400; margin-left: 8px; }
.buy { display: flex; gap: 8px; margin-top: 14px; }
.buy .el-button { flex: 1; }
.seller { display: flex; gap: 10px; align-items: center; }
.ava { width: 40px; height: 40px; border-radius: 50%; background: var(--p2); color: #fff; display: grid; place-items: center; }
.ava-img { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
.seller b { display: block; font-size: 13.5px; }
.seller small { color: var(--ink2); font-size: 12px; }
.muted { color: var(--ink2); font-size: 12.5px; }
</style>
""")

add("views/portal/Publish.vue", r"""
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
""")

add("views/portal/MyGoods.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>我的闲置</h2><p>在售 {{ counts['在售'] || 0 }} 件 ｜ 待审核 {{ counts['待审核'] || 0 }} 件 ｜ 已售出 {{ counts['已售出'] || 0 }} 件 ｜ 信用 {{ store.user?.creditScore }}</p></div>
      <div class="sp"></div><el-button type="primary" @click="$router.push('/portal/publish')">发布闲置</el-button></div>
    <div class="filter">
      <div class="fi"><label>状态</label>
        <el-select v-model="status" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option v-for="(v, k) in DICT.goods_status" :key="k" :label="v[0]" :value="k" />
        </el-select>
      </div>
      <div class="sp"></div><el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="还没有发布闲置，点击右上角发布第一件">
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="prod">
              <img class="thumb" :src="row.cover_url" alt="" />
              <div><b>{{ row.title }}</b><small>{{ row.goods_no }} · {{ row.campus_name }}</small></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="售价" width="110"><template #default="{ row }"><span class="num">¥{{ row.price }}</span></template></el-table-column>
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :code="row.status" type="goods_status" /></template></el-table-column>
        <el-table-column prop="view_count" label="浏览" width="90" />
        <el-table-column prop="create_time" label="发布时间" min-width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/portal/goods/${row.id}`)">查看</el-button>
            <el-button v-if="row.status === '在售'" link type="warning" @click="offShelf(row)">下架</el-button>
            <el-button v-if="row.status === '审核驳回'" link type="danger" @click="showRemark(row)">原因</el-button>
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
import { DICT } from '../../dict'
import { store } from '../../store'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const status = ref('')
const loading = ref(false)
const counts = ref({})

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/goods/page', { params: { page: page.value, size: 10, status: status.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
    const all = await http.get('/goods/page', { params: { page: 1, size: 100 } })
    const c = {}
    for (const r of all.records || []) c[r.status] = (c[r.status] || 0) + 1
    counts.value = c
  } finally {
    loading.value = false
  }
}

async function offShelf(row) {
  const { value } = await ElMessageBox.prompt('请填写下架原因', '下架确认', { inputValue: '暂时不想卖了' })
  await http.post(`/goods/${row.id}/off-shelf`, { reason: value })
  ElMessage.success('已下架')
  load()
}

const showRemark = (row) => ElMessageBox.alert(row.audit_remark || '未填写审核意见', '驳回原因')

onMounted(() => load())
</script>

<style scoped>
.prod { display: flex; gap: 10px; align-items: center; }
.prod b { font-weight: 600; font-size: 13px; display: block; }
.prod small { color: var(--ink2); font-size: 11.5px; }
</style>
""")

add("views/portal/MyOrders.vue", r"""
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
""")

add("views/portal/OrderDetail.vue", r"""
<template>
  <div v-if="order.id">
    <div class="page-h"><div><h2>订单 {{ order.order_no }}</h2><p>下单 {{ fmtTime(order.create_time) }} ｜ 面交 {{ fmtTime(order.appoint_time) }} ｜ {{ order.appoint_place }}</p></div>
      <div class="sp"></div><StatusTag :code="order.status" type="order_status" /></div>
    <div class="split">
      <div class="card">
        <h3>商品快照</h3>
        <div class="prod"><img :src="order.goods_cover" class="cover" alt="" />
          <div><b>{{ order.goods_title }}</b><small>成交价 ¥{{ order.deal_price }} ｜ {{ order.pay_type }}</small></div></div>
        <el-descriptions :column="2" border style="margin-top: 14px">
          <el-descriptions-item label="卖家 ID">{{ order.seller_id }}</el-descriptions-item>
          <el-descriptions-item label="买家 ID">{{ order.buyer_id }}</el-descriptions-item>
          <el-descriptions-item label="卖家接单">{{ fmtTime(order.seller_confirm_time) }}</el-descriptions-item>
          <el-descriptions-item label="买家确认">{{ fmtTime(order.buyer_confirm_time) }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{ order.cancel_reason || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="card">
        <h3>可执行操作</h3>
        <div class="acts">
          <el-button v-if="actions.includes('confirm')" type="primary" @click="confirm">确认接单</el-button>
          <el-button v-if="actions.includes('complete')" type="primary" @click="complete">确认收货</el-button>
          <el-button v-if="actions.includes('cancel')" @click="cancel">取消订单</el-button>
          <el-button v-if="actions.includes('dispute')" type="danger" plain @click="dispute">发起纠纷</el-button>
          <el-button v-if="actions.includes('review')" type="success" @click="reviewDialog = true">去评价</el-button>
          <span v-if="actions.includes('wait')" class="muted">纠纷处理中，等待运营调解</span>
        </div>
        <h3 style="margin-top: 18px">评价</h3>
        <p class="muted" v-if="!reviews.length">双方都提交后才互相可见</p>
        <div v-for="r in reviews" :key="r.id" class="review">
          <b>{{ r.anonymous ? '匿名用户' : '用户 ' + r.from_user_id }}</b>
          <span class="stars">{{ '★'.repeat(r.score) }}</span>
          <p>{{ r.content }}</p>
        </div>
      </div>
    </div>

    <el-dialog v-model="reviewDialog" title="评价本次交易" width="440px">
      <el-form label-width="80px">
        <el-form-item label="评分"><el-rate v-model="rv.score" /></el-form-item>
        <el-form-item label="标签">
          <el-checkbox-group v-model="rv.tags">
            <el-checkbox label="描述一致" value="描述一致" /><el-checkbox label="准时" value="准时" />
            <el-checkbox label="好沟通" value="好沟通" /><el-checkbox label="包装完好" value="包装完好" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="rv.content" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
        <el-form-item label="匿名"><el-switch v-model="rv.anonymous" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="reviewDialog = false">取消</el-button><el-button type="primary" @click="submitReview">提交评价</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import { fmtTime } from '../../utils/format'

const route = useRoute()
const order = ref({})
const actions = ref([])
const reviews = ref([])
const reviewDialog = ref(false)
const rv = reactive({ score: 5, tags: ['准时'], content: '', anonymous: 0 })
const id = computed(() => route.params.id)

async function load() {
  const data = await http.get(`/trade-order/${id.value}`)
  order.value = data.order
  actions.value = data.actions || []
  reviews.value = data.reviews || []
}

async function confirm() {
  const { value } = await ElMessageBox.prompt('填写实际成交价', '确认接单', { inputValue: String(order.value.deal_price) })
  await http.post(`/trade-order/${id.value}/confirm`, { dealPrice: Number(value), payType: order.value.pay_type })
  ElMessage.success('已确认接单')
  load()
}
async function complete() {
  await ElMessageBox.confirm('确认已当面验货并收到商品？', '确认收货', { type: 'warning' })
  await http.post(`/trade-order/${id.value}/complete`)
  ElMessage.success('已确认收货')
  load()
}
async function cancel() {
  const { value } = await ElMessageBox.prompt('请填写取消原因（不少于 5 字）', '取消订单')
  await http.post(`/trade-order/${id.value}/cancel`, { reason: value })
  ElMessage.success('已取消')
  load()
}
async function dispute() {
  const { value } = await ElMessageBox.prompt('描述未见面或货不对板的情况（不少于 5 字）', '发起纠纷')
  await http.post(`/trade-order/${id.value}/dispute`, { reason: value, targetStatus: '纠纷中', attachments: [] })
  ElMessage.success('已发起纠纷')
  load()
}
async function submitReview() {
  const data = await http.post(`/trade-order/${id.value}/review`, { ...rv, tags: rv.tags.join(',') })
  ElMessage.success(data.tip || '评价已提交')
  reviewDialog.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.prod { display: flex; gap: 12px; align-items: center; }
.cover { width: 96px; height: 72px; object-fit: cover; border-radius: 8px; }
.prod b { display: block; font-size: 14px; }
.prod small { color: var(--ink2); font-size: 12px; }
.acts { display: flex; flex-wrap: wrap; gap: 8px; }
.muted { color: var(--ink2); font-size: 12.5px; }
.review { border-top: 1px solid var(--line); padding-top: 10px; margin-top: 10px; font-size: 13px; }
.stars { color: var(--a); margin-left: 8px; font-size: 12px; }
</style>
""")

add("views/portal/MyCredit.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>我的信用</h2><p>信用分决定可同时在线商品数与发布权限：低于 60 限 3 件，低于 40 禁止发布 30 天</p></div></div>
    <div class="grid g4">
      <div class="kpi"><small>当前信用分</small><b>{{ store.user?.creditScore ?? '-' }}</b><i>{{ level }}</i></div>
      <div class="kpi"><small>近 30 天变动</small><b>{{ recent }}</b><i>来自流水统计</i></div>
      <div class="kpi"><small>在线商品上限</small><b>{{ (store.user?.creditScore ?? 100) < 60 ? '3 件' : '不限' }}</b><i>按当前信用分</i></div>
      <div class="kpi"><small>发布权限</small><b>{{ (store.user?.creditScore ?? 100) < 40 ? '受限' : '正常' }}</b><i>低于 40 分受限 30 天</i></div>
    </div>
    <div class="card" style="margin-top: 16px">
      <h3>信用分变动明细</h3>
      <el-table :data="rows" empty-text="暂无信用变动记录">
        <el-table-column prop="create_time" label="时间" min-width="170" />
        <el-table-column prop="reason_type" label="原因" min-width="130" />
        <el-table-column label="变动" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.change_value >= 0 ? 'var(--ok)' : 'var(--bad)' }">{{ row.change_value > 0 ? '+' : '' }}{{ row.change_value }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="after_score" label="变动后" width="100" />
        <el-table-column prop="biz_no" label="关联单号" min-width="170" />
        <el-table-column prop="remark" label="说明" min-width="220" />
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" /></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../../http'
import { store } from '../../store'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const level = computed(() => {
  const s = store.user?.creditScore ?? 100
  return s >= 120 ? '等级：优秀' : s >= 100 ? '等级：良好' : s >= 60 ? '等级：正常' : '等级：受限'
})
const recent = computed(() => rows.value.slice(0, 10).reduce((s, r) => s + (r.change_value || 0), 0))

async function load(p) {
  if (p) page.value = p
  const data = await http.get('/credit/page', { params: { page: page.value, size: 10 } })
  rows.value = data.records || []
  total.value = data.total || 0
}

onMounted(() => load())
</script>
""")

add("views/portal/MyReviews.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>我的评价</h2><p>双方都提交后才互相可见；完成后 7 天内可评价，逾期关闭</p></div></div>
    <div class="card">
      <h3>待我评价</h3>
      <el-table :data="pending" empty-text="没有待评价的订单">
        <el-table-column prop="order_no" label="订单号" min-width="170" />
        <el-table-column prop="goods_title" label="商品" min-width="240" />
        <el-table-column prop="buyer_confirm_time" label="完成时间" min-width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }"><el-button link type="primary" @click="$router.push(`/portal/orders/${row.id}`)">去评价</el-button></template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card">
      <h3>我收到的评价</h3>
      <el-table :data="received" empty-text="暂无评价">
        <el-table-column label="评价人" width="140">
          <template #default="{ row }">{{ row.anonymous ? '匿名用户' : '用户 ' + row.from_user_id }}</template>
        </el-table-column>
        <el-table-column label="评分" width="130"><template #default="{ row }"><span class="stars">{{ '★'.repeat(row.score) }}</span></template></el-table-column>
        <el-table-column prop="tags" label="标签" min-width="150" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="create_time" label="时间" min-width="170" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../../http'

const pending = ref([])
const received = ref([])

onMounted(async () => {
  const done = await http.get('/trade-order/page', { params: { page: 1, size: 50, status: '已完成' } })
  pending.value = (done.records || []).filter((o) => !o.review_count)
  const reviewPage = await http.get('/report/review/page', { params: { page: 1, size: 50 } })
  received.value = reviewPage.records || []
})
</script>

<style scoped>
.stars { color: var(--a); font-size: 12px; }
</style>
""")

add("views/portal/Messages.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>消息</h2><p>审核结果、订单提醒、纠纷处理、信用变动都会在这里通知</p></div>
      <div class="sp"></div><el-button @click="readAll">全部标为已读</el-button></div>
    <div class="card">
      <el-table :data="rows" empty-text="暂无消息">
        <el-table-column prop="msg_type" label="类型" width="100" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="360" show-overflow-tooltip />
        <el-table-column prop="create_time" label="时间" min-width="170" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><span :class="row.read_flag === 0 ? 'tag bad' : 'tag gray'">{{ row.read_flag === 0 ? '未读' : '已读' }}</span></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../../http'

const rows = ref([])
const load = async () => { rows.value = await http.get('/system/messages') }
const readAll = async () => { await http.put('/system/messages/read-all'); load() }

onMounted(load)
</script>
""")

add("views/portal/Profile.vue", r"""
<template>
  <div>
    <div class="page-h"><div><h2>个人资料</h2><p>头像上传后右上角同步显示；校区由运营按学号绑定，如需修改请联系运营</p></div></div>
    <div class="split">
      <div class="card">
        <h3>基本资料</h3>
        <el-form :model="form" label-width="100px">
          <el-form-item label="头像"><UploadBox v-model="form.avatar" biz-type="avatar" :multiple="false" :max="1" /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.realName" style="max-width: 320px" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" style="max-width: 320px" /></el-form-item>
          <el-form-item label="学号"><el-input :model-value="store.user?.studentNo" disabled style="max-width: 320px" /></el-form-item>
          <el-form-item label="校区"><el-input :model-value="store.user?.campusName" disabled style="max-width: 320px" /></el-form-item>
          <el-button type="primary" @click="save">保存修改</el-button>
        </el-form>
      </div>
      <div class="card">
        <h3>修改密码</h3>
        <el-form :model="pwd" label-width="100px">
          <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password style="max-width: 320px" /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password style="max-width: 320px" /></el-form-item>
          <el-form-item label="确认新密码"><el-input v-model="pwd.confirm" type="password" show-password style="max-width: 320px" /></el-form-item>
          <el-button type="primary" @click="savePwd">更新密码</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../http'
import UploadBox from '../../components/UploadBox.vue'
import { store } from '../../store'

const form = reactive({ realName: '', phone: '', avatar: '' })
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

async function load() {
  const data = await http.get('/auth/me')
  store.setAuth({ user: data.user, menus: data.menus })
  form.realName = data.user.realName
  form.phone = data.user.phone
  form.avatar = data.user.avatar
}

async function save() {
  await http.put('/auth/profile', form)
  ElMessage.success('资料已更新')
  load()
}

async function savePwd() {
  if (pwd.newPassword !== pwd.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  await http.put('/auth/password', { oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
  ElMessage.success('密码已更新')
  pwd.oldPassword = pwd.newPassword = pwd.confirm = ''
}

onMounted(load)
</script>
""")

for name, content in V.items():
    path = os.path.join(SRC, name.replace("/", os.sep))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

print("portal views:", len(V))
