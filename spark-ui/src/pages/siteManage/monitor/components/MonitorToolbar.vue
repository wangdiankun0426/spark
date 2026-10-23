<template>
  <div class="monitor-toolbar">
    <el-select
      v-if="showNode"
      :model-value="nodeKey"
      class="monitor-toolbar__node"
      placeholder="请选择节点"
      @update:model-value="value => emit('update:nodeKey', value)"
    >
      <el-option v-if="allowAll" label="全部节点" value="">
        <span class="monitor-toolbar__dot is-online"></span>
        <span>全部节点</span>
      </el-option>
      <el-option
        v-for="node in nodeList"
        :key="node.nodeKey"
        :label="node.nodeKey"
        :value="node.nodeKey"
        :disabled="!node.online"
      >
        <span class="monitor-toolbar__dot" :class="node.online ? 'is-online' : 'is-offline'"></span>
        <span>{{ node.nodeKey }}</span>
        <span class="monitor-toolbar__state">{{ node.online ? '在线' : '离线' }}</span>
      </el-option>
    </el-select>
    <el-radio-group
      :model-value="range"
      size="small"
      @update:model-value="value => emit('update:range', value)"
    >
      <el-radio-button
        v-for="option in RANGE_OPTIONS"
        :key="option.value"
        :label="option.value"
      >
        {{ option.label }}
      </el-radio-button>
    </el-radio-group>
  </div>
</template>

<script setup>
import { RANGE_OPTIONS } from '@/utils/monitorUtil.js'

defineProps({
  showNode: { type: Boolean, default: true },
  allowAll: { type: Boolean, default: false },
  nodeList: { type: Array, default: () => [] },
  nodeKey: { type: String, default: '' },
  range: { type: String, default: '1h' }
})

const emit = defineEmits(['update:nodeKey', 'update:range'])
</script>

<style scoped lang="scss">
.monitor-toolbar {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;

  &__node {
    width: 260px;
  }

  &__dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    margin-right: 6px;
    border-radius: 50%;

    &.is-online {
      background-color: var(--el-color-success);
    }

    &.is-offline {
      background-color: var(--el-color-info);
    }
  }

  &__state {
    float: right;
    color: $color-text-placeholder;
    font-size: 12px;
  }
}
</style>
