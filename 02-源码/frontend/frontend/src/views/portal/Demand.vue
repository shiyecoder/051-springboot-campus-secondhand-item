<template>
  <div>
    <div class="page-h"><div><h2>提交运输需求</h2><p>填写货物与装卸信息，调度员审核后生成运单</p></div></div>
    <div class="grid2">
      <div class="card">
        <div class="card-h"><h3>需求信息</h3></div>
        <el-form :model="form" label-width="96px">
          <el-form-item label="货物名称" required><el-input v-model="form.goods_name" placeholder="如 家电整机" /></el-form-item>
          <el-form-item label="货物类型" required>
            <el-select v-model="form.goods_type" style="width: 100%">
              <el-option v-for="t in types" :key="t" :value="t" :label="t" />
            </el-select>
          </el-form-item>
          <el-form-item label="数量(件)"><el-input-number v-model="form.quantity" :min="1" style="width: 100%" /></el-form-item>
          <el-form-item label="重量(吨)" required><el-input-number v-model="form.weight" :min="0.1" :precision="1" style="width: 100%" /></el-form-item>
          <el-form-item label="体积(m³)"><el-input-number v-model="form.volume" :min="0" :precision="1" style="width: 100%" /></el-form-item>
          <el-form-item label="发货地址" required><el-input v-model="form.from_address" /></el-form-item>
          <el-form-item label="收货地址" required><el-input v-model="form.to_address" /></el-form-item>
          <el-form-item label="期望装货" required><el-date-picker v-model="form.expect_load_time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
          <el-form-item label="要求送达" required><el-date-picker v-model="form.expect_arrive_time" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
          <el-form-item label="货物图片"><UploadBox v-model="form.image" /></el-form-item>
          <el-form-item label="特殊要求"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="如 禁止叠压、需尾板车" /></el-form-item>
        </el-form>
        <div style="text-align: right; display: flex; gap: 8px; justify-content: flex-end">
          <el-button @click="save(true)">保存草稿</el-button>
          <el-button type="primary" @click="save(false)">提交需求</el-button>
        </div>
      </div>
      <div class="card">
        <div class="card-h"><h3>提交须知</h3></div>
        <el-timeline>
          <el-timeline-item>提交需求：填写货物、重量体积与装卸地址</el-timeline-item>
          <el-timeline-item>调度审核：核对车型适配与时间窗，必要时拆分或合并运单</el-timeline-item>
          <el-timeline-item>配车发运：指派车辆与驾驶员后可在「我的运单」查看进度</el-timeline-item>
          <el-timeline-item>签收评价：确认数量并评价服务，回单由客服复核归档</el-timeline-item>
        </el-timeline>
        <div class="note">重量或体积超过单车核载时会被拆分为多张运单，系统会提前提示。</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import http from '../../http'
import UploadBox from '../../components/UploadBox.vue'
import { store } from '../../store'
import { nowStr } from '../../utils/format'

const router = useRouter()
const types = ['家电', '机床配件', '精密仪器', '纸箱成品', '布匹', '日用百货', '建材', '食品冷链']
const form = reactive({
  goods_name: '', goods_type: '家电', quantity: 100, weight: 10, volume: 30,
  from_address: '', to_address: '', expect_load_time: nowStr(), expect_arrive_time: nowStr(),
  image: '', remark: '', contact: store.user?.realName, contact_phone: store.user?.phone
})

async function save(draft) {
  if (!form.goods_name || !form.from_address || !form.to_address) {
    ElMessage.warning('请填写货物名称与发货、收货地址')
    return
  }
  await http.post('/demand', {
    ...form,
    demand_no: 'XQ' + Date.now(),
    customer_id: 0,
    customer_name: store.user?.orgName,
    status: draft ? 'CANCELED' : 'SUBMITTED',
    create_by: store.user?.username,
    create_time: nowStr()
  })
  ElMessage.success(draft ? '草稿已保存' : '需求已提交，等待调度审核')
  router.push('/portal/orders')
}
</script>
