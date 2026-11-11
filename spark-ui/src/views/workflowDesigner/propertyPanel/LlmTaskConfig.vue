<template>
  <div class="llm-task-config">
    <el-divider>LLM配置</el-divider>
    <el-form-item label="模型">
      <el-select
          v-model="form.config.modelId"
          placeholder="选择模型"
          style="width:100%"
          @change="emit('update')"
          clearable
          size="default"
      >
        <el-option v-for="m in modelList" :key="m.id" :label="m.name" :value="m.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="提示词">
      <var-field
          clearable
          v-model="form.config.prompt"
          textarea
          :rows="12"
          placeholder="输入提示词模板，支持变量"
          @change="emit('update')"
      />
      <div class="llm-tips">
        如需模型响应参数，在提示词中规范模型的响应数据即可在下游节点中使用
      </div>
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
import { pageModelListAPI } from '@/api/llm/model.js'
import VarField from '../var/VarField.vue'
import { getNodeOutputs } from '../nodes/index.js'

const props = defineProps({ form: { type: Object, required: true } })
const emit = defineEmits(['update'])

const modelList = ref([])
onMounted(async () => {
  // 字典接口失败时保持空候选，不阻断面板挂载
  try {
    const res = await pageModelListAPI({ page: false, type: 1 })
    if (res && res.code === 200 && res.data?.rows) {
      modelList.value = res.data.rows
    }
  } catch (e) {
    modelList.value = []
  }
})

const outputVar = computed(() => getNodeOutputs(props.form))
</script>

<style scoped lang="scss">
.llm-tips {
  width: 100%;
  margin-top: 8px;
  padding: 8px 10px;
  border: 1px solid $border-color-light;
  border-radius: 4px;
  background: $app-bg-sub;
  font-size: 12px;
  color: $color-text-secondary;
  line-height: 1.8;
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
