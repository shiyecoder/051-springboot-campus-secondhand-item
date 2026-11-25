<template>
  <div>
    <div class="page-h"><div><h2>用户管理</h2><p>学生账号通过学号导入并绑定校区；密码字段不下发前端</p></div>
      <div class="sp"></div><el-button type="primary" @click="open()">新增用户</el-button></div>
    <div class="filter">
      <div class="fi"><label>关键字</label><el-input v-model="keyword" placeholder="登录名/姓名/学号" clearable style="width: 190px" @keyup.enter="load(1)" /></div>
      <div class="fi"><label>角色</label>
        <el-select v-model="roleCode" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option label="平台管理员" value="ROLE_ADMIN" /><el-option label="校园运营人员" value="ROLE_OPERATOR" />
          <el-option label="学生卖家" value="ROLE_SELLER" /><el-option label="学生买家" value="ROLE_BUYER" />
        </el-select>
      </div>
      <div class="sp"></div><el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="暂无用户">
        <el-table-column prop="username" label="登录名" min-width="120" />
        <el-table-column prop="real_name" label="姓名" min-width="110" />
        <el-table-column prop="student_no" label="学号" min-width="130" />
        <el-table-column prop="campus_name" label="校区" width="100" />
        <el-table-column prop="credit_score" label="信用分" width="90" />
        <el-table-column prop="role_name" label="角色" min-width="120" />
        <el-table-column label="状态" width="90"><template #default="{ row }"><StatusTag :code="row.status" type="enable_status" /></template></el-table-column>
        <el-table-column prop="last_login" label="最近登录" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">编辑</el-button>
            <el-button link type="warning" @click="reset(row)">重置密码</el-button>
            <el-button link type="danger" @click="toggle(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager"><span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" /></div>
    </div>
    <el-drawer v-model="drawer" :title="(form.id ? '编辑' : '新增') + '用户'" size="520px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="登录名" required><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名" required><el-input v-model="form.real_name" /></el-form-item>
        <el-form-item label="学号"><el-input v-model="form.student_no" /></el-form-item>
        <el-form-item label="校区">
          <el-select v-model="form.campus_name" style="width: 100%" @change="(v) => (form.campus_id = { 东校区: 1, 西校区: 2, 北校区: 3 }[v])">
            <el-option label="东校区" value="东校区" /><el-option label="西校区" value="西校区" /><el-option label="北校区" value="北校区" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role_code" style="width: 100%">
            <el-option label="平台管理员" value="ROLE_ADMIN" /><el-option label="校园运营人员" value="ROLE_OPERATOR" />
            <el-option label="学生卖家" value="ROLE_SELLER" /><el-option label="学生买家" value="ROLE_BUYER" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id"><el-input v-model="form.password" placeholder="默认 123456" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="drawer = false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const keyword = ref('')
const roleCode = ref('')
const form = reactive({ username: '', real_name: '', student_no: '', campus_id: 1, campus_name: '东校区', phone: '', role_code: 'ROLE_BUYER', password: '123456', status: 1 })

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const data = await http.get('/system/users', { params: { page: page.value, size: 10, keyword: keyword.value || undefined, role_code: roleCode.value || undefined } })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open(row) {
  Object.assign(form, row || { username: '', real_name: '', student_no: '', campus_id: 1, campus_name: '东校区', phone: '', role_code: 'ROLE_BUYER', password: '123456', status: 1 })
  drawer.value = true
}

async function submit() {
  await http.post('/system/users', form)
  ElMessage.success('保存成功')
  drawer.value = false
  load()
}

async function reset(row) {
  await ElMessageBox.confirm(`将「${row.real_name}」密码重置为 123456？`, '重置密码', { type: 'warning' })
  await http.put(`/system/users/${row.id}/reset`)
  ElMessage.success('已重置')
}

async function toggle(row) {
  await http.put(`/system/users/${row.id}/status`, null, { params: { status: row.status === 1 ? 0 : 1 } })
  ElMessage.success('状态已更新')
  load()
}

onMounted(() => load())
</script>
