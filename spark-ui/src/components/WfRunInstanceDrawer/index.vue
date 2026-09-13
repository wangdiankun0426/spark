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
        <el-alert :type="runStatus === 2 ? 'success' : 'error'" :closable="false" show-icon>
          <template #title>
            <span v-if="runStatus === 2">运行成功 (耗时 {{ runDetail.durationMs != null ? runDetail.durationMs + 'ms' : '-' }})</span>
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
            class="section-title"
            style="color:#f56c6c"
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
              :type="n.status === 3 ? 'success' : n.status === 4 ? 'danger' : 'info'"
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
import { runWorkflowAPI, queryInstanceDetailAPI, queryInstanceNodesAPI } from '@/api/workflow/instance.js';
import { queryWorkflowDetailAPI } from '@/api/workflow/template';
import { detailFormValueAPI } from '@/api/form/formValue';

const props = defineProps({ modelValue: Boolean, workflow: Object });
const emit = defineEmits(['update:modelValue', 'success']);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
});

const formJson = ref(null);
const formId = ref(null);
const runFormJson = ref(null);
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
  runFormJson.value = null;
  runStatus.value = null;
  runDetail.value = null;
  nodes.value = [];
  // 加载模板详情，取绑定的输入表单定义
  if (w.id) {
    const dRes = await queryWorkflowDetailAPI({ id: w.id });
    if (dRes.code === 200 && dRes.data && dRes.data.formJson) {
      formId.value = dRes.data.formId;
      try {
        formJson.value = JSON.parse(dRes.data.formJson);
      } catch (e) { /* ignore */ }
    }
  }
});

async function handleRun() {
  // 必填校验并收集表单值作为运行输入参数
  const widgetList = (formJson.value && formJson.value.widgetList) || [];
  const missingWidget = widgetList.find(w => {
    if (!w.config || !w.config.required) {
      return false;
    }
    const v = w.config.value;
    return v === null || v === undefined || v === '' || (Array.isArray(v) && v.length === 0);
  });
  if (missingWidget) {
    ElMessage.warning(`【${missingWidget.config.label}】为必填项，请填写后再运行`);
    return;
  }
  // 表单值（落库form_obj_value，并作为运行参数，与流程申请一致）
  const values = [];
  widgetList.forEach(widget => {
    if (widget.config && widget.config.code) {
      values.push({
        code: widget.config.code,
        type: widget.type,
        value: widget.config.value,
        showValue: widget.config.showValue != null ? widget.config.showValue : widget.config.value
      });
    }
  });

  running.value = true;
  runStatus.value = null;
  runDetail.value = null;
  nodes.value = [];

  try {
    const res = await runWorkflowAPI({ templateId: props.workflow.id, formId: formId.value, values });
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
        // 加载落库的表单数据用于展示
        loadRunFormData(instanceId);
      }
    }
  } catch (e) {
    ElMessage.error('运行出错: ' + (e.message || '未知错误'));
  }
  running.value = false;
}

function handleClose() { emit('update:modelValue', false); emit('success'); }

/** 加载运行实例落库的表单数据并回填表单展示 */
async function loadRunFormData(instanceId) {
  const res = await detailFormValueAPI({ objId: instanceId });
  if (res.code !== 200 || !res.data || !res.data.formJson) {
    return;
  }
  try {
    const formJsonResult = JSON.parse(res.data.formJson);
    const values = res.data.values || [];
    const codes = values.map(v => v.code);
    (formJsonResult.widgetList || []).forEach(widget => {
      const index = codes.indexOf(widget.config && widget.config.code);
      if (index === -1) return;
      widget.config.value = values[index].value;
      widget.config.showValue = values[index].showValue;
    });
    runFormJson.value = formJsonResult;
  } catch (e) { /* ignore */ }
}
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
