<template>
  <CrudPage title="客户档案" desc="维护客户名称、联系人、结算周期、信用状态与开票信息" api="customer" entity="客户"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "customer_no",
    "label": "客户编号",
    "min": 124
  },
  {
    "key": "name",
    "label": "客户名称",
    "min": 201
  },
  {
    "key": "contact",
    "label": "联系人",
    "min": 86
  },
  {
    "key": "phone",
    "label": "电话",
    "min": 124
  },
  {
    "key": "settlement_cycle",
    "label": "结算周期",
    "min": 105
  },
  {
    "key": "credit_status",
    "label": "信用状态",
    "dict": "credit_status",
    "min": 100
  },
  {
    "key": "total_orders",
    "label": "累计运单",
    "type": "num",
    "min": 91
  },
  {
    "key": "total_amount",
    "label": "累计费用",
    "type": "money",
    "min": 124
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "客户名称",
    "type": "text",
    "placeholder": "客户名称/联系人"
  },
  {
    "key": "credit_status",
    "label": "信用状态",
    "type": "select",
    "dict": "credit_status"
  },
  {
    "key": "settlement_cycle",
    "label": "结算周期",
    "type": "select",
    "options": [
      {
        "value": "月结 30 天",
        "label": "月结 30 天"
      },
      {
        "value": "月结 45 天",
        "label": "月结 45 天"
      },
      {
        "value": "月结 60 天",
        "label": "月结 60 天"
      },
      {
        "value": "现结",
        "label": "现结"
      }
    ]
  }
]
const form = [
  {
    "key": "name",
    "label": "客户名称",
    "required": true
  },
  {
    "key": "contact",
    "label": "联系人",
    "required": true
  },
  {
    "key": "phone",
    "label": "联系电话",
    "required": true
  },
  {
    "key": "settlement_cycle",
    "label": "结算周期",
    "type": "select",
    "options": [
      {
        "value": "月结 30 天",
        "label": "月结 30 天"
      },
      {
        "value": "月结 45 天",
        "label": "月结 45 天"
      },
      {
        "value": "月结 60 天",
        "label": "月结 60 天"
      },
      {
        "value": "现结",
        "label": "现结"
      }
    ]
  },
  {
    "key": "credit_status",
    "label": "信用状态",
    "type": "select",
    "dict": "credit_status"
  },
  {
    "key": "invoice_title",
    "label": "开票抬头"
  },
  {
    "key": "tax_no",
    "label": "税号"
  },
  {
    "key": "bank_account",
    "label": "开户行与账号"
  },
  {
    "key": "remark",
    "label": "备注",
    "type": "textarea"
  }
]</script>
