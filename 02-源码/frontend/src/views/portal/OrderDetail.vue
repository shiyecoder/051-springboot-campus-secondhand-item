<template>
  <div v-if="order.id">
    <div class="page-h"><div><h2>订单 {{ order.order_no }}</h2><p>下单 {{ fmtTime(order.create_time) }} ｜ 面交 {{ fmtTime(order.appoint_time) }} ｜ {{ order.appoint_place }}</p></div>
      <div class="sp"></div><StatusTag :code="order.status" type="order_status" /></div>
    <div class="split">
      <div class="card">
        <h3>商品快照</h3>
        <div class="prod"><img :src="order.goods_cover" class="cover" alt="" />
          <div><b>{{ order.goods_title }}</b><small>成交价 ¥{{ order.deal_price }} ｜ {{ order.pay_type }}</small></div></div>
        <el-descriptions :column="2" border style="margin-top: 14px">
          <el-descriptions-item label="卖家 ID">{{ order.seller_id }}</el-descriptions-item>
          <el-descriptions-item label="买家 ID">{{ order.buyer_id }}</el-descriptions-item>
          <el-descriptions-item label="卖家接单">{{ fmtTime(order.seller_confirm_time) }}</el-descriptions-item>
          <el-descriptions-item label="买家确认">{{ fmtTime(order.buyer_confirm_time) }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{ order.cancel_reason || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="card">
        <h3>可执行操作</h3>
        <div class="acts">
          <el-button v-if="actions.includes('confirm')" type="primary" @click="confirm">确认接单</el-button>
          <el-button v-if="actions.includes('complete')" type="primary" @click="complete">确认收货</el-button>
          <el-button v-if="actions.includes('cancel')" @click="cancel">取消订单</el-button>
          <el-button v-if="actions.includes('dispute')" type="danger" plain @click="dispute">发起纠纷</el-button>
          <el-button v-if="actions.includes('review')" type="success" @click="reviewDialog = true">去评价</el-button>
          <span v-if="actions.includes('wait')" class="muted">纠纷处理中，等待运营调解</span>
        </div>
        <h3 style="margin-top: 18px">评价</h3>
        <p class="muted" v-if="!reviews.length">双方都提交后才互相可见</p>
        <div v-for="r in reviews" :key="r.id" class="review">
          <b>{{ r.anonymous ? '匿名用户' : '用户 ' + r.from_user_id }}</b>
          <span class="stars">{{ '★'.repeat(r.score) }}</span>
          <p>{{ r.content }}</p>
        </div>
      </div>
    </div>

    <el-dialog v-model="reviewDialog" title="评价本次交易" width="440px">
      <el-form label-width="80px">
        <el-form-item label="评分"><el-rate v-model="rv.score" /></el-form-item>
        <el-form-item label="标签">
          <el-checkbox-group v-model="rv.tags">
            <el-checkbox label="描述一致" value="描述一致" /><el-checkbox label="准时" value="准时" />
            <el-checkbox label="好沟通" value="好沟通" /><el-checkbox label="包装完好" value="包装完好" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="rv.content" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
        <el-form-item label="匿名"><el-switch v-model="rv.anonymous" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="reviewDialog = false">取消</el-button><el-button type="primary" @click="submitReview">提交评价</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../http'
import StatusTag from '../../components/StatusTag.vue'
import { fmtTime } from '../../utils/format'

const route = useRoute()
const order = ref({})
const actions = ref([])
const reviews = ref([])
const reviewDialog = ref(false)
const rv = reactive({ score: 5, tags: ['准时'], content: '', anonymous: 0 })
const id = computed(() => route.params.id)

async function load() {
  const data = await http.get(`/trade-order/${id.value}`)
  order.value = data.order
  actions.value = data.actions || []
  reviews.value = data.reviews || []
}

async function confirm() {
  const { value } = await ElMessageBox.prompt('填写实际成交价', '确认接单', { inputValue: String(order.value.deal_price) })
  await http.post(`/trade-order/${id.value}/confirm`, { dealPrice: Number(value), payType: order.value.pay_type })
  ElMessage.success('已确认接单')
  load()
}
async function complete() {
  await ElMessageBox.confirm('确认已当面验货并收到商品？', '确认收货', { type: 'warning' })
  await http.post(`/trade-order/${id.value}/complete`)
  ElMessage.success('已确认收货')
  load()
}
async function cancel() {
  const { value } = await ElMessageBox.prompt('请填写取消原因（不少于 5 字）', '取消订单')
  await http.post(`/trade-order/${id.value}/cancel`, { reason: value })
  ElMessage.success('已取消')
  load()
}
async function dispute() {
  const { value } = await ElMessageBox.prompt('描述未见面或货不对板的情况（不少于 5 字）', '发起纠纷')
  await http.post(`/trade-order/${id.value}/dispute`, { reason: value, targetStatus: '纠纷中', attachments: [] })
  ElMessage.success('已发起纠纷')
  load()
}
async function submitReview() {
  const data = await http.post(`/trade-order/${id.value}/review`, { ...rv, tags: rv.tags.join(',') })
  ElMessage.success(data.tip || '评价已提交')
  reviewDialog.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.prod { display: flex; gap: 12px; align-items: center; }
.cover { width: 96px; height: 72px; object-fit: cover; border-radius: 8px; }
.prod b { display: block; font-size: 14px; }
.prod small { color: var(--ink2); font-size: 12px; }
.acts { display: flex; flex-wrap: wrap; gap: 8px; }
.muted { color: var(--ink2); font-size: 12.5px; }
.review { border-top: 1px solid var(--line); padding-top: 10px; margin-top: 10px; font-size: 13px; }
.stars { color: var(--a); margin-left: 8px; font-size: 12px; }
</style>
