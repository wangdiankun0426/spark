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
        在提示词中约定模型以 JSON 响应，下方声明响应 JSON 的键，即可在下游节点引用对应字段
      </div>
    </el-form-item>
    <el-form-item label="输出声明">
      <div class="map-list">
        <el-button type="primary" link size="small" class="map-add" @click="addOutput">添加输出</el-button>
        <div v-for="(item, index) in form.config.outputs" :key="index" class="map-row">
          <el-input
              v-model="item.name"
              class="map-name"
              placeholder="参数名"
              clearable
              @change="emit('update')"
          />
          <el-input
              v-model="item.key"
              class="map-value"
              placeholder="参数，对应模型响应JSON的键"
              clearable
              @change="emit('update')"
          />
          <el-button type="danger" link size="small" @click="removeOutput(index)">删除</el-button>
        </div>
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

if (!Array.isArray(props.form.config.outputs)) {
  props.form.config.outputs = []
}

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

function addOutput() {
  props.form.config.outputs.push({ name: '', key: '' })
  emit('update')
}

function removeOutput(index) {
  props.form.config.outputs.splice(index, 1)
  emit('update')
}
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

.map-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.map-add {
  align-self: flex-end;
}

.map-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.map-name {
  width: 120px;
  flex-shrink: 0;
}

.map-value {
  flex: 1;
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
