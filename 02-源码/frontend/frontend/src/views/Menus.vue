<template>
  <div>
    <div class="page-h"><div><h2>菜单管理</h2><p>目录、菜单与按钮三级结构；菜单管理与分配菜单中的菜单管理选项按规范隐藏</p></div></div>
    <div class="card">
      <el-table :data="tree" row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="menu_name" label="菜单名称" min-width="200" />
        <el-table-column prop="menu_type" label="类型" min-width="100" />
        <el-table-column prop="path" label="路由路径" min-width="160" />
        <el-table-column prop="perm" label="权限标识" min-width="170" />
        <el-table-column prop="sort" label="排序" min-width="80" />
        <el-table-column label="可见" min-width="80">
          <template #default="{ row }"><span class="tag" :class="row.visible === 1 ? 'ok' : 'gray'">{{ row.visible === 1 ? '是' : '否' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="addChild(row)">新增子项</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" title="新增菜单" width="520px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="上级菜单"><el-input v-model="form.parent_name" disabled /></el-form-item>
        <el-form-item label="菜单名称"><el-input v-model="form.menu_name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.menu_type" style="width: 100%">
            <el-option value="menu" label="菜单" /><el-option value="button" label="按钮" /><el-option value="catalog" label="目录" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.perm" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'

const tree = ref([])
const dialog = ref(false)
const form = reactive({ parent_id: 0, parent_name: '', menu_name: '', menu_type: 'menu', path: '', perm: '', sort: 0 })

async function load() {
  tree.value = await http.get('/system/menus')
}

function addChild(row) {
  Object.assign(form, { parent_id: row.id, parent_name: row.menu_name, menu_name: '', menu_type: 'menu', path: '', perm: '', sort: row.sort + 1 })
  dialog.value = true
}

async function save() {
  await http.post('/system/menus', form)
  ElMessage.success('已保存')
  dialog.value = false
  load()
}

onMounted(load)
</script>
