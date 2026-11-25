<template>
  <CrudPage title="客户账单" desc="按客户与结算周期汇总运单费用；未完成签收复核的运单不计入最终对账" api="bill" entity="账单"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "bill_no",
    "label": "账单编号",
    "min": 140
  },
  {
    "key": "customer_name",
    "label": "客户",
    "min": 180
  },
  {
    "key": "period",
    "label": "账期",
    "min": 90
  },
  {
    "key": "order_count",
    "label": "运单数",
    "type": "num",
    "min": 90
  },
  {
    "key": "total_amount",
    "label": "账单金额",
    "type": "money",
    "min": 120
  },
  {
    "key": "paid_amount",
    "label": "已收金额",
    "type": "money",
    "min": 120
  },
  {
    "key": "unpaid_amount",
    "label": "未收金额",
    "type": "money",
    "min": 120
  },
  {
    "key": "status",
    "label": "状态",
    "dict": "bill_status",
    "min": 100
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "关键字",
    "type": "text",
    "placeholder": "账单编号/客户"
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "bill_status"
  },
  {
    "key": "period",
    "label": "账期",
    "type": "text",
    "placeholder": "如 2026-10"
  }
]
const form = [
  {
    "key": "customer_name",
    "label": "客户",
    "required": true
  },
  {
    "key": "period",
    "label": "账期",
    "required": true
  },
  {
    "key": "total_amount",
    "label": "账单金额",
    "type": "number"
  },
  {
    "key": "bill_date",
    "label": "出账日期",
    "type": "date"
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "bill_status"
  }
]</script>
