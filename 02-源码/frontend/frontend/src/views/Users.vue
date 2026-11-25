<template>
  <div>
    <div class="page-h"><div><h2>用户管理</h2><p>账号、角色、组织与状态；密码 BCrypt 保存，任何接口不返回密码字段</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">新建用户</el-button></div>
    <div class="filter">
      <div class="fi"><label>关键字</label><el-input v-model="q.keyword" placeholder="用户名/姓名" style="width: 190px" clearable /></div>
      <div class="fi"><label>角色</label>
        <el-select v-model="q.role_code" clearable placeholder="全部" style="width: 180px">
          <el-option v-for="r in roles" :key="r.role_code" :value="r.role_code" :label="r.role_name" />
        </el-select></div>
      <div class="sp"></div><el-button type="primary" @click="load">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无用户">
        <el-table-column label="头像" min-width="70"><template #default="{ row }"><img v-if="row.avatar" class="thumb" :src="row.avatar" style="width: 32px; height: 32px; border-radius: 50%" alt="" /></template></el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="real_name" label="姓名" min-width="100" />
        <el-table-column prop="role_name" label="角色" min-width="120" />
        <el-table-column prop="org_name" label="所属组织" min-width="160" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="最近登录" min-width="150"><template #default="{ row }"><span class="num">{{ fmtTime(row.last_login) }}</span></template></el-table-column>
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }"><span class="tag" :class="row.status === 1 ? 'ok' : 'gray'">{{ row.status === 1 ? '启用' : '停用' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">编辑</el-button>
            <el-button link @click="reset(row)">重置密码</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="toggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="(p) => { page = p; load() }" /></div>
    </div>

    <el-drawer v-model="drawer" :title="form.id ? '编辑用户' : '新建用户'" size="520px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="用户名" required><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名" required><el-input v-model="form.real_name" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role_code" style="width: 100%">
            <el-option v-for="r in roles" :key="r.role_code" :value="r.role_code" :label="r.role_name" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属组织">
          <el-select v-model="form.org_id" style="width: 100%">
            <el-option v-for="o in orgs" :key="o.id" :value="o.id" :label="o.org_name" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="初始密码"><el-input v-model="form.password" placeholder="留空则不修改，默认 123456" /></el-form-item>
        <el-form-item label="头像"><UploadBox v-model="form.avatar" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import UploadBox from '../components/UploadBox.vue'
import { fmtTime } from '../utils/format'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const roles = ref([])
const orgs = ref([])
const q = reactive({ keyword: '', role_code: '' })
const form = reactive({})

async function load() {
  loading.value = true
  try {
    const data = await http.get('/system/users', { params: { page: page.value, size: 10, ...q } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open(row) {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row ? { ...row } : { status: 1, role_code: 'ROLE_DISPATCHER', password: '123456' })
  drawer.value = true
}

async function save() {
  await http.post('/system/users', form)
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

async function reset(row) {
  await ElMessageBox.confirm('确认将「' + row.real_name + '」的密码重置为 123456？', '重置密码', { type: 'warning' })
  await http.put(`/system/users/${row.id}/reset`)
  ElMessage.success('已重置为 123456')
}

async function toggle(row) {
  await http.put(`/system/users/${row.id}/status?status=${row.status === 1 ? 0 : 1}`)
  ElMessage.success('状态已更新')
  load()
}

onMounted(async () => {
  roles.value = await http.get('/system/roles')
  orgs.value = await http.get('/system/orgs')
  load()
})
</script>
