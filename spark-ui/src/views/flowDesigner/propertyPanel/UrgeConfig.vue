<template>
  <div class="urge-config">
    <el-form-item label="定时催办">
      <el-switch
          :model-value="node.urgeEnabled === true"
          active-text="开启"
          inactive-text="关闭"
          @change="handleEnabledChange"
      />
    </el-form-item>
    <el-form-item v-if="node.urgeEnabled === true" label="催办间隔">
      <el-input-number
          :model-value="node.urgeInterval || 8"
          :min="1"
          :max="720"
          :step="1"
          controls-position="right"
          @change="handleIntervalChange"
      />
      <div class="urge-tip">节点停留超过该时长后，每隔该时长自动向当前审批人发送催办通知</div>
    </el-form-item>
  </div>
</template>

<script setup>
defineOptions({ name: 'UrgeConfig' })

const props = defineProps({
  node: { type: Object, required: true }
})

/**
 * 切换定时催办开关，开启时写入默认间隔
 * @param val
 */
function handleEnabledChange(val) {
  props.node.urgeEnabled = val
  if (val && !props.node.urgeInterval) {
    props.node.urgeInterval = 8
  }
}

/**
 * 修改催办间隔
 * @param val
 */
function handleIntervalChange(val) {
  props.node.urgeInterval = val || 8
}
</script>

<style scoped lang="scss">
.urge-config {
  width: 100%;
}

.urge-tip {
  width: 100%;
  font-size: 12px;
  line-height: 1.5;
  color: $color-text-secondary;
}
</style>
