<template>
  <div class="d-shell">
    <div class="d-top">
      <div class="logo">司机端 · {{ store.user?.realName }}</div>
      <div class="d-nav">
        <router-link v-for="n in nav" :key="n.to" :to="n.to" :class="{ on: route.path === n.to }">{{ n.label }}</router-link>
      </div>
      <div class="sp"></div>
      <el-button link style="color: #fff" @click="logout">退出</el-button>
    </div>
    <div class="d-body"><router-view /></div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { store } from '../store'

/** 司机端：绛红顶栏 + 单列任务卡，按钮 ≥44px，移动优先（设计系统 5 节） */
const route = useRoute()
const router = useRouter()
const nav = [
  { to: '/driver/tasks', label: '我的任务' },
  { to: '/driver/node', label: '节点上报' },
  { to: '/driver/fee', label: '费用提交' },
  { to: '/driver/exception', label: '异常上报' }
]
function logout() {
  store.clear()
  router.push('/login')
}
</script>

<style scoped>
.d-shell { min-height: 100vh; background: var(--bg); }
.d-top { background: var(--p); color: #fff; padding: 12px 20px; display: flex; align-items: center; gap: 18px; flex-wrap: wrap; }
.d-top .logo { font-size: 14px; font-weight: 600; }
.d-nav { display: flex; gap: 6px; flex-wrap: wrap; }
.d-nav a { color: #e7d6d3; text-decoration: none; font-size: 13px; padding: 8px 12px; border-radius: 5px; }
.d-nav a.on { background: rgba(255, 255, 255, .16); color: #fff; font-weight: 600; }
.sp { flex: 1; }
.d-body { max-width: 960px; margin: 0 auto; padding: 18px 16px 70px; }
</style>
