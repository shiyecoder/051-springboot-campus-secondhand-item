<template>
  <div>
    <div class="page-h"><div><h2>异常上报</h2><p>登记堵车、车辆故障、货损货差、拒收、地址变更与延误</p></div></div>
    <div class="card" style="max-width: 760px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="运单" required>
          <el-select v-model="form.order_id" style="width: 100%" @change="onOrder">
            <el-option v-for="o in orders" :key="o.id" :value="o.id" :label="`${o.order_no} · ${o.from_city}→${o.to_city}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="t in types" :key="t" :value="t" :label="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="影响程度">
          <el-radio-group v-model="form.level">
            <el-radio v-for="(v, k) in DICT.exception_level" :key="k" :label="k">{{ v[0] }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发生地点"><el-input v-model="form.place" placeholder="如 G15 沈海高速宁波段 K123" /></el-form-item>
        <el-form-item label="简要描述" required><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="现场照片"><UploadBox v-model="form.images" text="拍照上传" /></el-form-item>
      </el-form>
      <div class="note">严重货损或重大延误将自动通知客服与车队经理，需经理审核处理结果后关闭。</div>
      <div style="text-align: right; margin-top: 14px"><el-button type="primary" size="large" @click="submit">提交异常</el-button></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../http'
import UploadBox from '../../components/UploadBox.vue'
import { DICT } from '../../dict'
import { nowStr } from '../../utils/format'
import { store } from '../../store'

const orders = ref([])
const types = ['堵车/交通管制', '车辆故障', '货损货差', '拒收', '地址变更', '延误']
const form = reactive({ order_id: null, order_no: '', type: '堵车/交通管制', level: 'MID', place: '', description: '', images: '' })

async function load() {
  const data = await http.get('/order/page', { params: { page: 1, size: 20 } })
  orders.value = data.records || []
  if (orders.value.length) {
    form.order_id = orders.value[0].id
    form.order_no = orders.value[0].order_no
  }
}

function onOrder() {
  form.order_no = orders.value.find((o) => o.id === form.order_id)?.order_no || ''
}

async function submit() {
  if (!form.order_id || !form.description) {
    ElMessage.warning('请选择运单并填写简要描述')
    return
  }
  await http.post('/exception', {
    exception_no: 'EX' + Date.now(),
    order_id: form.order_id,
    order_no: form.order_no,
    type: form.type,
    level: form.level,
    description: form.description + (form.place ? '（' + form.place + '）' : ''),
    owner_name: store.user?.realName,
    deadline: nowStr(),
    images: form.images,
    status: 'OPEN',
    reporter: store.user?.realName,
    create_time: nowStr(),
    update_time: nowStr()
  })
  await http.post(`/order/${form.order_id}/transition`, { target: 'EXCEPTION', reason: form.type + '：' + form.description.slice(0, 40) })
  ElMessage.success('异常已上报，等待处理')
  form.description = ''
}

onMounted(load)
</script>
