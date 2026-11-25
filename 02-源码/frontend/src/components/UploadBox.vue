<template>
  <div class="up">
    <div v-for="(p, i) in list" :key="p + i" class="up-item">
      <img :src="p" alt="" />
      <i class="up-del" @click="remove(i)">×</i>
    </div>
    <label class="up-add" v-if="list.length < max">
      <input type="file" accept="image/jpeg,image/png,image/webp" hidden @change="pick" />
      <span v-if="!busy">＋ 上传</span>
      <span v-else>上传中…</span>
    </label>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../http'

/** 图片上传组件：只回传后端相对路径，不提供手填 URL 入口（禁止外链） */
const props = defineProps({
  modelValue: { type: [String, Array], default: '' },
  max: { type: Number, default: 6 },
  multiple: { type: Boolean, default: true },
  bizType: { type: String, default: 'goods' }
})
const emit = defineEmits(['update:modelValue'])
const busy = ref(false)

const list = computed(() =>
  Array.isArray(props.modelValue)
    ? props.modelValue.filter(Boolean)
    : String(props.modelValue || '').split(',').filter(Boolean)
)

async function pick(e) {
  const file = e.target.files[0]
  if (!file) return
  busy.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('bizType', props.bizType)
    const data = await http.post('/file/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    const next = props.multiple ? [...list.value, data.file_path] : [data.file_path]
    emit('update:modelValue', props.multiple || Array.isArray(props.modelValue) ? next : data.file_path)
    ElMessage.success('上传成功')
  } finally {
    busy.value = false
    e.target.value = ''
  }
}

function remove(i) {
  const next = list.value.filter((_, idx) => idx !== i)
  emit('update:modelValue', Array.isArray(props.modelValue) ? next : next.join(','))
}
</script>

<style scoped>
.up { display: flex; flex-wrap: wrap; gap: 8px; }
.up-item { position: relative; width: 88px; height: 66px; border-radius: 8px; overflow: hidden; border: 1px solid var(--line); }
.up-item img { width: 100%; height: 100%; object-fit: cover; display: block; }
.up-del { position: absolute; top: 2px; right: 4px; color: #fff; background: rgba(31, 41, 51, .6); width: 16px; height: 16px; line-height: 14px; text-align: center; border-radius: 50%; font-style: normal; cursor: pointer; font-size: 12px; }
.up-add { width: 88px; height: 66px; border: 1px dashed var(--line); border-radius: 8px; display: grid; place-items: center; color: var(--ink2); font-size: 12px; cursor: pointer; }
</style>
