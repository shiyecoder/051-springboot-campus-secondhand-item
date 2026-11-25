<template>
  <CrudPage title="驾驶员证件" desc="驾驶证、从业资格证与体检记录有效期管理" api="dcert" entity="证件"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "driver_name",
    "label": "姓名",
    "min": 100
  },
  {
    "key": "cert_type",
    "label": "证件类型",
    "min": 120
  },
  {
    "key": "cert_no",
    "label": "证件编号",
    "min": 170
  },
  {
    "key": "issuer",
    "label": "发证机关",
    "min": 200
  },
  {
    "key": "issue_date",
    "label": "生效日期",
    "type": "day",
    "min": 120
  },
  {
    "key": "expire_date",
    "label": "到期日期",
    "type": "day",
    "min": 120
  },
  {
    "key": "status",
    "label": "状态",
    "min": 110
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "姓名",
    "type": "text",
    "placeholder": "姓名/证件编号"
  },
  {
    "key": "cert_type",
    "label": "证件类型",
    "type": "select",
    "options": [
      {
        "value": "驾驶证",
        "label": "驾驶证"
      },
      {
        "value": "从业资格证",
        "label": "从业资格证"
      },
      {
        "value": "体检记录",
        "label": "体检记录"
      }
    ]
  }
]
const form = [
  {
    "key": "driver_name",
    "label": "姓名",
    "required": true
  },
  {
    "key": "cert_type",
    "label": "证件类型",
    "type": "select",
    "options": [
      {
        "value": "驾驶证",
        "label": "驾驶证"
      },
      {
        "value": "从业资格证",
        "label": "从业资格证"
      },
      {
        "value": "体检记录",
        "label": "体检记录"
      }
    ]
  },
  {
    "key": "cert_no",
    "label": "证件编号",
    "required": true
  },
  {
    "key": "issuer",
    "label": "发证机关"
  },
  {
    "key": "issue_date",
    "label": "生效日期",
    "type": "date"
  },
  {
    "key": "expire_date",
    "label": "到期日期",
    "type": "date"
  }
]</script>
