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
