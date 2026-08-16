<template>
  <el-drawer
      v-model="visible"
      :title="'运行: ' + (workflow?.name || '')"
      direction="ltr"
      size="100%"
      @close="handleClose"
  >
    <div v-if="workflow">
      <!--端点信息-->
      <el-alert
          v-if="endpoint"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom:16px"
      >
        <template #title>
          <span>端点: <code>POST /api/workflow/instance/{{ endpoint.path }}</code></span>
          <span
              v-if="endpoint.authType === 2"
              style="margin-left:12px;font-size:11px;color:#999"
          >API Key: {{ endpoint.apiKey }}
          </span>
        </template>
      </el-alert>

      <!--输入参数表单-->
      <el-form
          :model="params"
          label-width="auto"
          label-position="top"
          v-if="inputs.length > 0"
      >
        <el-form-item v-for="input in inputs" :key="input.name" :label="input.name" :required="input.required">
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

      <!--运行结果及详情（直接展示）-->
      <div v-if="runDetail" style="margin-top:16px">
        <el-divider />
        <el-alert :type="runStatus === 2 ? 'success' : 'error'" :closable="false" show-icon>
          <template #title>
            <span v-if="runStatus === 2">运行成功 (耗时 {{ runDetail.durationMs != null ? runDetail.durationMs + 'ms' : '-' }})</span>
            <span v-else>运行失败</span>
          </template>
        </el-alert>

        <el-descriptions :column="1" border size="small" style="margin-top:16px">
          <el-descriptions-item label="工作流">{{ runDetail.templateName }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ runDetail.revNum }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ runDetail.statusName }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ runDetail.durationMs != null ? runDetail.durationMs + 'ms' : '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="runDetail.createdByName" label="触发人">{{ runDetail.createdByName }}</el-descriptions-item>
          <el-descriptions-item label="触发时间">{{ runDetail.createdDt }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />
        <div class="section-title">输入参数</div>
        <el-input :model-value="formatJson(runDetail.inputJson)" type="textarea" :rows="4" readonly />
        <div class="section-title">输出结果</div>
        <el-input :model-value="formatJson(runDetail.outputJson)" type="textarea" :rows="6" readonly />
        <div v-if="runDetail.errorMsg" class="section-title" style="color:#f56c6c">错误信息</div>
        <el-alert v-if="runDetail.errorMsg" :title="runDetail.errorMsg" type="error" :closable="false" />

        <el-divider />
        <div class="section-title">节点执行日志</div>
        <el-timeline v-if="nodes.length > 0">
          <el-timeline-item
              v-for="n in nodes"
              :key="n.id"
              :timestamp="n.nodeName + ' (' + n.nodeType + ')'"
              :type="n.status === 3 ? 'success' : n.status === 4 ? 'danger' : 'info'"
              size="small"
          >
            <div class="node-status">{{ n.status === 3 ? '完成(' + n.durationMs + 'ms)' : n.status === 4 ? '失败: ' + (n.errorMsg || '') : '等待/运行中' }}</div>
            <div v-if="getNodeOutputText(n)" class="node-output">{{ getNodeOutputText(n) }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="无节点记录" :image-size="40" />
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { queryEndpointAPI } from '@/api/workflow/endpoint.js';
import { executeWorkflowAPI, queryInstanceDetailAPI, queryInstanceNodesAPI } from '@/api/workflow/instance.js';
import { queryVersionDetailAPI } from '@/api/workflow/version.js';

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
const runStatus = ref(null);
const runDetail = ref(null);
const nodes = ref([]);

function formatJson(str) {
  if (!str) return '';
  try {
    return JSON.stringify(JSON.parse(str), null, 2);
  } catch (e) {
    return str;
  }
}

/** 解析节点输出并返回可展示文本（LLM节点优先展示text输出） */
function getNodeOutputText(n) {
  if (!n.outputJson) return '';
  try {
    const out = JSON.parse(n.outputJson);
    if (typeof out.text === 'string' && out.text) {
      return out.text;
    }
    const str = JSON.stringify(out, null, 2);
    return str === '{}' ? '' : str;
  } catch (e) {
    return n.outputJson;
  }
}

watch(() => props.workflow, async (w) => {
  if (!w) return;
  inputs.value = [];
  params.value = {};
  runStatus.value = null;
  runDetail.value = null;
  nodes.value = [];
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
  runStatus.value = null;
  runDetail.value = null;
  nodes.value = [];

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
            runDetail.value = inst;
            break;
          }
        }
        retries--;
      }
      if (runDetail.value) {
        // 加载节点执行日志，直接展示
        const nRes = await queryInstanceNodesAPI({ instanceId });
        if (nRes.code === 200 && nRes.data) {
          nodes.value = nRes.data;
        }
      }
    }
  } catch (e) {
    ElMessage.error('运行出错: ' + (e.message || '未知错误'));
  }
  running.value = false;
}

function handleClose() { emit('update:modelValue', false); emit('success'); }
</script>

<style scoped lang="scss">
.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #172b4d;
  margin: $spacing-sm 0 6px;
}
.node-status {
  font-size: 12px;
  color: #172b4d;
}
.node-output {
  margin-top: 4px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 160px;
  overflow-y: auto;
}
</style>
