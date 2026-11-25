<template>
  <div>
    <div class="page-h"><div><h2>菜单管理</h2><p>目录 / 菜单 / 按钮三级；按钮权限用于控制行内操作</p></div></div>
    <div class="card">
      <el-table :data="rows" row-key="id" default-expand-all empty-text="暂无菜单">
        <el-table-column prop="menu_name" label="菜单名称" min-width="240" />
        <el-table-column prop="menu_type" label="类型" width="100" />
        <el-table-column prop="path" label="路由" min-width="180" />
        <el-table-column prop="perm" label="权限标识" min-width="180" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="可见" width="80"><template #default="{ row }">{{ row.visible ? '显示' : '隐藏' }}</template></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../http'

const rows = ref([])
onMounted(async () => {
  rows.value = await http.get('/system/menus')
})
</script>
