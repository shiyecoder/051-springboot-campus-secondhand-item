<template>
  <CrudPage
    title="信用分流水"
    desc="初始 100，好评 +2、差评 -5、放鸽子 -10、违规下架 -15，上下限 0–150；人工调整需填写原因"
    api="credit"
    entity="信用流水"
    :columns="columns"
    :filters="filters"
    :form-fields="[]"
    :can-create="false"
    :exportable="true"
  >
    <template #row-actions>
      <el-button link type="primary" @click="adjust">调整信用分</el-button>
    </template>
  </CrudPage>
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'

const columns = [
  { key: 'user_id', label: '用户ID', type: 'num', min: 90 },
  { key: 'change_value', label: '变动', type: 'num', min: 80 },
  { key: 'before_score', label: '变动前', type: 'num', min: 90 },
  { key: 'after_score', label: '变动后', type: 'num', min: 90 },
  { key: 'reason_type', label: '原因', dict: 'credit_reason', min: 120 },
  { key: 'biz_no', label: '关联单号', min: 160 },
  { key: 'remark', label: '说明', min: 220 },
  { key: 'create_time', label: '时间', type: 'time', min: 160 }
]
const filters = [
  { key: 'user_id', label: '用户ID' },
  { key: 'reason_type', label: '原因', type: 'select', dict: 'credit_reason' }
]

async function adjust() {
  const { value } = await ElMessageBox.prompt('按格式填写：用户ID,变动值,原因（如 7,-10,多次放鸽子核实）', '人工调整信用分')
  const parts = String(value || '').split(',')
  if (parts.length < 3) {
    ElMessage.warning('格式不正确')
    return
  }
  await http.post('/credit/adjust', { userId: Number(parts[0]), changeValue: Number(parts[1]), remark: parts.slice(2).join(',').trim() })
  ElMessage.success('已调整并写入流水')
}
</script>
