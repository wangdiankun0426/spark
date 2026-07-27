<template>
  <el-drawer
      v-model="visible"
      :title="drawerTitle"
      direction="ltr"
      size="100%"
      :destroy-on-close="true"
      class="chat-drawer"
  >
    <chat-panel
        v-if="visible && target && target.id"
        :target="target"
        :target-type="targetType"
        @space-created="handleSpaceCreated"
    />
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'
import ChatPanel from '@/components/Chat/chatPanel.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  target: { type: Object, default: () => ({}) },
  // 对方类型：agent=智能体，model=模型，user=用户
  targetType: { type: String, default: 'agent' }
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const drawerTitle = computed(() => {
  return props.target && props.target.name ? '与 ' + props.target.name + ' 对话' : 'AI 对话'
})

/**
 * 聊天空间创建成功后同步到 target
 */
function handleSpaceCreated({ targetId, spaceId }) {
  if (props.target && props.target.id === targetId) {
    props.target.chatSpaceId = spaceId
  }
}
</script>

<style scoped lang="scss">
:deep(.el-drawer__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
