<template>
  <span class="tag" :class="toneOf">{{ text }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { label, tone } from '../dict'

const props = defineProps({ code: [String, Number], type: String })
const text = computed(() => label(props.type, props.code))
const toneOf = computed(() => tone(props.type, props.code))
</script>
