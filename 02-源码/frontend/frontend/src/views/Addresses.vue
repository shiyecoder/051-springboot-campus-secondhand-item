<template>
  <CrudPage title="客户地址" desc="常用发货、收货地址及联系人、装卸时间与场地限制" api="address" entity="地址"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "customer_name",
    "label": "客户",
    "min": 145
  },
  {
    "key": "addr_type",
    "label": "类型",
    "min": 84
  },
  {
    "key": "city",
    "label": "城市",
    "min": 84
  },
  {
    "key": "address",
    "label": "详细地址",
    "min": 218
  },
  {
    "key": "contact",
    "label": "联系人",
    "min": 84
  },
  {
    "key": "phone",
    "label": "电话",
    "min": 104
  },
  {
    "key": "load_time",
    "label": "装卸时间",
    "min": 104
  },
  {
    "key": "site_limit",
    "label": "场地限制",
    "min": 137
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "关键字",
    "type": "text",
    "placeholder": "地址/联系人"
  },
  {
    "key": "addr_type",
    "label": "类型",
    "type": "select",
    "options": [
      {
        "value": "SHIP",
        "label": "发货"
      },
      {
        "value": "RECEIVE",
        "label": "收货"
      }
    ]
  },
  {
    "key": "city",
    "label": "城市",
    "type": "text",
    "placeholder": "如 杭州"
  }
]
const form = [
  {
    "key": "customer_name",
    "label": "客户名称",
    "required": true
  },
  {
    "key": "addr_type",
    "label": "地址类型",
    "type": "select",
    "options": [
      {
        "value": "SHIP",
        "label": "发货"
      },
      {
        "value": "RECEIVE",
        "label": "收货"
      }
    ]
  },
  {
    "key": "province",
    "label": "省"
  },
  {
    "key": "city",
    "label": "市"
  },
  {
    "key": "address",
    "label": "详细地址",
    "required": true
  },
  {
    "key": "contact",
    "label": "现场联系人"
  },
  {
    "key": "phone",
    "label": "联系电话"
  },
  {
    "key": "load_time",
    "label": "装卸时间"
  },
  {
    "key": "site_limit",
    "label": "场地限制"
  }
]</script>
