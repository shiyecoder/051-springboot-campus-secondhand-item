<template>
  <div>
    <div class="page-h"><div><h2>我的评价</h2><p>双方都提交后才互相可见；完成后 7 天内可评价，逾期关闭</p></div></div>
    <div class="card">
      <h3>待我评价</h3>
      <el-table :data="pending" empty-text="没有待评价的订单">
        <el-table-column prop="order_no" label="订单号" min-width="170" />
        <el-table-column prop="goods_title" label="商品" min-width="240" />
        <el-table-column prop="buyer_confirm_time" label="完成时间" min-width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }"><el-button link type="primary" @click="$router.push(`/portal/orders/${row.id}`)">去评价</el-button></template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card">
      <h3>我收到的评价</h3>
      <el-table :data="received" empty-text="暂无评价">
        <el-table-column label="评价人" width="140">
          <template #default="{ row }">{{ row.anonymous ? '匿名用户' : '用户 ' + row.from_user_id }}</template>
        </el-table-column>
        <el-table-column label="评分" width="130"><template #default="{ row }"><span class="stars">{{ '★'.repeat(row.score) }}</span></template></el-table-column>
        <el-table-column prop="tags" label="标签" min-width="150" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="create_time" label="时间" min-width="170" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../../http'

const pending = ref([])
const received = ref([])

onMounted(async () => {
  const done = await http.get('/trade-order/page', { params: { page: 1, size: 50, status: '已完成' } })
  pending.value = (done.records || []).filter((o) => !o.review_count)
  const reviewPage = await http.get('/report/review/page', { params: { page: 1, size: 50 } })
  received.value = reviewPage.records || []
})
</script>

<style scoped>
.stars { color: var(--a); font-size: 12px; }
</style>
