<template>
  <div class="pshell">
    <header class="pnav">
      <router-link class="logo" to="/portal">拾光集<span>校园闲置</span></router-link>
      <router-link v-for="n in nav" :key="n.path" class="lk" :class="{ on: isOn(n.path) }" :to="n.path">{{ n.name }}</router-link>
      <div class="sp"></div>
      <div class="search" @click="$router.push('/portal/search')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><path d="M20 20l-4-4" /></svg>
        搜索教材、数码、代步工具…
      </div>
      <el-dropdown @command="onCommand">
        <div class="who">
          <img v-if="store.user?.avatar" class="ava-img" :src="store.user.avatar" alt="" />
          <div v-else class="ava">{{ (store.user?.realName || 'U').slice(0, 1) }}</div>
          <div><small>{{ store.user?.campusName }}</small><i>信用 {{ store.user?.creditScore ?? '-' }}</i></div>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人资料</el-dropdown-item>
            <el-dropdown-item command="credit">我的信用</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </header>
    <div class="pbody"><router-view /></div>
    <footer class="foot">拾光集 · 校园二手物品交易平台 ｜ 仅限本校学生，面交验货，不接入在线支付与快递</footer>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { store } from '../store'

/** 门户骨架：64px 顶部导航 + 1280px 居中内容区，学生买家与卖家共用 */
const route = useRoute()
const router = useRouter()

const nav = [
  { name: '首页', path: '/portal' },
  { name: '逛闲置', path: '/portal/search' },
  { name: '发布闲置', path: '/portal/publish' },
  { name: '我的闲置', path: '/portal/mygoods' },
  { name: '我的订单', path: '/portal/orders' },
  { name: '我的信用', path: '/portal/credit' },
  { name: '我的评价', path: '/portal/reviews' }
]
const isOn = (p) => (p === '/portal' ? route.path === '/portal' : route.path.startsWith(p))

function onCommand(c) {
  if (c === 'logout') {
    store.clear()
    router.push('/login')
  } else if (c === 'profile') {
    router.push('/portal/profile')
  } else if (c === 'credit') {
    router.push('/portal/credit')
  }
}
</script>

<style scoped>
.pshell { min-height: 100vh; display: flex; flex-direction: column; }
.pnav { height: 64px; background: #fff; border-bottom: 1px solid var(--line); display: flex; align-items: center; gap: 20px; padding: 0 32px; position: sticky; top: 0; z-index: 9; }
.logo { font-weight: 700; color: var(--p); font-size: 16px; display: flex; align-items: center; gap: 8px; }
.logo span { font-weight: 400; font-size: 11px; color: var(--ink2); }
.lk { font-size: 14px; color: var(--ink2); padding: 6px 2px; }
.lk.on { color: var(--p); font-weight: 600; border-bottom: 2px solid var(--p); }
.search { display: flex; align-items: center; gap: 8px; background: var(--bg); border: 1px solid var(--line); border-radius: 999px; padding: 7px 14px; width: 300px; color: var(--ink2); font-size: 13px; cursor: pointer; }
.who { display: flex; align-items: center; gap: 8px; padding-left: 14px; border-left: 1px solid var(--line); cursor: pointer; }
.ava { width: 28px; height: 28px; border-radius: 50%; background: var(--p2); color: #fff; display: grid; place-items: center; font-size: 11px; }
.ava-img { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
.who small { display: block; color: var(--ink2); font-size: 11px; line-height: 1.2; }
.who i { font-style: normal; font-size: 12px; font-weight: 600; }
.pbody { padding: 24px 32px; max-width: 1280px; margin: 0 auto; width: 100%; flex: 1; }
.foot { padding: 22px 32px; color: var(--ink2); font-size: 12px; text-align: center; border-top: 1px solid var(--line); background: #fff; }
@media (max-width: 900px) {
  .pnav { height: auto; flex-wrap: wrap; gap: 10px; padding: 10px 14px; }
  .search { display: none; }
  .pbody { padding: 14px; }
}
</style>
