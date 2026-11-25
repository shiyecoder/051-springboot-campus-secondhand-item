<template>
  <div class="login">
    <div class="lleft">
      <div class="lbrand">拾光集 <span>校园二手</span></div>
      <h1>让闲置<br />在校园里流转起来</h1>
      <p>发布审核 · 分类检索 · 面交下单 · 双向评价 · 纠纷调解，只服务本校学生。</p>
      <div class="lstat">
        <div><b>{{ stats.goods ?? '-' }}</b><small>在售闲置</small></div>
        <div><b>{{ stats.deals ?? '-' }}</b><small>累计成交</small></div>
        <div><b>{{ stats.users ?? '-' }}</b><small>认证同学</small></div>
      </div>
      <div class="lbg" aria-hidden="true"></div>
    </div>
    <div class="lright">
      <div class="lform">
        <h2>登录</h2>
        <p class="sub">使用校园账号登录，学生账号由学号绑定校区</p>
        <el-form :model="form" @keyup.enter="submit">
          <el-form-item>
            <el-input v-model="form.username" size="large" placeholder="登录名" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="密码" />
          </el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">登录</el-button>
        </el-form>
        <div class="quick">
          <span>演示账号</span>
          <a @click="fill('admin')">管理员 admin</a>
          <a @click="fill('operator')">运营 operator</a>
          <a @click="fill('seller01')">卖家 seller01</a>
          <a @click="fill('buyer01')">买家 buyer01</a>
        </div>
        <p class="tip">默认密码 123456 ｜ 连续失败 5 次锁定 10 分钟</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../http'
import { roleHome, store } from '../store'

/** 登录页：左侧品牌与背景图（本地静态资源），右侧表单；不使用默认表单样式 */
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const stats = reactive({})

const fill = (u) => {
  form.username = u
  form.password = '123456'
}

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入登录名与密码')
    return
  }
  loading.value = true
  try {
    const data = await http.post('/auth/login', form)
    store.setAuth(data)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || roleHome(data.user.roleCode))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    Object.assign(stats, await http.get('/auth/site-stats'))
  } catch (e) { /* 统计失败不影响登录 */ }
})
</script>

<style scoped>
.login { display: grid; grid-template-columns: 1.15fr 1fr; min-height: 100vh; background: #fff; }
/* 左栏用真实校园照片打底，再压一层墨绿渐变蒙层，保证白字可读 */
.lleft { position: relative; padding: 56px 64px; color: #EAF3F1; overflow: hidden; display: flex; flex-direction: column;
  background-image: linear-gradient(155deg, rgba(20,107,91,.93), rgba(14,76,65,.95) 62%, rgba(11,59,51,.97)), url('/login-bg.jpg');
  background-size: cover; background-position: center; }
.lbrand { font-size: 17px; font-weight: 700; letter-spacing: 1px; }
.lbrand span { font-size: 11px; font-weight: 400; opacity: .7; margin-left: 8px; letter-spacing: 2px; }
.lleft h1 { font-size: 38px; line-height: 1.35; margin: 56px 0 16px; font-weight: 700; letter-spacing: 1px; }
.lleft p { font-size: 14px; line-height: 1.9; opacity: .92; max-width: 430px; }
.lstat { display: flex; gap: 44px; margin-top: auto; padding-top: 40px; position: relative; z-index: 2; }
.lstat b { font-size: 30px; font-variant-numeric: tabular-nums; }
.lstat small { display: block; font-size: 12px; opacity: .78; margin-top: 2px; }
.lbg { position: absolute; right: -80px; bottom: -60px; width: 420px; height: 420px; border-radius: 50%; background: radial-gradient(circle at 30% 30%, rgba(217,164,65,.45), rgba(217,164,65,0) 68%); }
.lright { display: grid; place-items: center; padding: 40px; background: #FBFCFC; }
.lform { width: 100%; max-width: 360px; }
.lform h2 { font-size: 24px; margin: 0 0 6px; }
.sub { font-size: 12.5px; color: var(--ink2); margin: 0 0 24px; }
.quick { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 18px; font-size: 12.5px; color: var(--ink2); }
.quick a { color: var(--p2); cursor: pointer; }
.quick a:hover { color: var(--p); text-decoration: underline; }
.tip { margin-top: 22px; font-size: 12px; color: var(--ink2); }
@media (max-width: 900px) {
  .login { grid-template-columns: 1fr; }
  .lleft { padding: 20px 22px; }
  .lleft h1 { font-size: 22px; margin: 12px 0 8px; }
  .lleft p { display: none; }
  .lstat { display: none; }
  .lbg { display: none; }
  .lright { padding: 24px 18px 40px; place-items: start center; }
}
</style>
