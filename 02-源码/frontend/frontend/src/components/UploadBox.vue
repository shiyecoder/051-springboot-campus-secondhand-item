<template>
  <div class="upload-box">
    <div v-if="modelValue" class="preview">
      <img :src="modelValue" alt="预览" />
      <a class="rm" @click="remove">删除</a>
    </div>
    <label v-else class="pick">
      <input type="file" accept="image/*,application/pdf" @change="onPick" />
      <span>+ {{ text }}</span>
    </label>
    <div class="hint">支持 jpg/png/pdf，单文件 ≤10MB，保存为项目内本地文件</div>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../http'

/** 上传组件：禁止外链，成功后回填 /uploads/... 相对路径 */
const props = defineProps({
  modelValue: { type: String, default: '' },
  text: { type: String, default: '上传图片' }
})
const emit = defineEmits(['update:modelValue'])

async function onPick(e) {
  const file = e.target.files && e.target.files[0]
  if (!file) return
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('单文件不超过 10MB')
    return
  }
  const fd = new FormData()
  fd.append('file', file)
  try {
    const data = await http.post('/file/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    emit('update:modelValue', data.path)
    ElMessage.success('上传成功')
  } catch (err) {
    /* 统一拦截已提示 */
  }
  e.target.value = ''
}

function remove() {
  ElMessageBox.confirm('确认删除该附件？删除后需要重新上传。', '删除确认', { type: 'warning' })
    .then(() => emit('update:modelValue', ''))
    .catch(() => {})
}
</script>

<style scoped>
.upload-box { display: flex; flex-direction: column; gap: 6px; }
.pick { border: 1px dashed #d8c8c4; border-radius: 6px; height: 74px; width: 132px; display: grid; place-items: center; color: #6b5e62; cursor: pointer; font-size: 12px; background: #fdfbfa; }
.pick input { display: none; }
.preview { position: relative; width: 132px; }
.preview img { width: 132px; height: 88px; object-fit: cover; border-radius: 6px; border: 1px solid #e7dfdc; }
.preview .rm { position: absolute; right: 4px; bottom: 4px; background: rgba(0, 0, 0, .55); color: #fff; font-size: 11px; padding: 1px 6px; border-radius: 3px; cursor: pointer; }
.hint { font-size: 11px; color: #9c8a86; }
</style>
