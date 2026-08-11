<template>
  <el-drawer v-model="visible" :title="'运行: ' + (workflow?.name || '')" direction="rtl" size="40%" @close="handleClose">
    <div v-if="workflow">
      <!--端点信息-->
      <el-alert v-if="endpoint" type="success" :closable="false" show-icon style="margin-bottom:16px">
        <template #title>
          <span>端点: <code>POST /api/workflow/instance/{{ endpoint.path }}</code></span>
          <span v-if="endpoint.authType === 2" style="margin-left:12px;font-size:11px;color:#999">API Key: {{ endpoint.apiKey }}</span>
        </template>
      </el-alert>

      <!--输入参数表单-->
      <el-form :model="params" label-width="auto" label-position="top" v-if="inputs.length > 0">
        <el-form-item v-for="input in inputs" :key="input.name" :label="input.name + (input.required ? ' *' : '')" :required="input.required">
          <el-input v-if="input.type === 'string' || input.type === 'text' || !input.type"
              v-model="params[input.name]" :placeholder="input.description || '请输入' + input.name" />
          <el-input-number v-else-if="input.type === 'number'"
              v-model="params[input.name]" style="width:100%" />
          <el-switch v-else-if="input.type === 'boolean'"
              v-model="params[input.name]" />
          <el-input v-else v-model="params[input.name]" type="textarea" :rows="3"
              :placeholder="input.description || '请输入JSON'" />
        </el-form-item>
      </el-form>
      <el-empty v-else description="此工作流无输入参数" :image-size="60" />

      <!--运行按钮和结果-->
      <div style="margin-top:16px">
        <el-button type="primary" @click="handleRun" :loading="running" style="width:100%">
          <el-icon><VideoPlay /></el-icon>{{ running ? '运行中...' : '开始运行' }}
        </el-button>
      </div>

      <!--运行结果-->
      <div v-if="runResult" style="margin-top:16px">
        <el-divider />
        <el-alert :type="runStatus === 2 ? 'success' : 'error'" :closable="false" show-icon>
          <template #title>
            <span v-if="runStatus === 2">运行成功 (耗时 {{ duration }}ms)</span>
            <span v-else>运行失败</span>
          </template>
        </el-alert>
        <div v-if="runStatus === 2" style="margin-top:12px">
          <el-button type="primary" style="width:100%" @click="openHistory">
            <el-icon><Document /></el-icon>查看运行详情
          </el-button>
        </div>
      </div>
    </div>

    <!--运行实例详情（复用公共组件）-->
    <instance-history-drawer v-model="historyVisible" :instance-id="lastInstanceId" />
  </el-drawer>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { queryEndpointAPI } from '@/api/workflow/endpoint.js';
import { executeWorkflowAPI, queryInstanceDetailAPI } from '@/api/workflow/instance.js';
import { queryVersionDetailAPI } from '@/api/workflow/version.js';
import InstanceHistoryDrawer from '@/components/WfRunInstanceDrawer/index.vue';

const props = defineProps({ modelValue: Boolean, workflow: Object });
const emit = defineEmits(['update:modelValue', 'success']);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const endpoint = ref(null);
const inputs = ref([]);
const params = ref({});
const running = ref(false);
const runResult = ref(null);
const runStatus = ref(null);
const duration = ref(0);

// 运行成功后记录实例 id，用于打开统一详情组件
const historyVisible = ref(false);
const lastInstanceId = ref(null);

function openHistory() { historyVisible.value = true; }

watch(() => props.workflow, async (w) => {
  if (!w) return;
  inputs.value = [];
  params.value = {};
  // 查询端点
  const epRes = await queryEndpointAPI({ templateId: w.id });
  if (epRes.code === 200 && epRes.data) { endpoint.value = epRes.data; }
  // 从版本DAG中提取start节点输入参数
  if (w.revId) {
    const vRes = await queryVersionDetailAPI({ id: w.revId });
    if (vRes.code === 200 && vRes.data && vRes.data.dagJson) {
      try {
        const dag = JSON.parse(vRes.data.dagJson);
        const startNode = (dag.nodes || []).find(n => n.type === 'startEvent');
        if (startNode && startNode.config && startNode.config.inputs) {
          inputs.value = startNode.config.inputs;
          const p = {};
          startNode.config.inputs.forEach(item => {
            p[item.name] = item.default !== undefined ? item.default : (item.type === 'number' ? null : '');
          });
          params.value = p;
        }
      } catch (e) { /* ignore */ }
    }
  }
});

async function handleRun() {
  if (!endpoint.value?.path) {
    ElMessage.warning('此工作流未配置端点');
    return;
  }
  running.value = true;
  runResult.value = null;

  try {
    const res = await executeWorkflowAPI(endpoint.value.path, params.value);
    if (res.code === 200 && res.data?.instanceId) {
      // 轮询查询运行结果
      const instanceId = res.data.instanceId;
      let retries = 30;
      while (retries > 0) {
        await new Promise(r => setTimeout(r, 1000));
        const detailRes = await queryInstanceDetailAPI({ id: instanceId });
        if (detailRes.code === 200 && detailRes.data) {
          const inst = detailRes.data;
          if (inst.status !== 1) {
            runStatus.value = inst.status;
            duration.value = inst.durationMs || 0;
            runResult.value = inst.outputJson || {};
            lastInstanceId.value = instanceId;
            break;
          }
        }
        retries--;
      }
    }
  } catch (e) {
    ElMessage.error('运行出错: ' + (e.message || '未知错误'));
  }
  running.value = false;
}

function handleClose() { emit('update:modelValue', false); emit('success'); }
</script>
