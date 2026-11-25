<template>
  <div>
    <div class="page-h"><div><h2>个人信息</h2><p>修改资料、修改密码、上传头像，头像上传后右上角同步显示</p></div></div>
    <div class="grid2">
      <div class="card">
        <div class="card-h"><h3>基本资料</h3></div>
        <div style="display: flex; gap: 20px">
          <div style="text-align: center">
            <img v-if="form.avatar" :src="form.avatar" style="width: 88px; height: 88px; border-radius: 50%; object-fit: cover; border: 1px solid #e7dfdc" alt="" />
            <div v-else class="ava-big">{{ (form.real_name || 'U').slice(0, 1) }}</div>
            <div style="margin-top: 8px"><UploadBox v-model="form.avatar" text="上传头像" /></div>
          </div>
          <el-form :model="form" label-width="80px" style="flex: 1">
            <el-form-item label="用户名"><span class="mut">{{ user.username }}（不可修改）</span></el-form-item>
            <el-form-item label="姓名"><el-input v-model="form.real_name" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
            <el-form-item label="所属组织"><span class="mut">{{ user.orgName }}</span></el-form-item>
            <el-form-item label="角色"><span class="mut">{{ user.roleName }}</span></el-form-item>
          </el-form>
        </div>
        <div style="text-align: right; margin-top: 12px"><el-button type="primary" @click="saveProfile">保存资料</el-button></div>
      </div>
      <div class="card">
        <div class="card-h"><h3>修改密码</h3></div>
        <el-form :model="pwd" label-width="80px">
          <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认密码"><el-input v-model="pwd.confirm" type="password" show-password /></el-form-item>
        </el-form>
        <div class="note">密码 8-20 位，需包含字母与数字；修改后需重新登录。连续失败 5 次锁定 10 分钟。</div>
        <div style="text-align: right; margin-top: 14px"><el-button type="primary" @click="savePwd">修改密码</el-button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import UploadBox from '../components/UploadBox.vue'
import { store } from '../store'

const user = store.user || {}
const form = reactive({ real_name: user.realName, phone: user.phone, avatar: user.avatar })
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

async function saveProfile() {
  await http.put('/auth/profile', { realName: form.real_name, phone: form.phone, avatar: form.avatar })
  const me = await http.get('/auth/me')
  store.setAuth({ user: me.user, menus: me.menus })
  ElMessage.success('资料已保存，右上角头像已同步')
}

async function savePwd() {
  if (pwd.newPassword !== pwd.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  await http.put('/auth/password', { oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
  ElMessage.success('密码已修改，请重新登录')
  store.clear()
  location.hash = '#/login'
}
</script>

<style scoped>
.ava-big { width: 88px; height: 88px; border-radius: 50%; background: #2f6f6a; color: #fff; display: grid; place-items: center; font-size: 26px; }
</style>
