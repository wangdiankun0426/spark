<template>
  <div class="agent-task-config">
    <el-divider>Agent配置</el-divider>
    <el-form-item label="智能体">
      <el-select
          v-model="form.config.agentId"
          placeholder="选择智能体"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option v-for="a in agentList" :key="a.id" :label="a.name" :value="a.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="任务描述">
      <var-field
          clearable
          v-model="form.config.task"
          textarea
          :rows="6"
          placeholder="输入Agent任务描述，支持变量"
          @change="emit('update')"
      />
    </el-form-item>
    <el-form-item label="记忆ID">
      <var-field
          clearable
          v-model="form.config.memoryId"
          placeholder="选填，隔离对话历史的记忆ID，支持变量"
          @change="emit('update')"
      />
      <div class="form-tips">不填时默认以节点ID作为记忆ID</div>
    </el-form-item>
    <el-divider>输出数据</el-divider>
    <div
        v-for="b in outputVar"
        :key="b.value"
        class="var-row"
    >
      <span class="var-label">{{ b.label }}</span>
      <code class="var-code" :title="b.value" v-text="b.value"></code>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { pageAgentListAPI } from '@/api/llm/agent.js'
import VarField from '../var/VarField.vue'
import { getNodeOutputs } from '../nodes/index.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const agentList = ref([])
onMounted(async () => {
  // 字典接口失败时保持空候选，不阻断面板挂载
  try {
    const res = await pageAgentListAPI({ page: false })
    if (res && res.code === 200 && res.data?.rows) {
      agentList.value = res.data.rows
    }
  } catch (e) {
    agentList.value = []
  }
})

const outputVar = computed(() => getNodeOutputs(props.form))
</script>

<style scoped lang="scss">
.form-tips {
  width: 100%;
  font-size: 12px;
  color: $color-text-secondary;
  line-height: 1.5;
  margin-top: 4px;
}

.var-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px;
  .var-label {
    flex-shrink: 0;
    font-size: 13px;
    color: $color-text-primary;
  }
  .var-code {
    flex: 1;
    min-width: 0;
    font-size: 12px;
    color: $app-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
