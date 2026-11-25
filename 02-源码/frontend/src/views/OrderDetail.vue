<template>
  <div v-if="order.id">
    <div class="page-h">
      <div><h2>订单 {{ order.order_no }}</h2><p>{{ order.status }} ｜ 下单 {{ fmtTime(order.create_time) }} ｜ 商品锁定至 {{ fmtTime(order.lock_expire_time) }}</p></div>
      <div class="sp"></div>
      <StatusTag :code="order.status" type="order_status" />
    </div>
    <div class="steps">
      <b>下单</b><span>→</span><b :class="{ dim: !order.seller_confirm_time }">卖家接单</b><span>→</span>
      <b :class="{ dim: !order.buyer_confirm_time }">买家确认收货</b><span>→</span><b :class="{ dim: order.status !== '已完成' }">双方评价</b>
    </div>
    <div class="split">
      <div class="card">
        <h3>商品快照</h3>
        <div class="prod">
          <img :src="order.goods_cover" class="cover" alt="" />
          <div><b>{{ order.goods_title }}</b><small>成交价 ¥{{ order.deal_price }} ｜ {{ order.pay_type }}</small></div>
        </div>
        <el-descriptions :column="2" border style="margin-top: 14px">
          <el-descriptions-item label="面交时间">{{ fmtTime(order.appoint_time) }}</el-descriptions-item>
          <el-descriptions-item label="面交点">{{ order.appoint_place }}</el-descriptions-item>
          <el-descriptions-item label="买家 ID">{{ order.buyer_id }}</el-descriptions-item>
          <el-descriptions-item label="卖家 ID">{{ order.seller_id }}</el-descriptions-item>
          <el-descriptions-item label="卖家接单">{{ fmtTime(order.seller_confirm_time) }}</el-descriptions-item>
          <el-descriptions-item label="买家确认">{{ fmtTime(order.buyer_confirm_time) }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" :span="2">{{ order.cancel_reason || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="card">
        <h3>可执行操作</h3>
        <p class="muted">操作按钮由后端按当前状态与操作人计算，前端只做展示。</p>
        <div class="acts">
          <el-button v-if="actions.includes('confirm')" type="primary" @click="confirm">确认接单</el-button>
          <el-button v-if="actions.includes('complete')" type="primary" @click="complete">确认收货</el-button>
          <el-button v-if="actions.includes('cancel')" plain @click="cancel">取消订单</el-button>
          <el-button v-if="actions.includes('dispute')" type="danger" plain @click="dispute">发起纠纷</el-button>
          <el-button v-if="actions.includes('review')" @click="$router.push('/portal/reviews')">去评价</el-button>
          <span v-if="actions.includes('wait')" class="muted">纠纷处理中，等待运营调解</span>
        </div>
        <h3 style="margin-top: 18px">评价（双盲）</h3>
        <p class="muted" v-if="!reviews.length">双方均提交后才互相可见，当前暂无可展示评价</p>
        <div v-for="r in reviews" :key="r.id" class="review">
          <b>{{ r.anonymous ? '匿名用户' : '用户 ' + r.from_user_id }}</b>
          <span class="stars">{{ '★'.repeat(r.score) }}</span>
          <p>{{ r.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { fmtTime } from '../utils/format'

const route = useRoute()
const order = ref({})
const actions = ref([])
const reviews = ref([])
const id = computed(() => route.params.id)

async function load() {
  const data = await http.get(`/trade-order/${id.value}`)
  order.value = data.order
  actions.value = data.actions || []
  reviews.value = data.reviews || []
}

async function confirm() {
  const { value } = await ElMessageBox.prompt('填写实际成交价（允许低于标价）', '确认接单', { inputValue: String(order.value.deal_price) })
  await http.post(`/trade-order/${id.value}/confirm`, { dealPrice: Number(value), payType: order.value.pay_type })
  ElMessage.success('已确认接单')
  load()
}
async function complete() {
  await http.post(`/trade-order/${id.value}/complete`)
  ElMessage.success('已确认收货')
  load()
}
async function cancel() {
  const { value } = await ElMessageBox.prompt('请填写取消原因（不少于 5 字）', '取消订单')
  await http.post(`/trade-order/${id.value}/cancel`, { reason: value })
  ElMessage.success('订单已取消')
  load()
}
async function dispute() {
  const { value } = await ElMessageBox.prompt('请描述问题（不少于 5 字），运营将在 48 小时内受理', '发起纠纷')
  await http.post(`/trade-order/${id.value}/dispute`, { reason: value, targetStatus: '纠纷中', attachments: [] })
  ElMessage.success('已发起纠纷，商品已冻结')
  load()
}

onMounted(load)
</script>

<style scoped>
.steps { display: flex; gap: 10px; align-items: center; font-size: 12.5px; color: var(--ink2); margin-bottom: 14px; }
.steps b { color: var(--p); }
.steps .dim { color: var(--ink2); font-weight: 400; }
.prod { display: flex; gap: 12px; align-items: center; }
.cover { width: 96px; height: 72px; object-fit: cover; border-radius: 8px; }
.prod b { display: block; font-size: 14px; }
.prod small { color: var(--ink2); font-size: 12px; }
.acts { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; }
.muted { color: var(--ink2); font-size: 12.5px; }
.review { border-top: 1px solid var(--line); padding-top: 10px; margin-top: 10px; font-size: 13px; }
.stars { color: var(--a); margin-left: 8px; font-size: 12px; }
</style>
