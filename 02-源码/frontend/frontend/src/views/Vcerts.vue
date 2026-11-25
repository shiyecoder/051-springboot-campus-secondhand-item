<template>
  <CrudPage title="车辆证件" desc="行驶证、年检、保险与营运证有效期预警（30 天内提醒）" api="vcert" entity="证件"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "plate_no",
    "label": "车牌号",
    "min": 120
  },
  {
    "key": "cert_type",
    "label": "证件类型",
    "min": 110
  },
  {
    "key": "cert_no",
    "label": "证件编号",
    "min": 170
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
    "label": "车牌号",
    "type": "text",
    "placeholder": "车牌号/证件编号"
  },
  {
    "key": "cert_type",
    "label": "证件类型",
    "type": "select",
    "options": [
      {
        "value": "行驶证",
        "label": "行驶证"
      },
      {
        "value": "年检",
        "label": "年检"
      },
      {
        "value": "商业保险",
        "label": "商业保险"
      },
      {
        "value": "营运证",
        "label": "营运证"
      }
    ]
  }
]
const form = [
  {
    "key": "plate_no",
    "label": "车牌号",
    "required": true
  },
  {
    "key": "cert_type",
    "label": "证件类型",
    "type": "select",
    "options": [
      {
        "value": "行驶证",
        "label": "行驶证"
      },
      {
        "value": "年检",
        "label": "年检"
      },
      {
        "value": "商业保险",
        "label": "商业保险"
      },
      {
        "value": "营运证",
        "label": "营运证"
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
