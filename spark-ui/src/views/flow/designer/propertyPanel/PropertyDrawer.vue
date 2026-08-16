<template>
  <!--连线配置-->
  <div class="attr-panel" v-if="sequence">
    <div class="panel-header">
      <span class="panel-title">连线配置</span>
      <el-button link @click="$emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div class="panel-body">
      <el-form :model="sequence" label-width="auto" size="small">
        <el-form-item label="连线ID">
          <el-input :model-value="sequence.id" readonly />
        </el-form-item>
        <el-form-item label="源节点">
          <el-input :value="getNodeName(sequence.sourceRef)" readonly />
        </el-form-item>
        <el-form-item label="目标节点">
          <el-input :value="getNodeName(sequence.targetRef)" readonly />
        </el-form-item>
      </el-form>
    </div>
    <div class="panel-footer">
      <el-button type="danger" size="small" style="width:100%" @click="$emit('delete-sequence', sequence.id)">
        <el-icon><Delete /></el-icon>删除连线
      </el-button>
    </div>
  </div>

  <!--节点配置-->
  <div class="attr-panel" v-else-if="node">
    <div class="panel-header">
      <span class="panel-title">节点配置</span>
      <el-button link @click="$emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div class="panel-body">
      <el-form :model="node" label-width="auto" size="small">
        <el-form-item label="节点ID">
          <el-input :model-value="node.id" readonly />
        </el-form-item>
        <el-form-item label="节点名称">
          <el-input v-model="node.name" />
        </el-form-item>
        <assignee-selector
            v-if="node.type === 'userTask'"
            :node="node"
            :field-options="fieldOptions"
        />
        <permission-config
            v-if="node.type === 'userTask'"
            :node="node"
        />
      </el-form>
    </div>
    <div class="panel-footer">
      <el-button type="danger" size="small" style="width:100%" @click="$emit('delete-node', node.id)">
        <el-icon><Delete /></el-icon>删除节点
      </el-button>
    </div>
  </div>

</template>

<script setup>
import AssigneeSelector from './AssigneeSelector.vue'
import PermissionConfig from './PermissionConfig.vue'

const props = defineProps({
  node: { type: Object, default: null },
  sequence: { type: Object, default: null },
  nodes: { type: Array, default: () => [] },
  fieldOptions: { type: Array, default: () => [] }
})

const emit = defineEmits(['delete-node', 'delete-sequence', 'close'])

/** 获取节点名称用于连线信息展示 */
function getNodeName(refId) {
  const n = props.nodes.find(el => el.id === refId)
  return n ? n.name : '未知节点'
}
</script>

<style scoped lang="scss">
.attr-panel {
  width: 400px;
  background: #fff;
  border-left: 1px solid $border-color;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px $spacing-md;
    border-bottom: 1px solid $border-color-light;
    flex-shrink: 0;
  }

  .panel-body {
    flex: 1;
    overflow-y: auto;
    padding: 12px $spacing-md;
  }

  .panel-footer {
    flex-shrink: 0;
    padding: 12px $spacing-md;
    border-top: 1px solid $border-color-light;
  }

  .panel-title {
    font-size: 14px;
    font-weight: 600;
    color: $color-text-primary;
  }
}
</style>
