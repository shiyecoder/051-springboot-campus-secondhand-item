<template>
  <div v-if="goods.id" class="split">
    <div class="card">
      <div class="gallery">
        <img v-for="(p, i) in images" :key="i" :src="p" alt="" :class="{ main: i === active }" @click="active = i" />
      </div>
      <h3 class="ttl">{{ goods.title }}</h3>
      <p class="desc">{{ goods.description }}</p>
      <el-descriptions :column="2" border style="margin-top: 14px">
        <el-descriptions-item label="成色">{{ goods.condition_level }}</el-descriptions-item>
        <el-descriptions-item label="交易方式">{{ goods.trade_type }}</el-descriptions-item>
        <el-descriptions-item label="入手价">{{ goods.original_price ? '¥' + goods.original_price : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="折价率">{{ discount ?? '-' }}{{ discount ? '%' : '' }}</el-descriptions-item>
        <el-descriptions-item label="校区">{{ goods.campus_name }}</el-descriptions-item>
        <el-descriptions-item label="浏览">{{ goods.view_count }} 次</el-descriptions-item>
      </el-descriptions>
      <div class="notice-bar" style="margin: 14px 0 0">交易风险提示：平台不支持在线支付与快递，请当面验货后再付款，勿向陌生人转账。</div>
    </div>

    <div>
      <div class="card">
        <div class="price">¥{{ goods.price }} <s v-if="goods.original_price">¥{{ goods.original_price }}</s></div>
        <p class="muted" v-if="ref_">定价参考：{{ ref_.grade }} 级 · 总分 {{ ref_.totalScore }} ｜ 建议 ¥{{ ref_.suggestLow }} ~ ¥{{ ref_.suggestHigh }}</p>
        <div class="buy">
          <el-button type="primary" :disabled="isOwner || goods.status !== '在售'" @click="openOrder">
            {{ isOwner ? '这是你发布的商品' : goods.status === '在售' ? '立即下单' : '当前不可下单' }}
          </el-button>
        </div>
      </div>

      <div class="card">
        <h3>卖家信息</h3>
        <div class="seller">
          <img v-if="seller.avatar" :src="seller.avatar" class="ava-img" alt="" />
          <div v-else class="ava">{{ (seller.realName || 'U').slice(0, 1) }}</div>
          <div><b>{{ seller.realName }}</b><small>{{ seller.campusName }} · 历史成交 {{ seller.dealCount }} 单</small></div>
        </div>
        <p class="muted" style="margin-top: 8px">信用分 <b style="color: var(--p)">{{ seller.creditScore }}</b>（{{ seller.creditScore >= 100 ? '优秀' : seller.creditScore >= 60 ? '正常' : '受限' }}）</p>
      </div>

      <div class="card">
        <h3>公告</h3>
        <p v-for="n in notices" :key="n.id" class="muted">{{ n.title }}</p>
      </div>
    </div>

    <el-dialog v-model="dialog" title="确认下单" width="460px">
      <el-form :model="order" label-width="90px">
        <el-form-item label="面交时间" required>
          <el-date-picker v-model="order.appointTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" placeholder="必须晚于当前时间且在 7 天内" />
        </el-form-item>
        <el-form-item label="面交点" required>
          <el-select v-model="order.appointPlace" style="width: 100%" placeholder="选择校区面交点">
            <el-option v-for="p in places" :key="p.id" :label="p.place_name + '（' + p.campus_name + '）'" :value="p.campus_name + '·' + p.place_name" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="order.payType" style="width: 100%">
            <el-option label="现金" value="现金" /><el-option label="转账" value="转账" /><el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <p class="muted">下单后商品锁定 24 小时，卖家需在 12 小时内确认，超时自动取消。平台不处理在线支付。</p>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../http'
import { splitImages } from '../../utils/format'
import { store } from '../../store'

/** 商品详情：六图轮播 + 卖家信用 + 面交点 + 定价参考，底部固定下单 */
const route = useRoute()
const router = useRouter()
const goods = ref({})
const seller = ref({})
const notices = ref([])
const discount = ref(null)
const places = ref([])
const ref_ = ref(null)
const active = ref(0)
const dialog = ref(false)
const order = reactive({ appointTime: '', appointPlace: '', payType: '现金' })
const images = computed(() => splitImages(goods.value.image_urls || goods.value.cover_url))
const isOwner = computed(() => store.user?.id === goods.value.seller_id)

async function load() {
  const data = await http.get(`/goods/${route.params.id}`)
  goods.value = data.goods
  seller.value = data.seller || {}
  notices.value = data.notices || []
  discount.value = data.discountRate
  try {
    ref_.value = await http.get(`/goods/${route.params.id}/price-reference`)
  } catch (e) { /* 定价参考失败不阻塞详情展示 */ }
}

async function openOrder() {
  if (!order.appointTime) {
    const d = new Date(Date.now() + 86400000)
    order.appointTime = d.toISOString().slice(0, 10) + ' 15:00:00'
  }
  places.value = await http.get('/campus-place/list', { params: { campus_id: goods.value.campus_id } })
  dialog.value = true
}

async function submit() {
  if (!order.appointTime || !order.appointPlace) {
    ElMessage.warning('请选择面交时间与面交点')
    return
  }
  const data = await http.post('/trade-order', null, {
    params: { goodsId: goods.value.id, appointTime: order.appointTime, appointPlace: order.appointPlace, payType: order.payType }
  })
  ElMessage.success('下单成功，订单号 ' + data.order_no)
  dialog.value = false
  router.push(`/portal/orders/${data.id}`)
}

onMounted(load)
</script>

<style scoped>
.gallery { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.gallery img { width: 100%; aspect-ratio: 4/3; object-fit: cover; border-radius: 8px; background: #EDF1F0; cursor: pointer; border: 2px solid transparent; }
.gallery img.main { border-color: var(--p); }
.ttl { font-size: 17px; margin: 16px 0 6px; }
.desc { font-size: 13px; color: var(--ink2); margin: 0; }
.price { font-size: 26px; color: var(--bad); font-weight: 700; }
.price s { font-size: 13px; color: var(--ink2); font-weight: 400; margin-left: 8px; }
.buy { display: flex; gap: 8px; margin-top: 14px; }
.buy .el-button { flex: 1; }
.seller { display: flex; gap: 10px; align-items: center; }
.ava { width: 40px; height: 40px; border-radius: 50%; background: var(--p2); color: #fff; display: grid; place-items: center; }
.ava-img { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; }
.seller b { display: block; font-size: 13.5px; }
.seller small { color: var(--ink2); font-size: 12px; }
.muted { color: var(--ink2); font-size: 12.5px; }
</style>
