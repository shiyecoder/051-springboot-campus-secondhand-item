<template>
  <CrudPage title="收付款" desc="仅登记线下收款、付款与对账状态，不接入在线支付" api="payment" entity="流水"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "payment_no",
    "label": "流水号",
    "min": 132
  },
  {
    "key": "ref_no",
    "label": "关联单号",
    "min": 132
  },
  {
    "key": "target_name",
    "label": "往来单位",
    "min": 146
  },
  {
    "key": "direction",
    "label": "方向",
    "min": 84
  },
  {
    "key": "amount",
    "label": "金额(元)",
    "type": "money",
    "min": 96
  },
  {
    "key": "pay_method",
    "label": "方式",
    "min": 96
  },
  {
    "key": "status",
    "label": "状态",
    "dict": "payment_status",
    "min": 88
  },
  {
    "key": "pay_date",
    "label": "日期",
    "type": "day",
    "min": 96
  },
  {
    "key": "operator",
    "label": "登记人",
    "min": 88
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "关键字",
    "type": "text",
    "placeholder": "流水号/往来单位"
  },
  {
    "key": "direction",
    "label": "方向",
    "type": "select",
    "options": [
      {
        "value": "RECV",
        "label": "收款"
      },
      {
        "value": "PAY",
        "label": "付款"
      }
    ]
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "payment_status"
  }
]
const form = [
  {
    "key": "target_name",
    "label": "往来单位",
    "required": true
  },
  {
    "key": "direction",
    "label": "方向",
    "type": "select",
    "options": [
      {
        "value": "RECV",
        "label": "收款"
      },
      {
        "value": "PAY",
        "label": "付款"
      }
    ]
  },
  {
    "key": "amount",
    "label": "金额",
    "type": "number"
  },
  {
    "key": "pay_method",
    "label": "方式"
  },
  {
    "key": "pay_date",
    "label": "日期",
    "type": "date"
  },
  {
    "key": "remark",
    "label": "备注",
    "type": "textarea"
  }
]</script>
