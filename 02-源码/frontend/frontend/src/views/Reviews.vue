<template>
  <CrudPage title="服务评价" desc="客户签收后对时效、货损与服务态度评分" api="review" entity="评价"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="false" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "review_no",
    "label": "评价编号",
    "min": 124
  },
  {
    "key": "order_no",
    "label": "运单编号",
    "min": 133
  },
  {
    "key": "customer_name",
    "label": "客户",
    "min": 160
  },
  {
    "key": "score",
    "label": "评分",
    "type": "num",
    "min": 84
  },
  {
    "key": "content",
    "label": "评价内容",
    "min": 219
  },
  {
    "key": "tags",
    "label": "标签",
    "min": 106
  },
  {
    "key": "review_time",
    "label": "评价时间",
    "type": "time",
    "min": 133
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "关键字",
    "type": "text",
    "placeholder": "评价编号/客户/内容"
  },
  {
    "key": "score",
    "label": "评分",
    "type": "select",
    "options": [
      {
        "value": "1",
        "label": "1 星"
      },
      {
        "value": "2",
        "label": "2 星"
      },
      {
        "value": "3",
        "label": "3 星"
      },
      {
        "value": "4",
        "label": "4 星"
      },
      {
        "value": "5",
        "label": "5 星"
      }
    ]
  }
]
const form = []</script>
