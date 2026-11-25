<template>
  <CrudPage title="车辆档案" desc="牌照、车型、核载、容积、能源类型、所属车队与状态（维修停用不参与配车）" api="vehicle" entity="车辆"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "image",
    "label": "照片",
    "type": "image",
    "min": 70
  },
  {
    "key": "plate_no",
    "label": "车牌号",
    "min": 120
  },
  {
    "key": "vehicle_type",
    "label": "车型",
    "min": 120
  },
  {
    "key": "load_weight",
    "label": "核载(t)",
    "type": "num",
    "min": 90
  },
  {
    "key": "volume",
    "label": "容积(m³)",
    "type": "num",
    "min": 95
  },
  {
    "key": "energy_type",
    "label": "能源",
    "min": 80
  },
  {
    "key": "fleet_name",
    "label": "所属车队",
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
    "dict": "vehicle_status",
    "min": 105
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "车牌号",
    "type": "text",
    "placeholder": "车牌号/车队"
  },
  {
    "key": "vehicle_type",
    "label": "车型",
    "type": "select",
    "options": [
      {
        "value": "9.6m 厢式",
        "label": "9.6m 厢式"
      },
      {
        "value": "13m 平板",
        "label": "13m 平板"
      },
      {
        "value": "7.6m 冷藏",
        "label": "7.6m 冷藏"
      },
      {
        "value": "9.6m 高栏",
        "label": "9.6m 高栏"
      },
      {
        "value": "13m 厢式",
        "label": "13m 厢式"
      }
    ]
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "vehicle_status"
  },
  {
    "key": "fleet_name",
    "label": "车队",
    "type": "text",
    "placeholder": "如 杭州一队"
  }
]
const form = [
  {
    "key": "plate_no",
    "label": "车牌号",
    "required": true
  },
  {
    "key": "vehicle_type",
    "label": "车型",
    "type": "select",
    "options": [
      {
        "value": "9.6m 厢式",
        "label": "9.6m 厢式"
      },
      {
        "value": "13m 平板",
        "label": "13m 平板"
      },
      {
        "value": "7.6m 冷藏",
        "label": "7.6m 冷藏"
      },
      {
        "value": "9.6m 高栏",
        "label": "9.6m 高栏"
      },
      {
        "value": "13m 厢式",
        "label": "13m 厢式"
      }
    ]
  },
  {
    "key": "load_weight",
    "label": "核载(吨)",
    "type": "number"
  },
  {
    "key": "volume",
    "label": "容积(m³)",
    "type": "number"
  },
  {
    "key": "energy_type",
    "label": "能源类型"
  },
  {
    "key": "fleet_name",
    "label": "所属车队"
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "vehicle_status"
  },
  {
    "key": "image",
    "label": "车辆照片",
    "type": "image"
  }
]</script>
