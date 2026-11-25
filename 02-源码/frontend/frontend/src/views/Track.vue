<template>
  <CrudPage title="在途节点" desc="驾驶员手工上报，客户门户展示最新状态与历史节点（不做实时地图定位）" api="node" entity="节点"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "node_no",
    "label": "节点编号",
    "min": 129
  },
  {
    "key": "order_no",
    "label": "运单编号",
    "min": 129
  },
  {
    "key": "node_type",
    "label": "节点",
    "dict": "node_type",
    "min": 86
  },
  {
    "key": "place",
    "label": "地点",
    "min": 190
  },
  {
    "key": "node_time",
    "label": "上报时间",
    "type": "time",
    "min": 129
  },
  {
    "key": "description",
    "label": "说明",
    "min": 207
  },
  {
    "key": "reporter",
    "label": "上报人",
    "min": 86
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "关键字",
    "type": "text",
    "placeholder": "节点编号/运单编号/地点"
  },
  {
    "key": "node_type",
    "label": "节点类型",
    "type": "select",
    "dict": "node_type"
  }
]
const form = [
  {
    "key": "order_no",
    "label": "运单编号",
    "required": true
  },
  {
    "key": "node_type",
    "label": "节点类型",
    "type": "select",
    "dict": "node_type"
  },
  {
    "key": "place",
    "label": "地点",
    "required": true
  },
  {
    "key": "node_time",
    "label": "发生时间",
    "type": "date"
  },
  {
    "key": "description",
    "label": "说明",
    "type": "textarea"
  },
  {
    "key": "images",
    "label": "现场图片",
    "type": "image"
  }
]</script>
