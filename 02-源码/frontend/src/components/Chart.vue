<template>
  <div ref="el" :style="{ width: '100%', height: height + 'px' }"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

/** ECharts 轻封装：统一主色序列，随窗口自适应，卸载时销毁避免内存泄漏 */
const props = defineProps({
  option: { type: Object, required: true },
  height: { type: Number, default: 220 }
})
const el = ref(null)
let chart = null

const paint = () => {
  if (chart) chart.setOption({ color: ['#146B5B', '#3E8E7E', '#D9A441', '#B9C2C6'], ...props.option }, true)
}
const resize = () => chart && chart.resize()

onMounted(() => {
  chart = echarts.init(el.value)
  paint()
  window.addEventListener('resize', resize)
})
watch(() => props.option, paint, { deep: true })
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart && chart.dispose()
})
</script>
