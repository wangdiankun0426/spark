<template>
  <div class="menu-perm-group">
    <template v-for="node in nodes" :key="node.id">
      <div class="menu-perm-row">
        <el-checkbox
            :class="depth === 0 ? 'menu-perm-root' : ''"
            :model-value="stateOf(node).checked"
            :indeterminate="stateOf(node).indeterminate"
            @change="(checked) => onToggle(node, checked)"
        >
          {{ node.name }}
        </el-checkbox>
      </div>
      <MenuPermGroup
          v-if="node.children.length"
          :nodes="node.children"
          :depth="depth + 1"
          :state-of="stateOf"
          :on-toggle="onToggle"
      />
    </template>
  </div>
</template>

<script setup>
defineOptions({ name: 'MenuPermGroup' });

defineProps({
  nodes: { type: Array, default: () => [] },
  depth: { type: Number, default: 0 },
  stateOf: { type: Function, required: true },
  onToggle: { type: Function, required: true },
});
</script>

<style scoped lang="scss">
.menu-perm-group {
  width: 100%;
}
.menu-perm-row {
  margin-bottom: 4px;
}
.menu-perm-group > .menu-perm-group {
  margin-left: 20px;
}
.menu-perm-root :deep(.el-checkbox__label) {
  font-weight: 600;
}
</style>
