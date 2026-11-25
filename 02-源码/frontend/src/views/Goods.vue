<template>
  <CrudPage
    title="商品管理"
    desc="覆盖待审核/在售/已下架/已售出/审核驳回/冻结六种状态；状态只能通过审核与流转变更"
    api="goods"
    entity="商品"
    :columns="columns"
    :filters="filters"
    :form-fields="[]"
    :can-create="false"
    :action-width="250"
    link-base="/portal/goods"
  >
    <template #row-actions="{ row }">
      <el-button link type="warning" @click="offShelf(row)" v-if="row.status === '在售'">下架</el-button>
      <el-button link type="primary" @click="priceRef(row)">定价参考</el-button>
    </template>
  </CrudPage>
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'

/** 商品管理：后台全量列表，行内提供下架与定价参考 */
// 列宽合计控制在 1200 以内，1600 屏宽下不用横向滚动就能看全
const columns = [
  { key: 'cover_url', label: '封面', type: 'image', min: 64 },
  { key: 'goods_no', label: '商品编号', min: 125 },
  { key: 'title', label: '标题', min: 190 },
  { key: 'campus_name', label: '校区', min: 80 },
  { key: 'condition_level', label: '成色', dict: 'condition_level', min: 88 },
  { key: 'price', label: '售价', type: 'money', min: 90 },
  { key: 'original_price', label: '原价', type: 'money', min: 90 },
  { key: 'status', label: '状态', dict: 'goods_status', min: 90 },
  { key: 'create_time', label: '发布时间', type: 'time', min: 145 }
]
const filters = [
  { key: 'keyword', label: '关键词', placeholder: '标题或商品编号' },
  { key: 'status', label: '状态', type: 'select', dict: 'goods_status' },
  { key: 'condition_level', label: '成色', type: 'select', dict: 'condition_level' },
  { key: 'campus_id', label: '校区', type: 'select', options: [{ value: 1, label: '东校区' }, { value: 2, label: '西校区' }, { value: 3, label: '北校区' }] }
]

async function offShelf(row) {
  const { value } = await ElMessageBox.prompt('请填写下架原因（不少于 5 字）', '下架确认', { inputPlaceholder: '例如：与买家协商后暂停出售' })
  await http.post(`/goods/${row.id}/off-shelf`, { reason: value })
  ElMessage.success('已下架')
  location.reload()
}

async function priceRef(row) {
  const d = await http.get(`/goods/${row.id}/price-reference`)
  // missing 后端是逗号分隔字符串，兼容成数组前先归一，避免 join 报错
  const miss = Array.isArray(d.missing) ? d.missing.join('；') : (d.missing || '')
  ElMessageBox.alert(
    `建议价区间 ¥${d.suggestLow ?? '-'} ~ ¥${d.suggestHigh ?? '-'}<br/>总分 ${d.totalScore}（${d.grade} 级）<br/>${d.conclusion}` +
    (miss ? `<br/><br/>未满足条件：${miss}` : ''),
    '定价参考（规则 ' + d.ruleVersion + '）',
    { dangerouslyUseHTMLString: true }
  )
}
</script>
