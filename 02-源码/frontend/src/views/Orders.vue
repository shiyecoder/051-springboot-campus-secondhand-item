<template>
  <CrudPage
    title="订单管理"
    desc="状态机：待卖家确认 → 待面交 → 已完成；取消、纠纷分支均写操作日志并通知双方"
    api="trade-order"
    entity="订单"
    :columns="columns"
    :filters="filters"
    :form-fields="[]"
    :can-create="false"
    link-base="/admin/orders"
    link-key="order_no"
  />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  { key: 'order_no', label: '订单号', type: 'link', min: 170 },
  { key: 'goods_title', label: '商品', min: 220 },
  { key: 'buyer_id', label: '买家', type: 'num', min: 80 },
  { key: 'seller_id', label: '卖家', type: 'num', min: 80 },
  { key: 'deal_price', label: '成交价', type: 'money', min: 100 },
  { key: 'pay_type', label: '支付', min: 80 },
  { key: 'appoint_time', label: '面交时间', type: 'time', min: 160 },
  { key: 'status', label: '状态', dict: 'order_status', min: 110 },
  { key: 'create_time', label: '下单时间', type: 'time', min: 160 }
]
const filters = [
  { key: 'keyword', label: '订单号' },
  { key: 'status', label: '状态', type: 'select', dict: 'order_status' }
]
</script>
