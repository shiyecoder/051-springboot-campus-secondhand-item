<template>
  <div class="p-shell">
    <div class="p-top">
      <div class="logo">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#5B2533" stroke-width="1.8">
          <path d="M2 8h10v8H2zM12 11h4l3 3v2h-7z" /><circle cx="6" cy="18" r="1.6" /><circle cx="16" cy="18" r="1.6" />
        </svg>
        运联通 · 货主门户
      </div>
      <div class="p-nav">
        <router-link v-for="n in nav" :key="n.to" :to="n.to" :class="{ on: route.path === n.to }">{{ n.label }}</router-link>
      </div>
      <div class="sp"></div>
      <el-dropdown @command="onCommand">
        <div class="who">
          <div class="ava">{{ (store.user?.realName || 'C').slice(0, 1) }}</div>
          <div><small>客户账号</small><i>{{ store.user?.orgName }}</i></div>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="p-body"><router-view /></div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { store } from '../store'

/** 货主门户：白底导航 + 绛红 hero，圆角大、留白多（设计系统 5 节） */
const route = useRoute()
const router = useRouter()
const nav = [
  { to: '/portal', label: '首页' },
  { to: '/portal/orders', label: '我的运单' },
  { to: '/portal/demand', label: '提交需求' },
  { to: '/portal/sign', label: '签收确认' },
  { to: '/portal/review', label: '服务评价' }
]

function onCommand(c) {
  if (c === 'logout') {
    store.clear()
    router.push('/login')
  }
}
</script>

<style scoped>
.p-shell { min-height: 100vh; background: var(--bg); }
.p-top { height: 60px; background: #fff; border-bottom: 1px solid var(--line); display: flex; align-items: center; padding: 0 40px; gap: 28px; position: sticky; top: 0; z-index: 9; }
.p-top .logo { font-size: 15px; font-weight: 600; color: var(--p); display: flex; align-items: center; gap: 8px; }
.p-nav { display: flex; gap: 22px; font-size: 13px; }
.p-nav a { color: var(--ink2); padding: 19px 0; text-decoration: none; border-bottom: 2px solid transparent; }
.p-nav a.on { color: var(--p); border-bottom-color: var(--p); font-weight: 600; }
.sp { flex: 1; }
.who { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.ava { width: 30px; height: 30px; border-radius: 50%; background: var(--p); color: #fff; display: grid; place-items: center; font-size: 12px; }
.who small { display: block; color: var(--ink2); font-size: 11px; line-height: 1.2; }
.who i { font-style: normal; font-size: 12px; font-weight: 600; }
.p-body { max-width: 1180px; margin: 0 auto; padding: 26px 24px 60px; }
</style>
