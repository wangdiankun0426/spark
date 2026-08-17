<template>
  <!--连线配置-->
  <div class="property-drawer" v-if="edge">
    <div class="drawer-header">
      <span class="drawer-title">连线配置</span>
      <el-button link @click="$emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div class="drawer-body">
      <el-form :model="edge" label-width="auto" size="small">
        <el-form-item label="连线ID">
          <el-input :model-value="edge.id" readonly />
        </el-form-item>
        <el-form-item label="源节点">
          <el-input :value="getNodeName(edge.source)" readonly />
        </el-form-item>
        <el-form-item label="目标节点">
          <el-input :value="getNodeName(edge.target)" readonly />
        </el-form-item>
      </el-form>
    </div>
    <div class="drawer-footer">
      <el-button type="danger" size="small" style="width:100%" @click="$emit('delete-edge')">
        <el-icon><Delete /></el-icon>删除连线
      </el-button>
    </div>
  </div>

  <!--节点配置-->
  <div class="property-drawer" v-else-if="node">
    <div class="drawer-header">
      <span class="drawer-title">节点配置</span>
      <el-button link @click="$emit('close')"><el-icon><Close /></el-icon></el-button>
    </div>
    <div class="drawer-body">
      <el-form :model="form" label-width="auto" size="small">
        <el-form-item label="节点ID">
          <el-input :model-value="node.id" readonly />
        </el-form-item>
        <el-form-item label="节点名称">
          <el-input v-model="form.name" @change="emitUpdate" />
        </el-form-item>

        <!--开始节点配置-->
        <template v-if="node.type === 'startEvent'">
          <el-divider content-position="left">输入参数</el-divider>
          <div v-for="(input, idx) in form.config.inputs" :key="idx" class="kv-row">
            <el-input
                size="default"
                v-model="input.name"
                placeholder="参数名"
                style="width:200px"
                @change="emitUpdate"
            />
            <el-select
                size="default"
                v-model="input.type"
                style="width:120px"
                @change="emitUpdate">
              <el-option label="文本" value="string" />
              <el-option label="数字" value="number" />
              <el-option label="布尔" value="boolean" />
              <el-option label="JSON" value="json" />
            </el-select>
            <el-checkbox
                v-model="input.required"
                @change="emitUpdate"
            >必填</el-checkbox>
            <el-button
                link
                type="danger"
                size="small"
                @click="removeInput(idx)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-button size="small" @click="addInput">+ 添加参数</el-button>
        </template>

        <!--结束节点配置-->
        <template v-if="node.type === 'endEvent'">
          <el-divider content-position="left">输出映射</el-divider>
          <div v-for="(out, idx) in form.config.outputs" :key="idx" class="kv-row">
            <el-input
                v-model="out.name"
                placeholder="输出字段名"
                style="width:160px"
                @change="emitUpdate" />
            <el-input
                v-model="out.source"
                placeholder="来源变量"
                style="width:160px"
                @change="emitUpdate" />
            <el-button
                link
                type="danger"
                size="small"
                @click="removeOutput(idx)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-button size="small" @click="addOutput">+ 添加映射</el-button>
        </template>

        <!--LLM节点配置-->
        <template v-if="node.type === 'llmTask'">
          <el-divider content-position="left">LLM配置</el-divider>
          <el-form-item label="模型">
            <el-select
                v-model="form.config.modelId"
                placeholder="选择模型"
                style="width:100%"
                @change="emitUpdate"
                clearable
                size="default"
            >
              <el-option
                  v-for="m in modelList"
                  :key="m.id"
                  :label="m.name"
                  :value="m.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="提示词">
            <el-input v-model="form.config.prompt" type="textarea" :rows="6" placeholder="输入提示词模板，使用 {{变量名}} 引用变量" @change="emitUpdate" />
          </el-form-item>
        </template>

        <!--删除节点-->
        <el-divider />
        <el-button type="danger" size="small" @click="$emit('delete-node', node.id)" style="width:100%">
          <el-icon><Delete /></el-icon>删除此节点
        </el-button>
      </el-form>
    </div>
  </div>

</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { pageModelListAPI } from '@/api/llm/model.js';

const props = defineProps({
  node: { type: Object, default: null },
  edge: { type: Object, default: null },
  nodes: { type: Array, default: () => [] }
});
const emit = defineEmits(['update', 'close', 'delete-node', 'delete-edge']);

const form = ref({});
const modelList = ref([]);

onMounted(async () => {
  const res = await pageModelListAPI({ page: false, type: 1 });
  if (res.code === 200 && res.data?.rows) {
    modelList.value = res.data.rows;
  }
});

watch(() => props.node, (newNode) => {
  if (newNode) {
    form.value = JSON.parse(JSON.stringify(newNode));
  } else {
    form.value = {};
  }
}, { immediate: true, deep: true });

function emitUpdate() {
  emit('update', form.value);
}

/** 获取节点名称用于连线信息展示 */
function getNodeName(nodeId) {
  const n = props.nodes.find(n => n.id === nodeId);
  return n ? n.name : '未知节点';
}

// 开始节点输入参数
function addInput() {
  if (!form.value.config) {
    form.value.config = {};
  }
  if (!form.value.config.inputs) {
    form.value.config.inputs = [];
  }
  form.value.config.inputs.push({ name: '', type: 'string', required: false, default: '', description: '' });
  emitUpdate();
}
function removeInput(idx) {
  form.value.config.inputs.splice(idx, 1); emitUpdate();
}

// 结束节点输出映射
function addOutput() {
  if (!form.value.config) {
    form.value.config = {};
  }
  if (!form.value.config.outputs) {
    form.value.config.outputs = [];
  }
  form.value.config.outputs.push({ name: '', source: '' });
  emitUpdate();
}
function removeOutput(idx) {
  form.value.config.outputs.splice(idx, 1);
  emitUpdate();
}

</script>

<style scoped lang="scss">
.property-drawer {
  width: 400px;
  background: #fff;
  border-left: 1px solid $border-color;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid $border-color-light;
}
.drawer-title {
  font-weight: 600;
  font-size: 14px;
  color: $color-text-primary;
}
.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}
.drawer-footer {
  flex-shrink: 0;
  padding: 12px 16px;
  border-top: 1px solid $border-color-light;
}
.kv-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}
</style>
