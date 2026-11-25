<template>
  <span class="tag" :class="toneOf">{{ text }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { label, tone } from '../dict'

const props = defineProps({
  code: { type: [String, Number], default: '' },
  type: { type: String, required: true }
})

const text = computed(() => label(props.type, props.code))
const toneOf = computed(() => tone(props.type, props.code))
</script>
