<template>
  <div>
    <div class="page-h"><div><h2>节点上报</h2><p>手工上报已装车、已发车、运输中、到达、卸货与签收节点</p></div></div>
    <div class="grid2">
      <div class="card">
        <div class="card-h"><h3>上报节点</h3></div>
        <el-form :model="form" label-width="96px">
          <el-form-item label="运单" required>
            <el-select v-model="form.order_id" style="width: 100%" @change="onOrder">
              <el-option v-for="o in orders" :key="o.id" :value="o.id" :label="`${o.order_no} · ${o.from_city}→${o.to_city}`" />
            </el-select>
          </el-form-item>
          <el-form-item label="节点类型" required>
            <el-select v-model="form.node_type" style="width: 100%">
              <el-option v-for="(v, k) in DICT.node_type" :key="k" :value="k" :label="v[0]" />
            </el-select>
          </el-form-item>
          <el-form-item label="当前地点" required><el-input v-model="form.place" placeholder="如 G15 沈海高速宁波段" /></el-form-item>
          <el-form-item label="发生时间"><el-date-picker v-model="form.node_time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
          <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="现场照片"><UploadBox v-model="form.images" text="拍照上传" /></el-form-item>
        </el-form>
        <div style="text-align: right"><el-button type="primary" size="large" @click="submit">提交上报</el-button></div>
      </div>
      <div class="card">
        <div class="card-h"><h3>已上报节点</h3></div>
        <el-timeline v-if="nodes.length">
          <el-timeline-item v-for="(n, i) in nodes" :key="n.id" :timestamp="fmtTime(n.node_time)" placement="top"
            :color="i === 0 ? '#5B2533' : '#2D765C'">
            <b>{{ n.node_name }}</b>
            <div class="mut" style="font-size: 12px">{{ n.place }} · {{ n.description || '—' }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="选择运单后可查看已上报节点" :image-size="70" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../http'
import UploadBox from '../../components/UploadBox.vue'
import { DICT } from '../../dict'
import { nowStr, fmtTime } from '../../utils/format'
import { store } from '../../store'

const orders = ref([])
const nodes = ref([])
const form = reactive({ order_id: null, order_no: '', node_type: 'TRANSIT', place: '', node_time: nowStr(), description: '', images: '' })

async function load() {
  const data = await http.get('/order/page', { params: { page: 1, size: 20 } })
  orders.value = data.records || []
  if (orders.value.length) {
    form.order_id = orders.value[0].id
    form.order_no = orders.value[0].order_no
    await loadNodes()
  }
}

async function loadNodes() {
  const data = await http.get('/node/page', { params: { page: 1, size: 20, order_id: form.order_id } })
  nodes.value = data.records || []
}

async function onOrder() {
  form.order_no = orders.value.find((o) => o.id === form.order_id)?.order_no || ''
  await loadNodes()
}

async function submit() {
  if (!form.order_id || !form.place) {
    ElMessage.warning('请选择运单并填写当前地点')
    return
  }
  await http.post('/node', {
    ...form,
    node_no: 'JD' + Date.now(),
    node_name: DICT.node_type[form.node_type]?.[0] || form.node_type,
    reporter: store.user?.realName,
    create_time: nowStr()
  })
  ElMessage.success('节点已上报，客户门户可见')
  form.place = ''
  form.description = ''
  await loadNodes()
}

onMounted(load)
</script>
