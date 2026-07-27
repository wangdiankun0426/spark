<template>
  <el-icon class="document-icon">
    <component :is="iconComponent" />
  </el-icon>
</template>

<script setup>
import { computed } from 'vue'
import { getDocumentCategory, DOCUMENT_CATEGORY } from '@/utils/documentUtil'
import DefaultDocumentIcon from '@/assets/icons/docIcon/unkonw.vue'

const documentIconModules = import.meta.glob('@/assets/icons/docIcon/*.vue', { eager: true })

const documentIconMap = {}
Object.keys(documentIconModules).forEach(key => {
  const fileName = key.split('/').pop().replace(/\.vue$/, '')
  documentIconMap[fileName.toLowerCase()] = documentIconModules[key].default
})

// 类别 -> 默认图标文件名（用于没有同名图标的后缀 fallback，如 jpg/png 走 image.vue）
const CATEGORY_ICON_FALLBACK = {
  [DOCUMENT_CATEGORY.WORD]: 'doc',
  [DOCUMENT_CATEGORY.EXCEL]: 'xls',
  [DOCUMENT_CATEGORY.PPT]: 'ppt',
  [DOCUMENT_CATEGORY.PDF]: 'pdf',
  [DOCUMENT_CATEGORY.TXT]: 'txt',
  [DOCUMENT_CATEGORY.MARKDOWN]: 'md',
  [DOCUMENT_CATEGORY.IMAGE]: 'image',
  [DOCUMENT_CATEGORY.VIDEO]: 'video',
  [DOCUMENT_CATEGORY.AUDIO]: 'audio'
}

const props = defineProps({
  ext: {
    type: String,
    default: ''
  }
})

const iconComponent = computed(() => {
  if (!props.ext) return DefaultDocumentIcon
  const ext = props.ext.toLowerCase().replace(/^\./, '')
  // 优先按后缀精确匹配，保留 docx/xlsx/pptx 等独立图标
  if (documentIconMap[ext]) return documentIconMap[ext]
  // 找不到则按类别 fallback 到统一图标
  const category = getDocumentCategory(ext)
  const fallbackName = CATEGORY_ICON_FALLBACK[category]
  return (fallbackName && documentIconMap[fallbackName]) || DefaultDocumentIcon
})
</script>

<style scoped lang="scss">
.document-icon {
  font-size: 30px;
  vertical-align: middle;
  margin-right: $spacing-xs;
}
.document-icon :deep(svg) {
  width: 1em;
  height: 1em;
}
</style>
