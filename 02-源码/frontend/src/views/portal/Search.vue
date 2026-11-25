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
