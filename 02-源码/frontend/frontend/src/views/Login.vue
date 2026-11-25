<template>
  <div class="login">
    <div class="left">
      <div class="bg" :style="{ backgroundImage: `url(${bg})` }"></div>
      <div class="veil"></div>
      <div class="left-in">
        <div class="brand">
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="#E8C9C3" stroke-width="1.8">
            <path d="M2 8h10v8H2zM12 11h4l3 3v2h-7z" /><circle cx="6" cy="18" r="1.6" /><circle cx="16" cy="18" r="1.6" />
          </svg>
          运联通 TMS
        </div>
        <h1>让每一车货<br />都有据可查</h1>
        <p>运输需求 → 审核建单 → 车辆驾驶员调度 → 装车发运 → 节点上报 → 签收回单 → 费用对账，一条台账走完公路运输全流程。</p>
        <div class="stats">
          <div><b class="num">{{ stats.orders }}</b><span>在管运单</span></div>
          <div><b class="num">{{ stats.ontime }}%</b><span>准时送达率</span></div>
          <div><b class="num">{{ stats.vehicles }}</b><span>在册车辆</span></div>
        </div>
        <div class="foot">本系统仅登记线下收付款，不接入在线支付 · 演示环境</div>
      </div>
    </div>
    <div class="right">
      <form @submit.prevent="submit">
        <div>
          <div class="title">账号登录</div>
          <div class="mut" style="margin-top: 6px; font-size: 12px">请使用单位分配的账号登录</div>
        </div>
        <el-input v-model="form.username" size="large" placeholder="用户名" />
        <el-input v-model="form.password" size="large" type="password" placeholder="密码" show-password @keyup.enter="submit" />
        <div class="role-pick">
          <span v-for="r in roles" :key="r.username" :class="{ on: form.username === r.username }" @click="fill(r)">{{ r.label }}</span>
        </div>
        <el-button type="primary" size="large" :loading="loading" native-type="submit">登 录</el-button>
        <div class="rows">
          <span class="mut">忘记密码请联系车队管理员重置</span>
          <span class="mut">v1.0</span>
        </div>
        <div class="note">演示账号：admin / admin123（车队经理）· dispatcher01 / 123456（调度员）· driver01 / 123456（驾驶员）· customer01 / 123456（客户）· finance01 / 123456（财务）</div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../http'
import { roleHome, store } from '../store'

/** 登录页：左侧品牌叙事（真实车队照片 + 绛红蒙层），右侧账号表单；数字取自后端公开统计接口 */
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const bg = '/uploads/seed/scene/01.jpg'
const form = reactive({ username: 'dispatcher01', password: '123456' })
const stats = reactive({ orders: '-', ontime: '-', vehicles: '-' })
const roles = [
  { label: '调度员', username: 'dispatcher01' },
  { label: '驾驶员', username: 'driver01' },
  { label: '客户', username: 'customer01' },
  { label: '财务', username: 'finance01' },
  { label: '经理', username: 'admin' },
  { label: '备用经理', username: 'manager02' }
]

function fill(r) {
  form.username = r.username
  form.password = r.username === 'admin' ? 'admin123' : '123456'
}

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名与密码')
    return
  }
  loading.value = true
  try {
    const data = await http.post('/auth/login', { ...form })
    store.setAuth(data)
    ElMessage.success('欢迎回来，' + data.user.realName)
    router.push(route.query.redirect || roleHome(data.user.roleCode))
  } catch (e) {
    /* 统一拦截已提示 */
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const s = await http.get('/auth/site-stats')
    stats.orders = s.orders
    stats.ontime = s.ontime
    stats.vehicles = s.vehicles
  } catch (e) {
    /* 打不开就用占位短横线，不抛错打断登录 */
  }
})
</script>

<style scoped>
.login { display: grid; grid-template-columns: 1.15fr .85fr; min-height: 100vh; }
.left { position: relative; color: #fff; padding: 56px; overflow: hidden; display: flex; }
.bg { position: absolute; inset: 0; background-size: cover; background-position: center; transform: scale(1.02); }
.veil { position: absolute; inset: 0; background: linear-gradient(150deg, rgba(46, 15, 22, .94), rgba(91, 37, 51, .88) 45%, rgba(122, 57, 71, .82)); }
.left-in { position: relative; display: flex; flex-direction: column; justify-content: space-between; width: 100%; }
.brand { display: flex; align-items: center; gap: 10px; font-size: 15px; letter-spacing: 1px; margin-bottom: 44px; }
h1 { font-size: 30px; line-height: 1.35; font-weight: 600; letter-spacing: 1px; margin: 0; }
p { opacity: .92; margin-top: 14px; font-size: 13px; max-width: 430px; line-height: 1.8; text-shadow: 0 1px 8px rgba(0, 0, 0, .45); }
.stats { display: flex; gap: 34px; margin-top: 36px; }
.stats b { font-size: 22px; font-weight: 600; }
.stats span { display: block; opacity: .72; font-size: 11.5px; }
.foot { opacity: .58; font-size: 11.5px; margin-top: 40px; }
.right { background: #fff; display: grid; place-items: center; padding: 40px; }
form { width: 360px; display: flex; flex-direction: column; gap: 14px; }
.title { font-size: 20px; font-weight: 600; }
.role-pick { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.role-pick span { border: 1px solid var(--line); border-radius: 5px; padding: 7px 4px; text-align: center; font-size: 11.5px; color: var(--ink2); cursor: pointer; }
.role-pick span.on { border-color: var(--p); color: var(--p); background: #fbf3f4; font-weight: 600; }
.rows { display: flex; justify-content: space-between; font-size: 12px; }

/* 移动端：主视觉压成顶部横幅（只留品牌与主张），表单整宽下沉，避免两侧并排把表单挤出屏幕 */
@media (max-width: 900px) {
  .login { grid-template-columns: 1fr; min-height: 100vh; }
  .left { padding: 22px 22px 26px; min-height: 200px; }
  .left-in { justify-content: flex-start; }
  .brand { margin-bottom: 18px; font-size: 13.5px; }
  h1 { font-size: 22px; line-height: 1.4; }
  p, .stats, .foot { display: none; }
  .right { padding: 26px 22px 34px; align-items: flex-start; }
  form { width: 100%; max-width: 420px; gap: 12px; }
  .note { font-size: 11px; line-height: 1.7; }
}
</style>
