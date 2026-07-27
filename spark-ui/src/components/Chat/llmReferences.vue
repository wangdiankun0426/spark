<template>
  <div v-if="references && references.length" class="chat-references">
    <div class="chat-references-title">
      <span>参考文档</span>
    </div>
    <ul class="chat-references-list">
      <li
          v-for="(ref, idx) in references"
          :key="idx"
          class="chat-references-item"
          :title="ref.docName"
          @click="handlePreview(ref)"
      >
        <DocumentIcon class="chat-references-icon" :ext="getExt(ref.docName)" />
        <span class="chat-references-name">{{ ref.docName }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import DocumentIcon from '@/components/DocumentIcon'

defineProps({
  // 后端返回的引用文档列表 [{ docId, docName, score }]
  references: { type: Array, default: () => [] }
})

const router = useRouter()

/**
 * 从文件名提取扩展名（不含 .），无后缀返回空串
 */
function getExt(fileName) {
  if (!fileName) return ''
  const idx = fileName.lastIndexOf('.')
  if (idx < 0 || idx === fileName.length - 1) return ''
  return fileName.slice(idx + 1)
}

/**
 * 点击引用文档，新标签页打开预览
 */
function handlePreview(ref) {
  if (!ref || !ref.docId) {
    return
  }
  const { href } = router.resolve({ path: '/document/preview', query: { id: ref.docId } })
  window.open(href, '_blank')
}
</script>

<style scoped lang="scss">
.chat-references {
  margin-top: $spacing-sm;
  margin-left: auto;
  padding-top: $spacing-sm;
  border-top: 1px solid $border-color;
  max-width: 100%;
}

.chat-references-title {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: 14px;
  color: $color-text-secondary;
  margin-left: 20px;
  margin-bottom: $spacing-xs;
}

.chat-references-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.chat-references-item {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: 2px;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: $transition-fast;
  font-size: 14px;
  color: $color-text-primary;

  &:hover {
    background-color: $color-primary-soft;
  }
}

.chat-references-icon {
  font-size: 16px;
  flex-shrink: 0;
  margin-right: 0;
  vertical-align: middle;
}

.chat-references-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
