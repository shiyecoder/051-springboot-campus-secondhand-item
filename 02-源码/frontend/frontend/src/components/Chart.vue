<template>
  <div ref="el" :style="{ height: height + 'px', width: '100%' }"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

/** ECharts 封装：统一主色系与空态，容器隐藏时也能正确 resize */
const props = defineProps({
  option: { type: Object, required: true },
  height: { type: Number, default: 240 }
})

const el = ref(null)
let chart = null

const render = () => {
  if (!chart && el.value) chart = echarts.init(el.value)
  if (chart) chart.setOption(props.option, true)
}

const resize = () => chart && chart.resize()

onMounted(() => {
  render()
  window.addEventListener('resize', resize)
})

watch(() => props.option, render, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  if (chart) chart.dispose()
  chart = null
})
</script>
