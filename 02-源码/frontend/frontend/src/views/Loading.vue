<template>
  <CrudPage title="装车发运" desc="登记实际装货数量与现场信息；差异超过阈值需调度员确认" api="load" entity="装车登记"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "load_no",
    "label": "装车单号",
    "min": 150
  },
  {
    "key": "order_no",
    "label": "运单编号",
    "min": 150
  },
  {
    "key": "plan_weight",
    "label": "计划重量",
    "min": 100
  },
  {
    "key": "real_weight",
    "label": "实际重量",
    "min": 100
  },
  {
    "key": "diff_rate",
    "label": "差异率",
    "type": "num",
    "min": 90
  },
  {
    "key": "diff_flag",
    "label": "差异判定",
    "min": 110
  },
  {
    "key": "load_time",
    "label": "装车时间",
    "type": "time",
    "min": 150
  },
  {
    "key": "status",
    "label": "状态",
    "dict": "load_status",
    "min": 100
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "关键字",
    "type": "text",
    "placeholder": "装车单号/运单编号"
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "load_status"
  }
]
const form = [
  {
    "key": "order_no",
    "label": "运单编号",
    "required": true
  },
  {
    "key": "plan_weight",
    "label": "计划重量",
    "type": "number"
  },
  {
    "key": "plan_volume",
    "label": "计划体积",
    "type": "number"
  },
  {
    "key": "real_weight",
    "label": "实际重量",
    "type": "number"
  },
  {
    "key": "real_volume",
    "label": "实际体积",
    "type": "number"
  },
  {
    "key": "load_time",
    "label": "装车时间",
    "type": "date"
  },
  {
    "key": "site_contact",
    "label": "现场联系人"
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "load_status"
  },
  {
    "key": "remark",
    "label": "差异说明",
    "type": "textarea"
  },
  {
    "key": "cargo_images",
    "label": "货物照片",
    "type": "textarea"
  }
]</script>
