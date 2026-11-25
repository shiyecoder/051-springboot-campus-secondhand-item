<template>
  <div>
    <div class="page-h"><div><h2>角色管理</h2><p>角色、数据范围与菜单授权；按钮权限与菜单权限分开控制</p></div></div>
    <div class="card" style="margin-bottom: 14px">
      <el-table :data="rows" size="small">
        <el-table-column prop="role_code" label="角色编码" min-width="180" />
        <el-table-column prop="role_name" label="角色名称" min-width="140" />
        <el-table-column prop="data_scope" label="数据范围" min-width="110" />
        <el-table-column prop="menu_count" label="菜单数" min-width="90" />
        <el-table-column prop="user_count" label="用户数" min-width="90" />
        <el-table-column prop="remark" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="assign(row)">分配菜单</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" v-if="cur">
      <div class="card-h"><h3>为「{{ cur.role_name }}」分配菜单</h3><div class="sp"></div>
        <el-button type="primary" size="small" @click="save">保存授权</el-button></div>
      <el-tree ref="treeRef" :data="menuTree" show-checkbox node-key="id" :default-checked-keys="checked"
        :props="{ label: 'menu_name', children: 'children' }" default-expand-all />
      <div class="note" style="margin-top: 12px">菜单权限不等于接口权限：写接口仍会做角色与数据范围校验。</div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'

const rows = ref([])
const menuTree = ref([])
const checked = ref([])
const cur = ref(null)
const treeRef = ref(null)

async function load() {
  rows.value = await http.get('/system/roles')
  menuTree.value = await http.get('/system/menus')
}

async function assign(row) {
  cur.value = row
  checked.value = await http.get(`/system/roles/${row.id}/menus`)
}

async function save() {
  const ids = treeRef.value.getCheckedKeys().concat(treeRef.value.getHalfCheckedKeys())
  await http.put(`/system/roles/${cur.value.id}/menus`, ids)
  ElMessage.success('授权已保存，重新登录后生效')
  load()
}

onMounted(load)
</script>
