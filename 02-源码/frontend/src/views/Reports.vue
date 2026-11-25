<template>
  <div>
    <div class="page-h"><div><h2>举报与纠纷</h2><p>48 小时内受理；处理结果通过站内消息通知双方；被举报人可申诉一次，由另一名运营复核</p></div></div>
    <div class="filter">
      <div class="fi"><label>状态</label>
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option v-for="(v, k) in DICT.report_status" :key="k" :label="v[0]" :value="k" />
        </el-select>
      </div>
      <div class="fi"><label>类型</label>
        <el-select v-model="query.reason_type" clearable placeholder="全部" style="width: 180px" @change="load(1)">
          <el-option v-for="(v, k) in DICT.report_reason" :key="k" :label="v[0]" :value="k" />
        </el-select>
      </div>
      <div class="sp"></div>
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <div class="card">
      <el-table :data="rows" v-loading="loading" empty-text="当前筛选条件下无举报记录">
        <el-table-column prop="report_no" label="举报编号" min-width="150" />
        <el-table-column prop="biz_title" label="关联对象" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="110"><template #default="{ row }"><StatusTag :code="row.reason_type" type="report_reason" /></template></el-table-column>
        <el-table-column prop="description" label="问题描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="110"><template #default="{ row }"><StatusTag :code="row.status" type="report_status" /></template></el-table-column>
        <el-table-column label="申诉" width="110"><template #default="{ row }"><StatusTag :code="row.appeal_status" type="appeal_status" /></template></el-table-column>
        <el-table-column label="证据" width="80">
          <template #default="{ row }"><el-button link type="primary" @click="peek(row)">查看</el-button></template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="['已处理','已驳回'].includes(row.status)" @click="open(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <span>共 {{ total }} 条</span>
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
      </div>
    </div>

    <el-drawer v-model="drawer" title="举报处理" size="520px">
      <el-descriptions :column="1" border v-if="current.id">
        <el-descriptions-item label="举报编号">{{ current.report_no }}</el-descriptions-item>
        <el-descriptions-item label="关联对象">{{ current.biz_title || current.biz_type + ' #' + current.biz_id }}</el-descriptions-item>
        <el-descriptions-item label="举报类型">{{ current.reason_type }}</el-descriptions-item>
        <el-descriptions-item label="问题描述">{{ current.description }}</el-descriptions-item>
        <el-descriptions-item label="证据图片">
          <div class="ev"><img v-for="(p, i) in splitImages(current.evidence_urls)" :key="i" :src="p" alt="" /></div>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="96px" style="margin-top: 16px">
        <el-form-item label="处理结果" required>
          <el-input v-model="form.result" type="textarea" :rows="3" placeholder="不少于 5 字，例如：核实后下架商品并对卖家扣 10 分" />
        </el-form-item>
        <el-form-item label="处置">
          <el-checkbox v-model="form.freezeGoods">冻结商品</el-checkbox>
          <el-checkbox v-model="form.limitUser">限制账号</el-checkbox>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="form.targetStatus" style="width: 180px">
            <el-option label="处理中" value="处理中" />
            <el-option label="已处理" value="已处理" />
            <el-option label="已驳回" value="已驳回" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawer = false">取消</el-button>
        <el-button type="primary" @click="submit">提交处理</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'
import StatusTag from '../components/StatusTag.vue'
import { splitImages } from '../utils/format'
import { DICT } from '../dict'

/** 举报受理与处理：处理结果必填，可同时冻结商品或限制账号 */
const rows = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const drawer = ref(false)
const current = ref({})
const query = reactive({ status: '', reason_type: '' })
const form = reactive({ result: '', targetStatus: '已处理', freezeGoods: false, limitUser: false })

async function load(p) {
  if (p) page.value = p
  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    Object.keys(query).forEach((k) => query[k] && (params[k] = query[k]))
    const data = await http.get('/report/page', { params })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function open(row) {
  current.value = row
  form.result = ''
  form.targetStatus = '已处理'
  form.freezeGoods = false
  form.limitUser = false
  drawer.value = true
}

const peek = (row) => {
  current.value = row
  drawer.value = true
}

async function submit() {
  if (form.result.trim().length < 5) {
    ElMessage.warning('处理结果需填写不少于 5 字')
    return
  }
  await http.post(`/report/${current.value.id}/handle`, form)
  ElMessage.success('处理完成')
  drawer.value = false
  load()
}

onMounted(() => load())
</script>

<style scoped>
.ev { display: flex; gap: 6px; flex-wrap: wrap; }
.ev img { width: 72px; height: 54px; object-fit: cover; border-radius: 6px; }
</style>
