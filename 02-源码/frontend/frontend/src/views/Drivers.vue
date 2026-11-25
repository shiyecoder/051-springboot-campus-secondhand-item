<template>
  <CrudPage title="驾驶员" desc="驾驶证类型、证件有效期、联系电话与可用状态；休假与过期不可派车" api="driver" entity="驾驶员"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "avatar",
    "label": "头像",
    "type": "image",
    "min": 70
  },
  {
    "key": "name",
    "label": "姓名",
    "min": 100
  },
  {
    "key": "driver_no",
    "label": "工号",
    "min": 100
  },
  {
    "key": "license_type",
    "label": "驾驶证",
    "min": 90
  },
  {
    "key": "license_expire",
    "label": "有效期至",
    "type": "day",
    "min": 120
  },
  {
    "key": "phone",
    "label": "电话",
    "min": 130
  },
  {
    "key": "fleet_name",
    "label": "车队",
    "min": 110
  },
  {
    "key": "ontime_rate",
    "label": "准时率%",
    "type": "num",
    "min": 90
  },
  {
    "key": "status",
    "label": "状态",
    "dict": "driver_status",
    "min": 95
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "姓名",
    "type": "text",
    "placeholder": "姓名/工号/电话"
  },
  {
    "key": "license_type",
    "label": "驾驶证",
    "type": "select",
    "options": [
      {
        "value": "A2",
        "label": "A2"
      },
      {
        "value": "B2",
        "label": "B2"
      }
    ]
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "driver_status"
  },
  {
    "key": "fleet_name",
    "label": "车队",
    "type": "text",
    "placeholder": "如 宁波二队"
  }
]
const form = [
  {
    "key": "name",
    "label": "姓名",
    "required": true
  },
  {
    "key": "phone",
    "label": "联系电话",
    "required": true
  },
  {
    "key": "license_type",
    "label": "驾驶证类型",
    "type": "select",
    "options": [
      {
        "value": "A2",
        "label": "A2"
      },
      {
        "value": "B2",
        "label": "B2"
      }
    ]
  },
  {
    "key": "license_expire",
    "label": "驾驶证有效期",
    "type": "date"
  },
  {
    "key": "fleet_name",
    "label": "所属车队"
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "driver_status"
  },
  {
    "key": "avatar",
    "label": "头像",
    "type": "image"
  }
]</script>
