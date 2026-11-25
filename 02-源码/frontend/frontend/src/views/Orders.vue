<template>
  <CrudPage title="运单中心" desc="条件区 + 业务摘要 + 数据表格；编号点击进入详情，行内保留查看与调度" api="order" entity="运单"
    :columns="columns" :filters="filters" :form-fields="form" :can-create="true" />
</template>

<script setup>
import CrudPage from '../components/CrudPage.vue'

const columns = [
  {
    "key": "order_no",
    "label": "运单编号",
    "type": "link",
    "min": 143
  },
  {
    "key": "cargo_image",
    "label": "货物",
    "type": "image",
    "min": 84
  },
  {
    "key": "customer_name",
    "label": "发货方",
    "min": 154
  },
  {
    "key": "plate_no",
    "label": "车牌/司机",
    "min": 143
  },
  {
    "key": "plan_load_time",
    "label": "计划装货",
    "type": "time",
    "min": 143
  },
  {
    "key": "status",
    "label": "运单状态",
    "dict": "order_status",
    "min": 104
  },
  {
    "key": "exception_flag",
    "label": "异常",
    "dict": "exception_flag",
    "min": 85
  },
  {
    "key": "receivable",
    "label": "应收(元)",
    "type": "money",
    "min": 104
  }
]
const filters = [
  {
    "key": "keyword",
    "label": "运单编号",
    "type": "text",
    "placeholder": "运单编号/客户/货物"
  },
  {
    "key": "status",
    "label": "运单状态",
    "type": "select",
    "dict": "order_status"
  },
  {
    "key": "vehicle_type_req",
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
    "label": "发货方",
    "required": true
  },
  {
    "key": "receiver_name",
    "label": "收货方",
    "required": true
  },
  {
    "key": "goods_name",
    "label": "货物名称",
    "required": true
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
    "key": "from_city",
    "label": "始发城市"
  },
  {
    "key": "to_city",
    "label": "目的城市"
  },
  {
    "key": "from_address",
    "label": "装货地址"
  },
  {
    "key": "to_address",
    "label": "卸货地址"
  },
  {
    "key": "vehicle_type_req",
    "label": "要求车型",
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
    "key": "plan_load_time",
    "label": "计划装货",
    "type": "date"
  },
  {
    "key": "plan_arrive_time",
    "label": "预计到达",
    "type": "date"
  },
  {
    "key": "require_arrive_time",
    "label": "要求送达",
    "type": "date"
  },
  {
    "key": "remark",
    "label": "注意事项",
    "type": "textarea"
  }
]</script>
