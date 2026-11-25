<template>
  <div>
    <div class="page-h"><div><h2>角色管理</h2><p>菜单权限 + 按钮权限 + 数据范围三层控制；分配菜单时「菜单管理」项默认隐藏</p></div></div>
    <div class="card">
      <el-table :data="rows" empty-text="暂无角色">
        <el-table-column prop="role_name" label="角色名称" min-width="140" />
        <el-table-column prop="role_code" label="编码" min-width="150" />
        <el-table-column label="数据范围" width="120">
          <template #default="{ row }">{{ { all: '全部', dept: '本部门', self: '本人' }[row.data_scope] || row.data_scope }}</template>
        </el-table-column>
        <el-table-column prop="menu_count" label="菜单数" width="100" />
        <el-table-column prop="user_count" label="用户数" width="100" />
        <el-table-column prop="remark" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="assign(row)">分配菜单</el-button></template>
        </el-table-column>
      </el-table>
    </div>
    <el-drawer v-model="drawer" :title="'为「' + (current.role_name || '') + '」分配菜单'" size="480px">
      <el-tree ref="treeRef" :data="tree" show-checkbox node-key="id" :props="{ label: 'menu_name', children: 'children' }" default-expand-all />
      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" @click="save">保存授权</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'

const rows = ref([])
const tree = ref([])
const drawer = ref(false)
const current = ref({})
const treeRef = ref(null)

async function load() {
  rows.value = await http.get('/system/roles')
  tree.value = await http.get('/system/menus')
}

async function assign(row) {
  current.value = row
  drawer.value = true
  const ids = await http.get(`/system/roles/${row.id}/menus`)
  setTimeout(() => treeRef.value && treeRef.value.setCheckedKeys(ids), 60)
}

async function save() {
  const checked = treeRef.value.getCheckedKeys()
  const half = treeRef.value.getHalfCheckedKeys()
  await http.put(`/system/roles/${current.value.id}/menus`, [...checked, ...half])
  ElMessage.success('授权已保存，重新登录后生效')
  drawer.value = false
  load()
}

onMounted(load)
</script>
