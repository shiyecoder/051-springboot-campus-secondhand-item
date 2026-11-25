<template>
  <div class="shell">
    <aside class="side">
      <div class="brand">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#E8F0EE" stroke-width="1.8">
          <path d="M4 8h16v12H4z" /><path d="M8 8V5h8v3" /><path d="M9 13h6" />
        </svg>
        <b>拾光集</b><span>SECOND HAND</span>
      </div>
      <nav class="nav">
        <template v-for="g in menus" :key="g.id">
          <div class="grp">{{ g.name }}</div>
          <router-link v-for="m in g.children || []" :key="m.id" :to="m.path" :class="{ on: isOn(m.path) }">
            <svg viewBox="0 0 24 24" fill="currentColor" v-html="iconOf(m.icon)"></svg>{{ m.name }}
          </router-link>
        </template>
      </nav>
    </aside>
    <div class="main">
      <header class="top">
        <div class="crumb">校园二手 / <b>{{ route.meta.title || '' }}</b></div>
        <div class="sp"></div>
        <router-link class="iconbtn" to="/admin/messages" title="消息中心">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M6 16V10a6 6 0 1 1 12 0v6l2 3H4z" /></svg>
          <i class="dot" v-if="unread"></i>
        </router-link>
        <el-dropdown @command="onCommand">
          <div class="who">
            <img v-if="store.user?.avatar" class="ava-img" :src="store.user.avatar" alt="" />
            <div v-else class="ava">{{ (store.user?.realName || 'U').slice(0, 1) }}</div>
            <div><small>{{ store.user?.campusName || '' }}</small><i>{{ store.user?.realName }} · {{ store.user?.roleName }}</i></div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="messages">消息中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>
      <section class="content"><router-view /></section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { store } from '../store'
import http from '../http'

/** 后台骨架：220px 侧栏 + 56px 顶栏 + 自适应内容区（设计系统第 5 节） */
const route = useRoute()
const router = useRouter()
const unread = ref(0)

const ICONS = {
  dash: '<path d="M3 13h8V3H3v10zm10 8h8V11h-8v10zM3 21h8v-6H3v6zM13 9h8V3h-8v6z"/>',
  check: '<path d="M4 12l5 5L20 6" fill="none" stroke="currentColor" stroke-width="2"/>',
  box: '<path d="M3 7l9-4 9 4v10l-9 4-9-4z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  order: '<path d="M6 2h9l5 5v15H6z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  star: '<path d="M12 3l2.8 5.9 6.2.8-4.6 4.3 1.2 6.2L12 17.4 6.4 20.2l1.2-6.2L3 9.7l6.2-.8z" fill="none" stroke="currentColor" stroke-width="1.5"/>',
  alert: '<path d="M12 3l9 17H3z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  medal: '<circle cx="12" cy="14" r="6" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M8 3l3 6M16 3l-3 6" stroke="currentColor" stroke-width="1.6"/>',
  chart: '<path d="M4 20V9M10 20V4M16 20v-7M22 20H2" fill="none" stroke="currentColor" stroke-width="1.7"/>',
  gear: '<circle cx="12" cy="12" r="3.2" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  tag: '<path d="M3 12l9-9 9 9-9 9z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  pin: '<path d="M12 22s7-6.3 7-12A7 7 0 1 0 5 10c0 5.7 7 12 7 12z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  rule: '<path d="M4 6h16M4 12h10M4 18h13" stroke="currentColor" stroke-width="1.7"/>',
  bell: '<path d="M6 16V10a6 6 0 1 1 12 0v6l2 3H4z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  user: '<circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="M4 21c0-4.4 3.6-7 8-7s8 2.6 8 7" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  shield: '<path d="M12 3l8 3v6c0 5-3.4 8.2-8 9-4.6-.8-8-4-8-9V6z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  list: '<path d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" stroke-width="1.7"/>',
  book: '<path d="M4 4h7v16H4zM13 4h7v16h-7z" fill="none" stroke="currentColor" stroke-width="1.6"/>',
  doc: '<path d="M5 3h10l4 4v14H5z" fill="none" stroke="currentColor" stroke-width="1.6"/>'
}
const iconOf = (k) => ICONS[k] || ICONS.doc
const isOn = (p) => p && (route.path === p || route.path.startsWith(p + '/'))
const menus = computed(() => store.menus.filter((m) => m.type === 'catalog'))

function onCommand(c) {
  if (c === 'logout') {
    store.clear()
    router.push('/login')
  } else if (c === 'profile') {
    router.push('/admin/profile')
  } else if (c === 'messages') {
    router.push('/admin/messages')
  }
}

onMounted(async () => {
  try {
    unread.value = await http.get('/auth/unread')
  } catch (e) {
    unread.value = 0
  }
})
</script>

<style scoped>
.shell { display: flex; min-height: 100vh; }
.side { width: 220px; flex: 0 0 220px; background: var(--p); color: #EAF3F1; display: flex; flex-direction: column; position: sticky; top: 0; height: 100vh; }
.brand { height: 56px; display: flex; align-items: center; gap: 9px; padding: 0 18px; border-bottom: 1px solid rgba(255,255,255,.12); }
.brand b { font-size: 14px; letter-spacing: .5px; }
.brand span { font-size: 10px; opacity: .6; letter-spacing: 1.4px; }
.nav { padding: 10px 0; overflow: auto; flex: 1; }
.nav .grp { padding: 12px 18px 6px; font-size: 11px; opacity: .55; letter-spacing: 1px; }
.nav a { display: flex; align-items: center; gap: 9px; padding: 9px 18px; color: #DCEBE7; font-size: 13px; border-left: 3px solid transparent; }
.nav a:hover { background: rgba(255,255,255,.08); color: #fff; }
.nav a.on { background: rgba(255,255,255,.14); color: #fff; border-left-color: var(--a); font-weight: 600; }
.nav a svg { width: 15px; height: 15px; opacity: .85; flex: 0 0 15px; }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.top { height: 56px; background: #fff; border-bottom: 1px solid var(--line); display: flex; align-items: center; gap: 16px; padding: 0 24px; position: sticky; top: 0; z-index: 9; }
.crumb { font-size: 12px; color: var(--ink2); }
.crumb b { color: var(--ink); font-weight: 600; }
.iconbtn { width: 32px; height: 32px; border-radius: 6px; display: grid; place-items: center; color: var(--ink2); position: relative; }
.iconbtn:hover { background: #EAF3F1; color: var(--p); }
.dot { position: absolute; top: 5px; right: 5px; width: 6px; height: 6px; background: var(--bad); border-radius: 50%; }
.who { display: flex; align-items: center; gap: 8px; padding-left: 14px; border-left: 1px solid var(--line); cursor: pointer; }
.ava { width: 28px; height: 28px; border-radius: 50%; background: var(--p2); color: #fff; display: grid; place-items: center; font-size: 11px; }
.ava-img { width: 28px; height: 28px; border-radius: 50%; object-fit: cover; }
.who small { display: block; color: var(--ink2); font-size: 11px; line-height: 1.2; }
.who i { font-style: normal; font-size: 12px; font-weight: 600; }
.content { padding: 24px; flex: 1; }
</style>
