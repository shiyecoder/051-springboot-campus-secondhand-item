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
      <img :src="bannerImg" alt="" />
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
const bannerImg = '/uploads/seed/bg/campus01.jpg'
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
