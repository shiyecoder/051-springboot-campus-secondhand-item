<template>
  <CrudPage title="运输需求" desc="客户提交的需求单，审核后拆分或合并生成运单" api="demand" entity="需求"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "demand_no",
    "label": "需求编号",
    "min": 110
  },
  {
    "key": "customer_name",
    "label": "客户",
    "min": 126
  },
  {
    "key": "goods_name",
    "label": "货物",
    "min": 94
  },
  {
    "key": "weight",
    "label": "重量(t)",
    "type": "num",
    "min": 84
  },
  {
    "key": "volume",
    "label": "体积(m³)",
    "type": "num",
    "min": 84
  },
  {
    "key": "from_address",
    "label": "发货地址",
    "min": 130
  },
  {
    "key": "to_address",
    "label": "收货地址",
    "min": 130
  },
  {
    "key": "expect_load_time",
    "label": "期望装货",
    "type": "time",
    "min": 118
  },
  {
    "key": "status",
    "label": "状态",
    "dict": "demand_status",
    "min": 84
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "关键字",
    "type": "text",
    "placeholder": "需求编号/客户/货物"
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "demand_status"
  },
  {
    "key": "goods_type",
    "label": "货物类型",
    "type": "select",
    "options": [
      {
        "value": "家电",
        "label": "家电"
      },
      {
        "value": "机床配件",
        "label": "机床配件"
      },
      {
        "value": "精密仪器",
        "label": "精密仪器"
      },
      {
        "value": "纸箱成品",
        "label": "纸箱成品"
      },
      {
        "value": "布匹",
        "label": "布匹"
      },
      {
        "value": "日用百货",
        "label": "日用百货"
      },
      {
        "value": "建材",
        "label": "建材"
      },
      {
        "value": "食品冷链",
        "label": "食品冷链"
      }
    ]
  }
]
const form = [
  {
    "key": "customer_name",
    "label": "客户名称",
    "required": true
  },
  {
    "key": "goods_type",
    "label": "货物类型",
    "type": "select",
    "required": true,
    "options": [
      {
        "value": "家电",
        "label": "家电"
      },
      {
        "value": "机床配件",
        "label": "机床配件"
      },
      {
        "value": "精密仪器",
        "label": "精密仪器"
      },
      {
        "value": "纸箱成品",
        "label": "纸箱成品"
      },
      {
        "value": "布匹",
        "label": "布匹"
      },
      {
        "value": "日用百货",
        "label": "日用百货"
      },
      {
        "value": "建材",
        "label": "建材"
      },
      {
        "value": "食品冷链",
        "label": "食品冷链"
      }
    ]
  },
  {
    "key": "goods_name",
    "label": "货物名称",
    "required": true
  },
  {
    "key": "quantity",
    "label": "件数",
    "type": "number"
  },
  {
    "key": "weight",
    "label": "重量(吨)",
    "type": "number"
  },
  {
    "key": "volume",
    "label": "体积(m³)",
    "type": "number"
  },
  {
    "key": "from_address",
    "label": "发货地址",
    "required": true
  },
  {
    "key": "to_address",
    "label": "收货地址",
    "required": true
  },
  {
    "key": "expect_load_time",
    "label": "期望装货",
    "type": "date"
  },
  {
    "key": "expect_arrive_time",
    "label": "要求送达",
    "type": "date"
  },
  {
    "key": "status",
    "label": "状态",
    "type": "select",
    "dict": "demand_status"
  },
  {
    "key": "remark",
    "label": "特殊要求",
    "type": "textarea"
  },
  {
    "key": "image",
    "label": "货物图片",
    "type": "image"
  }
]</script>
