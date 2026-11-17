<template>
  <el-drawer
      v-model="visible"
      :title="'运行: ' + (workflow?.name || '')"
      direction="ltr"
      size="100%"
      @close="handleClose"
      :close-on-click-modal="false"
  >
    <div v-if="workflow">
      <form-view
          :form="formJson"
          v-if="formJson && formJson.widgetList && formJson.widgetList.length > 0"
      />
      <el-empty v-else description="此工作流无输入参数" :image-size="60" />

      <!--运行按钮和结果-->
      <div style="margin-top:16px">
        <el-button type="primary" @click="handleRun" :loading="running" style="width:100%">
          <el-icon><VideoPlay /></el-icon>{{ running ? '运行中...' : '开始运行' }}
        </el-button>
      </div>

      <!--运行结果及详情-->
      <div v-if="runDetail" style="margin-top:16px">
        <el-divider />
        <el-alert :type="runStatus === 2 ? 'success' : runStatus === 6 ? 'warning' : 'error'" :closable="false" show-icon>
          <template #title>
            <span v-if="runStatus === 2">运行成功 (耗时 {{ runDetail.durationMs != null ? runDetail.durationMs + 'ms' : '-' }})</span>
            <span v-else-if="runStatus === 6">已暂停：等待人工审核，可在「待审核」页处理</span>
            <span v-else-if="runStatus === 7">已驳回</span>
            <span v-else>运行失败</span>
          </template>
        </el-alert>

        <div class="section-title">实例数据</div>
        <el-descriptions
            :column="1"
            border
            size="small"
            style="margin-top:16px"
        >
          <el-descriptions-item label="工作流">{{ runDetail.templateName }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ runDetail.revNum }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ runDetail.statusName }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ runDetail.durationMs != null ? runDetail.durationMs + 'ms' : '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="runDetail.createdByName" label="触发人">{{ runDetail.createdByName }}</el-descriptions-item>
          <el-descriptions-item label="触发时间">{{ runDetail.createdDt }}</el-descriptions-item>
        </el-descriptions>

        <div
            v-if="runDetail.errorMsg"
            class="section-title section-title--error"
        >错误信息</div>
        <el-alert
            v-if="runDetail.errorMsg"
            :title="runDetail.errorMsg"
            type="error"
            :closable="false"
        />

        <el-divider />

        <div class="section-title">执行日志</div>
        <el-timeline v-if="nodes.length > 0">
          <el-timeline-item
              v-for="n in nodes"
              :key="n.id"
              :timestamp="n.statusName"
              :type="n.status === 3 ? 'success' : n.status === 4 ? 'danger' : n.status === 1 ? 'warning' : 'info'"
          >
            <div class="node-status">{{n.nodeName}} ( {{n.nodeType}} )</div>
            <div v-if="getNodeText(n.inputJson)">
              输入数据:
              <div class="node-output">{{ getNodeText(n.inputJson) }}</div>
            </div>
            <div v-if="getNodeText(n.outputJson)">
              输出数据:
              <div class="node-output">{{ getNodeText(n.outputJson) }}</div>
            </div>
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
import FormView from '@/components/FormView';
import { collectFormValues, validateFormValues } from '@/views/formDesigner/formSchema.js';
import { runWorkflowAPI, queryInstanceDetailAPI, queryInstanceNodesAPI } from '@/api/workflow/instance.js';
import { queryWorkflowDetailAPI } from '@/api/workflow/template';

const props = defineProps({ modelValue: Boolean, workflow: Object });
const emit = defineEmits(['update:modelValue', 'success']);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const formJson = ref(null);
const formId = ref(null);
const formRevId = ref(null);
const running = ref(false);
const runStatus = ref(null);
const runDetail = ref(null);
const nodes = ref([]);

/** 解析节点参数并返回可展示文本 */
function getNodeText(n) {
  if (!n) return '';
  try {
    const out = JSON.parse(n);
    if (typeof out.text === 'string' && out.text) {
      return out.text;
    }
    const str = JSON.stringify(out, null, 2);
    return str === '{}' ? '' : str;
  } catch (e) {
    return n;
  }
}

watch(() => props.workflow, async (w) => {
  if (!w) return;
  formJson.value = null;
  formId.value = null;
  formRevId.value = null;
  runStatus.value = null;
  runDetail.value = null;
  nodes.value = [];
  // 加载模板详情，取绑定的输入表单定义
  if (w.id) {
    const dRes = await queryWorkflowDetailAPI({ id: w.id });
    if (dRes.code === 200 && dRes.data && dRes.data.formJson) {
      formId.value = dRes.data.formId;
      formRevId.value = dRes.data.formRevId || null;
      try {
        formJson.value = JSON.parse(dRes.data.formJson);
      } catch (e) { /* ignore */ }
    }
  }
});

async function handleRun() {
  // 必填校验并收集表单值作为运行输入参数
  const validation = validateFormValues(formJson.value, '运行');
  if (!validation.valid) {
    ElMessage.warning(validation.message);
    return;
  }
  const values = collectFormValues(formJson.value);

  running.value = true;
  runStatus.value = null;
  runDetail.value = null;
  nodes.value = [];

  try {
    const res = await runWorkflowAPI({
      templateId: props.workflow.id,
      formId: formId.value,
      formRevId: formRevId.value,
      values
    });
    if (res.code === 200 && res.data?.id) {
      // 轮询查询运行结果
      const instanceId = res.data.id;
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
  color: $app-text-primary;
  margin: $spacing-sm 0 6px;
}
.section-title--error {
  color: $el-color-danger;
}
.node-status {
  font-size: 12px;
  color: $app-text-primary;
}
.node-output {
  margin-top: 4px;
  padding: 8px;
  background: $app-bg-sub;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 160px;
  overflow-y: auto;
}
</style>
